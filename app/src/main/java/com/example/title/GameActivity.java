package com.example.title;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Locale;
import java.util.Random;

@SuppressLint("ClickableViewAccessibility")
public class GameActivity extends AppCompatActivity {

    // הגדרת משתנים
    private int difficultyLevel; // רמת הקושי שנבחרה במסך הקודם
    private int scoreCounter = 0;
    private final Random random = new Random();
    private TextView scoreTextView, timerTextView;
    private ConstraintLayout gameLayout;
    private ImageButton targetButton;

    private SoundPool soundPool; // אתחול אובייקט סאונד
    private int shootSoundId;
    private int missedshotSoundId;
    private ObjectAnimator targetAnimation; //אתחול אובייקט אנימציה
    private CountDownTimer gameTimer;
    private int eggSizePx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initUI();
        initSound();

        difficultyLevel = getIntent().getIntExtra("difficulty_level", 1); // רמת הקושי של בחירת המשתמש. אילולא זה, הקוד היה ניגש לבחירת המחדל.

        startTimer();
        setupInputListeners();
    }

    // מגדיר את כל הפקדים שיופיעו על המסך
    private void initUI() {
        targetButton = findViewById(R.id.targetbtn);
        gameLayout = findViewById(R.id.gameLayout);
        timerTextView = findViewById(R.id.Timer);
        scoreTextView = findViewById(R.id.scorecounter);
        scoreTextView.setText("0");
    }

    // מגדיר את הסאונד
    private void initSound() {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)   // דווקא 5 כי זה לא מעמיס על המעבד והזיכרון של הטלפון
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)   // להגדיר את הצליל כמדיה ולא צלצול או התראות
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)  // הגדרת סוג הסאונד כדי שהמערכת תדע איך לעבד
                        .build())
                .build();
        shootSoundId = soundPool.load(this, R.raw.shoot, 1);
        missedshotSoundId = soundPool.load(this, R.raw.missedshot, 1);

    }


    // מה יקרה כשהמשתמש ילחץ על משהו במסך
    private void setupInputListeners() {
        // לחיצה על המסך (על הרקע)
        gameLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) { // האירוע של רגע הלחיצה
                throwEgg(event.getX(), event.getY());
            }
            return true;
        });

        // לחיצה על המטרה
        targetButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) { // האירוע של רגע הלחיצה
                float absoluteX = targetButton.getX() + event.getX();
                float absoluteY = targetButton.getY() + event.getY();
                throwEgg(absoluteX, absoluteY); // שקלול הלחיצה בגבולות המטרה
            }
            return false;
        });
    }

    // זריקת הביצה
    private void throwEgg(float touchX, float touchY) {
        soundPool.play(missedshotSoundId, 1, 1, 0, 0, 1);
        final ImageView flyingEgg = new ImageView(this);
        flyingEgg.setImageResource(R.drawable.egg);
        eggSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());   //מגדיר גודל עבור כל המסכים של כל המכשירים. לביצה לבד כי גודלה היחיד שמשתנה
        flyingEgg.setLayoutParams(new ConstraintLayout.LayoutParams(eggSizePx, eggSizePx));     // שינוי הביצה בפועל

        // נקודת התחלה
        flyingEgg.setX(1080);
        flyingEgg.setY(1920);

        gameLayout.addView(flyingEgg);


        flyingEgg.animate()

                .x(touchX - (eggSizePx / 2f))
                .y(touchY - (eggSizePx / 2f))
                .rotation(360f)
                .scaleX(0.7f).scaleY(0.7f)
                .setDuration(500)
                .withEndAction(() -> checkCollision(flyingEgg))
                .start();
    }

    private void checkCollision(ImageView egg) {


        // מציאת מרכז הביצה
        float eggCenterX = egg.getX() + (eggSizePx / 2f);
        float eggCenterY = egg.getY() + (eggSizePx / 2f);

        float targetRadius = targetButton.getWidth() / 2f;

        // מציאת מרכז המטרה
        float targetCenterX = targetButton.getX() + (targetButton.getWidth() / 2f);
        float targetCenterY = targetButton.getY() + (targetButton.getHeight() / 2f);


        // נוסחאת מרחק בין שתי נקודות
        double distance = Math.sqrt(
                Math.pow(eggCenterX - targetCenterX, 2) +
                        Math.pow(eggCenterY - targetCenterY, 2)
        );

        if (distance <= (targetRadius)) { // מה שיקרה אם הביצה פוגעת במטרה
            soundPool.play(shootSoundId, 1, 1, 0, 0, 1);
            // עצירת תנועת הביצה
            egg.animate().cancel();

            // שינוי לביצה שבורה ועדכון ניקוד
            egg.setImageResource(R.drawable.cracked);
            scoreCounter += (difficultyLevel == 1) ? 1 : 2;
            scoreTextView.setText(String.valueOf(scoreCounter));

            // שמירת מרחק יחסי כדי שהביצה "תידבק" למטרה
            float offsetX = egg.getX() - targetButton.getX();
            float offsetY = egg.getY() - targetButton.getY();

            // אנימציית היעלמות משותפת
            targetButton.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setUpdateListener(animation -> {
                        egg.setX(targetButton.getX() + offsetX);
                        egg.setY(targetButton.getY() + offsetY);
                        egg.setAlpha(targetButton.getAlpha()); // רמת השקיפות
                    })
                    .withEndAction(() -> {
                        gameLayout.removeView(egg);
                        if (difficultyLevel == 1) moveTargetStatic();
                        else moveTargetMoving();
                        targetButton.setAlpha(1f);
                    })
                    .start();

        } else { // מה יקרה אם הביצה לא פוגעת במטרה
            if (scoreCounter > 0) scoreCounter--;
            scoreTextView.setText(String.valueOf(scoreCounter));

            egg.animate()
                    .scaleX(0f).scaleY(0f).alpha(0f)
                    .setDuration(500)
                    .withEndAction(() -> {
                        gameLayout.removeView(egg);
                    })
                    .start();
        }
    }

    // משנה את מיקום המטרה הסטטית
    private void moveTargetStatic() {
        if (gameLayout.getWidth() == 0) return;
        int verticalMargin = (int) (gameLayout.getHeight() * 0.2f);
        targetButton.setX(random.nextInt(gameLayout.getWidth() - targetButton.getWidth()));
        targetButton.setY(verticalMargin + random.nextInt(Math.max(1, gameLayout.getHeight() - 2 * verticalMargin - targetButton.getHeight())));
    }

    // משנה את מיקום המטרה הדינאמית
    private void moveTargetMoving() {
        if (targetAnimation != null)
            targetAnimation.cancel();

        int verticalMargin = (int) (gameLayout.getHeight() * 0.2f); // כדי שהמטרה לא תדרוס את החלק העליון, הניקוד והטיימר

        targetButton.setY(verticalMargin + random.nextInt(Math.max(1, gameLayout.getHeight() - 2 * verticalMargin - targetButton.getHeight())));

        boolean leftToRight = random.nextBoolean();
        float startX = leftToRight ? -targetButton.getWidth() : gameLayout.getWidth();
        float endX = leftToRight ? gameLayout.getWidth() : -targetButton.getWidth();

        targetAnimation = ObjectAnimator.ofFloat(targetButton, "x", startX, endX);
        targetAnimation.setDuration(2000);
        targetAnimation.setInterpolator(new LinearInterpolator());
        targetAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (targetButton.getX() == endX)
                    moveTargetMoving();
            }
        });
        targetAnimation.start();
    }

    // מפעיל טיימר 30 שניות על לכל המשחק
    private void startTimer() {
        gameTimer = new CountDownTimer(31000, 10) {
            @Override
            public void onTick(long millis) {
                // 1. עדכון הטקסט של הטיימר (כמו שהיה לך)
                timerTextView.setText(String.format(Locale.getDefault(), "%.2f", millis / 1000f));

                // 2. הקסם: אם נשארו 5 שניות (5000 מילישניות) או פחות - צובעים באדום
                if (millis <= 5000) {
                    timerTextView.setTextColor(Color.parseColor("#820707"));
                } else {
                    // החזרה לצבע המקורי אם הטיימר מתחיל מחדש
                    timerTextView.setTextColor(Color.parseColor("#DCE2E8"));
                }
            }

            @Override
            public void onFinish() {
                // צביעה סופית ליתר ביטחון ומעבר מסך
                timerTextView.setTextColor(Color.parseColor("#820707"));

                Intent intent = new Intent(GameActivity.this, DoubleOrNothingActivity.class);
                intent.putExtra("current_score", scoreCounter);
                startActivity(intent);

                // הפעלת האנימציה - המסך החדש עולה, המסך הנוכחי דועך
                overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                finish();
            }
        }.start();
    }

    // כשהמשתמש סגר את האפליקציה או שהמסך הזה הפסיק לעבוד
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (targetAnimation != null)
            targetAnimation.cancel();
        if (gameTimer != null)
            gameTimer.cancel();
        soundPool.release();
    }
}