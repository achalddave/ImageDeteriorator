package com.example.basiccameraapp;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class ImageViewerActivity extends Activity {
	static String TAG = ImageViewerActivity.class.getName();
	static int refreshRate = 300;
	ImageView mCurrentImage;
	Bitmap mBitmap;
	String mPath;
	ArtifactInducer mArtifactInducer;
	SharedPreferences mPrefs;
	int mTimeViewed;
	Timer mImageUpdateTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_viewer);

		mPrefs = getSharedPreferences(MainActivity.DEFAULT_PREFS_NAME, MODE_PRIVATE);

		mCurrentImage = (ImageView) findViewById(R.id.currentImage);
		mArtifactInducer = new BlurArtifactInducer();

		mCurrentImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageViewerActivity.this.finish();
			}
		});

		Intent intent = getIntent();
		mPath = intent.getStringExtra(GalleryActivity.INTENT_KEY_PATH);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (mBitmap == null) {
			mBitmap = GalleryActivity.decodeSampledBitmapFromPath(
					getApplicationContext(), mPath, mCurrentImage.getHeight(),
					mCurrentImage.getWidth());
			induceArtifacts();
		}
	}

	@Override
	public void onResume() {
		mTimeViewed = mPrefs.getInt(mPath, 0);
		mImageUpdateTimer = new Timer();
		mImageUpdateTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (mBitmap != null) {
					mTimeViewed = Math.min(mTimeViewed + refreshRate, MainActivity.sTimeToImageDeath);
					ImageViewerActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							induceArtifacts();
						}
					});
				}
			}
		}, 0, refreshRate);
		super.onResume();
	}

	@Override
	public void onStop() {
		mImageUpdateTimer.cancel();
		mPrefs.edit().putInt(mPath, mTimeViewed).commit();
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_viewer, menu);
		return true;
	}

	private void induceArtifacts() {
		updateImage(mArtifactInducer.induceArtifacts(mBitmap,
				mCurrentImage.getWidth(), mCurrentImage.getHeight(),
				(float) mTimeViewed / MainActivity.sTimeToImageDeath));
	}

	/*
	private void updateImage() {
		updateImage(mBitmap);
	}
	*/

	private void updateImage(Bitmap bmp) {
		mCurrentImage.setImageBitmap(bmp);
	}
}