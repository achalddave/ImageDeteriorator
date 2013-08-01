package com.example.basiccameraapp;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryActivity extends Activity {
	GridView gallery;
	static String TAG;
	static String PATH = "path";
	int numCols = 5;
	
	static {
		TAG = GalleryActivity.class.getName();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Full screen!
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_gallery);

		gallery = (GridView) findViewById(R.id.gallery);
		gallery.setAdapter(new GalleryAdapter(this, getApplicationContext()
				.fileList()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gallery, menu);
		return true;
	}

	private static int calculateInSampleSize(BitmapFactory.Options options,
			int requestedHeight, int requestedWidth) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > requestedHeight || width > requestedWidth) {
			// Get ratios between requested and actual dimensions
			final int heightRatio = Math.round((float) height
					/ (float) requestedHeight);
			final int widthRatio = Math.round((float) width
					/ (float) requestedWidth);

			// Choose the smaller ratio so that the final image has dimensions
			// that are at least as large as the requested dimensions.
			inSampleSize = Math.min(heightRatio, widthRatio);
		}
		Log.d(TAG, "inSampleSize: " + inSampleSize);
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromByteArray(byte[] data, int requestedHeight, int requestedWidth) {
		// Decode with inJustDecodeBounds = true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		
		options.inSampleSize = calculateInSampleSize(options, requestedHeight, requestedWidth);
		
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}

	public static byte[] readBytesFromPath(Context mContext, String path) {
		/*
		 * TODO: Check which version of this is faster
        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
        	FileInputStream is = openFileInput(path);
        	while ((len = is.read(buffer)) > -1) {
        		if (len != 0) {
        			// if we're going to go out of byteArray bounds
        			if (count + len > byteArr.length) {
        				// make byteArray bigger
        				byte[] newbuf = new byte[(count + len)*2];
        				System.arraycopy(byteArr, 0, newbuf, 0, count);
        				byteArr = newbuf;
        			}

        			// "append" to byteArray
        			System.arraycopy(buffer, 0, byteArr, count, len);
        			count += len;
        		}
        	}
        	is.close();
        } catch (FileNotFoundException e) {
        	Log.e(TAG, "Couldn't find file " + path);
        	e.printStackTrace();
        } catch (IOException e) {
        	Log.e(TAG, "Couldn't read from file " + path + " properly...");
			e.printStackTrace();
		}
        return byteArr;
		 */

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int i;
		byte[] buffer = new byte[1024];
		try {
			FileInputStream is = mContext.openFileInput(path);
			while ((i = is.read(buffer)) != -1) {
				bos.write(buffer);
			}
			is.close();
			return bos.toByteArray();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Couldn't find file " + path);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "Couldn't close file " + path);
			e.printStackTrace();
		}
		return null;
	}
	
	private void viewImage(String path) {
		Intent intent = new Intent(this, ImageViewerActivity.class);
		intent.putExtra(PATH, path);
		startActivity(intent);
	}

	class GalleryAdapter extends BaseAdapter {
		Context mContext;
		String[] mImagePaths;

		public GalleryAdapter(Context context, String[] imagePaths) {
			mContext = context;
			mImagePaths = imagePaths;
		}

		@Override
		public Object getItem(int position) {
			return mImagePaths[position];
		}

		@Override
		public long getItemId(int position) {
			// not used
			Log.d(TAG, "Called getItemId on position " + position
					+ "; this shouldn't happen...");
			return -1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;
			if (convertView == null) {
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(0,10,0,10);
			} else {
				imageView = (ImageView) convertView;
			}

			final String path = (String) getItem(position);
			/*
			Bitmap bmp = null;
			try {
				FileInputStream is = openFileInput(path);
				bmp = BitmapFactory.decodeStream(is);
				is.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Couldn't find file " + path);
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "Couldn't close file " + path);
				e.printStackTrace();
			}
			*/

			byte[] data = GalleryActivity.readBytesFromPath(GalleryActivity.this, path);
			Bitmap bmp = GalleryActivity.decodeSampledBitmapFromByteArray(data, 100, 100);

			if (bmp != null) {
				imageView.setImageBitmap(bmp);
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						viewImage(path);
					}
				});
			} else {
				imageView.setImageResource(android.R.drawable.ic_menu_help);
			}
			return imageView;
		}

		@Override
		public int getCount() {
			return mImagePaths.length;
		}

	}
	
}
