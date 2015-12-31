package com.yunweather.app.util;

import android.app.Application;
import android.content.Context;

public class YunApplication extends Application {
	private static Context context;
    
    @Override
    public void onCreate() {
        context = getApplicationContext();
    }
    
    public static Context getContext() {
        return context;
    }
}
