package com.plateonic.android.com.plateonic.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Oleg on 2/8/14.
 */
public class VolleySingleton {

    private static RequestQueue mRequestQueue;

    public static RequestQueue getInstance(Context applicationContext) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(applicationContext);
        }
        return mRequestQueue;
    }

}
