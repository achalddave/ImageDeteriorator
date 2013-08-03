package com.example.basiccameraapp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
	static String TAG = GalleryActivity.class.getName();
	static String PATH = "path";
	int numCols = 5;

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
		return inSampleSize;
	}

	/*
	 * Returns a Bitmap decoded to fit requested dimensions.
	 * 
	 * This method decodes a Bitmap in a memory efficient way. Context needs to be
	 * passed so `openFileInput` can be called (which allows us to access the app's
	 * private files).
	 */
	public static Bitmap decodeSampledBitmapFromPath(Context context,
			String path, int requestedHeight, int requestedWidth) {
		// Decode with inJustDecodeBounds = true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		/* 
		 * Have to open two input streams, because you can only read from a stream
		 * once (there are ways to sidestep this, but I couldn't get them working).
		 * 
		 * Note that reading a byte array from the file and then using it does not work, 
		 * as we run out of memory.
		 */
		FileInputStream fis;
		try {
			fis = context.openFileInput(path);
			BitmapFactory.decodeStream(fis, null, options);
			fis.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Couldn't find file: " + path, e);
		} catch (IOException e) {
			Log.e(TAG, "Couldn't close filestream for file: " + path, e);
		}

		/* now actually decode the file with the necessary dimensions */
		options.inSampleSize = calculateInSampleSize(options, requestedHeight,
				requestedWidth);
		options.inJustDecodeBounds = false;
		Bitmap b = null;
		try {
			fis = context.openFileInput(path);
			b = BitmapFactory.decodeStream(fis, null, options);
			fis.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Couldn't find file: " + path, e);
		} catch (IOException e) {
			Log.e(TAG, "Couldn't close filestream for file: " + path, e);
		}
		return b;
	}

	private void viewImage(String path) {
		Intent intent = new Intent(this, ImageViewerActivity.class);
		intent.putExtra(PATH, path);
		startActivity(intent);
	}

	class GalleryAdapter extends BaseAdapter {
		Context mContext;
		String[] mImagePaths;
		Bitmap[] mBitmaps;

		public GalleryAdapter(Context context, String[] imagePaths) {
			mContext = context;
			mImagePaths = imagePaths;
			mBitmaps = new Bitmap[imagePaths.length];
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

			if (mBitmaps[position] == null) {
				imageView.setImageResource(android.R.drawable.ic_menu_help);
				ImageLoaderData img = new ImageLoaderData(imageView, this, position);
				new ImageLoader().execute(img);
			} else {
				imageView.setImageBitmap(mBitmaps[position]);
			}

			return imageView;
		}

		@Override
		public int getCount() {
			return mImagePaths.length;
		}

	}

	class ImageLoader extends AsyncTask<ImageLoaderData, Void, ImageLoaderData[]> {
		@Override
		protected ImageLoaderData[] doInBackground(ImageLoaderData... images) {
			for (ImageLoaderData image : images) {
				Bitmap bmp = GalleryActivity.decodeSampledBitmapFromPath(
						getApplicationContext(), image.getPath(), 100, 100);

				image.mBmp = bmp;
			}
			return images;
		}

		@Override
		protected void onPostExecute(ImageLoaderData[] images) {
			for (final ImageLoaderData image : images) {
				ImageView imageView = image.mImageView;
				if (image.mBmp != null) {
					image.mAdapter.mBitmaps[image.mPosition] = image.mBmp;
					imageView.setImageBitmap(image.mBmp);
					imageView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							viewImage((String) image.getPath());
						}
					});
				} else {
					imageView.setImageResource(android.R.drawable.ic_menu_help);
				}
			}
		}
	}

	class ImageLoaderData {
		public ImageView mImageView;
		public GalleryAdapter mAdapter;
		public int mPosition;
		public Bitmap mBmp;
		public ImageLoaderData(ImageView imageView, GalleryAdapter adapter, int position) {
			mImageView = imageView;
			mAdapter = adapter;
			mPosition = position;
		}
		
		public String getPath() {
			return (String) mAdapter.getItem(mPosition);
		}
	}
}
