package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }

    public void recoverAccount(View view){
        Toast.makeText(getApplicationContext(), "Recovery link is sent to your email", Toast.LENGTH_SHORT).show();
    }
}