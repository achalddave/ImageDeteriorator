package com.example.basiccameraapp;

import android.graphics.Bitmap;

public abstract class ArtifactInducer {
	protected int originalWidth, originalHeight;

	/**
	 * @param image 			 The image to induce artifacts on
	 * @param originalWidth		 The original width of the image, if the image being passed in is a different size.
	 * 							 Original dimensions are useful when you are viewing the image as a thumbnail but 
	 * 							 would like to induce artifacts as if it was the original image.
	 * @param originalHeight     As with originalWidth, but with Height.
	 * @param artifactIntensity	 The artifact intesnity; ranges from 0 to 1.
	 * @return					 Bitmap with artifacts induced; may be the original Bitmap if intensity is 0.
	 */
	public abstract Bitmap induceArtifacts(Bitmap image, int originalWidth, int originalHeight, float artifactIntensity);
}