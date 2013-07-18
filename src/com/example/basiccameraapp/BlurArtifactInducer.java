package com.example.basiccameraapp;

import android.graphics.Bitmap;

public class BlurArtifactInducer extends ArtifactInducer {
	@Override
	public Bitmap induceArtifacts(Bitmap image, float artifactIntensity) {
		//			  offset	     map artifactIntensity to be bwn 0.5, 1
		float scale = 1.001f - (artifactIntensity / 2 + 0.5f);
		int scaledHeight = (int) (image.getHeight() * scale);
		scaledHeight = scaledHeight == 0 ? 1 : scaledHeight;
		int scaledWidth = (int) (image.getWidth() * scale);
		scaledWidth = scaledWidth == 0 ? 1 : scaledWidth;
		return Bitmap.createScaledBitmap(
				Bitmap.createScaledBitmap(image, scaledHeight, scaledWidth, true),
				image.getWidth(), image.getHeight(), true);
	}
}