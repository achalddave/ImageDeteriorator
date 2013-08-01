package com.example.basiccameraapp;

import android.graphics.Bitmap;
import android.util.Log;

public class BlurArtifactInducer extends ArtifactInducer {
	private static String TAG = BlurArtifactInducer.class.getName();

	@Override
	public Bitmap induceArtifacts(Bitmap image, float artifactIntensity) {
		//			  offset	     map artifactIntensity to be bwn 0.8, 1
		float scale = 1.001f - (artifactIntensity*0.2f + 0.8f);
		int scaledHeight = (int) (image.getHeight() * scale);
		scaledHeight = scaledHeight == 0 ? 1 : scaledHeight;
		int scaledWidth = (int) (image.getWidth() * scale);
		scaledWidth = scaledWidth == 0 ? 1 : scaledWidth;
		Log.d(TAG, "H: " + image.getHeight() + ", W: " + image.getWidth());
		Log.d(TAG, "Scaled H: " + scaledHeight + ", W: " + scaledWidth);
		return Bitmap.createScaledBitmap(
				Bitmap.createScaledBitmap(image, scaledHeight, scaledWidth, true),
				image.getWidth(), image.getHeight(), true);
	}
}