package com.example.title;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText loginEmail, loginPassword;
    private TextView gotoSignup;
    private  Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.login_btn);
        gotoSignup = findViewById(R.id.SwichtoSignupactivity_btn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString(); // ממיר את הטקסט החופשי (type Editable) לטקסט ומאחסן במשתנה
                String password = loginPassword.getText().toString();
                if(!email.isEmpty()&& Patterns.EMAIL_ADDRESS.matcher(email).matches()){ //האם האימייל שהזין המשתמש מתאים לכתיבת חוקי המייל
                    if (!password.isEmpty())
                    {
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() { //תיצור אובייקט מתוך מאזין שיפעל רק אם הפעולה בו מצליחה
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                    else // אם המשתמש לא הזין סיסמא
                    {
                        loginPassword.setError("Password cannot be empty");
                    }
                }
                else if (email.isEmpty())
                {
                    loginEmail.setError("Password cannot be empty");
                } else
                {
                    loginEmail.setError("Please enter valid email");
                }
            }
        });
        gotoSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }
}
