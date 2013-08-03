package com.example.basiccameraapp;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class ImageCache extends LruCache<String, Bitmap> {
	private static String TAG = "ImageCache";

	public ImageCache(int maxSize) {
		super(maxSize);
	}
}
