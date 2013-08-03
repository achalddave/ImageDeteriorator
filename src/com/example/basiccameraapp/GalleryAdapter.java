package com.example.basiccameraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.basiccameraapp.application.BasicCameraApplication;

public class GalleryAdapter extends BaseAdapter {
	static String TAG = GalleryAdapter.class.getName();
	ImageCache mImageCache;
	Context mContext;
	String[] mImagePaths;
	BasicCameraApplication mApp;

	public GalleryAdapter(Context context, String[] imagePaths, ImageCache imageCache) {
		mContext = context;
		mImagePaths = imagePaths;
		mImageCache = imageCache;
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

		Bitmap cached = mImageCache.get((String) getItem(position));
		if (cached == null) {
			imageView.setImageResource(android.R.drawable.ic_menu_help);
			ImageLoaderData img = new ImageLoaderData(imageView, this, position);
			new ImageLoader().execute(img);
		} else {
			imageView.setImageBitmap(cached);
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
					image.mAdapter.mContext, image.getPath(), 100, 100);

			image.mBmp = bmp;
		}
		return images;
	}

	@Override
	protected void onPostExecute(ImageLoaderData[] images) {
		for (final ImageLoaderData image : images) {
			ImageView imageView = image.mImageView;
			final String path = image.getPath();
			if (image.mBmp != null) {
				image.mAdapter.mImageCache.put(path, image.mBmp);
				imageView.setImageBitmap(image.mBmp);
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						GalleryActivity.viewImage(image.mAdapter.mContext, path);
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
