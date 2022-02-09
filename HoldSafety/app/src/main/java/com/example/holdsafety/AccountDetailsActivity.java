package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AccountDetailsActivity extends AppCompatActivity {
    public Uri imageURI;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    StorageReference fStorage;
    DocumentReference docRef;
    Boolean isNumberChanged = false, isEmailChanged = false;
    String userPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        Toast.makeText(AccountDetailsActivity.this, user.getUid(), Toast.LENGTH_SHORT).show();

        TextView txtLastName, txtFirstName, txtMiddleName, txtBirthDate, txtSex, lblAccountStatus;
        EditText txtMobileNumber, txtEmail;
        Button btnSave;

        txtLastName = findViewById(R.id.txtLastName);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtMiddleName = findViewById(R.id.txtMiddleName);
        txtBirthDate = findViewById(R.id.txtBirthDate);
        txtSex = findViewById(R.id.txtSex);
        txtMobileNumber = findViewById(R.id.txtMobileNumber);
        txtEmail = findViewById(R.id.txtEmail);
        lblAccountStatus = findViewById(R.id.lblAccountStatus);
        btnSave = findViewById(R.id.btnSave);

        //show details
        docRef = db.collection("users").document(user.getUid());

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

                if(isVerified){
                    lblAccountStatus.setTextColor(getResources().getColor(R.color.green));
                    lblAccountStatus.setText("Verified Account");
                } else if (!isVerified && isProfileComplete){
                    lblAccountStatus.setTextColor(getResources().getColor(R.color.yellow));
                    lblAccountStatus.setText("Pending Verification");
                } else if (!isProfileComplete && !isVerified){
                    lblAccountStatus.setTextColor(getResources().getColor(R.color.red));
                    lblAccountStatus.setText("Not Verified");
                }

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

                dialogSaveChanges.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do nothing here, override this button later to change the close behaviour
                    }
                });

                dialogSaveChanges.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        //reset values
                        isEmailChanged = false;
                        isNumberChanged = false;
                        userPassword = "";

                        finish();
                        startActivity(getIntent());
                    }
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
        });
    }

    public void uploadID(View view){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        //deprecated -> wont work
        //startActivityForResult(intent, 1);
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

    public void changePassword(View view){
        startActivity(new Intent(AccountDetailsActivity.this, ChangePasswordActivity.class));
        finish();
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

                                        Toast.makeText(AccountDetailsActivity.this, "Changes Saved", Toast.LENGTH_LONG).show();

                                        //insert email verification here

                                        finish();
                                        startActivity(getIntent());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        isNumberChanged = false;
                                        isEmailChanged = false;
                                        userPassword = "";

                                        Toast.makeText(AccountDetailsActivity.this, "Update Email Failed" + "\nChanges not Saved", Toast.LENGTH_LONG).show();

                                        finish();
                                        startActivity(getIntent());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isNumberChanged = false;
                        isEmailChanged = false;
                        userPassword = "";

                        Toast.makeText(AccountDetailsActivity.this, "Reauthentication Failed" + "\nChanges not Saved", Toast.LENGTH_LONG).show();

                        finish();
                        startActivity(getIntent());
                    }
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
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isNumberChanged = false;
                        isEmailChanged = false;
                        userPassword = "";

                        Toast.makeText(AccountDetailsActivity.this, "Incorrect Password" + "\nChanges not Saved", Toast.LENGTH_LONG).show();

                        finish();
                        startActivity(getIntent());
                    }
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

        dialogSaveChanges.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                //reset values
                isEmailChanged = false;
                isNumberChanged = false;
                userPassword = "";

                finish();
                startActivity(getIntent());
            }
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

    public void removeAccount(View view){
        //add password reauthentication after delete photo works
        user = FirebaseAuth.getInstance().getCurrentUser();

        AlertDialog.Builder dialogRemoveAccount;
        dialogRemoveAccount = new AlertDialog.Builder(AccountDetailsActivity.this);
        dialogRemoveAccount.setTitle("Remove Account");
        dialogRemoveAccount.setMessage("Are you sure you want to delete your account? " +
                "Keep in mind that all information and files would be deleted from the system.");

        dialogRemoveAccount.setPositiveButton("Delete", (dialogInterface, i) ->
                user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //DELETE IMAGE
                FirebaseStorage.getInstance().getReference("id").child(user.getUid()).delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(AccountDetailsActivity.this, "Deleted Image", Toast.LENGTH_LONG).show();
                }).addOnFailureListener(v1 -> {
                    Toast.makeText(AccountDetailsActivity.this, "Failed", Toast.LENGTH_LONG).show();
                });

                db.collection("users").document(user.getUid()).delete();
                db.collection("emergencyContacts").document(user.getUid()).delete();

                startActivity(new Intent(AccountDetailsActivity.this, LoginActivity.class));
                finish();
            }
            else{
                Toast.makeText(AccountDetailsActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        }));

        dialogRemoveAccount.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = dialogRemoveAccount.create();
        alertDialog.show();
    }

    public void goBack(View view){
        startActivity(new Intent(AccountDetailsActivity.this, MenuActivity.class));
        finish();
    }
}