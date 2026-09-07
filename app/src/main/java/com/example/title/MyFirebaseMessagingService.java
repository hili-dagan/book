package com.example.title;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // חילוץ הנתונים מההודעה שנשלחה בפורמט HTTP v1
        String title = "";
        String body = "";

        // הגדרת ההודעה אם נשלח כתצוגה
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        // הגדרת ההודעה אם נשלח כמידע
        else if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
        }

        if (!title.isEmpty()) {
            sendNotification(title, body);
        }
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, ScoreboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // שימוש ב-FLAG_IMMUTABLE שתואם לגרסאות אנדרואיד חדשות
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // שם ה-Channel חייב להיות זהה למה שמוגדר במערכת
        String channelId = "high_priority_notifications";

        NotificationCompat.Builder notificationBuilder =    // בניית ההתראה
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH) // למכשירים ישנים
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // עבור אנדרואיד 8.0 ומעלה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Score Updates Channel",
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription("This channel is used for high score alerts");
            channel.enableLights(true);
            channel.enableVibration(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            // מזהה ייחודי לכל הודעה (כדי שלא ידרסו אחת את השנייה אם תרצה)
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
    }
}
