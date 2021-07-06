package kr.co.menovel;

import android.app.Application;
import android.content.Context;

public class AppApplication extends Application {
    private static AppApplication instance;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        context = getApplicationContext();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static AppApplication getInstance() {
        return instance;
    }
    public static Context getContext() {
        return context;
    }
}
