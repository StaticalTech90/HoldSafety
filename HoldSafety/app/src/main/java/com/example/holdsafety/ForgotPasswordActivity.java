package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        txtEmail = findViewById(R.id.txtEmail);

        mAuth = FirebaseAuth.getInstance();
    }

    public void recoverAccount(View view){
        String email = txtEmail.getText().toString().trim();

        if(TextUtils.isEmpty(email)) {
            txtEmail.setError("Enter Email");
        } else {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            //email exists
                            Toast.makeText(ForgotPasswordActivity.this, "Recovery link is sent to your email", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            //email does not exist
                            Toast.makeText(ForgotPasswordActivity.this, "Unable to send reset email. Please make sure your email is correct.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        }
    }

    public void goBack(View view){
        startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
        finish();
    }
}