package kr.co.menovel.fcm;



import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.StrictMode;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import kr.co.menovel.R;
import kr.co.menovel.util.HTTPUtil;

public class FCMService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            sendNotification(remoteMessage.getData());
        }
    }

    private void sendNotification(Map<String, String> msg) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent;

        int s = (int) System.currentTimeMillis();

        try {
            String title = msg.containsKey("title") ? msg.get("title") : "미노벨";
            String contents = msg.containsKey("message") ? msg.get("message") : "";
            String img_url = msg.containsKey("img_url") ? msg.get("img_url") : "";
            String url = msg.containsKey("url") ? msg.get("url") : "";

            Intent intent = new Intent(this, PushReceiveActivity.class);
            intent.putExtra("url", url);
            contentIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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
                CharSequence name = getString(R.string.channel_name);
                String description = getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(getPackageName(), name, importance);
                channel.setDescription(description);
                mNotificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder mBuilder;

            if (img_url != null && !img_url.equals("")) {
                mBuilder = new NotificationCompat.Builder(this, getPackageName())
                        .setSmallIcon(R.mipmap.app_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.app_icon))
                        .setTicker(title)
                        .setContentTitle(title)
                        .setContentText(contents)
                        .setAutoCancel(true);

                NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
                style.bigPicture(imgBitmap);
                mBuilder.setStyle(style);
            } else {
                mBuilder = new NotificationCompat.Builder(this, getPackageName())
                        .setSmallIcon(R.mipmap.app_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.app_icon))
                        .setTicker(title)
                        .setContentTitle(title)
                        .setContentText(contents)
                        .setAutoCancel(true);
            }

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(s, mBuilder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
