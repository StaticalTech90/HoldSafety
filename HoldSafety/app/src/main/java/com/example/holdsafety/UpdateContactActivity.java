package com.example.holdsafety;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateContactActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;

    ImageView btnBack;
    EditText contactLastName, contactFirstName, contactEmail, contactMobileNumber, contactRelation;
    Button btnUpdate;

    Boolean isLastNameChanged, isFirstNameChanged, isRelationChanged, isNumberChanged, isEmailChanged;

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

        isLastNameChanged = false;
        isFirstNameChanged = false;
        isRelationChanged = false;
        isNumberChanged = false;
        isEmailChanged = false;

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

            //Toast.makeText(this, previousActivity.getStringExtra("email"), Toast.LENGTH_SHORT).show();

            //Need String vars because maarte
            contactLastName.setText(lastName);
            contactFirstName.setText(firstName);
            contactEmail.setText(email);
            contactMobileNumber.setText(mobileNumber);
            contactRelation.setText(relation);

            checkUpdates();
            btnUpdate.setOnClickListener(view -> updateContact());
        } else {
            Toast.makeText(this, "Unable to get Extras", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUpdates() {
        //check if there are changes for each edit texts
        contactLastName.addTextChangedListener(new TextWatcher() {
            String newLastName;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newLastName = contactLastName.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(lastName.equals(newLastName)){
                    isLastNameChanged = false;
                    Toast.makeText(UpdateContactActivity.this, "No Changes", Toast.LENGTH_SHORT).show();
                } else{
                    isLastNameChanged = true;
                    //Toast.makeText(UpdateContactActivity.this, "New Last Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        contactFirstName.addTextChangedListener(new TextWatcher() {
            String newFirstName;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newFirstName = contactFirstName.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(firstName.equals(newFirstName)){
                    isFirstNameChanged = false;
                    Toast.makeText(UpdateContactActivity.this, "No Changes", Toast.LENGTH_SHORT).show();
                } else{
                    isFirstNameChanged = true;
                    //Toast.makeText(UpdateContactActivity.this, "New First Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        contactRelation.addTextChangedListener(new TextWatcher() {
            String newRelation;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newRelation = contactRelation.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(relation.equals(newRelation)){
                    isRelationChanged = false;
                    Toast.makeText(UpdateContactActivity.this, "No Changes", Toast.LENGTH_SHORT).show();
                } else{
                    isRelationChanged = true;
                    //Toast.makeText(UpdateContactActivity.this, "New Relation", Toast.LENGTH_SHORT).show();
                }
            }
        });

        contactMobileNumber.addTextChangedListener(new TextWatcher() {
            String newMobileNumber;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newMobileNumber = contactMobileNumber.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mobileNumber.equals(newMobileNumber)){
                    isNumberChanged = false;
                    Toast.makeText(UpdateContactActivity.this, "No Changes", Toast.LENGTH_SHORT).show();
                } else{
                    isNumberChanged = true;
                    //Toast.makeText(UpdateContactActivity.this, "New Mobile Number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        contactEmail.addTextChangedListener(new TextWatcher() {
            String newEmail;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newEmail = contactEmail.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mobileNumber.equals(newEmail)){
                    isEmailChanged = false;
                    Toast.makeText(UpdateContactActivity.this, "No Changes", Toast.LENGTH_SHORT).show();
                } else{
                    isEmailChanged = true;
                    //Toast.makeText(UpdateContactActivity.this, "New Email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateContact(){
        String changedEmail = contactEmail.getText().toString().trim();
        String changedFirstName = contactFirstName.getText().toString().trim();
        String changedLastName = contactLastName.getText().toString().trim();
        String changedMobileNumber = contactMobileNumber.getText().toString().trim();
        String changedRelation = contactRelation.getText().toString().trim();

        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        String mobileNumberRegex = "^(09|\\+639)\\d{9}$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Pattern mobileNumberPattern = Pattern.compile(mobileNumberRegex);
        Matcher emailMatcher = emailPattern.matcher(changedEmail);
        Matcher mobileNumberMatcher = mobileNumberPattern.matcher(changedMobileNumber);


        if(isLastNameChanged || isFirstNameChanged || isRelationChanged || isNumberChanged || isEmailChanged){
            if(TextUtils.isEmpty(changedLastName)) {
                contactLastName.setError("Please enter last name");
            } else if(TextUtils.isEmpty(changedFirstName)) {
                contactFirstName.setError("Please enter first name");
            } else if(TextUtils.isEmpty(changedRelation)){
                contactRelation.setError("Please enter relation with contact");
            } else if(TextUtils.isEmpty(changedMobileNumber)){
                contactMobileNumber.setError("Please enter mobile number");
            } else if (!mobileNumberMatcher.matches()) {
                contactMobileNumber.setError("Please enter a valid mobile number");
            } else if(TextUtils.isEmpty(changedEmail)){
                contactEmail.setError("Please enter email");
            } else if (!emailMatcher.matches()) {
                contactEmail.setError("Please enter a valid email");
            } else {
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
        } else {
            Toast.makeText(UpdateContactActivity.this, "No changes made", Toast.LENGTH_SHORT).show();
        }


    }

    private void goBack() {
        finish();
    }
}