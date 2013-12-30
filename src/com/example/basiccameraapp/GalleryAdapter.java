package com.example.basiccameraapp;

import java.util.HashMap;
import java.util.Timer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.basiccameraapp.application.BasicCameraApplication;

public class GalleryAdapter extends BaseAdapter {
	static String TAG = GalleryAdapter.class.getName();
	static int refreshRate = 500;
	static int initialDelay = 2000;

	Activity mActivity;
	BasicCameraApplication mApp;
	SharedPreferences mPrefs;

	GridView mGallery;
	ImageCache mImageCache; // Cache artifact induced images
	String[] mImagePaths;
	View.OnClickListener mImageViewListener;
	HashMap<String, ImageLoader> mImageLoadersByPath;

	ArtifactInducer mArtifactInducer;
	Timer mThumbnailsUpdateTimer;
	int mDisplayHeight, mDisplayWidth;
	HashMap<String, Integer> mImageViewTimesCache;

	public GalleryAdapter(Activity activity, GridView gallery, String[] imagePaths, ImageCache imageCache) {
		mActivity = activity;
		mGallery = gallery;
		mImagePaths = imagePaths;
		mImageCache = imageCache;
		mImageLoadersByPath = new HashMap<String, ImageLoader>();
		mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> gallery, View view, int pos,
					long id) {
				String path = (String) view.getTag();
				if (path != null) {
					GalleryActivity.viewImage(GalleryAdapter.this.mActivity, path);
				}
			}
		});

		mImageViewTimesCache = new HashMap<String, Integer>();
		mArtifactInducer = new BlurArtifactInducer();

		mPrefs = activity.getSharedPreferences(MainActivity.DEFAULT_PREFS_NAME, Context.MODE_PRIVATE);
		if (! mPrefs.contains(MainActivity.PREFS_KEY_SCREEN_HEIGHT)) {
			DisplayMetrics metrics = mActivity.getResources().getDisplayMetrics();
			Editor prefsEditor = mPrefs.edit();
			mDisplayHeight = metrics.heightPixels;
			mDisplayWidth  = metrics.widthPixels;
			prefsEditor.putInt(MainActivity.PREFS_KEY_SCREEN_HEIGHT, metrics.heightPixels);
			prefsEditor.putInt(MainActivity.PREFS_KEY_SCREEN_WIDTH, metrics.widthPixels);
			prefsEditor.commit();
		} else {
			mDisplayHeight = mPrefs.getInt(MainActivity.PREFS_KEY_SCREEN_HEIGHT, -1);
			mDisplayWidth = mPrefs.getInt(MainActivity.PREFS_KEY_SCREEN_WIDTH, -1);
		}
	}

	public void startRefresh() {
		populateImageViewTimesCache();
		notifyDataSetChanged();
	}

	public void stopRefresh() {
	}

	public void populateImageViewTimesCache() {
		mImageViewTimesCache.clear();
		for (String path : mImagePaths) {
			Log.d(TAG, "Adding to cache" + path + ": " + mPrefs.getInt(path, 0));
			mImageViewTimesCache.put(path, mPrefs.getInt(path, 0));
		}
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
		String path = (String) getItem(position);
		if (convertView == null) {
			imageView = new ImageView(mActivity);
			imageView.setTag(path);
			imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(0,10,0,10);
		} else {
			imageView = (ImageView) convertView;
			imageView.setTag(path);
		}

		Bitmap cached = mImageCache.get(path);
		if (cached == null) {
			if (! mImageLoadersByPath.containsKey(path)) {
				ImageLoader loader = new ImageLoader();
				loader.execute(path);
				mImageLoadersByPath.put(path, loader);
			}
			if (imageView.getAnimation() == null) {
				Animation rotation = AnimationUtils.loadAnimation(mActivity, R.anim.loading);
				imageView.setImageResource(R.drawable.loading_circle);
				imageView.startAnimation(rotation);
			}
		} else {
			imageView.clearAnimation();
			imageView.setImageBitmap(cached);
		}

		return imageView;
	}
	
	@Override
	public int getCount() {
		return mImagePaths.length;
	}

	public Bitmap getArtifactInducedBitmap(String path, Bitmap bmp) {
		int timeViewed = mImageViewTimesCache.get(path).intValue();
		return mArtifactInducer.induceArtifacts(bmp, mDisplayWidth, mDisplayHeight, (float) timeViewed/MainActivity.sTimeToImageDeath);
	}

	class ImageLoader extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... paths) {
			String path = paths[0];
			Bitmap bmp = GalleryActivity.decodeSampledBitmapFromPath(
					GalleryAdapter.this.mActivity, path, 100, 100);
			GalleryAdapter.this.mImageCache.put(path, getArtifactInducedBitmap(path, bmp));
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			GalleryAdapter.this.notifyDataSetChanged();
		}
	}
}

