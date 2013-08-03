package com.example.basiccameraapp.application;

import android.app.Application;

import com.example.basiccameraapp.ImageCache;

public class BasicCameraApplication extends Application {
	public ImageCache mImageCache;
	// 4 MiB
	private static int sMaxCacheSize = 4 * 1024 * 1024;
	public boolean cacheInitialized = false;
	
	public void initImageCache() {
		mImageCache = new ImageCache(sMaxCacheSize);
		cacheInitialized = true;
	}
}
