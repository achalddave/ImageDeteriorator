package com.example.basiccameraapp;

import android.graphics.Bitmap;

public abstract class ArtifactInducer {
	public abstract Bitmap induceArtifacts(Bitmap image, float artifactIntensity);
}