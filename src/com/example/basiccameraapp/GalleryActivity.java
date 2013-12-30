package com.example.basiccameraapp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import com.example.basiccameraapp.application.BasicCameraApplication;

public class GalleryActivity extends Activity {
	static String TAG = GalleryActivity.class.getName();
	static String INTENT_KEY_PATH = "path";

	BasicCameraApplication mApp;
	GridView mGallery;
	GalleryAdapter mGalleryAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_gallery);

		mApp = (BasicCameraApplication) getApplication();
		if (!mApp.mCacheInitialized)
			mApp.initImageCache();

		String[] imagePaths = getApplicationContext().fileList();
		mGallery = (GridView) findViewById(R.id.gallery);
		mGalleryAdapter = new GalleryAdapter(this, mGallery, imagePaths, mApp.mImageCache);
		mGallery.setAdapter(mGalleryAdapter);
	}

	@Override
	public void onResume() {
		mGalleryAdapter.startRefresh();
		super.onResume();
	}
	
	@Override
	public void onStop() {
		mGalleryAdapter.stopRefresh();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gallery, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reset_times:
			resetImageViewTimes();
			return true;
		case R.id.clear_images:
			deleteAfterConfirmation();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
		 * once (there are ways to sidestep this (calling reset on the stream), but I
		 * couldn't get them working, and I'm not convinced they'd be better).
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

	static void viewImage(Context context, String path) {
		Intent intent = new Intent(context, ImageViewerActivity.class);
		intent.putExtra(INTENT_KEY_PATH, path);
		context.startActivity(intent);
	}

	private void updateAdapterPaths() {
		String[] imagePaths = getApplicationContext().fileList();
		mGalleryAdapter.mImagePaths = imagePaths;
	}

	public void resetImageViewTimes() {
		Log.d(TAG, "Resetting times");
		mGalleryAdapter.mPrefs.edit().clear().commit();
		mGalleryAdapter.populateImageViewTimesCache();
		mGalleryAdapter.notifyDataSetChanged();
	}
	
	private void deleteAfterConfirmation() {
		new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Delete images?")
			.setMessage("Are you sure you want to delete all saved images? This action *cannot* be undone!")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					deleteImages();
				}
			})
			.setNegativeButton("No", null)
			.show();
	}
	
	private void deleteImages() {
		for (String path : getApplicationContext().fileList()) {
			deleteFile(path);
		}
		updateAdapterPaths();
		mGalleryAdapter.notifyDataSetChanged();
	}

	public void openCamera(View sender) {
		this.finish();
	}
}