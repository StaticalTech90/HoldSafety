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
        btnTermsAndConditions.setOnClickListener(view -> termsAndConditions());
        btnAbout.setOnClickListener(view -> aboutSystem());
    }

    public void contactDevelopers(){
        Intent contactDevelopers = new Intent(getApplicationContext(), ContactDevelopersActivity.class);
        startActivity(contactDevelopers);
    }

    public void userManual(){
        Toast.makeText(getApplicationContext(), "User Manual", Toast.LENGTH_SHORT).show();
    }

    public void termsAndConditions(){
        Toast.makeText(getApplicationContext(), "Terms and Conditions", Toast.LENGTH_SHORT).show();
    }

    public void aboutSystem(){
        Toast.makeText(getApplicationContext(), "About the System", Toast.LENGTH_SHORT).show();
    }

    private void goBack(){
        startActivity(new Intent(OthersActivity.this, LoginActivity.class));
        finish();
    }
}