package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgotPasswordActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    LogHelper logHelper;
    EditText txtEmail;
    ImageView btnBack;
    Button btnRecover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        logHelper = new LogHelper(getApplicationContext(), mAuth, this);

        txtEmail = findViewById(R.id.txtEmail);
        btnBack = findViewById(R.id.backArrow);
        btnRecover = findViewById(R.id.btnSendRecoveryLink);

        btnBack.setOnClickListener(view -> goBack());
        btnRecover.setOnClickListener(view -> recoverAccount());
    }

    public void recoverAccount(){
        String email = txtEmail.getText().toString().trim();
        if(TextUtils.isEmpty(email)) {
            txtEmail.setError("Enter Email");
        } else {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    //email exists
                    logHelper.saveToFirebase("recoverAccount", "SUCCESS", "Recovery link sent");
                    //Toast.makeText(ForgotPasswordActivity.this, "Recovery link is sent to your email", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //email does not exist
                    logHelper.saveToFirebase("recoverAccount", "SUCCESS", "Unable to send reset email");
                    //Toast.makeText(ForgotPasswordActivity.this, "Unable to send reset email. Please make sure your email is correct.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void goBack() {
        finish();
    }
}