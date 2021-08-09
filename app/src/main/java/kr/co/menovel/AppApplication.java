package kr.co.menovel;

import android.app.Application;
import android.content.Context;

import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

public class AppApplication extends Application {
    private static AppApplication instance;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        context = getApplicationContext();

        // Kakao Init
        KakaoSDK.init(new KakaoAdapter() {
            @Override
            public IApplicationConfig getApplicationConfig() {
                return new IApplicationConfig() {
                    @Override
                    public Context getApplicationContext() {
                        return AppApplication.this;
                    }
                };
            }
        });
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
