package com.example.title;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Random;

public class DoubleOrNothingActivity extends AppCompatActivity {

    private ImageView coinImageView;
    private TextView resultTextView;
    private Button headsButton, tailsButton, skipbutton;
    private ImageButton info_btn;
    private Random random;
    private static final int HEADS = 0;
    private static final int TAILS = 1;
    private int userChoice = -1;
    private int coinResult = -1;
    private String txtres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_double_or_nothing);

        coinImageView = findViewById(R.id.coin_iv);
        resultTextView = findViewById(R.id.result_tv);
        headsButton = findViewById(R.id.heads_btn);
        tailsButton = findViewById(R.id.eggs_btn);
        skipbutton = findViewById(R.id.skip_btn);
        info_btn =  findViewById(R.id.info_btn);
        random = new Random();

        Intent intent = getIntent();
        int score = intent.getIntExtra("current_score", 0);

        Skipping(skipbutton, score);

        // Enable 3D rendering
        coinImageView.setCameraDistance(8000);

        headsButton.setOnClickListener(v -> {
            userChoice = HEADS;
            startCoinFlip(score);
        });

        tailsButton.setOnClickListener(v -> {
            userChoice = TAILS;
            startCoinFlip(score);
        });
        info_btn.setOnClickListener(v -> {
            information();
        });
    }
    private void Skipping(Button skipbutton, int score)
    {
        skipbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DoubleOrNothingActivity.this, ScoreboardActivity.class);
                intent.putExtra("current_score", score);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                finish();
            }
        });
    }

    private void information ()
    {
        FragmentManager fm = getSupportFragmentManager(); //מבקש גישה לכל הפעולות שאחראיות לפרגמנט הזה
        Fragment existingFragment = fm.findFragmentByTag("informationFragment");

        if (existingFragment != null && existingFragment.isVisible()) {
            fm.popBackStack(); //כפתור היציאה
        } else {
            fm.beginTransaction() //יוצר את הפרגמנט
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, new informationFragment(), "informationFragment")
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        }
    }
    private void startCoinFlip(int score) {
        headsButton.setEnabled(false);
        tailsButton.setEnabled(false);
        skipbutton.setEnabled(false);
        resultTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));

        // Generate result first
        coinResult = random.nextInt(2);

        // Start flip animation
        boolean showHeads = (coinResult == HEADS);
        CoinFlipAnimation.flipCoinMultiple(coinImageView, showHeads, new CoinFlipAnimation.FlipCallback() {
            @Override
            public void onFlipComplete() {
                showResult(score);
            }
        });
    }

    private void showResult(int score) {
        // Update the image to final state
        if (coinResult == HEADS) {
            txtres = "Head";
            coinImageView.setImageResource(R.drawable.headhen);
        } else {
            txtres = "tails";
            coinImageView.setImageResource(R.drawable.tailshen);
        }

        if (userChoice == coinResult) {
            resultTextView.setText("Congratulations! You won! 🎉");
            resultTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(DoubleOrNothingActivity.this, ScoreboardActivity.class);
                intent.putExtra("current_score", score * 2);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);

            }, 2000);
        }
        else {
            resultTextView.setText("Sorry! It was " + txtres + ". Try again! 😢");
            resultTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(DoubleOrNothingActivity.this, ScoreboardActivity.class);
                intent.putExtra("current_score", 0);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                finish();
            }, 2000);

        }

    }

}