package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateContactActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;

    ImageView btnBack;
    EditText contactLastName, contactFirstName, contactEmail, contactMobileNumber, contactRelation;
    Button btnUpdate;

    DocumentReference docRef;

    Intent previousActivity;
    String email, firstName, lastName, mobileNumber, relation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.backArrow);

        btnBack.setOnClickListener(view -> goBack());

        //get data from previous activity
        previousActivity = getIntent();
        String docId = previousActivity.getStringExtra("documentId").trim();
        docRef = db.collection("emergencyContacts")
                .document(user.getUid()).collection("contacts").document(docId);

        if(previousActivity != null){
            previousActivity.getStringExtra("documentId");

            contactLastName = findViewById(R.id.txtLastName);
            contactFirstName = findViewById(R.id.txtFirstName);
            contactEmail = findViewById(R.id.txtEmailUpdate);
            contactMobileNumber = findViewById(R.id.txtMobileNumber);
            contactRelation = findViewById(R.id.txtRelation);
            btnUpdate = findViewById(R.id.btnUpdateContact);

            email = previousActivity.getStringExtra("email").trim();
            firstName = previousActivity.getStringExtra("firstName").trim();
            lastName = previousActivity.getStringExtra("lastName").trim();
            mobileNumber = previousActivity.getStringExtra("mobileNumber").trim();
            relation = previousActivity.getStringExtra("relation").trim();

            Toast.makeText(this, previousActivity.getStringExtra("email"), Toast.LENGTH_SHORT).show();

            //Need String vars because maarte
            contactLastName.setText(lastName);
            contactFirstName.setText(firstName);
            contactEmail.setText(email);
            contactMobileNumber.setText(mobileNumber);
            contactRelation.setText(relation);

            btnUpdate.setOnClickListener(view -> updateContact());
        } else {
            Toast.makeText(this, "unable to get extras", Toast.LENGTH_SHORT).show();
        }

    }

    public void updateContact(){
        String changedEmail = contactEmail.getText().toString().trim();
        String changedFirstName = contactFirstName.getText().toString().trim();
        String changedLastName = contactLastName.getText().toString().trim();
        String changedMobileNumber = contactMobileNumber.getText().toString().trim();
        String changedRelation = contactRelation.getText().toString().trim();

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                docRef.update("email", changedEmail);
                docRef.update("firstName", changedFirstName);
                docRef.update("lastName", changedLastName);
                docRef.update("mobileNumber", changedMobileNumber);
                docRef.update("relation", changedRelation);

                Toast.makeText(this, "Successfully updated", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(this, SelectContactActivity.class));
                finish();
            }
            else {
                Toast.makeText(UpdateContactActivity.this, "Failed to update", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goBack(){
        startActivity(new Intent(UpdateContactActivity.this, SelectContactActivity.class));
        finish();
    }
}