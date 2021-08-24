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
import android.os.StrictMode;

import androidx.core.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import kr.co.menovel.MainActivity;
import kr.co.menovel.R;
import kr.co.menovel.fcm.PushReceiveActivity;
import kr.co.menovel.util.HTTPUtil;

public class AlarmRecevier extends BroadcastReceiver {

    public AlarmRecevier() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectNetwork().penaltyLog().build());

        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent;

        int s = (int) System.currentTimeMillis();

        try {
            String title = intent.getStringExtra("title");
            String msg = intent.getStringExtra("msg");
            String url = intent.getStringExtra("url");
            String img_url = intent.getStringExtra("img_url");

            Intent intent2 = new Intent(context, PushReceiveActivity.class);
            intent2.putExtra("url", url);
            contentIntent = PendingIntent.getActivity(context, 1, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

            Bitmap imgBitmap = null;
            if(img_url != null && !img_url.equals("")) {
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

//                URL imgUrl = new URL(HTTPUtil.ip + img_url);
                URL imgUrl = new URL(img_url);
                HttpsURLConnection conn = (HttpsURLConnection) imgUrl.openConnection();
                conn.connect();
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                imgBitmap = BitmapFactory.decodeStream(bis);
            }

            // 오레오 버전
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = context.getString(R.string.channel_name);
                String description = context.getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(context.getPackageName(), name, importance);
                channel.setDescription(description);
                mNotificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder mBuilder;

            if (img_url != null && !img_url.equals("")) {
                mBuilder = new NotificationCompat.Builder(context, context.getPackageName())
                        .setSmallIcon(R.mipmap.app_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_icon))
                        .setTicker(title)
                        .setContentTitle(title)
                        .setContentText(msg)
                        .setAutoCancel(true);

                NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
                style.bigPicture(imgBitmap);
                mBuilder.setStyle(style);
            } else {
                mBuilder = new NotificationCompat.Builder(context, context.getPackageName())
                        .setSmallIcon(R.mipmap.app_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_icon))
                        .setTicker(title)
                        .setContentTitle(title)
                        .setContentText(msg)
                        .setAutoCancel(true);
            }

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(s, mBuilder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private Bitmap getImageFromURL(String img_url) {
//        Bitmap imgBitmap = null;
//
//        try {
//            URL imgUrl = new URL(img_url);
//            URLConnection conn = imgUrl.openConnection();
//            conn.connect();
//
//            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
//            imgBitmap = BitmapFactory.decodeStream(bis);
//            bis.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return imgBitmap;
//    }
}
