package com.example.agrima;

import android.app.Application;
import android.content.Context;

public class AppGlobals extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        AppGlobals.context = getApplicationContext();
    }

    public static Context getContext() {
        return AppGlobals.context;
    }
}
