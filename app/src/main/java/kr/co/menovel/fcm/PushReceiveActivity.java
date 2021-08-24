package kr.co.menovel.fcm;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import kr.co.menovel.Foreground;
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
            if (Foreground.get().isBackground()) {
                Intent i = new Intent(this, MainActivity.class);
                i.putExtra("url", url);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else {
                Intent i = new Intent(MainActivity.WEB_LINK_ACTION);
                i.putExtra("url", url);
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                finish();
            }
        } else {
            Intent i = new Intent(this, SplashActivity.class);
            i.putExtra("url", url);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(i);
            finish();
        }
    }
}
