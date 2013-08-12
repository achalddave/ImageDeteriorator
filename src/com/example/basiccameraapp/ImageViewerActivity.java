package com.example.basiccameraapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ImageViewerActivity extends Activity {
	static String TAG = ImageViewerActivity.class.getName();
	ImageView mCurrentImage;
	SeekBar mAtifactSlider;
	Bitmap mBitmap;
	String mPath;
	ArtifactInducer mArtifactInducer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_viewer);

		mCurrentImage = (ImageView) findViewById(R.id.currentImage);
		mAtifactSlider = (SeekBar) findViewById(R.id.artifactSlider);
		mArtifactInducer = new BlurArtifactInducer();

		Intent intent = getIntent();
		mPath = intent.getStringExtra(GalleryActivity.INTENT_KEY_PATH);

		mAtifactSlider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar slider, int progress, boolean fromUser) {
				induceArtifacts((float) progress / (float) slider.getMax());
			}

			@Override
			public void onStartTrackingTouch(SeekBar slider) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar slider) {
			}
		});
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (mBitmap == null) {
			mBitmap = GalleryActivity.decodeSampledBitmapFromPath(
					getApplicationContext(), mPath, mCurrentImage.getHeight(),
					mCurrentImage.getWidth());
			updateImage();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_viewer, menu);
		return true;
	}

	private void induceArtifacts(float intensity) {
		Log.d(TAG, "Intensity: " + intensity);
		updateImage(mArtifactInducer.induceArtifacts(mBitmap, intensity));
	}

	private void updateImage() {
		updateImage(mBitmap);
	}

	private void updateImage(Bitmap bmp) {
		mCurrentImage.setImageBitmap(bmp);
	}
}