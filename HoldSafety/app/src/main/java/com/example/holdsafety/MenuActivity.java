package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    private GoogleApiClient googleApiClient;
    GoogleSignInClient gsc;

    GoogleSignInOptions gso;
    TextView btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();
        btnLogout = findViewById(R.id.txtLogout);

        /*
        gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("233680747912-m8q45hor79go5n8aqfkuneklnkshudqs.apps.googleusercontent.com")
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);


        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

         */

        btnLogout.setOnClickListener(this::logoutUser);
    }

    public void userAccount(View view){
        Intent intent = new Intent (MenuActivity.this, AccountDetailsActivity.class);
        startActivity(intent);
    }

    public void designateContacts(View view){
        //Toast.makeText(getApplicationContext(), "Designate Contacts", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent (getApplicationContext(), DesignateContactActivity.class);
        startActivity(intent);
    }

    public void contactDevelopers(View view){
        Toast.makeText(getApplicationContext(), "Contact Developers", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent (getApplicationContext(), ContactDevelopersActivity.class);
        startActivity(intent);
    }

    public void userManual(View view){
        Toast.makeText(getApplicationContext(), "User Manual", Toast.LENGTH_SHORT).show();
    }

    public void termsAndConditions(View view){
        Toast.makeText(getApplicationContext(), "Terms and Conditions", Toast.LENGTH_SHORT).show();
    }

    public void aboutSystem(View view){
        Toast.makeText(getApplicationContext(), "About the System", Toast.LENGTH_SHORT).show();
    }

    public void viewReports(View view){
        //Toast.makeText(getApplicationContext(), "About the System", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent (getApplicationContext(), ReportsActivity.class);
        startActivity(intent);
    }

    public void logoutUser(View view){
            //========SIGN OUT
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
            //========END OF SIGN OUT


    }
}