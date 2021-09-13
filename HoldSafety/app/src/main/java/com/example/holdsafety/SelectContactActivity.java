package com.example.holdsafety;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectContactActivity extends AppCompatActivity {
    RecyclerView recyclerViewContacts;
    String[] contactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);

        contactName = getResources().getStringArray(R.array.contact_name);
        recyclerViewContacts = findViewById(R.id.recyclerviewContactList);

        ContactAdapter contactAdapter = new ContactAdapter(this, contactName);
        recyclerViewContacts.setAdapter(contactAdapter);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));

    }


}