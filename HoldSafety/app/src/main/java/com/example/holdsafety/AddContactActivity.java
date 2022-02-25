package com.example.holdsafety;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class AddContactActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ImageView btnBack;
    private TextView lblContactCount;
    private EditText etContactLastName, etContactFirstName, etContactMobileNumber, etContactEmail;
    private Spinner etRelation;
    Button btnSaveContact;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Toast.makeText(getApplicationContext(), "Designate Contacts", Toast.LENGTH_SHORT).show();

        btnBack = findViewById(R.id.backArrow);
        etContactLastName = findViewById(R.id.txtContactLastName);
        etContactFirstName = findViewById(R.id.txtContactFirstName);
        etContactMobileNumber = findViewById(R.id.txtContactMobileNumber);
        etContactEmail = findViewById(R.id.txtContactEmail);
        etRelation = findViewById(R.id.txtRelationWithContact);
        lblContactCount = findViewById(R.id.lblNumOfContacts);
        btnSaveContact = findViewById(R.id.btnSaveContact);

        getContactCount();

        btnBack.setOnClickListener(view -> goBack());
        btnSaveContact.setOnClickListener(view -> saveContact());

        String[] relation = new String[]{"Relation With Contact", "Parent", "Sibling", "Relative", "Close Friend", "Acquaintance"};
        List<String> relationList = new ArrayList<>(Arrays.asList(relation));

        ArrayAdapter<String> spinnerRelationAdapter = new ArrayAdapter<String>(this, R.layout.spinner, relationList){
            @Override
            public boolean isEnabled(int position){
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                if(position==0){
                    tv.setTextColor(getResources().getColor(R.color.hint_color));
                }

                else{
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        spinnerRelationAdapter.setDropDownViewResource(R.layout.spinner);
        etRelation.setAdapter(spinnerRelationAdapter);
        etRelation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                    Toast.makeText
                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void saveContact(){
        Map<String, Object> docContacts = new HashMap<>();

        String contactLastName = etContactLastName.getText().toString().trim();
        String contactFirstName = etContactFirstName.getText().toString().trim();
        String contactMobileNumber = etContactMobileNumber.getText().toString().trim();
        String contactEmail = etContactEmail.getText().toString().trim();
        String contactRelation = etRelation.getSelectedItem().toString().trim();

        Toast.makeText
                (getApplicationContext(), "Selected Relation In String: " + contactRelation, Toast.LENGTH_SHORT)
                .show();

        docContacts.put("lastName", contactLastName);
        docContacts.put("firstName", contactFirstName);
        docContacts.put("mobileNumber", contactMobileNumber);
        docContacts.put("email", contactEmail);
        docContacts.put("relation", contactRelation);

        if(TextUtils.isEmpty(etContactLastName.getText())){
            etContactLastName.setHint("please enter contact last name");
            etContactLastName.setError("please enter contact last name");
        } else if(TextUtils.isEmpty(etContactFirstName.getText())){
            etContactFirstName.setHint("please enter contact first name");
            etContactFirstName.setError("please enter contact first name");
        } else if(TextUtils.isEmpty(etContactMobileNumber.getText())){
            etContactMobileNumber.setHint("please enter contact mobile number");
            etContactMobileNumber.setError("please enter contact mobile number");
        } else if(TextUtils.isEmpty(etContactEmail.getText())){
            etContactEmail.setHint("please enter contact email");
            etContactEmail.setError("please enter contact email");
        } else {
            db.collection("emergencyContacts").document(user.getUid()).collection("contacts")
                    .add(docContacts).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),
                            "Successfully Added Contact",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            Objects.requireNonNull(task.getException()).toString(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void getContactCount(){
        FirebaseFirestore.getInstance()
                .collection("emergencyContacts")
                .document(user.getUid()).collection("contacts").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot contactSnap : task.getResult()) {
                            count++;
                        }
                        lblContactCount.setText(count + " out of 5");
                    }

                    else{
                        Toast.makeText(this, "No Contacts Available", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goBack() {
        finish();
    }
}