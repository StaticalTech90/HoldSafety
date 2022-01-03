package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterGoogleActivity extends AppCompatActivity {
    EditText etLastName, etFirstName, etMiddleName, etMobileNo;
    Button proceed;
    Spinner spinnerSex;
    public Uri imageURI;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String email;
    Date birthDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_google);

        // check if a user is signed in
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        // get the details from their Google account
        if(signInAccount != null) {
            email = signInAccount.getEmail();
        }

        spinnerSex = findViewById(R.id.txtSex);
        String[] sex = new String[]{"Sex", "M", "F"};
        List<String> sexList = new ArrayList<>(Arrays.asList(sex));

        ArrayAdapter<String> spinnerSexAdapter = new ArrayAdapter<String>(this, R.layout.spinner_sex, sexList) {
            @Override
            public boolean isEnabled(int position){
                if(position == 0){
                    return false;
                } else{
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                if(position==0){
                    tv.setTextColor(getResources().getColor(R.color.hint_color));
                } else{
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        spinnerSexAdapter.setDropDownViewResource(R.layout.spinner_sex);
        spinnerSex.setAdapter(spinnerSexAdapter);

        spinnerSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    //refactored userRegister onClickListener into independent function
    public void userRegister(View view){
        Map<String, Object> docUsers = new HashMap<>();
        etLastName = findViewById(R.id.txtLastName);
        etFirstName = findViewById(R.id.txtFirstName);
        etMiddleName = findViewById(R.id.txtMiddleName);
        etMobileNo = findViewById(R.id.txtMobileNumber);
        proceed = findViewById(R.id.btnProceed);
        spinnerSex = findViewById(R.id.txtSex);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userID");
        String uEmail = intent.getStringExtra("userEmail");
        String lastName = etLastName.getText().toString();
        String firstName = etFirstName.getText().toString();
        String middleName = etMiddleName.getText().toString();
        String mobileNo = etMobileNo.getText().toString();
        String sex = spinnerSex.getSelectedItem().toString();

        if(TextUtils.isEmpty(etLastName.getText())) {
            etLastName.setHint("Enter Last Name");
            etLastName.setError("Enter Last Name");
        } else if(TextUtils.isEmpty(etFirstName.getText())) {
            etFirstName.setHint("Enter First Name");
            etFirstName.setError("Enter First Name");
        } else if(TextUtils.isEmpty(etMobileNo.getText())) {
            etMobileNo.setHint("Enter Mobile number");
            etMobileNo.setError("Enter Mobile number");
        }

        docUsers.put("LastName", lastName);
        docUsers.put("FirstName", firstName);
        docUsers.put("MiddleName", middleName);
        docUsers.put("Sex", sex);
        docUsers.put("BirthDate", birthDate); // currently empty
        docUsers.put("MobileNumber", mobileNo);
        docUsers.put("Email", uEmail);

        db.collection("users").document(userId).set(docUsers)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "DocumentSnapshot successfully written!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error writing document", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }

    public void uploadID(View view){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityIfNeeded(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageURI = data.getData();
            Toast.makeText(getApplicationContext(), "Selected " + imageURI, Toast.LENGTH_SHORT).show();
            //uploadPicture();
        }
    }

    public void goBack(View view){
        Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
        finish();
        startActivity(intent);
    }
}