package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user;

    final Calendar calendar = Calendar.getInstance();

    TextView lblLink;

    private EditText etLastName;
    private EditText etFirstName;
    private EditText etMiddleName;
    private EditText etBirthdate;
    private Spinner spinnerSex;
    private EditText etMobileNumber;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConPassword;

    Button btnRegister;
    Button btnUpload;

    private static final int EXTERNAL_STORAGE_REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        lblLink = findViewById(R.id.txtImageLink);

        etLastName = findViewById(R.id.txtLastName);
        etFirstName = findViewById(R.id.txtFirstName);
        etMiddleName = findViewById(R.id.txtMiddleName);
        etBirthdate = findViewById(R.id.txtBirthDate);
        spinnerSex = findViewById(R.id.txtSex);
        etMobileNumber = findViewById(R.id.txtMobileNumber);
        etEmail = findViewById(R.id.txtEmail);
        etPassword = findViewById(R.id.txtPassword);
        etConPassword = findViewById(R.id.txtConfirmPassword);
        btnRegister = findViewById(R.id.registerButton);

        btnUpload = findViewById(R.id.btnUploadID);

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
        etBirthdate.setOnClickListener(view -> new DatePickerDialog(
                RegisterActivity.this,
                date,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        );

        //Upload Image
        btnUpload.setOnClickListener(v -> { pickImage(); });

        btnRegister.setOnClickListener(v -> {
            try {
                userRegister(v);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

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

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String valid = "01-01-2004"; //age restriction 18
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
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign up success
                                Log.d(TAG, "signUpWithEmailPassword:success");
                                user = mAuth.getCurrentUser();
                                docUsers.put("ID", user.getUid());
                                docUsers.put("isVerified", false);

                                //TODO: Ung URL ng image i-sasave sa document ng user
                                if(!lblLink.getText().equals("")){
                                    uploadPhotoToStorage();
                                }
                                //uploadImage();

                                //insert to db with success/failure listeners
                                db.collection("users").document(user.getUid()).set(docUsers)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User successfully registered!"))
                                        .addOnFailureListener(e -> Log.w(TAG, "error", e));

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

    //update edittext value
    private void updateDate(){
        //matched with line 174
        String myFormat="MM-dd-yyyy";
        SimpleDateFormat dateFormat=new SimpleDateFormat(myFormat, Locale.US);
        etBirthdate.setText(dateFormat.format(calendar.getTime()));
    }

    //PUT IMAGE UPLOAD HERE
    private void uploadPhotoToStorage() {
        if (!lblLink.getText().equals("")) {
            //UPLOAD TO FIREBASE STORAGE
            FirebaseStorage.getInstance()
                    .getReference("id")
                    .child(user.getUid())
                    .putFile(Uri.parse(lblLink.getText().toString()))
                    .addOnSuccessListener(taskSnapshot -> Toast.makeText(RegisterActivity.this,
                            "Upload successful",
                            Toast.LENGTH_SHORT).show()).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(RegisterActivity.this, "Upload failed.", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(snapshot -> {

                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());

            });

        }

    }

    private void pickImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            //DENIED PERMISSION
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_REQ_CODE);
            return;
        }

        //PICK IMAGE
        //PERMISSION GRANTED
        //OPEN IMAGE PICKER
        Intent imagePickIntent = new Intent();
        imagePickIntent.setType("image/*");
        imagePickIntent.setAction(Intent.ACTION_GET_CONTENT);

        activityResultLauncher.launch(imagePickIntent);
    }

    //ACTIVITY RESULT
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == RESULT_OK) {

                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getData() != null) {

                                Uri imageUri = data.getData();
                                lblLink.setText(imageUri.toString());

                            }
                        }

                    }
                }
            });


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == EXTERNAL_STORAGE_REQ_CODE) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //DENIED ONCE
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_REQ_CODE);
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //PERMISSION GRANTED
                pickImage();
            }

        }

    }
}