package kr.co.menovel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import static kr.co.menovel.util.CommonUtil.SPLASH_TIME;

public class SplashActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };

        startApp();
    }

    @Override
    public void onBackPressed() {
        handler.removeCallbacks(runnable);
        finish();
    }

    private void startApp() {
        handler.postDelayed(runnable, SPLASH_TIME);
    }
}