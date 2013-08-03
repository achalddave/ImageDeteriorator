package com.example.basiccameraapp;

import java.lang.reflect.Method;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class ImageCache extends LruCache<String, Bitmap> {
	private static String TAG = ImageCache.class.getName();

	public ImageCache(int maxSize) {
		super(maxSize);
	}

    protected int sizeOf(String key, Bitmap value) {
    	return value.getRowBytes() * value.getHeight();
    }
}