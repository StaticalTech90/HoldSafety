package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MenuActivity extends AppCompatActivity {
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;
    LogHelper logHelper;
    ImageView btnBack;
    ConstraintLayout btnUserAccount, btnDesignateContact, btnContactDevelopers, btnUserManual, btnTermsAndConditions, btnAbout;
    TextView btnViewReports, btnLogout, txtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        logHelper = new LogHelper(getApplicationContext(), mAuth, user, this);


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

        displayName();

        btnBack.setOnClickListener(view -> goBack());
        btnUserAccount.setOnClickListener(view -> userAccount());
        btnDesignateContact.setOnClickListener(view -> designateContacts());
        btnContactDevelopers.setOnClickListener(view -> contactDevelopers());
        btnUserManual.setOnClickListener(view -> userManual());
        btnTermsAndConditions.setOnClickListener(view -> termsOfService());
        btnAbout.setOnClickListener(view -> aboutSystem());
        btnViewReports.setOnClickListener(view -> viewReports());
        btnLogout.setOnClickListener(view -> logoutUser());
    }

    private void displayName() {
        //txtName.setText(mAuth.getCurrentUser().getDisplayName());
        db.collection("users").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String username;
                String lastName = documentSnapshot.getString("LastName");
                String firstName = documentSnapshot.getString("FirstName");

                username = firstName + " " + lastName;
                txtName.setText(username);
            }
        });

    }

    private void userAccount() {
        logHelper.saveToFirebase("userAccount", "CLICK EVENT", "Go to AccountDetailsActivity");
        Intent userAccount = new Intent (MenuActivity.this, AccountDetailsActivity.class);
        startActivity(userAccount);
    }

    private void designateContacts() {
        logHelper.saveToFirebase("designateContacts", "CLICK EVENT", "Go to DesignateContactActivity");
        Intent designateContacts = new Intent (getApplicationContext(), DesignateContactActivity.class);
        startActivity(designateContacts);
    }

    private void contactDevelopers() {
        logHelper.saveToFirebase("contactDevelopers", "CLICK EVENT", "Go to ContactDevelopersActivity");

        Intent contactDevelopers = new Intent (getApplicationContext(), ContactDevelopersActivity.class);
        startActivity(contactDevelopers);
    }

    private void userManual() {
        logHelper.saveToFirebase("userManual", "CLICK EVENT", "Go to UserManualActivity");

        Intent userManual = new Intent (getApplicationContext(), UserManualActivity.class);
        startActivity(userManual);
    }

    private void termsOfService() {
        logHelper.saveToFirebase("termsOfService", "CLICK EVENT", "Go to TermsOfServiceActivity");

        Intent termsOfService = new Intent (getApplicationContext(), TermsOfServiceActivity.class);
        startActivity(termsOfService);
    }

    private void aboutSystem() {
        logHelper.saveToFirebase("aboutSystem", "CLICK EVENT", "Go to AboutSystemActivity");

        Intent aboutSystem = new Intent (getApplicationContext(), AboutSystemActivity.class);
        startActivity(aboutSystem);
    }

    private void viewReports() {
        logHelper.saveToFirebase("viewReports", "CLICK EVENT", "Go to ReportsActivity");

        Intent viewReports = new Intent (getApplicationContext(), ReportsActivity.class);
        startActivity(viewReports);
    }

    private void logoutUser() {

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
                    logHelper.saveToFirebase("logoutUser", "SUCCESS", "User signed out");
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