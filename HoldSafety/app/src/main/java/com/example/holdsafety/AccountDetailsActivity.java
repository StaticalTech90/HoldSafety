package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
    FirebaseUser user;
    FirebaseFirestore db;
    StorageReference fStorage;
    DocumentReference docRef;
    Boolean isNumberChanged = false, isEmailChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        fStorage = FirebaseStorage.getInstance().getReference();

        TextView txtLastName, txtFirstName, txtMiddleName, txtBirthDate, txtSex;
        EditText txtMobileNumber, txtEmail;
        Button btnSave;

        txtLastName = findViewById(R.id.txtLastName);
        txtFirstName = findViewById(R.id.txtFirstName);
        txtMiddleName = findViewById(R.id.txtMiddleName);
        txtBirthDate = findViewById(R.id.txtBirthDate);
        txtSex = findViewById(R.id.txtSex);
        txtMobileNumber = findViewById(R.id.txtMobileNumber);
        txtEmail = findViewById(R.id.txtEmail);
        btnSave = findViewById(R.id.btnSave);

        //show details
        docRef = db.collection("users").document(user.getUid());

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String lastName = documentSnapshot.getString("LastName");
                    String firstName = documentSnapshot.getString("FirstName");
                    String middleName = documentSnapshot.getString("MiddleName");
                    String birthDate = documentSnapshot.getString("BirthDate");
                    String sex = documentSnapshot.getString("Sex");
                    String currentMobileNumber = documentSnapshot.getString("MobileNumber");
                    String currentEmail = documentSnapshot.getString("Email");

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
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newMobileNumber, newEmail;

                newMobileNumber = txtMobileNumber.getText().toString().trim();
                newEmail = txtEmail.getText().toString().trim();

                //checks if fields are empty
                if(TextUtils.isEmpty(newMobileNumber) && TextUtils.isEmpty(newEmail)){
                    txtMobileNumber.setError("Email is required");
                    txtEmail.setError("Password is required");
                    return;
                }

                if(TextUtils.isEmpty(newMobileNumber)){
                    txtMobileNumber.setError("Mobile Number is required");
                    return;
                }

                if(TextUtils.isEmpty(newEmail)){
                    txtEmail.setError("Email is required");
                    return;
                }

                //not empty fields, no changes
                if(!isEmailChanged && !isNumberChanged){
                    Toast.makeText(AccountDetailsActivity.this, "No Changes Made", Toast.LENGTH_LONG).show();
                }

                //if there are new changes
                if(isNumberChanged){
                    docRef.update("MobileNumber", newMobileNumber);
                    isNumberChanged = false;
                    Toast.makeText(AccountDetailsActivity.this, "Mobile Number Successfully Changed", Toast.LENGTH_LONG).show();

                    //if email is not changed reload activity
                    if(!isEmailChanged){
                        finish();
                        startActivity(getIntent());
                    }
                }

                if(isEmailChanged) {
                    AlertDialog.Builder dialogEmail;
                    dialogEmail = new AlertDialog.Builder(AccountDetailsActivity.this);
                    dialogEmail.setTitle("Change Email");
                    dialogEmail.setMessage("You have 48 hours to verify your new email. Failure to do so will delete your account." +
                            "Are you sure you want to change?");

                    dialogEmail.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            docRef.update("Email", newEmail);
                            changeEmail(newEmail);
                            Toast.makeText(AccountDetailsActivity.this, "Check your email for verification", Toast.LENGTH_LONG).show();

                            isEmailChanged = false;

                            finish();
                            startActivity(getIntent());
                        }
                    });

                    dialogEmail.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            isEmailChanged = false;
                            finish();
                            startActivity(getIntent());
                        }
                    });

                    AlertDialog changeEmailDialog = dialogEmail.create();
                    changeEmailDialog.show();
                }
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

    public void userRegister(View view){
        Toast.makeText(getApplicationContext(), "Register", Toast.LENGTH_SHORT).show();
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
        String newPass = "newpassword";
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), "password");

        // Prompt the user to re-provide their sign-in credentials
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Password updated");

                                                FirebaseAuth.getInstance().signOut();
                                                Toast.makeText(AccountDetailsActivity.this, "Password Changed", Toast.LENGTH_SHORT).show();

                                                startActivity(new Intent(AccountDetailsActivity.this, MainActivity.class));
                                                finish();

                                            } else {
                                                Log.d(TAG, "Error password not updated");
                                            }
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "Error auth failed");
                                }
                            }
                        });
    }

    public void changeEmail(String newEmail){
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), "password"); //change this to re-enter password later
        // prompt the user to re-provide their sign-in credentials

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "User re-authenticated.");

                        user.updateEmail(newEmail)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User email address updated.");
                                        }
                                    }
                                });
                    }
                });
    }

    public void removeAccount(View view){
        AlertDialog.Builder dialog;
        dialog = new AlertDialog.Builder(AccountDetailsActivity.this);
        dialog.setTitle("Remove Account");
        dialog.setMessage("Are you sure you want to delete your account? " +
                "Keep in mind that all information and files would be deleted from the system.");

        dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    db.collection("users").document(user.getUid()).delete();

                                    //not working
                                    StorageReference idRef = fStorage.child("id/" + user.getUid());
                                    idRef.delete();

                                    Toast.makeText(AccountDetailsActivity.this, "Account Deleted", Toast.LENGTH_LONG).show();

                                    startActivity(new Intent(AccountDetailsActivity.this, MainActivity.class));
                                    finish();
                                }
                                else{
                                    Toast.makeText(AccountDetailsActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        dialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }
}