package com.example.basiccameraapp.application;

import android.app.Application;

import com.example.basiccameraapp.ImageCache;

public class BasicCameraApplication extends Application {
	private ImageCache mImagesCache;
	
	public void initImageCache(int size) {
		mImagesCache = new ImageCache(size);
	}
}
