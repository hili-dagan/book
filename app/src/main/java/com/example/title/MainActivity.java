//איפה שהקוד ממוקם
package com.example.title;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button loginActivityButton;

    @Override //תדרוס את כל האון קריאייט שהיה
    protected void onCreate(Bundle savedInstanceState) { //יוצר את קופסאת הזיכרון של האפליקציה
        super.onCreate(savedInstanceState); //תריץ את מה שחובה להיות מורץ
        setContentView(R.layout.activity_main);

        // אתחול רכיבים, נקרא למנהל המחלקה הקיים
        mAuth = FirebaseAuth.getInstance();
        MusicManager.getInstance(this).startMusic();

        Button startbtn = findViewById(R.id.startbtn);
        loginActivityButton = findViewById(R.id.login_btn);
        ImageButton settingsBtn = findViewById(R.id.settings_btn);

        // כפתור הגדרות - פותח את הפרגמנט עם אנימציה
        settingsBtn.setOnClickListener(v -> {
            settingsFragment();
        });

        // כפתור התחלה
        startbtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, difficultyActivity.class));
        });

        // כפתור התחברות/התנתקות
        loginActivityButton.setOnClickListener(v -> handleLoginLogout());
    }

    //מעבר ללוג-אין והודעת טקסט במידה והמשתמש התנתק
    private void handleLoginLogout() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            updateUI();
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    private void settingsFragment()
    {
        // ניצור מופע חדש של פרגמנט ההגדרות
        settingsFragment fragment = new settingsFragment();

        getSupportFragmentManager().beginTransaction()
                // הגדרת האנימציה: כניסה מלמטה, יציאה בפייד, וחזור (בסגירה) בירידה למטה
                .setCustomAnimations(
                        R.anim.slide_up,         // אנימציית כניסה של ההגדרות (עולה)
                        android.R.anim.fade_out, // אנימציית יציאה של האקטיביטי ברקע
                        android.R.anim.fade_in,  // אנימציית חזרה של האקטיביטי כשסוגרים
                        R.anim.slide_down        // אנימציית יציאה של ההגדרות (יורד חזרה)
                )
                // מחליף את ה-Container הקיים בפרגמנט (וודא שיש לך FrameLayout ב-XML בשם fragment_container)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // מאפשר חזרה למסך הראשי בלחיצה על Back או closeBtn
                .commit();
    }
    //יציג מיד כל שינוי שיש בתחברות/התנתקות משתמש
    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

    private void updateUI() {
        if (mAuth.getCurrentUser() != null) {
            loginActivityButton.setText("Log Out");
        } else {
            loginActivityButton.setText("Log In");
        }
    }
}
