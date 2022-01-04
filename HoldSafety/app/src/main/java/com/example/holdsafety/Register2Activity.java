package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register2Activity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference fStorage = FirebaseStorage.getInstance().getReference();
    FirebaseUser user;
    Intent intent;

    private EditText etMobileNumber;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConPassword;
    private Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        mAuth = FirebaseAuth.getInstance();

        etMobileNumber = findViewById(R.id.txtMobileNumber);
        etEmail = findViewById(R.id.txtEmail);
        etPassword = findViewById(R.id.txtPassword);
        etConPassword = findViewById(R.id.txtConfirmPassword);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageURI = data.getData();
            Toast.makeText(getApplicationContext(), "Selected " + imageURI, Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadID(View view){
        intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityIfNeeded(intent, 1);
    }

    public void uploadImage(){
        if (imageURI != null) {
            // Code for showing progressDialog while uploading
//            ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
//            progressDialog.setTitle("Uploading...");
//            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref = fStorage.child("id/"+ user.getUid());

            // adding listeners on upload
            // or failure of image
            // Progress Listener for loading
            // percentage on the dialog box
            ref.putFile(imageURI).addOnSuccessListener(taskSnapshot -> {

                // Image uploaded successfully
                // Dismiss dialog
                //progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {

                // Error, Image not uploaded
                //progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(taskSnapshot -> {
//                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                            progressDialog.setMessage("Uploaded " + (int)progress + "%");
            });
        }
    }

    public void userRegister(View view){
        //get values from register1Activity
        Map<String, Object> docUsers = new HashMap<>();
        Intent intent = getIntent();

        EditText etMobileNumber = findViewById(R.id.txtMobileNumber);
        EditText etEmail = findViewById(R.id.txtEmail);
        EditText etPassword = findViewById(R.id.txtCurrentPassword);
        EditText etConPassword = findViewById(R.id.txtNewPassword);

        String lastName = intent.getStringExtra("lastName");
        String firstName = intent.getStringExtra("firstName");
        String middleName = intent.getStringExtra("middleName");
        String sex = intent.getStringExtra("sex");
        String birthDate = intent.getStringExtra("birthDate");
        String mobileNumber = etMobileNumber.getText().toString();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String cPassword = etConPassword.getText().toString();

        docUsers.put("LastName", lastName);
        docUsers.put("FirstName", firstName);
        docUsers.put("MiddleName", middleName);
        docUsers.put("Sex", sex);
        docUsers.put("BirthDate", birthDate);
        docUsers.put("MobileNumber", mobileNumber);
        docUsers.put("Email", email);


        if(TextUtils.isEmpty(etMobileNumber.getText())){
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
                        uploadImage();

                        db.collection("users").document(user.getUid()).set(docUsers)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

                        startActivity(new Intent(getApplicationContext(), MenuActivity.class));
                    } else {
                        // If sign up fails, display a message to the user.
                        Log.w(TAG, "signUpWithEmailPassword:failure", task.getException());
                        Toast.makeText(getApplicationContext(),
                                Objects.requireNonNull(task.getException()).toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        //nani
        //Toast.makeText(getApplicationContext(), "Register", Toast.LENGTH_SHORT).show();
    }
}