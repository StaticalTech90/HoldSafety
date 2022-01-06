package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void userAccount(View view){
        Intent intent = new Intent (getApplicationContext(), AccountDetailsActivity.class);
        startActivity(intent);
    }

    public void designateContacts(View view){
        //Toast.makeText(getApplicationContext(), "Designate Contacts", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent (getApplicationContext(), AddContactActivity.class);
        startActivity(intent);
    }

    public void contactDevelopers(View view){
        Toast.makeText(getApplicationContext(), "Contact Developers", Toast.LENGTH_SHORT).show();
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
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(MenuActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MenuActivity.this, MainActivity.class));
        finish();
    }
}