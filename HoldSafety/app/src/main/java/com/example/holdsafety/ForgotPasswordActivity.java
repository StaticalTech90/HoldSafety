package com.example.holdsafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    EditText etEmail;
    Button sendRecoveryLink;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.txtEmail);
        sendRecoveryLink = findViewById(R.id.btnSendRecoveryLink);

        auth = FirebaseAuth.getInstance();

        sendRecoveryLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Reset password via email link
                String email = etEmail.getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    etEmail.setHint("Enter Email");
                    etEmail.setError("Enter Email");
                }

                auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "We have sent an email", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotPasswordActivity.this, "Failed to send an email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // TODO: Go to OTP input screen (if needed)
                Intent otpInput;
            }
        });
    }

    public void recoverAccount(View view){
        Toast.makeText(getApplicationContext(), "Recovery link is sent to your email", Toast.LENGTH_SHORT).show();
    }
}