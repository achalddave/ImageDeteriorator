package com.example.basiccameraapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.basiccameraapp.application.BasicCameraApplication;

public class MainActivity extends Activity {
	static String TAG = MainActivity.class.getName();
	static String DEFAULT_PREFS_NAME = BasicCameraApplication.class.getName();
	static String PREFS_KEY_SCREEN_HEIGHT = "screenHeight";
	static String PREFS_KEY_SCREEN_WIDTH = "screenWidth";
	static int sTimeToImageDeath = 3600000;

	Camera mCamera;
	Preview mPreview;
	int mCurrentCameraId;
	Button mGalleryOpener;
	Button mCameraSwitcher;
	PictureCallback mPictureCallback;

	SharedPreferences mPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPreview = new Preview(this);
		setContentView(mPreview);
		mPreview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCamera.takePicture(null, null, new PictureCallback() {
					@Override
					public void onPictureTaken(byte[] data, Camera camera) {
						new WritePhotoTask().execute(data);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								mCamera.startPreview();
							}
						}, 100);
					}
				});
			}
		});

		mCurrentCameraId = CameraInfo.CAMERA_FACING_BACK;
		TAG = getClass().getName();

		LayoutInflater overlayInflater = LayoutInflater.from(getBaseContext());
		View cameraOverlay = overlayInflater.inflate(R.layout.camera_overlay,
				null);
		RelativeLayout.LayoutParams cameraOverlayParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		this.addContentView(cameraOverlay, cameraOverlayParams);

		mGalleryOpener = (Button) findViewById(R.id.galleryOpener);
		mPrefs = getSharedPreferences(MainActivity.DEFAULT_PREFS_NAME, MODE_PRIVATE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupCameraAndPreview();
	}

	@Override
	protected void onPause() {
		super.onPause();
		destroyCameraAndPreview();
	}

	void setupCameraAndPreview() {
		mCamera = Camera.open(mCurrentCameraId);
		mPreview.setCamera(mCamera);
	}

	void destroyCameraAndPreview() {
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void openGallery(View sender) {
		Intent intent = new Intent(this, GalleryActivity.class);
		startActivity(intent);
	}

	class Preview extends ViewGroup implements SurfaceHolder.Callback {
		private final String TAG = Preview.class.getName();

		SurfaceView mSurfaceView;
		SurfaceHolder mHolder;
		Size mPreviewSize;
		List<Size> mSupportedPreviewSizes;
		Camera mCamera;

		@SuppressWarnings("deprecation")
		Preview(Context context) {
			super(context);

			mSurfaceView = new SurfaceView(context);
			addView(mSurfaceView);

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			mHolder = mSurfaceView.getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public void setCamera(Camera camera) {
			mCamera = camera;
			if (mCamera != null) {
				mSupportedPreviewSizes = mCamera.getParameters()
						.getSupportedPreviewSizes();
				requestLayout();
			}
		}

		private int getNeededRotation() {
			Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
			Camera.getCameraInfo(mCurrentCameraId, info);

			// angle by which the display is rotated to be upright
			int rotation = MainActivity.this.getWindowManager()
					.getDefaultDisplay().getRotation();

			// angle that the camera image needs to be rotated to offset camera
			// orientation
			int cameraOrientationOffset = info.orientation;

			int degrees = 0;
			switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
			}

			if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				// camera's cw is our ccw
				cameraOrientationOffset = 360 - cameraOrientationOffset;
			}
			int result = ((cameraOrientationOffset - degrees + 360) % 360);
			return result;
		}

		public void switchCamera(Camera camera) {
			setCamera(camera);
			try {
				camera.setPreviewDisplay(mHolder);
			} catch (IOException exception) {
				Log.e(TAG, "IOException caused by setPreviewDisplay()",
						exception);
			}
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			requestLayout();

			camera.setParameters(parameters);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			// We purposely disregard child measurements because act as a
			// wrapper to a SurfaceView that centers the camera preview instead
			// of stretching it.
			final int width = resolveSize(getSuggestedMinimumWidth(),
					widthMeasureSpec);
			final int height = resolveSize(getSuggestedMinimumHeight(),
					heightMeasureSpec);
			setMeasuredDimension(width, height);

			if (mSupportedPreviewSizes != null) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
						width, height);
			}
		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			if (changed && getChildCount() > 0) {
				final View child = getChildAt(0);

				final int width = r - l;
				final int height = b - t;

				int previewWidth = width;
				int previewHeight = height;
				if (mPreviewSize != null) {
					previewWidth = mPreviewSize.width;
					previewHeight = mPreviewSize.height;
				}

				// Center the child SurfaceView within the parent.
				if (width * previewHeight > height * previewWidth) {
					final int scaledChildWidth = previewWidth * height
							/ previewHeight;
					child.layout((width - scaledChildWidth) / 2, 0,
							(width + scaledChildWidth) / 2, height);
				} else {
					final int scaledChildHeight = previewHeight * width
							/ previewWidth;
					child.layout(0, (height - scaledChildHeight) / 2, width,
							(height + scaledChildHeight) / 2);
				}
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, acquire the camera and tell it
			// where
			// to draw.
			try {
				if (mCamera != null) {
					mCamera.setPreviewDisplay(holder);
				}
			} catch (IOException exception) {
				Log.e(TAG, "IOException caused by setPreviewDisplay()",
						exception);
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// Surface will be destroyed when we return, so stop the preview.
			if (mCamera != null) {
				mCamera.stopPreview();
			}
		}

		private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
			final double ASPECT_TOLERANCE = 0.1;
			double targetRatio = (double) w / h;
			if (sizes == null)
				return null;

			Size optimalSize = null;
			double minDiff = Double.MAX_VALUE;

			int targetHeight = h;

			// Try to find an size match aspect ratio and size
			for (Size size : sizes) {
				double ratio = (double) size.width / size.height;
				if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
					continue;
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}

			// Cannot find the one match the aspect ratio, ignore the
			// requirement
			if (optimalSize == null) {
				minDiff = Double.MAX_VALUE;
				for (Size size : sizes) {
					if (Math.abs(size.height - targetHeight) < minDiff) {
						optimalSize = size;
						minDiff = Math.abs(size.height - targetHeight);
					}
				}
			}
			return optimalSize;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			// Now that the size is known, set up the camera parameters and
			// begin
			// the preview.
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			requestLayout();

			mCamera.setParameters(parameters);
			mCamera.setDisplayOrientation(getNeededRotation());
			mCamera.startPreview();
		}

	}

	/** Create a File for saving an image or video */
	public File getOutputMediaFile() {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		String fileName = "IMG_" + timeStamp + ".png";

		// get internal storage directory;
		return new File(getApplicationContext().getFilesDir(), fileName);
	}

	class WritePhotoTask extends AsyncTask<byte[], Object, Object> {
		@Override
		protected String doInBackground(byte[]... photos) {
			File output = getOutputMediaFile();
			if (output.exists()) output.delete();

			try {
				FileOutputStream fos = new FileOutputStream(output);
				fos.write(photos[0]);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, "File not found " + e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, "Error accessing file: " + e.getMessage());
			}

			return null;
		}
	}
}
