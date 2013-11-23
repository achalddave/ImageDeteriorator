package com.example.basiccameraapp;

import android.graphics.Bitmap;
import android.util.Log;

public class BlurArtifactInducer extends ArtifactInducer {
	private static String TAG = BlurArtifactInducer.class.getName();

	@Override
	public Bitmap induceArtifacts(Bitmap image, int originalWidth, int originalHeight, float artifactIntensity) {
		if (Float.compare(0, artifactIntensity) == 0) {
			return image;
		}

		/* The blur-by-scaling method we use makes it so that the difference between
		 * a scale of 0.9 and 1 is far more visible than the difference between 0 and
		 * 0.1. This function attempts to scale the artifact intensity such that
		 * callers of induceArtifacts see a linear-like drop off in quality (relative
		 * to artifactIntensity). This function is a hack primarily driven by experiment
		 * and does not have much theory behind it.
		 */
		float scale = (float) (1 - Math.pow(Math.log(artifactIntensity+1)/Math.log(2),1.0/35.0d));

		int proportionateWidth = originalWidth;
		int proportionateHeight = (int) (originalWidth * ((float) image.getHeight() / image.getWidth()));
		if (proportionateHeight > originalHeight) {
			proportionateHeight = originalHeight;
			proportionateWidth = (int) (originalHeight * ((float) image.getWidth() / image.getHeight()));
		}

		int scaledHeight = (int) (proportionateHeight * scale);
		scaledHeight = scaledHeight == 0 ? 1 : scaledHeight;
		int scaledWidth = (int) (proportionateWidth * scale);
		scaledWidth = scaledWidth == 0 ? 1 : scaledWidth;
		
		if (scaledWidth > image.getWidth() && scaledHeight > image.getHeight()) {
			return image;
		}

		Log.i(TAG, "Scaled width: " + scaledWidth + "; height: " + scaledHeight);
		Log.i(TAG, "Image  width: " + image.getWidth() + "; height: " + image.getHeight());
		Log.i(TAG, "Proportionate width: " + image.getWidth() + "; height: " + image.getHeight());
		if (proportionateHeight < image.getHeight() && proportionateWidth < image.getWidth()) {
			return Bitmap.createScaledBitmap(
					Bitmap.createScaledBitmap(image, scaledHeight, scaledWidth, true),
					proportionateWidth, proportionateHeight, true);
		} else {
			return Bitmap.createScaledBitmap(
					Bitmap.createScaledBitmap(image, scaledHeight, scaledWidth, true),
					image.getWidth(), image.getHeight(), true);
		}
	}
}