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

    Intent intentLogout;

    ConstraintLayout btnUserAccount, btnDesignateContact, btnContactDevelopers, btnUserManual, btnTermsAndConditions, btnAbout;
    TextView btnViewReports, btnLogout, txtName;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        logHelper = new LogHelper(getApplicationContext(), mAuth, user,this);


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
        Intent intentUserAccount = new Intent (MenuActivity.this, AccountDetailsActivity.class);
        startActivity(intentUserAccount);
    }

    private void designateContacts() {
        Intent intentDesignateContacts = new Intent (MenuActivity.this, DesignateContactActivity.class);
        startActivity(intentDesignateContacts);
    }

    private void contactDevelopers() {
        Intent intentContactDevelopers = new Intent (MenuActivity.this, ContactDevelopersActivity.class);
        startActivity(intentContactDevelopers);
    }

    private void userManual() {
        Intent intentUserManual = new Intent (MenuActivity.this, UserManualActivity.class);
        startActivity(intentUserManual);
    }

    private void termsOfService() {
        Intent intentTermsOfService = new Intent (MenuActivity.this, TermsOfServiceActivity.class);
        startActivity(intentTermsOfService);
    }

    private void aboutSystem() {
        Intent intentAboutSystem = new Intent (MenuActivity.this, AboutSystemActivity.class);
        startActivity(intentAboutSystem);
    }

    private void viewReports() {
        Intent intentViewReports = new Intent (MenuActivity.this, ReportsActivity.class);
        startActivity(intentViewReports);
    }

    private void logoutUser() {
            intentLogout = new Intent(MenuActivity.this, LoginActivity.class);
            GoogleSignInOptions gso = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken("233680747912-m8q45hor79go5n8aqfkuneklnkshudqs.apps.googleusercontent.com")
                    .requestEmail()
                    .build();

            GoogleSignInClient gsc = GoogleSignIn.getClient(MenuActivity.this, gso);
            gsc.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    logHelper.saveToFirebase("logoutUser", "SUCCESS", "User signed out");
                    //Toast.makeText(MenuActivity.this, "Sign Out", Toast.LENGTH_SHORT).show();
                    startActivity(intentLogout);

                    //clears logged-in instance
                    intentLogout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    finish();
                }
            });

        mAuth.signOut();

        //clears logged-in instance
        intentLogout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        logHelper.saveToFirebase("logoutUser", "SUCCESS", "User signed out");
        finish();
    }

    private void goBack() {
        finish();
        onBackPressed();
    }
}