package com.plateonic.android.com.plateonic.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by Oleg on 2/8/14.
 */
public class VolleySingleton {

    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;

    public static RequestQueue getInstance(Context applicationContext) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(applicationContext);
            mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache());
        }
        return mRequestQueue;
    }

    public static ImageLoader getImageLoader(Context applicationContext) {
        if (mRequestQueue == null) {
            getInstance(applicationContext);
        }
        return mImageLoader;
    }

    // from http://stackoverflow.com/questions/16682595/android-volley-imageloader-bitmaplrucache-parameter
    public static class BitmapLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {
        public BitmapLruCache() {
            this(getDefaultLruCacheSize());
        }

        public static int getDefaultLruCacheSize() {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;

            return cacheSize;
        }

        public BitmapLruCache(int sizeInKiloBytes) {
            super(sizeInKiloBytes);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight() / 1024;
        }

        @Override
        public Bitmap getBitmap(String url) {
            return get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            put(url, bitmap);
        }
    }

}
