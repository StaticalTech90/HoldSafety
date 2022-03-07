package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class UserManualActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manual);
    }

    private void goBack() {
        finish();
    }
}