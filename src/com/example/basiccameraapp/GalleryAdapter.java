package com.example.basiccameraapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.basiccameraapp.application.BasicCameraApplication;

public class GalleryAdapter extends BaseAdapter {
	static String TAG = GalleryAdapter.class.getName();
	ImageCache mImageCache;
	Context mContext;
	String[] mImagePaths;
	View.OnClickListener mImageViewListener;
	BasicCameraApplication mApp;
	GridView mGallery;

	public GalleryAdapter(Context context, GridView gallery, String[] imagePaths, ImageCache imageCache) {
		mContext = context;
		mGallery = gallery;
		mImagePaths = imagePaths;
		mImageCache = imageCache;
		mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> gallery, View view, int pos,
					long id) {
				String path = (String) view.getTag();
				if (path != null) {
					Log.i(TAG, "Opening image: " + path);
					GalleryActivity.viewImage(GalleryAdapter.this.mContext, path);
				} else {
					Log.e(TAG, "No path associated with image at position " + pos + "!");
				}
			}
		});
	}

	@Override
	public Object getItem(int position) {
		return mImagePaths[position];
	}

	@Override
	public long getItemId(int position) {
		// Android calls this when an item is clicked so an id can be passed
		// to onItemClickListener. But we ignore the id currently.
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

		String path = (String) getItem(position);
		Bitmap cached = mImageCache.get(path);
		if (cached == null) {
			imageView.setImageResource(android.R.drawable.ic_menu_help);
			ImageLoaderData img = new ImageLoaderData(imageView, this, position);
			new ImageLoader().execute(img);
		} else {
			imageView.setTag(path);
			imageView.setImageBitmap(cached);
		}

		return imageView;
	}

	@Override
	public int getCount() {
		return mImagePaths.length;
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
					imageView.setTag(path);
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

