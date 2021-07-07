package kr.co.menovel.service;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import kr.co.menovel.MainActivity;
import kr.co.menovel.SplashActivity;

public class PushReceiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(1);

        String url = getIntent().getStringExtra("url");

        if (MainActivity.isRunning) {
            // TODO 푸시 선택 시 페이지 이동이 있을 경우 처리
        } else {
            Intent i = new Intent(this, SplashActivity.class);
            i.putExtra("url", url);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(i);
            finish();
        }
    }
}
