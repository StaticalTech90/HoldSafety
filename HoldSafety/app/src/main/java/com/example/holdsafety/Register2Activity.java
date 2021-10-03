package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Register2Activity extends AppCompatActivity {
    public Uri imageURI;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        mAuth = FirebaseAuth.getInstance();
    }

    public void uploadID(View view){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //startActivityForResult(intent, 1); this is deprecated
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
                        FirebaseUser user = mAuth.getCurrentUser();

                        db.collection("users").document(user.getUid()).set(docUsers)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });

                        startActivity(new Intent(getApplicationContext(), MenuActivity.class));
                    } else {
                        // If sign up fails, display a message to the user.
                        Log.w(TAG, "signUpWithEmailPassword:failure", task.getException());
                        Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        //nani
        //Toast.makeText(getApplicationContext(), "Register", Toast.LENGTH_SHORT).show();
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
}