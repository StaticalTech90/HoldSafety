package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void userAccount(View view){
        Intent intent = new Intent (this, AccountDetailsActivity.class);
        startActivity(intent);
    }

    public void designateContacts(View view){
        Toast.makeText(getApplicationContext(), "Designate Contacts", Toast.LENGTH_SHORT).show();
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


}