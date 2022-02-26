package com.example.holdsafety;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String email = etEmail.getText().toString().trim();
        String message = etMessage.getText().toString().trim();

        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(etEmail.getText());

        if(TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter email");
        } else if (!emailMatcher.matches()) {
            etEmail.setError("Please enter valid email");
        } else if (TextUtils.isEmpty(message)) {
            etMessage.setError("Please input a message");
        } else {
            //Get user's emergency contacts
            FirebaseFirestore.getInstance()
                    .collection("admin")
                    .get()
                    .addOnCompleteListener(task -> {
                        //Get each contact details
                        for(QueryDocumentSnapshot snapshot : task.getResult()){
                            String email1 = snapshot.getString("Email");

                            if(email1 !=null){
                                //Send Email
                                String username = "holdsafety.ph@gmail.com";
                                String password = "HoldSafety@4qmag";
                                String subject = "HOLDSAFETY CONTACT DEVELOPERS: " + selectedConcern ;
                                String message1 = "Sender: " + etEmail.getText().toString().trim()
                                        + " Message: " + etMessage.getText().toString().trim();

                                List<String> recipients = Collections.singletonList(email1);
                                //email of sender, password of sender, list of recipients, email subject, email body
                                new MailTask(ContactDevelopersActivity.this).execute(username, password, recipients, subject, message1);

                                Toast.makeText(ContactDevelopersActivity.this, "Email Sent", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            Toast.makeText(getApplicationContext(), "Successfully sent a message to developers", Toast.LENGTH_SHORT).show();
            goBack();
        }
    }

    public void dropdownContact(){
        Spinner spinnerAction = findViewById(R.id.txtAction);
        String[] action = new String[]{"Concern", "Report a bug", "Feedback"};
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
    }

    private void goBack(){
        finish();
    }
}