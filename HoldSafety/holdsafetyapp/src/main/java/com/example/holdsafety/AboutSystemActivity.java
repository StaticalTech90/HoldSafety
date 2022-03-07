package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class AboutSystemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_system);
    }

    private void goBack() {
        finish();
    }
}