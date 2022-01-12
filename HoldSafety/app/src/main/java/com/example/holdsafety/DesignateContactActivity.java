package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class DesignateContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designate_contact);
    }

    public void addContact(View view){
        //Toast.makeText(getApplicationContext(), "Add Contact", Toast.LENGTH_LONG).show();
        startActivity(new Intent (DesignateContactActivity.this, AddContactActivity.class));
    }

    public void updateContact(View view){
        //Toast.makeText(getApplicationContext(), "Update Contact", Toast.LENGTH_SHORT).show();
        startActivity(new Intent (DesignateContactActivity.this, SelectContactActivity.class));
    }
}