package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccountDetailsActivity extends AppCompatActivity {
    public Uri imageURI;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference imageRef = FirebaseStorage.getInstance().getReference("id");
    DocumentReference docRef;

    Boolean isNumberChanged = false, isEmailChanged = false;
    String userPassword = "";
    String idUri, userId;
    HashMap<String, Object> docUsers = new HashMap<>();

    ImageView btnBack;
    TextView txtLastName, txtFirstName, txtMiddleName, txtBirthDate, txtSex, lblAccountStatus, txtImage, btnRemoveAccount, btnChangePass;
    EditText txtMobileNumber, txtEmail;
    Button btnSave, btnUploadID;

    private static final int EXTERNAL_STORAGE_REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        userId = user.getUid();
        docRef = db.collection("users").document(userId);

        txtLastName = findViewById(R.id.txtLastName);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtMiddleName = findViewById(R.id.txtMiddleName);
        txtBirthDate = findViewById(R.id.txtBirthDate);
        txtSex = findViewById(R.id.txtSex);
        txtMobileNumber = findViewById(R.id.txtMobileNumber);
        txtEmail = findViewById(R.id.txtEmail);
        txtImage = findViewById(R.id.txtImageLink);
        lblAccountStatus = findViewById(R.id.lblAccountStatus);
        btnBack = findViewById(R.id.backArrow);
        btnRemoveAccount = findViewById(R.id.btnRemoveAccount);
        btnChangePass = findViewById(R.id.btnChangePassword);
        btnSave = findViewById(R.id.btnSave);
        btnUploadID = findViewById(R.id.btnUploadID);

        //show details
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                String lastName = documentSnapshot.getString("LastName");
                String firstName = documentSnapshot.getString("FirstName");
                String middleName = documentSnapshot.getString("MiddleName");
                String birthDate = documentSnapshot.getString("BirthDate");
                String sex = documentSnapshot.getString("Sex");
                String currentMobileNumber = documentSnapshot.getString("MobileNumber");
                String currentEmail = documentSnapshot.getString("Email");

                Boolean isVerified = documentSnapshot.getBoolean("isVerified");
                Boolean isProfileComplete = documentSnapshot.getBoolean("profileComplete");

                setAccountStatus(isVerified, isProfileComplete);

                txtLastName.setText(lastName);
                txtFirstName.setText(firstName);
                txtMiddleName.setText(middleName);
                txtBirthDate.setText(birthDate);
                txtSex.setText(sex);
                txtMobileNumber.setText(currentMobileNumber);
                txtEmail.setText(currentEmail);

                //compares current saved data to new input
                txtMobileNumber.addTextChangedListener(new TextWatcher() {
                    String newNumber;
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        newNumber = txtMobileNumber.getText().toString().trim();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(currentMobileNumber.equals(newNumber)){
                            isNumberChanged = false;
                            Toast.makeText(AccountDetailsActivity.this, "No Changes", Toast.LENGTH_SHORT).show();
                        } else{
                            isNumberChanged = true;
                            Toast.makeText(AccountDetailsActivity.this, "New Number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                txtEmail.addTextChangedListener(new TextWatcher() {
                    String newEmail;
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        newEmail = txtEmail.getText().toString().trim();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(currentEmail.equals(newEmail)){
                            isEmailChanged = false;
                            Toast.makeText(AccountDetailsActivity.this, "No Changes", Toast.LENGTH_SHORT).show();
                        } else{
                            isEmailChanged = true;
                            Toast.makeText(AccountDetailsActivity.this, "New Email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else {
                Toast.makeText(AccountDetailsActivity.this, "Details not found. Please contact developer.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(view -> goBack());
        btnRemoveAccount.setOnClickListener(view -> removeAccount());
        btnChangePass.setOnClickListener(view -> changePassword());
        btnUploadID.setOnClickListener(v -> pickImage());

        btnSave.setOnClickListener(view -> {
            String newMobileNumber, newEmail;

            newMobileNumber = txtMobileNumber.getText().toString().trim();
            newEmail = txtEmail.getText().toString().trim();

            //checks if fields are empty
            if (TextUtils.isEmpty(newMobileNumber) && TextUtils.isEmpty(newEmail)) {
                txtMobileNumber.setError("Email is required");
                txtEmail.setError("Password is required");
                return;
            }

            if (TextUtils.isEmpty(newMobileNumber)) {
                txtMobileNumber.setError("Mobile Number is required");
                return;
            }

            if (TextUtils.isEmpty(newEmail)) {
                txtEmail.setError("Email is required");
                return;
            }

            //not empty fields, no changes
            if (!isEmailChanged && !isNumberChanged) {
                Toast.makeText(AccountDetailsActivity.this, "No Changes Made", Toast.LENGTH_LONG).show();
            }
            else if (isEmailChanged || isNumberChanged){
                String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
                String mobileNumberRegex = "^(09|\\+639)\\d{9}$";
                Pattern emailPattern = Pattern.compile(emailRegex);
                Pattern mobileNumberPattern = Pattern.compile(mobileNumberRegex);
                Matcher emailMatcher = emailPattern.matcher(txtEmail.getText());
                Matcher mobileNumberMatcher = mobileNumberPattern.matcher(txtMobileNumber.getText());

                if (!mobileNumberMatcher.matches()) {
                    txtMobileNumber.setError("Please enter a valid mobile number");
                } else if (!emailMatcher.matches()){
                    txtEmail.setError("Please enter a valid email");
                } else {
                    //pop up for re-enter password
                    EditText txtInputPassword;

                    AlertDialog.Builder dialogSaveChanges;
                    dialogSaveChanges = new AlertDialog.Builder(AccountDetailsActivity.this);
                    dialogSaveChanges.setTitle("Save Changes");

                    //if only number is changed
                    if (isNumberChanged && !isEmailChanged){
                        dialogSaveChanges.setMessage("Re-enter password to save changes:");
                    }

                    //if both
                    if (isEmailChanged){
                        dialogSaveChanges.setMessage("Note: Upon changing your email, you will not be able to save recordings until the new email is verified." +
                                "\n\nRe-enter password to save changes:");
                    }

                    txtInputPassword = new EditText(AccountDetailsActivity.this);
                    dialogSaveChanges.setView(txtInputPassword);

                    txtInputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    //saves user input if not empty
                    txtInputPassword.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            userPassword = txtInputPassword.getText().toString().trim();
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                        }
                    });

                    dialogSaveChanges.setPositiveButton("Done", (dialogInterface, i) -> {
                        //Do nothing here, override this button later to change the close behaviour
                    });

                    dialogSaveChanges.setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.dismiss();

                        //reset values
                        isEmailChanged = false;
                        isNumberChanged = false;
                        userPassword = "";

                        finish();
                        startActivity(getIntent());
                    });

                    AlertDialog changeEmailDialog = dialogSaveChanges.create();
                    changeEmailDialog.show();

                    //Override
                    changeEmailDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (TextUtils.isEmpty(userPassword)) {
                                txtInputPassword.setError("Password is required");
                            } else {
                                userPassword = txtInputPassword.getText().toString();

                                if (isNumberChanged) {
                                    changeNumber(newMobileNumber);
                                }

                                if (isEmailChanged) {
                                    changeEmail(newEmail);
                                }

                            }
                        }
                    }); //end of dialog code
                }
            }
        });
    }

    private void setAccountStatus(Boolean isVerified, Boolean isProfileComplete) {
        if(isVerified){
            lblAccountStatus.setTextColor(getResources().getColor(R.color.green));
            lblAccountStatus.setText("Verified Account");
            btnUploadID.setVisibility(View.GONE);
        } else if (!isVerified && isProfileComplete){
            lblAccountStatus.setTextColor(getResources().getColor(R.color.yellow));
            lblAccountStatus.setText("Pending Verification");
            btnUploadID.setVisibility(View.GONE);
        } else if (!isProfileComplete && !isVerified){
            lblAccountStatus.setTextColor(getResources().getColor(R.color.red));
            lblAccountStatus.setText("Not Verified");
        }
    }

    //PUT IMAGE UPLOAD HERE
    private void uploadPhotoToStorage() {
        imageRef.child(user.getUid())
                .putFile(Uri.parse(txtImage.getText().toString()))
                .addOnSuccessListener(taskSnapshot -> imageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(uri -> {
                    idUri = String.valueOf(uri);
                    docUsers.put("imgUri", idUri);
                    docUsers.put("profileComplete", true);
                    Log.i("URI gDUrl()", idUri);

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

                    setAccountStatus(false, true);
                })).addOnFailureListener(e -> Toast.makeText(AccountDetailsActivity.this, "Upload failed.",
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
                                txtImage.setText(imageUri.toString());
                                uploadPhotoToStorage();
                            }
                        }
                    }
                }
            });

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageURI = data.getData();
            //Toast.makeText(getApplicationContext(), "Selected " + imageURI, Toast.LENGTH_SHORT).show();
            //uploadPicture();
        }
    }

    public void changePassword(){
        startActivity(new Intent(AccountDetailsActivity.this, ChangePasswordActivity.class));
    }

    public void changeEmail(String newEmail){
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(),userPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //updates email in Auth
                        user.updateEmail(newEmail)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Log.d(TAG, "User email address updated.");

                                        //updates email in document
                                        docRef.update("Email", newEmail);

                                        //reset values
                                        isEmailChanged = false;
                                        userPassword = "";

                                        //Toast.makeText(AccountDetailsActivity.this, "Changes Saved", Toast.LENGTH_LONG).show();

                                        //insert email verification here

                                        finish();
                                        startActivity(getIntent());
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    isNumberChanged = false;
                                    isEmailChanged = false;
                                    userPassword = "";
                                    finish();
                                    startActivity(getIntent());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    isNumberChanged = false;
                    isEmailChanged = false;
                    userPassword = "";

                    //Toast.makeText(AccountDetailsActivity.this, "Reauthentication Failed" + "\nChanges not Saved", Toast.LENGTH_LONG).show();

                    finish();
                    startActivity(getIntent());
                });
    }

    public void changeNumber(String newMobileNumber){
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(),userPassword);

        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        docRef.update("MobileNumber", newMobileNumber);
                        isNumberChanged = false;
                        Toast.makeText(AccountDetailsActivity.this, "Mobile Number Successfully Changed", Toast.LENGTH_LONG).show();

                        isNumberChanged = false;

                        if(!isEmailChanged){
                            userPassword = "";

                            finish();
                            startActivity(getIntent());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    isNumberChanged = false;
                    isEmailChanged = false;
                    userPassword = "";

                    Toast.makeText(AccountDetailsActivity.this, "Incorrect Password" + "\nChanges not Saved", Toast.LENGTH_LONG).show();

                    finish();
                    startActivity(getIntent());
                });
    }

    //ignore this - will delete
    public void reAuthenticate(String  newEmail, String newMobileNumber){
        EditText txtInputPassword;

        AlertDialog.Builder dialogSaveChanges;
        dialogSaveChanges = new AlertDialog.Builder(AccountDetailsActivity.this);
        dialogSaveChanges.setTitle("Save Changes");

        //if only number is changed
        if (isNumberChanged && !isEmailChanged){
            dialogSaveChanges.setMessage("Re-enter password to save changes:");
        }

        //if both
        if (isEmailChanged){
            dialogSaveChanges.setMessage("You have 48 hours to verify your new email. Failure to do so will delete your account." +
                    "\n\nRe-enter password to save changes:");
        }

        txtInputPassword = new EditText(AccountDetailsActivity.this);
        dialogSaveChanges.setView(txtInputPassword);

        txtInputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        //saves new details input if not empty
        txtInputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                userPassword = txtInputPassword.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        dialogSaveChanges.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Do nothing here, override this button later to change the close behaviour
            }
        });

        dialogSaveChanges.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();

            //reset values
            isEmailChanged = false;
            isNumberChanged = false;
            userPassword = "";

            finish();
            startActivity(getIntent());
        });

        AlertDialog changeEmailDialog = dialogSaveChanges.create();
        changeEmailDialog.show();

        //Override  - only continues if there is a password input
        changeEmailDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if(TextUtils.isEmpty(userPassword)){
                txtInputPassword.setError("Password is required");
            }
            else {
                userPassword = txtInputPassword.getText().toString();

                if (isNumberChanged){
                    changeNumber(newMobileNumber);
                }

                if (isEmailChanged){
                    changeEmail(newEmail);
                }
            }
        });
    }

    public void removeAccount(){
        //add password reauthentication after delete photo works
        user = FirebaseAuth.getInstance().getCurrentUser();

        AlertDialog.Builder dialogRemoveAccount;
        dialogRemoveAccount = new AlertDialog.Builder(AccountDetailsActivity.this);
        dialogRemoveAccount.setTitle("Remove Account");
        dialogRemoveAccount.setMessage("Are you sure you want to delete your account? " +
                "Keep in mind that all information and files would be deleted from the system.");

        dialogRemoveAccount.setPositiveButton("Delete", (dialogInterface, i) -> user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //DELETE IMAGE
                imageRef.child(user.getUid()).delete()
                .addOnSuccessListener(v -> Toast.makeText(AccountDetailsActivity.this, "Deleted Image", Toast.LENGTH_LONG).show()).addOnFailureListener(v1 -> {
                    //Toast.makeText(AccountDetailsActivity.this, "Failed", Toast.LENGTH_LONG).show();
                });

                db.collection("users").document(user.getUid()).delete();
                db.collection("emergencyContacts").document(user.getUid()).delete();

                startActivity(new Intent(AccountDetailsActivity.this, LoginActivity.class));
                finish();
            }
        }));
        dialogRemoveAccount.setNegativeButton("Dismiss", (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog alertDialog = dialogRemoveAccount.create();
        alertDialog.show();
    }

    private void goBack(){
        finish();
    }
}