package com.example.title;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

public class ScoreboardActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference globalScoreRef;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    private TextView firstScoreValue, secondScoreValue, thirdScoreValue, currentScoreText, personalBestText;

    @Override
    protected void onCreate(Bundle savedInstanceState)   {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        // אתחול Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        globalScoreRef = firebaseDatabase.getReference("Score");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userRef = firebaseDatabase.getReference("Users").child(currentUser.getUid());
        }

        // אתחול UI
        firstScoreValue = findViewById(R.id.first_score_value);
        secondScoreValue = findViewById(R.id.second_score_value);
        thirdScoreValue = findViewById(R.id.third_score_value);
        currentScoreText = findViewById(R.id.current_score_value);
        personalBestText = findViewById(R.id.personal_best_value);

        Button rturn_btn = findViewById(R.id.return_btn);
        Button playagain_btn = findViewById(R.id.playagain_btn);

        int newScore = getIntent().getIntExtra("current_score", 0);
        currentScoreText.setText(String.valueOf(newScore));


        rturn_btn.setOnClickListener(v -> {
            startActivity(new Intent(ScoreboardActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in,R.anim.slide_down);
            finish();

        });
        playagain_btn.setOnClickListener(v -> {
            startActivity(new Intent(ScoreboardActivity.this, difficultyActivity.class));
            overridePendingTransition(android.R.anim.fade_in,R.anim.slide_down);
            finish();

        });
        // הפעלת לוגיקת העדכון
        handleScoreUpdate(newScore);
        GlobalBest();
        PersonalBest();
        updateFcmToken();
        // 1. יצירת ערוץ התראות (Notification Channel) - חובה מאנדרואיד 8
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelId = "game_alerts"; // ה-ID של הערוץ בו השרת משתמש
            CharSequence name = "התראות משחק";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;

            android.app.NotificationChannel channel = new android.app.NotificationChannel(channelId, name, importance);
            channel.setDescription("עדכונים על שיאים וטבלת מובילים");

            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 2. בקשת הרשאת ריצה מהמשתמש (Runtime Permission) - חובה מאנדרואיד 13
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }


    // לוקח את הטוקן של המשתמש
    private void updateFcmToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {      // מבקש מפיירבייס את הטוקן של המכשיר
            if (!task.isSuccessful()) {
                Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.getException());      // מחזיר אובייקט מסוג אקספטיישן שמסביר מה השגיאה
                return;
            }

            String token = task.getResult();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();        // שומר במשתנה את היוזר הקיים.

            if (user != null && token != null) {
                // עדכון בנתיב המשתמש
                FirebaseDatabase.getInstance().getReference("Users")    // התיקייה של כל המשתמשים
                        .child(user.getUid())   // התקייה הספציפית של המשתמש
                        .child("fcmToken")  //יוצר או ניגש לטוקן המשתמש
                        .setValue(token)
                        .addOnSuccessListener(aVoid -> Log.d("FCM_TOKEN", "Token updated successfully!"));  // במקביל, תשלח הודעה אם ההתחברות הצליחה
            }
        });
    }


    // מכניס את תוצאות שלושת המובילים
    private void GlobalBest() {
        globalScoreRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Score score = snapshot.getValue(Score.class);
                if (score != null) {
                    firstScoreValue.setText(String.valueOf(score.top1));
                    secondScoreValue.setText(String.valueOf(score.top2));
                    thirdScoreValue.setText(String.valueOf(score.top3));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    // הצגת השיא האישי של המשתמש על המסך ועדכונו בזמן אמת.
    private void PersonalBest() {
        if (userRef != null) {
            userRef.child("personalBest").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer best = snapshot.getValue(Integer.class);
                    personalBestText.setText("High Score: " + (best != null ? best : 0));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            personalBestText.setText("Not logged in");
        }
    }


    //
    private void handleScoreUpdate(int newScore) {
        // האם השיא האישי של המשתמש נשבר
        if (userRef != null) {
            userRef.child("personalBest").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int existingBest = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                    if (newScore > existingBest) userRef.child("personalBest").setValue(newScore);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // Updating global chart
        globalScoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Score scores = snapshot.exists() ? snapshot.getValue(Score.class) : new Score(0, 0, 0, "");     // מקבל ניקוד חדש לטבלה
                String previousTop1User = scores.top1UserUID;
                String currentUID = (mAuth.getCurrentUser() != null) ? mAuth.getUid() : "";     // מקבל את שם המשתמש ששיחק

                if (newScore > scores.top1) {
                    // מעדכן אם יש שיא גלובלי חדש
                    if (previousTop1User != null && !previousTop1User.isEmpty() && !previousTop1User.equals(currentUID)) {
                        notifyPreviousWinner(previousTop1User);
                    }

                    // אם כן, מחליף את התוצאות
                    scores.top3 = scores.top2;
                    scores.top2 = scores.top1;
                    scores.top1 = newScore;
                    scores.top1UserUID = currentUID; // מעדכן את שם המוביל
                } else if (newScore > scores.top2) { // מעדכן אם מישהו עקף את המקום השני
                    scores.top3 = scores.top2;
                    scores.top2 = newScore;
                } else if (newScore > scores.top3) { // מעדכן אם מישהו עקף את המקום הלשישי
                    scores.top3 = newScore;
                }

                globalScoreRef.setValue(scores);    // מעדכן את כל הטבלה
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // מעדכן את המקום הראשון אם עקפו אותו
    private void notifyPreviousWinner(String oldWinnerUID) {
        firebaseDatabase.getReference("Users").child(oldWinnerUID).child("fcmToken")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String token = snapshot.getValue(String.class);
                        if (token != null && !token.isEmpty()) {
                            sendNotificationV1(token, "So bad!", "You no longer 1st place in EggShooter!");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    // שליחת ההתראה
    private void sendNotificationV1(String targetToken, String title, String body) {
        new Thread(() -> {
            // אם השרתים של גוגל אופליין, האפליקציה לא תקרוס
            try {
                InputStream stream = getAssets().open("service_account.json");
                com.google.auth.oauth2.GoogleCredentials credentials =
                        com.google.auth.oauth2.GoogleCredentials.fromStream(stream)
                                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));
                credentials.refreshIfExpired();
                String accessToken = credentials.getAccessToken().getTokenValue();

                String url = "https://fcm.googleapis.com/v1/projects/signupfirebase-89a33/messages:send";

                // 1. יצירת הודעת ההתראה הבסיסית
                JSONObject notification = new JSONObject()
                        .put("title", title)
                        .put("body", body);

                // 2. הגדרת ערוץ ההתראות הספציפי עבור מכשירי אנדרואיד (חובה לתאימות)
                JSONObject androidNotification = new JSONObject()
                        .put("channel_id", "game_alerts"); // חייב להתאים ל-Channel ID שיצרת ב-onCreate

                JSONObject androidConfig = new JSONObject()
                        .put("notification", androidNotification);

                // 3. חיבור הכל לתוך אובייקט ה-message הרשמי של FCM v1
                JSONObject message = new JSONObject()
                        .put("token", targetToken)
                        .put("notification", notification)
                        .put("android", androidConfig); // הוספת הגדרות האנדרואיד שכוללות את הערוץ

                JSONObject jsonBody = new JSONObject().put("message", message);

                // שליחת הבקשה לשרת של גוגל
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.toString().getBytes("utf-8"));
                }

                Log.d("FCM_SEND", "Response Code: " + conn.getResponseCode());
            } catch (Exception e) {
                Log.e("FCM_SEND", "Error sending FCM: " + e.getMessage());
            }
        }).start();
    }
}