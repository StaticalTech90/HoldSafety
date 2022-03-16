package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.Manifest;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterGoogleActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    StorageReference imageRef;

    ImageView btnBack;
    EditText etMiddleName, etMobileNo, etBirthDate;
    TextView lblName;
    Button btnProceed, btnUpload;
    Spinner spinnerSex;
    static Uri imageURI;
    String idPicUri;

    final Calendar calendar = Calendar.getInstance();

    TextView lblLink;
    String firstName, lastName, email, selectedSex;
    Map<String, Object> docUsers = new HashMap<>();
    HashMap<String, String> googleSignInMap;

    private static final int EXTERNAL_STORAGE_REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_google);

        //Get data from db and auto-input in the form
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        imageRef = FirebaseStorage.getInstance().getReference("id");

        Intent intent = getIntent();
        googleSignInMap = (HashMap<String, String>)intent.getSerializableExtra("googleSignInMap");

        lblLink = findViewById(R.id.txtImageLink);
        etMiddleName = findViewById(R.id.txtMiddleName);
        etMobileNo = findViewById(R.id.txtMobileNumber);
        etBirthDate = findViewById(R.id.txtBirthDate);
        spinnerSex = findViewById(R.id.txtSex);
        lblName = findViewById(R.id.lblGoogleName);
        btnBack = findViewById(R.id.backArrow);
        btnProceed = findViewById(R.id.btnProceed);
        btnUpload = findViewById(R.id.btnUploadID);


        // check if a user is signed in
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        // get the details from their Google account
        if(signInAccount != null) {
            email = signInAccount.getEmail();
        }

        dropdownSex();
        selectBirthDate();

        //get details from prev intent
        lastName = googleSignInMap.get("lastName");
        firstName = googleSignInMap.get("firstName");
        String username =  lastName + ", " + firstName;
        lblName.setText(username);

        btnBack.setOnClickListener(view -> goBack());
        btnUpload.setOnClickListener(view -> pickImage());
        btnProceed.setOnClickListener(view -> {
            try {
                userRegister();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    //refactored userRegister onClickListener into independent function
    public void userRegister() throws ParseException {
        user = mAuth.getCurrentUser();
        String mobileNumberRegex = "^(09|\\+639)\\d{9}$";
        Pattern mobileNumberPattern = Pattern.compile(mobileNumberRegex);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

        String userId = user.getUid();
        String uEmail = googleSignInMap.get("email");
        String middleName = etMiddleName.getText().toString();
        String birthDate = etBirthDate.getText().toString().trim();
        String mobileNo = etMobileNo.getText().toString();
        String sex = spinnerSex.getSelectedItem().toString();

        Matcher mobileNumberMatcher = mobileNumberPattern.matcher(etMobileNo.getText());

        Boolean validInput = true;
        if(TextUtils.isEmpty(birthDate)) {
            //assert parsedDate != null;
            etBirthDate.getText().clear();
            etBirthDate.setHint("Please enter valid birthdate");
            etBirthDate.setError("Please enter valid birthdate");
            validInput = false;
        }

        if(spinnerSex.getSelectedItem().equals("Sex")) {
            ((TextView)spinnerSex.getSelectedView()).setError("please select sex");
            validInput = false;
        }

        if(TextUtils.isEmpty(etMobileNo.getText())) {
            etMobileNo.setError("Enter Mobile number");
            validInput = false;
        } else if (!mobileNumberMatcher.matches()) {
            etMobileNo.setError("Please enter a valid mobile number");
            validInput = false;
        } else if(etMobileNo.getText().length() != 11) {
            etMobileNo.setError("Please enter a valid mobile number");
            validInput = false;
        }

        if(validInput){
            docUsers.put("ID", user.getUid());
            docUsers.put("Email", uEmail);
            docUsers.put("LastName", lastName);
            docUsers.put("FirstName", firstName);
            docUsers.put("MiddleName", middleName);
            docUsers.put("BirthDate", birthDate);
            docUsers.put("Sex", sex);
            docUsers.put("MobileNumber", mobileNo);
            docUsers.put("isVerified", false);

            if (!lblLink.getText().equals("")) {
                uploadPhotoToStorage();
                docUsers.put("profileComplete", true);
            } else {
                docUsers.put("profileComplete", false);
            }

            db.collection("users").document(userId).set(docUsers)
                    .addOnSuccessListener(aVoid -> {

                        Intent landing = new Intent(RegisterGoogleActivity.this,
                                LandingActivity.class);
                        startActivity(landing);
                        finish();
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(getApplicationContext(),
                                "Error writing document",
                                Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error writing document", e);
                    });
        }
    }

    //Since the methods here are async, set this value ahead of the rest of the document fields
    private void uploadPhotoToStorage() {
            imageRef.child(user.getUid())
                    .putFile(Uri.parse(lblLink.getText().toString()))
                    .addOnSuccessListener(taskSnapshot -> imageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(uri -> {
                        idPicUri = String.valueOf(uri);
                        docUsers.put("imgUri", idPicUri);
                        Log.i("URI gDUrl()", idPicUri);

                        db.collection("users").document(user.getUid()).set(docUsers)
                                .addOnSuccessListener(aVoid -> {
                                    Log.i(TAG, "Image pushed");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error writing document", e);
                                });
                    }))
                    .addOnFailureListener(e -> Toast.makeText(RegisterGoogleActivity.this, "Upload failed.",
                            Toast.LENGTH_SHORT).show()
                    );
    }

    private void pickImage() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) { //DENIED PERMISSION
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
                                imageURI = data.getData();
                                lblLink.setText(imageURI.toString());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageURI = data.getData();
        }
    }

    public String dropdownSex() {
        //Sex selector spinner
        String[] sex = new String[]{"Sex", "M", "F"};
        List<String> sexList = new ArrayList<>(Arrays.asList(sex));

        ArrayAdapter<String> spinnerSexAdapter = new ArrayAdapter<String>(this, R.layout.spinner_sex, sexList) {
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
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                    selectedSex = spinnerSex.getSelectedItem().toString().trim();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        return selectedSex;
    }

    //update BIRTHDATE value
    private void updateBirthDate() {
        String myFormat = "MM-dd-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
        etBirthDate.setText(dateFormat.format(calendar.getTime()));
        etBirthDate.setError(null);
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

        etBirthDate.setOnClickListener(view -> {
            DatePickerDialog dpDialog = new DatePickerDialog(RegisterGoogleActivity.this, startDateListener, mYear, mMonth, mDay);
            dpDialog.getDatePicker().setMaxDate(maxCal.getTimeInMillis());
            dpDialog.show();
        });
    }

    @Override
    public void onBackPressed() {
        //remobve any instance of the selected google acocunt
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("233680747912-m8q45hor79go5n8aqfkuneklnkshudqs.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mAuth.signOut();
        user.delete();

        GoogleSignInClient gsc = GoogleSignIn.getClient(RegisterGoogleActivity.this, gso);
        gsc.signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        });

        super.onBackPressed();
        finish();
    }

    public void goBack(){
        finish();
    }
}