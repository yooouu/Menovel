package kr.co.menovel.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;

import kr.co.menovel.MainActivity;
import kr.co.menovel.R;
import kr.co.menovel.util.HTTPUtil;

public class AlarmRecevier extends BroadcastReceiver {
    String title, msg, img_url;

    public AlarmRecevier() {}

    NotificationManager manager;
    NotificationCompat.Builder builder;

    // For Oreo Version
    private static String CHANNEL_ID = "kr.co.menovel";
    private static String CHANNEL_NAME = "menovel";

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        String title = intent.getStringExtra("title");
        String msg = intent.getStringExtra("msg");
        String img_url = intent.getStringExtra("img_url");

        builder = null;
        manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            );
            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        // Call Activity When Notification Alert Clicked
        Intent intent2 = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 101, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            Bitmap imgBitmap = null;
            if(img_url != null && !img_url.equals("")) {
                URL imgUrl = new URL(HTTPUtil.ip + img_url);
                URLConnection conn = imgUrl.openConnection();
                conn.connect();
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                imgBitmap = BitmapFactory.decodeStream(bis);
                bis.close();
            }

            if (img_url != null && !img_url.equals("")) {
                builder.setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker(title)
                        .setContentTitle(title)
                        .setContentText(msg)
                        .setAutoCancel(true);

                NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
                style.bigPicture(imgBitmap);
                builder.setStyle(style);
            } else {
                builder.setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker(title)
                        .setContentTitle(title)
                        .setContentText(msg)
                        .setAutoCancel(true);
            }

            builder.setContentIntent(pendingIntent);

            Notification notification = builder.build();
            manager.notify(1, notification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
