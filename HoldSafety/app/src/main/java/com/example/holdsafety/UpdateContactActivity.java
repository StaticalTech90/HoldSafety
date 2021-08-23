package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class UpdateContactActivity extends AppCompatActivity {
    EditText contactLastName;
    String lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);

        contactLastName = findViewById(R.id.txtLastName);

        getData();
        setData();
    }

    public void updateContact(View view){
        Toast.makeText(getApplicationContext(), "Update Contact", Toast.LENGTH_SHORT).show();
    }


    public void getData(){
        if(getIntent().hasExtra("contactName")){
            lastName = getIntent().getStringExtra("contactName");
        }

        else{
            Toast.makeText(getApplicationContext(), "No Data", Toast.LENGTH_SHORT).show();
        }
    }

    public void setData(){
        contactLastName.setText(lastName);
    }
}