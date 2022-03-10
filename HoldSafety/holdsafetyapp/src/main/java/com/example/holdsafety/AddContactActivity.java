package com.example.holdsafety;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddContactActivity extends AppCompatActivity {
    LogHelper logHelper;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    ImageView btnBack;
    private TextView lblContactCount;
    private EditText etContactLastName, etContactFirstName, etContactMobileNumber, etContactEmail;
    private Spinner etRelation;
    Button btnSaveContact;
    int emergencyContactCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        logHelper = new LogHelper(this, mAuth, user, this);

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

        String[] relation = new String[]{"Relation With Contact *", "Parent", "Sibling", "Relative", "Close Friend", "Acquaintance"};
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

                if(position == 0) {
                    tv.setTextColor(getResources().getColor(R.color.hint_color));
                } else {
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
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    public void saveContact(){
        if(emergencyContactCount < 5){
            Map<String, Object> docContacts = new HashMap<>();

            String contactLastName = etContactLastName.getText().toString().trim();
            String contactFirstName = etContactFirstName.getText().toString().trim();
            String contactMobileNumber = etContactMobileNumber.getText().toString().trim();
            String contactEmail = etContactEmail.getText().toString().trim();
            String contactRelation = etRelation.getSelectedItem().toString().trim();

            String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
            String mobileNumberRegex = "^(09|\\+639)\\d{9}$";
            Pattern emailPattern = Pattern.compile(emailRegex);
            Pattern mobileNumberPattern = Pattern.compile(mobileNumberRegex);
            Matcher emailMatcher = emailPattern.matcher(contactEmail);
            Matcher mobileNumberMatcher = mobileNumberPattern.matcher(contactMobileNumber);

            docContacts.put("lastName", contactLastName);
            docContacts.put("firstName", contactFirstName);
            docContacts.put("mobileNumber", contactMobileNumber);
            docContacts.put("email", contactEmail);
            docContacts.put("relation", contactRelation);

            if(TextUtils.isEmpty(contactLastName)){
                etContactLastName.setError("Please enter contact last name");
            } else if(TextUtils.isEmpty(contactFirstName)) {
                etContactFirstName.setError("Please enter contact first name");
            } else if (contactRelation.equals("Relation With Contact *")) {
                ((TextView)etRelation.getSelectedView()).setError("Please select relation with contact");
            } else if(TextUtils.isEmpty(contactMobileNumber)){
                etContactMobileNumber.setError("Please enter contact mobile number");
            }  else if (!mobileNumberMatcher.matches()) {
                etContactMobileNumber.setError("Please enter a valid mobile number");
            } else if(TextUtils.isEmpty(contactEmail)){
                etContactEmail.setError("Please enter contact email");
            } else if (!emailMatcher.matches()) {
                etContactEmail.setError("Please enter valid email");
            } else {
                //check if contact exists in the user's profile
                db.collection("emergencyContacts").document(user.getUid()).collection("contacts").get().addOnCompleteListener(task -> {
                   if(task.isSuccessful()) {
                       Log.d("CONTACT_CHECK", "Contacts fetched.");
                       boolean existing = false;
                       int contactAmount = task.getResult().size();
                       Log.d("CONTACT_CHECK", "Contact list size: " + contactAmount);
                       int counter = 0;

                       if(contactAmount == 0) { // NO CONTACTS YET
                           db.collection("emergencyContacts").document(user.getUid()).collection("contacts")
                                   .add(docContacts).addOnCompleteListener(this, task1 -> {
                               if (task1.isSuccessful()) {
                                   logHelper.saveToFirebase("saveContact", "Added Contact", "NONE");

                                   Toast.makeText(getApplicationContext(), "Successfully Added Contact", Toast.LENGTH_SHORT).show();
                                   goBack();
                               } else {
                                   logHelper.saveToFirebase("saveContact", "ERROR", task1.getException().getLocalizedMessage());

                                   Toast.makeText(getApplicationContext(),
                                           Objects.requireNonNull(task1.getException()).toString(),
                                           Toast.LENGTH_SHORT).show();
                               }
                           });
                       } else {
                           for(QueryDocumentSnapshot contactSnap : task.getResult()) {
                               String email = contactSnap.getString("email");
                               String mobileNumber = contactSnap.getString("mobileNumber");
                               Log.d("CONTACT_CHECK", "Contact email: " + email + " mobileNumber: " + mobileNumber);

                               if(contactMobileNumber.equals(mobileNumber)) {
                                   existing = true;
                                   etContactMobileNumber.setError("This number is already an emergency contact");
                               } else if(contactEmail.equals(email)) {
                                   existing = true;
                                   etContactEmail.setError("This email is already an emergency contact");
                               }

                               //now at the last contact, final check if it exists
                               if(counter == contactAmount) {
                                   if(!existing) {
                                       db.collection("emergencyContacts").document(user.getUid()).collection("contacts")
                                               .add(docContacts).addOnCompleteListener(this, task1 -> {
                                           if (task1.isSuccessful()) {
                                               logHelper.saveToFirebase("saveContact", "Added Contact", "NONE");

                                               Toast.makeText(getApplicationContext(), "Successfully Added Contact", Toast.LENGTH_SHORT).show();
                                               goBack();
                                           } else {
                                               logHelper.saveToFirebase("saveContact", "ERROR", task1.getException().getLocalizedMessage());

                                               Toast.makeText(getApplicationContext(),
                                                       Objects.requireNonNull(task1.getException()).toString(),
                                                       Toast.LENGTH_SHORT).show();
                                           }
                                       });
                                   }
                               }
                               counter++;
                           }
                       }
                   } else {
                       logHelper.saveToFirebase("saveContact", "ERROR", "Failed to fetch contacts");

                       Log.d("CONTACT_CHECK", "Failed to fetch contacts");
                   }
                });
            }
        } else {
            Toast.makeText(getApplicationContext(), "Number of Emergency Contact exceeds to limit", Toast.LENGTH_SHORT).show();
        }
    }

    public void getContactCount() {
        FirebaseFirestore.getInstance()
                .collection("emergencyContacts")
                .document(user.getUid()).collection("contacts").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot ignored : task.getResult()) {
                            emergencyContactCount++;
                        }
                        lblContactCount.setText(emergencyContactCount + " out of 5");
                    } else {
                        logHelper.saveToFirebase("getContactCount", "ERROR", "No Contacts Available");

                        Toast.makeText(this, "No Contacts Available", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goBack() {
        finish();
    }
}