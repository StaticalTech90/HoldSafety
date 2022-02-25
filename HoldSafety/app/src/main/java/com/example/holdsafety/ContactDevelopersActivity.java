package com.example.holdsafety;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ContactDevelopersActivity extends AppCompatActivity {
    private String selectedConcern;
    ImageView btnBack;
    EditText etMessage, etEmail;
    Button btnSend;

    FirebaseUser user;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_developers);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        dropdownContact();
        btnBack = findViewById(R.id.backArrow);
        etEmail = findViewById(R.id.txtEmail);
        etMessage = findViewById(R.id.txtMessage);
        btnSend = findViewById(R.id.btnSend);

        //Get data from db and auto-input in the form
        checkUserEmail();

        btnBack.setOnClickListener(view -> goBack());
        btnSend.setOnClickListener(view -> sendMessage());
    }

    public void checkUserEmail(){
        if(mAuth.getCurrentUser() == null){
            Toast.makeText(getApplicationContext(), "No User ", Toast.LENGTH_SHORT).show();
        }

        else{
            Toast.makeText(getApplicationContext(), "User: " + user.getEmail(), Toast.LENGTH_SHORT).show();
            etEmail.setText(user.getEmail());
            etEmail.setTextColor(getResources().getColor(R.color.dark_blue));
            etEmail.setEnabled(false);
        }
    }

    public void sendMessage(){
        //Get user's emergency contacts
        FirebaseFirestore.getInstance()
                .collection("admin")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //Get each contact details
                        for(QueryDocumentSnapshot snapshot : task.getResult()){
                            String contactID = snapshot.getId();

                            //String mobileNumber = snapshot.getString("mobileNumber");
                            //String firstName = snapshot.getString("firstName");
                            //String lastName = snapshot.getString("lastName");
                            String email = snapshot.getString("Email");


                            //Toast.makeText(getApplicationContext(), "Developer: " + email, Toast.LENGTH_LONG).show();
                            if(email!=null){
                                //Send Email
                                String username = "holdsafety.ph@gmail.com";
                                String password = "HoldSafety@4qmag";
                                String subject = "HOLDSAFETY CONTACT DEVELOPERS: " + selectedConcern ;
                                String message = "Sender: " + etEmail.getText().toString().trim()
                                + " Message: " + etMessage.getText().toString().trim();

                                List<String> recipients = Collections.singletonList(email);
                                //email of sender, password of sender, list of recipients, email subject, email body
                                new MailTask(ContactDevelopersActivity.this).execute(username, password, recipients, subject, message);

                                Toast.makeText(ContactDevelopersActivity.this, "Email Sent", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
        Toast.makeText(getApplicationContext(), "Successfully sent a message to developers", Toast.LENGTH_SHORT).show();
    }

    public String dropdownContact(){
        Spinner spinnerAction = findViewById(R.id.txtAction);
        String[] action = new String[]{"Report a bug", "Feedback", "Concern"};
        List<String> actionList = new ArrayList<>(Arrays.asList(action));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner, actionList);
        //set the spinners adapter to the previously created one.
        spinnerAction.setAdapter(adapter);

        spinnerAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                // Notify the selected item text
                Toast.makeText
                        (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                        .show();
                selectedConcern  = (String) spinnerAction.getSelectedItem().toString().trim();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return selectedConcern;
    }

    private void goBack(){
        finish();
    }
}