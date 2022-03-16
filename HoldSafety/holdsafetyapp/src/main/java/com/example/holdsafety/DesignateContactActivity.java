package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class DesignateContactActivity extends AppCompatActivity {
    ImageView btnBack;
    ConstraintLayout btnAddContact, btnUpdateContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designate_contact);

        btnBack = findViewById(R.id.backArrow);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnUpdateContact = findViewById(R.id.btnUpdateContact);

        btnBack.setOnClickListener(view -> goBack());
        btnAddContact.setOnClickListener(view -> addContact());
        btnUpdateContact.setOnClickListener(view -> updateContact());
    }

    public void addContact() {
        startActivity(new Intent (DesignateContactActivity.this, AddContactActivity.class));
    }

    public void updateContact() {
        startActivity(new Intent (DesignateContactActivity.this, SelectContactActivity.class));
    }

    private void goBack() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}