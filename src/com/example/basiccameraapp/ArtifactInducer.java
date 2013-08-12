package com.example.basiccameraapp;

import android.graphics.Bitmap;

public abstract class ArtifactInducer {
	protected int originalWidth, originalHeight;

	public abstract Bitmap induceArtifacts(Bitmap image, int originalWidth, int originalHeight, float artifactIntensity);
}