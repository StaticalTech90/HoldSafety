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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    StorageReference imageRef;
    FirebaseUser user;
    LogHelper logHelper;

    final Calendar calendar = Calendar.getInstance();
    String email, password, idPicUri;
    TextView lblLink;

    HashMap<String, Object> docUsers = new HashMap<>();
    private EditText etLastName, etFirstName, etMiddleName, etBirthdate, etMobileNumber, etEmail, etPassword, etConPassword;
    private Spinner spinnerSex;
    TextView txtTogglePass, txtToggleConfirmPass;
    ImageView btnBack;
    Button btnUpload, btnRegister;

    private static final int EXTERNAL_STORAGE_REQ_CODE = 1000;
    private static final int OTP_REQUEST_CODE_REGISTER = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        imageRef = FirebaseStorage.getInstance().getReference("id");
        user = mAuth.getCurrentUser();

        logHelper = new LogHelper(this, mAuth, user,this);

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
        btnBack = findViewById(R.id.backArrow);
        btnUpload = findViewById(R.id.btnUploadID);
        btnRegister = findViewById(R.id.registerButton);

        txtTogglePass = findViewById(R.id.txtTogglePass);
        txtToggleConfirmPass = findViewById(R.id.txtToggleConfirmPass);

        txtTogglePass.setVisibility(View.GONE);
        txtToggleConfirmPass.setVisibility(View.GONE);
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etConPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

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

                if(position==0) {
                    tv.setTextColor(getResources().getColor(R.color.hint_color));
                } else {
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
                if(position > 0) {
                    // Notify the selected item text
//                    Toast.makeText
//                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
//                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        selectBirthDate();

        btnBack.setOnClickListener(view -> goBack());
        btnUpload.setOnClickListener(view -> pickImage());
        btnRegister.setOnClickListener(view -> {
            try { userRegister(); }
            catch (ParseException e) { e.printStackTrace(); }
        });

        //masking pass
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(etPassword.getText().length() > 0){
                    txtTogglePass.setVisibility(View.VISIBLE);
                } else {
                    txtTogglePass.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
        txtTogglePass.setOnClickListener(view -> {
            if(txtTogglePass.getText() == "SHOW"){
                txtTogglePass.setText("HIDE");
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                txtTogglePass.setText("SHOW");
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.length());
        });

        //masking confirm pass
        etConPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(etConPassword.getText().length() > 0){
                    txtToggleConfirmPass.setVisibility(View.VISIBLE);
                } else {
                    txtToggleConfirmPass.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });
        txtToggleConfirmPass.setOnClickListener(view -> {
            if(txtToggleConfirmPass.getText() == "SHOW"){
                txtToggleConfirmPass.setText("HIDE");
                etConPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                txtToggleConfirmPass.setText("SHOW");
                etConPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etConPassword.setSelection(etConPassword.length());
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("USER", "Current User: " + user);
    }

    public void userRegister() throws ParseException {
        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        String passRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[-@%}+'!/#$^?:;,(\")~`.=&{><_])(?=\\S+$).{8,}$";
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
        email = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        String cPassword = etConPassword.getText().toString().trim();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

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
                           docUsers.put("isVerified", false);

                           //Data validation and register
                           //Date parsedDate = dateFormat.parse(birthDate);
                           Boolean validInput = true;

                           //assert parsedDate != null;
                           Matcher emailMatcher = emailPattern.matcher(etEmail.getText());
                           Matcher passMatcher = passPattern.matcher(etPassword.getText());
                           Matcher mobileNumberMatcher = mobileNumberPattern.matcher(etMobileNumber.getText());

                           if(TextUtils.isEmpty(lastName)) {
                               etLastName.setError("Please enter last name");
                               validInput = false;
                           }

                           if(TextUtils.isEmpty(firstName)) {
                               etFirstName.setError("Please enter first name");
                               validInput = false;
                           }

                           if(TextUtils.isEmpty(birthDate)) {
                               //assert parsedDate != null;
                               etBirthdate.getText().clear();
                               etBirthdate.setHint("Please enter valid birthdate");
                               etBirthdate.setError("Please enter valid birthdate");
                               validInput = false;
                           }

                           if(spinnerSex.getSelectedItem().equals("Sex *")) {
                               ((TextView) spinnerSex.getSelectedView()).setError("Please select sex");
                               validInput = false;
                           }

                           if(TextUtils.isEmpty(mobileNumber)) {
                               etMobileNumber.setError("Please enter mobile number");
                               validInput = false;
                           } else if (!mobileNumberMatcher.matches()) {
                               etMobileNumber.setError("Please enter a valid mobile number");
                               validInput = false;
                           } else if(etMobileNumber.getText().length() != 11) {
                               etMobileNumber.setError("Please enter a valid mobile number");
                               validInput = false;
                           }

                           if(TextUtils.isEmpty(email)) {
                               etEmail.setError("Please enter email");
                               validInput = false;
                           } else if (!emailMatcher.matches()) {
                               etEmail.setError("Please enter valid email");
                               validInput = false;
                           }

                           if(TextUtils.isEmpty(etPassword.getText())) {
                               etPassword.setError("Please enter password");
                               validInput = false;
                           } else if(!passMatcher.matches()) {
                               txtTogglePass.setVisibility(View.GONE);
                               etPassword.setError("Password must contain the following: " +
                                       "\n - At least 8 characters" +
                                       "\n - At least 1 digit" +
                                       "\n - At least one upper and lower case letter" +
                                       "\n - A special character (such as @, #, etc.)" +
                                       "\n - No spaces or tabs");
                               validInput = false;
                           }

                           if(TextUtils.isEmpty(cPassword)) {
                               etConPassword.setError("Please re-enter password");
                               validInput = false;
                           } else if(!password.equals(cPassword)) {
                               txtToggleConfirmPass.setVisibility(View.GONE);
                               etConPassword.setError("Passwords don't match");
                               validInput = false;
                           }

                           if(validInput) {
                               //Verify user's email
                               Intent otp = new Intent(RegisterActivity.this, OTPActivity.class);
                               otp.putExtra("RequestCode", OTP_REQUEST_CODE_REGISTER);
                               otp.putExtra("Email", email);
                               otp.putExtra("Password", password);

                               Log.d("REQUEST", "STARTING OTPACTIVITY...");
                               Log.d("REQUEST", "mAuth = " + mAuth);
                               startActivityForResult(otp, OTP_REQUEST_CODE_REGISTER);
                           }

                       } else { //ACCOUNT EXISTS, DISPLAY ERROR
                           logHelper.saveToFirebase("userRegister", "ERROR", "Duplicate email");
                           Toast.makeText(RegisterActivity.this, "Email already in use", Toast.LENGTH_LONG).show();
                           etEmail.setError("This email is already in use");
                       }
                   });
               }
           }
        });
    }

    //PUT IMAGE UPLOAD HERE
    private void uploadPhotoToStorage() {
        imageRef.child(user.getUid())
                .putFile(Uri.parse(lblLink.getText().toString()))
                .addOnSuccessListener(taskSnapshot -> imageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(uri -> {
                    idPicUri = String.valueOf(uri);
                    docUsers.put("imgUri", idPicUri);
                    Log.i("URI gDUrl()", idPicUri);

                    db.collection("users").document(user.getUid()).update(docUsers)
                            .addOnSuccessListener(aVoid -> {
                                logHelper.saveToFirebase("uploadPhotoToStorage",
                                        "SUCCESS",
                                        "Image inserted to db");

                                Log.i(TAG, "Image pushed");
                            })
                            .addOnFailureListener(e -> {
                                logHelper.saveToFirebase("uploadPhotoToStorage",
                                        "ERROR",
                                        e.getLocalizedMessage());
                                Log.e(TAG, "Error writing document " + e.getLocalizedMessage());
                            });
                })).addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Upload failed.",
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

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == OTP_REQUEST_CODE_REGISTER && resultCode == RESULT_OK) {
            Log.i("REGISTRATION", "CREATING ACCOUNT...");
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, task -> {
                if(task.isSuccessful()) {
                    Log.i("REGISTRATION", "ACCOUNT CREATED!");
                    // Sign up success
                    user = mAuth.getCurrentUser();
                    assert user != null;
                    docUsers.put("ID", user.getUid());

                    if(!lblLink.getText().equals("")) {
                        uploadPhotoToStorage();
                        docUsers.put("profileComplete", true);
                    }

                    db.collection("users").document(user.getUid()).set(docUsers)
                            .addOnSuccessListener(unused -> imageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(uri -> {
                                idPicUri = String.valueOf(uri);
                                docUsers.put("imgUri", idPicUri);

                                logHelper.saveToFirebase("onActivityResult",
                                        "SUCCESS", "ACCOUNT DETAILS SAVED TO DB");

                                Log.i("REGISTRATION", "Account details saved to db");
                                Log.i("URI gDUrl()", idPicUri);

                                db.collection("users").document(user.getUid()).update(docUsers)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.i(TAG, "Image pushed");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w(TAG, "Error writing document", e);
                                        });
                            }))
                            .addOnFailureListener(e -> Log.w(TAG, "error", e));
                } else {
                    // If sign up fails, display a message to the user.
                    logHelper.saveToFirebase("onActivityResult",
                            "ERROR", task.getException().getLocalizedMessage());
                    Log.w(TAG, "signUpWithEmailPassword:failure", task.getException());
                    Toast.makeText(RegisterActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                mAuth.signOut();
                Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(login);
                finish();
            });

        }
    }

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

    //update BIRTHDATE value
    private void updateBirthDate() {
        String myFormat = "MM-dd-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        etBirthdate.setText(dateFormat.format(calendar.getTime()));
        etBirthdate.setError(null);
    }

    private void selectBirthDate() {
        Calendar maxCal = Calendar.getInstance();
        maxCal.add(Calendar.YEAR, -18);
        int mYear = maxCal.get(Calendar.YEAR);
        int mMonth = maxCal.get(Calendar.MONTH);
        int mDay = maxCal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener startDateListener = (arg0, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH,monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            updateBirthDate();
        };

        etBirthdate.setOnClickListener(view -> {
            DatePickerDialog dpDialog = new DatePickerDialog(RegisterActivity.this, startDateListener, mYear, mMonth, mDay);
            dpDialog.getDatePicker().setMaxDate(maxCal.getTimeInMillis());
            dpDialog.show();
        });
    }

    public void goBack() {
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}