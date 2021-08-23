package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class DesignateContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designate_contact);
    }

    public void addContact(View view){
        //Toast.makeText(getApplicationContext(), "Add Contact", Toast.LENGTH_LONG).show();
        Intent intent = new Intent (getApplicationContext(), AddContactActivity.class);
        startActivity(intent);
    }

    public void updateContact(View view){
        //Toast.makeText(getApplicationContext(), "Update Contact", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent (getApplicationContext(), SelectContactActivity.class);
        startActivity(intent);
    }
}