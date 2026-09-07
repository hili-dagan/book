package com.example.title;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class difficultyActivity extends AppCompatActivity {

    private View fadeOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        fadeOverlay = findViewById(R.id.fade_overlay);
        RadioGroup radioGroup = findViewById(R.id.radio_group);

        RadioButton staticRb = findViewById(R.id.static_rb);
        RadioButton movingRb = findViewById(R.id.moving_rb);

// ביטול מוחלט של ה-Tinting (הצביעה) האוטומטית של אנדרואיד
        staticRb.setButtonTintList(null);
        movingRb.setButtonTintList(null);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int level = 0;
                String levelName = "";

                if (checkedId == R.id.static_rb) {
                    level = 1;
                    levelName = "static target";
                } else if (checkedId == R.id.moving_rb) {
                    level = 2;
                    levelName = "Dynamic target";
                } else {
                    return;
                }

                // קריאה לפונקציה שמציגה את הדיאלוג
                showConfirmationDialog(level, levelName);
            }
        });
    }

    private void showConfirmationDialog(final int level, String levelName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pay attention!!");
        builder.setMessage("You have 30 secondes to hit as much as " + levelName + " as you can!");

        builder.setPositiveButton("start", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // במקום לעבור מסך מיד, נקרא לאנימציה
                startFadeToWhite(level);
            }
        });

        AlertDialog dialog = builder.create();


        // נוריד את הבלוק דיאלוג למטה כדי לא להסתיר את הבחירה של המשתמש
        if (dialog.getWindow() != null) {
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();

            layoutParams.y = 200;

            dialog.getWindow().setAttributes(layoutParams);
        }

        dialog.show();
    }

    private void startFadeToWhite(int level) {
        // 1. הפיכת השכבה לנראית אבל שקופה לגמרי
        fadeOverlay.setVisibility(View.VISIBLE);
        fadeOverlay.setAlpha(0f);

        // 2. הרצת אנימציה לשינוי השקיפות (Alpha) ל-1
        fadeOverlay.animate()
                .alpha(1f)
                .setDuration(1000) // משך זמן האנימציה - שנייה אחת
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // 3. רק כשהאנימציה מסתיימת - עוברים מסך
                        Intent intent = new Intent(difficultyActivity.this, GameActivity.class);
                        intent.putExtra("difficulty_level", level);
                        startActivity(intent);

                        // אנימציה נוספת למעבר חלק בין ה-Activities
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                })
                .start();
    }
}