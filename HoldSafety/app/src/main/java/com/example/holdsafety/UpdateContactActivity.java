package com.example.holdsafety;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class UpdateContactActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;

    EditText contactLastName, contactFirstName, contactEmail, contactMobileNumber, contactRelation;
    Button updateBtn;

    DocumentReference docRef;

    Intent intent;

    String email;
    String firstName;
    String lastName;
    String mobileNumber;
    String relation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);

        //get data from previous activity
        intent = getIntent();

        String docId = intent.getStringExtra("documentId").trim();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("emergencyContacts")
                .document(user.getUid()).collection("contacts").document(docId);

        if(intent != null){
            intent.getStringExtra("documentId");

            contactLastName = findViewById(R.id.txtLastName);
            contactFirstName = findViewById(R.id.txtFirstName);
            contactEmail = findViewById(R.id.txtEmailUpdate);
            contactMobileNumber = findViewById(R.id.txtMobileNumber);
            contactRelation = findViewById(R.id.txtRelation);
            updateBtn = findViewById(R.id.btnUpdateContact);

            //SET??????
            email = intent.getStringExtra("email").trim();
            firstName = intent.getStringExtra("firstName").trim();
            lastName = intent.getStringExtra("lastName").trim();
            mobileNumber = intent.getStringExtra("mobileNumber").trim();
            relation = intent.getStringExtra("relation").trim();

            Toast.makeText(this, intent.getStringExtra("email"), Toast.LENGTH_SHORT).show();

            //Need String vars because maarte
            contactLastName.setText(lastName);
            contactFirstName.setText(firstName);
            contactEmail.setText(email);
            contactMobileNumber.setText(mobileNumber);
            contactRelation.setText(relation);

            // :: means lambda
            updateBtn.setOnClickListener(this::updateContact);

        } else {
            Toast.makeText(this, "unable to get extras", Toast.LENGTH_SHORT).show();
        }

    }

    public void updateContact(View view){
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
}