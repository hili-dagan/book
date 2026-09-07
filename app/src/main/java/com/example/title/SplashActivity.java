package com.example.title;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // טעינת ה-XML ישירות (בלי binding שמנסה לחפש מחלקה שלא קיימת)
        setContentView(R.layout.activity_splash);

        // לנקוט משנה זהירות - מחכים 3 שניות ומעבירים למסך הראשי
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // שים לב שהשם של ה-Activity הראשי שלך הוא אכן MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }
}