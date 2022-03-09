package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class OthersActivity extends AppCompatActivity {
    ImageView btnBack;
    ConstraintLayout btnContactDevelopers, btnUserManual, btnTermsAndConditions, btnAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others);

        btnBack = findViewById(R.id.backArrow);
        btnContactDevelopers = findViewById(R.id.btnContactDevelopers);
        btnUserManual = findViewById(R.id.btnUserManual);
        btnTermsAndConditions = findViewById(R.id.btnTermsAndConditions);
        btnAbout = findViewById(R.id.btnAbout);

        btnBack.setOnClickListener(view -> goBack());
        btnContactDevelopers.setOnClickListener(view -> contactDevelopers());
        btnUserManual.setOnClickListener(view -> userManual());
        btnTermsAndConditions.setOnClickListener(view -> termsOfService());
        btnAbout.setOnClickListener(view -> aboutSystem());
    }

    private void contactDevelopers() {
        Intent contactDevelopers = new Intent (getApplicationContext(), ContactDevelopersActivity.class);
        startActivity(contactDevelopers);
    }

    private void userManual() {
        Intent userManual = new Intent (getApplicationContext(), UserManualActivity.class);
        startActivity(userManual);
    }

    private void termsOfService() {
        Intent termsOfService = new Intent (getApplicationContext(), TermsOfServiceActivity.class);
        startActivity(termsOfService);
    }

    private void aboutSystem() {
        Intent aboutSystem = new Intent (getApplicationContext(), AboutSystemActivity.class);
        startActivity(aboutSystem);
    }

    private void goBack(){
        finish();
    }
}