package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference fStorage = FirebaseStorage.getInstance().getReference();
    FirebaseUser user;
    Intent intent;

    private EditText etLastName;
    private EditText etFirstName;
    private EditText etMiddleName;
    private EditText etBirthdate;
    private Spinner spinnerSex;
    private EditText etMobileNumber;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConPassword;

    //idk how to dis properly
    private Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etLastName = findViewById(R.id.txtLastName);
        etFirstName = findViewById(R.id.txtFirstName);
        etMiddleName = findViewById(R.id.txtMiddleName);
        etBirthdate = findViewById(R.id.txtBirthDate);
        spinnerSex = findViewById(R.id.txtSex);
        etMobileNumber = findViewById(R.id.txtMobileNumber);
        etEmail = findViewById(R.id.txtEmail);
        etPassword = findViewById(R.id.txtPassword);
        etConPassword = findViewById(R.id.txtConfirmPassword);

        String[] sex = new String[]{"Sex *", "M", "F"};
        List<String> sexList = new ArrayList<>(Arrays.asList(sex));

        ArrayAdapter<String> spinnerSexAdapter = new ArrayAdapter<String>(this, R.layout.spinner_sex, sexList){
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

    //TODO: Consider refactoring to onclickListener
    //TODO: Consider refactoring birthdate
    public void userRegister(View view) throws ParseException {
        Map<String, Object> docUsers = new HashMap<>();

        String lastName = etLastName.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String middleName = etMiddleName.getText().toString().trim();
        String sex = spinnerSex.getSelectedItem().toString().trim();
        String birthDate = etBirthdate.getText().toString().trim();
        String mobileNumber = etMobileNumber.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String cPassword = etConPassword.getText().toString().trim();

        //TODO: DatePickerDialog thing for date (Study); FRFabul
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String valid = "01-01-2002"; //age restriction
        Date validDate = dateFormat.parse(valid);

        //put data in hashmap to insert to db
        docUsers.put("LastName", lastName);
        docUsers.put("FirstName", firstName);
        docUsers.put("MiddleName", middleName);
        docUsers.put("Sex", sex);
        docUsers.put("BirthDate", birthDate);
        docUsers.put("MobileNumber", mobileNumber);
        docUsers.put("Email", email);

        //validate then register user
        try{
            Date parsedDate = dateFormat.parse(birthDate);
            assert parsedDate != null;
            if(parsedDate.after(validDate)){
                etBirthdate.setHint("Please enter valid birthdate (mm-dd-yyyy)");
                etBirthdate.setError("Please enter valid birthdate (mm-dd-yyyy)");
            } else {
                if(TextUtils.isEmpty(etLastName.getText())){
                    etLastName.setHint("please enter last name");
                    etLastName.setError("please enter last name");
                } else if(TextUtils.isEmpty(etFirstName.getText())){
                    etFirstName.setHint("please enter first name");
                    etFirstName.setError("please enter first name");
                } else if(TextUtils.isEmpty(etBirthdate.getText())){
                    etBirthdate.setHint("please enter birthdate (mm-dd-yyyy)");
                    etBirthdate.setError("please enter birthdate (mm-dd-yyyy)");
                } else if(spinnerSex.getSelectedItem().equals("Sex")){
                    ((TextView)spinnerSex.getSelectedView()).setError("please select sex");
                } else if(TextUtils.isEmpty(etMobileNumber.getText())){
                    etMobileNumber.setHint("please enter mobile number");
                    etMobileNumber.setError("please enter mobile number");
                } else if(TextUtils.isEmpty(etEmail.getText())){
                    etEmail.setHint("please enter email");
                    etEmail.setError("please enter email");
                } else if(TextUtils.isEmpty(etPassword.getText())){
                    etPassword.setHint("please enter password");
                    etPassword.setError("please enter password");
                } else if(TextUtils.isEmpty(etConPassword.getText())){
                    etConPassword.setHint("please re-enter password");
                    etConPassword.setError("please re-enter password");
                } else {

                    //register
                    if(!password.equals(cPassword)){
                        Toast.makeText(getApplicationContext(), "Passwords must be the same.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "HELO", Toast.LENGTH_LONG).show();
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign up success
                                Log.d(TAG, "signUpWithEmailPassword:success");
                                user = mAuth.getCurrentUser();
                                docUsers.put("ID", user.getUid());
                                docUsers.put("isVerified", false);

                                //TODO: Ung URL ng image i-sasave sa document ng user
                                //uploadImage();

                                //insert to db with success/failure listeners
                                db.collection("users").document(user.getUid()).set(docUsers)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User successfully registered!"))
                                        .addOnFailureListener(e -> Log.w(TAG, "error", e));

                                Toast.makeText(getApplicationContext(), "Passwords must be the same.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MenuActivity.class));
                            } else {
                                // If sign up fails, display a message to the user.
                                Log.w(TAG, "signUpWithEmailPassword:failure", task.getException());
                                Toast.makeText(getApplicationContext(),
                                        "Signup Failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                // else {
//                    //send data over to register2 activity
//                    //not the best practice but fragments are tricky
//                    Intent intent = new Intent (getApplicationContext(), Register2Activity.class);
//                    intent.putExtra("lastName", sLastName);
//                    intent.putExtra("firstName", sFirstName);
//                    intent.putExtra("middleName", sMiddleName);
//                    intent.putExtra("sex", sSex);
//                    intent.putExtra("date", sDate);
//                    intent.putExtra("birthDate", sDate);
//                    startActivity(intent);
//                }
            }
        } catch(ParseException pe){
            etBirthdate.setHint("Please enter valid birthdate");
            etBirthdate.setError("Please enter valid birthdate");
        }
    }

    public void goBack(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(intent);
    }

}