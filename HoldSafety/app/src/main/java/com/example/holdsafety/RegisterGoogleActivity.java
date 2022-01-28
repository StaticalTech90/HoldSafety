package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterGoogleActivity extends AppCompatActivity {
    EditText etMiddleName, etMobileNo, etBirthDate;
    TextView lblName;
    Button btnProceed;
    Spinner spinnerSex;
    public Uri imageURI;

    FirebaseUser user;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef;

    final Calendar calendar = Calendar.getInstance();

    String firstName, lastName, email, selectedSex;
    Date birthDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_google);

        //Get data from db and auto-input in the form
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        etMiddleName = findViewById(R.id.txtMiddleName);
        etMobileNo = findViewById(R.id.txtMobileNumber);
        etBirthDate = findViewById(R.id.txtBirthDate);
        btnProceed = findViewById(R.id.btnProceed);
        spinnerSex = findViewById(R.id.txtSex);
        lblName = findViewById(R.id.lblGoogleName);

        // check if a user is signed in
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        // get the details from their Google account
        if(signInAccount != null) {
            email = signInAccount.getEmail();
        }

        dropdownSex();
        selectBirthdate();

        if(user != null) {
            docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()) {

                        //REFACTORED FUNCTIONALITY TO SHOW ACCOUNT NAME AS LABEL
                        firstName = (documentSnapshot.getString("FirstName"));
                        lastName = documentSnapshot.getString("LastName");
                        String username =  lastName + ", " + firstName;
                        //etLastName.setText(documentSnapshot.getString("LastName"));
                        //etFirstName.setText(documentSnapshot.getString("FirstName"));
                        //etLastName.setFocusable(false);
                        //etFirstName.setFocusable(false);
                        lblName.setText(username);
                    }
                }
            });
        }
    }

    //refactored userRegister onClickListener into independent function
    //TODO: turn this back into an onClickListener
    public void userRegister(View view){
        user = mAuth.getCurrentUser();
        Map<String, Object> docUsers = new HashMap<>();
        String mobileNumberRegex = "^(09|\\+639)\\d{9}$";
        Pattern mobileNumberPattern = Pattern.compile(mobileNumberRegex);

        String userId = user.getUid();
        String uEmail = user.getEmail();
        //String firstName = etFirstName.getText().toString();
        String middleName = etMiddleName.getText().toString();
        String birthDate = etBirthDate.getText().toString().trim();
        String mobileNo = etMobileNo.getText().toString();
        String sex = spinnerSex.getSelectedItem().toString();

        Matcher mobileNumberMatcher = mobileNumberPattern.matcher(etMobileNo.getText());

        if(spinnerSex.getSelectedItem().equals("Sex")) {
            ((TextView)spinnerSex.getSelectedView()).setError("please select sex");
        } else if(TextUtils.isEmpty(etMobileNo.getText())) {
            etMobileNo.setError("Enter Mobile number");
        } else if (!mobileNumberMatcher.matches()) {
            etMobileNo.setError("Please enter a valid mobile number");
        } else if(etMobileNo.getText().length() != 11) {
            etMobileNo.setError("Please enter a valid mobile number");
        } else {
            docUsers.put("ID", user.getUid());
            docUsers.put("LastName", lastName);
            docUsers.put("FirstName", firstName);
            docUsers.put("MiddleName", middleName);
            docUsers.put("Sex", sex);
            //TODO: PLS NOT EMPTY BIRTHDATE
            docUsers.put("BirthDate", birthDate); // currently empty
            docUsers.put("MobileNumber", mobileNo);
            docUsers.put("Email", uEmail);
            docUsers.put("profileComplete", true);
            docUsers.put("isVerified", false);

            db.collection("users").document(userId).set(docUsers)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "DocumentSnapshot successfully written!", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "DocumentSnapshot successfully written!");

                            Intent landing = new Intent(RegisterGoogleActivity.this, LandingActivity.class);
                            startActivity(landing);
                            finish();
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

    public String dropdownSex(){
        //Sex selector spinner
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
                    selectedSex = (String) spinnerSex.getSelectedItem().toString().trim();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return selectedSex;
    }

    //update edittext value
    private void updateDate(){
        //matched with line 174
        String myFormat="MM-dd-yyyy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        etBirthDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void selectBirthdate(){
        DatePickerDialog.OnDateSetListener date =new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,day);
                updateDate();
            }
        };

        //show DatePickerDialog using this listener
        etBirthDate.setOnClickListener(view -> new DatePickerDialog(
                RegisterGoogleActivity.this,
                date,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        );
    }

    public void goBack(View view){
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}