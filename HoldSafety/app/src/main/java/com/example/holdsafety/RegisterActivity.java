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
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference imageRef = FirebaseStorage.getInstance().getReference("id");
    FirebaseUser user;

    HashMap<String, Object> docUsers = new HashMap<>();
    Intent otp;

    final Calendar calendar = Calendar.getInstance();
    String idUri;

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

    TextView txtTogglePass, txtToggleConfirmPass;

    Button btnRegister;
    Button btnUpload;

    private static final int EXTERNAL_STORAGE_REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();



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

        txtTogglePass = findViewById(R.id.txtTogglePass);
        txtToggleConfirmPass = findViewById(R.id.txtToggleConfirmPass);



        txtTogglePass.setVisibility(View.GONE);
        txtToggleConfirmPass.setVisibility(View.GONE);
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        otp = new Intent(RegisterActivity.this, RegisterOTPActivity.class);

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

        DatePickerDialog.OnDateSetListener date = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,day);
            updateDate();
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
        btnUpload.setOnClickListener(v -> pickImage());

        btnRegister.setOnClickListener(v -> {
            try {
                userRegister();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        //masking pass
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(etPassword.getText().length() > 0){
                    txtTogglePass.setVisibility(View.VISIBLE);
                }
                else{
                    txtTogglePass.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        txtTogglePass.setOnClickListener(view -> {
            if(txtTogglePass.getText() == "SHOW"){
                txtTogglePass.setText("HIDE");
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else{
                txtTogglePass.setText("SHOW");
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.length());
        });


        //masking confirm pass
        etConPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(etConPassword.getText().length() > 0){
                    txtToggleConfirmPass.setVisibility(View.VISIBLE);
                }
                else{
                    txtToggleConfirmPass.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        txtToggleConfirmPass.setOnClickListener(view -> {
            if(txtToggleConfirmPass.getText() == "SHOW"){
                txtToggleConfirmPass.setText("HIDE");
                etConPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else{
                txtToggleConfirmPass.setText("SHOW");
                etConPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etConPassword.setSelection(etConPassword.length());
        });

    }

    public void userRegister() throws ParseException {
        String emailRegex = "^(.+)@(.+)$";
        String passRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        String mobileNumberRegex = "^(09|\\+639)\\d{9}$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Pattern passPattern = Pattern.compile(passRegex);
        Pattern mobileNumberPattern = Pattern.compile(mobileNumberRegex);

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

        //CHECK IF EMAIL IS ALREADY IN DB
        CollectionReference colRef = db.collection("users");

        colRef.get().addOnCompleteListener(taskCheck -> {
           if(taskCheck.isSuccessful()) {
               boolean isExisting = false;

               for(QueryDocumentSnapshot userSnap : taskCheck.getResult()) {
                   String dbEmail = userSnap.getString("Email");

                   assert dbEmail != null;
                   if(dbEmail.equals(email)) { //CHECK IF EMAIL EXISTS IN ANY USER DETAILS
                       isExisting = true;
                   }
               }

               if(isExisting) { //EMAIL ALREADY IN USE, DISPLAY ERROR
                   Toast.makeText(this, "Email already in use! Sign in instead", Toast.LENGTH_LONG).show();
                   etEmail.setError("Email already in use");
               } else { //EMAIL IS NEW, PROCEED WITH REGISTRATION
                   String userUID = colRef.document().getId(); //GET THE DOCUMENT ID OF THE ACC TO BE CREATED
                   colRef.document(userUID).get().addOnSuccessListener(documentSnapshot -> {
                       if(!documentSnapshot.exists()) { //EMAIL IS NEW, CREATE ACCOUNT
                           //put data in hashmap to insert to db
                           docUsers.put("LastName", lastName);
                           docUsers.put("FirstName", firstName);
                           docUsers.put("MiddleName", middleName);
                           docUsers.put("Sex", sex);
                           docUsers.put("BirthDate", birthDate);
                           docUsers.put("MobileNumber", mobileNumber);
                           docUsers.put("Email", email);
                           docUsers.put("profileComplete", false);

                           //Data validation and register
                           try{
                               Date parsedDate = dateFormat.parse(birthDate);
                               assert parsedDate != null;
                               Matcher emailMatcher = emailPattern.matcher(etEmail.getText());
                               Matcher passMatcher = passPattern.matcher(etPassword.getText());
                               Matcher mobileNumberMatcher = mobileNumberPattern.matcher(etMobileNumber.getText());

                               if(parsedDate.after(validDate)){
                                   etBirthdate.setError("Please enter valid birthdate (mm-dd-yyyy)");
                               } else {
                                   if(TextUtils.isEmpty(etLastName.getText())) {
                                       etLastName.setError("Please enter last name");
                                   } else if(TextUtils.isEmpty(etFirstName.getText())) {
                                       etFirstName.setError("Please enter first name");
                                   } else if(TextUtils.isEmpty(etBirthdate.getText())) {
                                       etBirthdate.setError("Please enter birthdate (mm-dd-yyyy)");
                                   } else if(spinnerSex.getSelectedItem().equals("Sex")) {
                                       ((TextView)spinnerSex.getSelectedView()).setError("please select sex");
                                   } else if(TextUtils.isEmpty(etMobileNumber.getText())) {
                                       etMobileNumber.setError("Please enter mobile number");
                                   } else if (!mobileNumberMatcher.matches()) {
                                       etMobileNumber.setError("Please enter a valid mobile number");
                                   } else if(etMobileNumber.getText().length() != 11) {
                                       etMobileNumber.setError("Please enter a valid mobile number");
                                   } else if(TextUtils.isEmpty(etEmail.getText())) {
                                       etEmail.setError("Please enter email");
                                   } else if (!emailMatcher.matches()) {
                                       etEmail.setError("Please enter valid email");
                                   } else if(TextUtils.isEmpty(etPassword.getText())) {
                                       etPassword.setError("Please enter password");
                                   } else if(!passMatcher.matches()) {
                                       etPassword.setError("Password must contain the following: " +
                                               "\n - At least 8 characters" +
                                               "\n - At least 1 digit" +
                                               "\n - At least one upper and lower case letter" +
                                               "\n - A special character (such as @, #, etc.)" +
                                               "\n - No spaces or tabs");
                                   } else if(TextUtils.isEmpty(etConPassword.getText())) {
                                       etConPassword.setError("Please re-enter password");
                                   } else if(!password.equals(cPassword)) {
                                       etConPassword.setError("Passwords don't match");
                                   } else {
                                       Toast.makeText(RegisterActivity.this, "HELO", Toast.LENGTH_LONG).show();
                                       mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, task -> {
                                           if (task.isSuccessful()) {
                                               // Sign up success
                                               Log.d(TAG, "signUpWithEmailPassword:success");
                                               user = mAuth.getCurrentUser();
                                               assert user != null;
                                               docUsers.put("ID", user.getUid());
                                               docUsers.put("isVerified", false);

                                               if (!lblLink.getText().equals("")) {
                                                   uploadPhotoToStorage();
                                                   docUsers.put("profileComplete", true);
                                               } else {
                                                   docUsers.put("profileComplete", false);
                                               }

                                               //Verify user's email
                                               otp.putExtra("Email", etEmail.getText().toString());
                                               otp.putExtra("UserDetails", docUsers);
                                               startActivity(otp);
                                               finish();
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
                           } catch(ParseException pe){
                               etBirthdate.setHint("Please enter valid birthdate");
                               etBirthdate.setError("Please enter valid birthdate");
                           }
                       } else { //ACCOUNT EXISTS, DISPLAY ERROR
                           Toast.makeText(RegisterActivity.this, "Email already in use", Toast.LENGTH_LONG).show();
                           etEmail.setError("This email is already in use");
                       }
                   });
               }
           }
        });


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
        imageRef.child(user.getUid())
                .putFile(Uri.parse(lblLink.getText().toString()))
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(uri -> {
                        idUri = String.valueOf(uri);
                        docUsers.put("imgUri", idUri);
                        Log.i("URI gDUrl()", idUri);
                        //otp.putExtra("imgUri", idUri);
                        //otp.putExtra("UserDetails", docUsers);

                        db.collection("users").document(user.getUid()).update(docUsers)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getApplicationContext(),
                                            "pushed image to document",
                                            Toast.LENGTH_SHORT).show();
                                    Log.i(TAG, "Image pushed");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(),
                                            "Error writing document",
                                            Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "Error writing document", e);
                                });
                    });
                }).addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Upload failed.",
                        Toast.LENGTH_SHORT).show());
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

    public void goBack(View view){
        finish();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }
}