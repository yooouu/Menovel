package kr.co.menovel.fcm;



import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import kr.co.menovel.R;

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

        try {
            String title = msg.containsKey("title") ? msg.get("title") : "미노벨";
            String contents = msg.containsKey("message") ? msg.get("message") : "";
            String url = msg.containsKey("link") ? msg.get("link") : "";

            Intent intent = new Intent(this, PushReceiveActivity.class);
            intent.putExtra("url", url);
            contentIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            // 오레오 버전
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = getString(R.string.channel_name);
                String description = getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(getPackageName(), name, importance);
                channel.setDescription(description);
                mNotificationManager.createNotificationChannel(channel);
            }
            // TODO add app icon image
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getPackageName())
//                    .setSmallIcon(R.mipmap.app_icon)
                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.app_icon))
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentTitle(title)
                    .setContentText(contents)
                    .setAutoCancel(true);

            NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
            bigText.bigText(contents);
            mBuilder.setStyle(bigText);
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(1, mBuilder.build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
