package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    ImageView btnBack;
    ConstraintLayout btnUserAccount, btnDesignateContact, btnContactDevelopers, btnUserManual, btnTermsAndConditions, btnAbout;
    TextView btnViewReports, btnLogout, txtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.backArrow);
        btnUserAccount = findViewById(R.id.layoutAccountDetails);
        btnDesignateContact = findViewById(R.id.btnDesignateContact);
        btnContactDevelopers = findViewById(R.id.btnContactDevelopers);
        btnUserManual = findViewById(R.id.btnUserManual);
        btnTermsAndConditions = findViewById(R.id.btnTermsAndConditions);
        btnAbout = findViewById(R.id.btnAbout);
        btnViewReports = findViewById(R.id.btnViewReports);
        btnLogout = findViewById(R.id.txtLogout);
        txtName = findViewById(R.id.txtName);

        txtName.setText(mAuth.getCurrentUser().getDisplayName());

        btnBack.setOnClickListener(view -> goBack());
        btnUserAccount.setOnClickListener(view -> userAccount());
        btnDesignateContact.setOnClickListener(view -> designateContacts());
        btnContactDevelopers.setOnClickListener(view -> contactDevelopers());
        btnUserManual.setOnClickListener(view -> userManual());
        btnTermsAndConditions.setOnClickListener(view -> termsAndConditions());
        btnAbout.setOnClickListener(view -> aboutSystem());
        btnViewReports.setOnClickListener(view -> viewReports());
        btnLogout.setOnClickListener(view -> logoutUser());
    }

    private void userAccount(){
        Intent userAccount = new Intent (MenuActivity.this, AccountDetailsActivity.class);
        startActivity(userAccount);
    }

    private void designateContacts(){
        Intent designateContacts = new Intent (getApplicationContext(), DesignateContactActivity.class);
        startActivity(designateContacts);
    }

    private void contactDevelopers(){
        Intent contactDevelopers = new Intent (getApplicationContext(), ContactDevelopersActivity.class);
        startActivity(contactDevelopers);
    }

    private void userManual(){
        Toast.makeText(getApplicationContext(), "User Manual", Toast.LENGTH_SHORT).show();
    }

    private void termsAndConditions(){
        Toast.makeText(getApplicationContext(), "Terms and Conditions", Toast.LENGTH_SHORT).show();
    }

    private void aboutSystem(){
        Toast.makeText(getApplicationContext(), "About the System", Toast.LENGTH_SHORT).show();
    }

    private void viewReports(){
        Intent viewReports = new Intent (getApplicationContext(), ReportsActivity.class);
        startActivity(viewReports);
    }

    private void logoutUser(){
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("233680747912-m8q45hor79go5n8aqfkuneklnkshudqs.apps.googleusercontent.com")
                    .requestEmail()
                    .build();

            mAuth.signOut();

            GoogleSignInClient gsc = GoogleSignIn.getClient(MenuActivity.this, gso);
            gsc.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(MenuActivity.this, "Sign Out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MenuActivity.this, LoginActivity.class));
                    finish();
                }
            });
    }

    private void goBack() {
        finish();
    }
}