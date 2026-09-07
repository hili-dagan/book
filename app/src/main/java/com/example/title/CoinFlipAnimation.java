package com.example.title;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class CoinFlipAnimation {

    public static void flipCoinMultiple(View view, boolean showHeads, FlipCallback callback) {
        // סיבוב של 3 פעמים מלאות + חצי סיבוב אם זה Tails
        float totalRotation = 1080f + (showHeads ? 0f : 180f);

        ObjectAnimator flip = ObjectAnimator.ofFloat(view, "rotationY", 0f, totalRotation);
        flip.setDuration(1200);
        flip.setInterpolator(new AccelerateDecelerateInterpolator());

        // האזנה לערכי האנימציה כדי להחליף את התמונה באמצע הסיבוב
        flip.addUpdateListener(animation -> {
            float currentRotation = (float) animation.getAnimatedValue();
            // בכל פעם שהמטבע משלים חצי סיבוב (בזווית של 90, 270, 450 וכו'), בודקים איזה צד להציג
            if (view instanceof ImageView) {
                ImageView iv = (ImageView) view;
                // חישוב פשוט: האם אנחנו בחלק הקדמי או האחורי של הסיבוב הנוכחי
                float normalizedRotation = currentRotation % 360;
                if (normalizedRotation > 90 && normalizedRotation < 270) {
                    iv.setImageResource(R.drawable.tailshen);
                    // הפיכת התמונה כדי שלא תראה בכתב ראי כשהיא ב-180 מעלות
                    iv.setScaleX(-1.0f);
                } else {
                    iv.setImageResource(R.drawable.headhen);
                    iv.setScaleX(1.0f);
                }
            }
        });

        // אנימציית קפיצה (Scale)
        ValueAnimator scaleAnim = ValueAnimator.ofFloat(1.0f, 1.3f, 1.1f, 1.0f);
        scaleAnim.setDuration(1200);
        scaleAnim.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            view.setScaleY(scale);
            // אנחנו לא נוגעים ב-ScaleX כאן כי ה-updateListener למעלה מנהל אותו
        });
        scaleAnim.setInterpolator(new LinearInterpolator());

        flip.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // קיבוע מצב סופי
                if (showHeads) {
                    view.setRotationY(0f);
                    view.setScaleX(1.0f);
                    ((ImageView)view).setImageResource(R.drawable.headhen);
                } else {
                    view.setRotationY(180f);
                    view.setScaleX(-1.0f); // שומר על התמונה לא הפוכה
                    ((ImageView)view).setImageResource(R.drawable.tailshen);
                }

                if (callback != null) {
                    callback.onFlipComplete();
                }
            }
        });

        flip.start();
        scaleAnim.start();
    }

    public interface FlipCallback {
        void onFlipComplete();
    }
}