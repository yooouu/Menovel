package kr.co.menovel;

import android.app.Application;
import android.content.Context;

import com.kakao.sdk.common.KakaoSdk;

public class AppApplication extends Application {
    private static AppApplication instance;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        context = getApplicationContext();

        // Kakao Init
        // TODO Set kakao native key
        KakaoSdk.init(this, "kakao_native_app_key");
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
