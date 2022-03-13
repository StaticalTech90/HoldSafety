package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class TermsOfServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_service);

        ImageView btnBack = findViewById(R.id.backArrow);
        btnBack.setOnClickListener(view -> goBack());
    }

    private void goBack() {
        finish();
    }
}