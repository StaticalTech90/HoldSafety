package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    //add forgot password function

    String currentPassword, newPassword, confirmNewPassword;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        user = FirebaseAuth.getInstance().getCurrentUser();

        TextView txtCurrentPassword, txtNewPassword, txtConfirmNewPassword;
        Button btnConfirm, btnCancel;

        txtCurrentPassword = findViewById(R.id.txtCurrentPassword);
        txtNewPassword = findViewById(R.id.txtNewPassword);
        txtConfirmNewPassword = findViewById(R.id.txtConfirmNewPassword);

        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);

        txtCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                currentPassword = txtCurrentPassword.getText().toString().trim();
            }
        });

        txtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                newPassword = txtNewPassword.getText().toString().trim();
            }
        });

        txtConfirmNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                confirmNewPassword = txtConfirmNewPassword.getText().toString().trim();
            }
        });
        
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //checks if fields are empty
                if (TextUtils.isEmpty(currentPassword) && TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(confirmNewPassword)) {
                    txtCurrentPassword.setError("Field is empty");
                    txtNewPassword.setError("Field is empty");
                    txtConfirmNewPassword.setError("Field is empty");
                    return;
                }

                if (TextUtils.isEmpty(currentPassword) && TextUtils.isEmpty(newPassword)) {
                    txtCurrentPassword.setError("Field is empty");
                    txtNewPassword.setError("Field is empty");
                    return;
                }

                if (TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(confirmNewPassword)) {
                    txtNewPassword.setError("Field is empty");
                    txtConfirmNewPassword.setError("Field is empty");
                    return;
                }

                if (TextUtils.isEmpty(currentPassword) && TextUtils.isEmpty(confirmNewPassword)) {
                    txtCurrentPassword.setError("Field is empty");
                    txtConfirmNewPassword.setError("Field is empty");
                    return;
                }

                if (TextUtils.isEmpty(currentPassword)) {
                    txtCurrentPassword.setError("Field is empty");
                    return;
                }

                if (TextUtils.isEmpty(newPassword)) {
                    txtNewPassword.setError("Field is empty");
                    return;
                }

                if (TextUtils.isEmpty(confirmNewPassword)) {
                    txtConfirmNewPassword.setError("Field is empty");
                    return;
                }

                //if all fields are not empty
                if (!TextUtils.isEmpty(currentPassword) && !TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(confirmNewPassword)) {
                    //check if new password and confirm new password matches
                    if(!newPassword.equals(confirmNewPassword)){
                        txtConfirmNewPassword.setError("Password mismatch");
                        return;
                    }

                    changePassword();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reset values
                currentPassword = "";
                newPassword = "";
                confirmNewPassword = "";

                startActivity(new Intent(ChangePasswordActivity.this, AccountDetailsActivity.class));
                finish();
            }
        });
    }

    public void changePassword(){
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), currentPassword);

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Password updated");

                                        //reset values
                                        currentPassword = "";
                                        newPassword = "";
                                        confirmNewPassword = "";

                                        FirebaseAuth.getInstance().signOut();
                                        Toast.makeText(ChangePasswordActivity.this, "Password Changed", Toast.LENGTH_SHORT).show();

                                        startActivity(new Intent(ChangePasswordActivity.this, MainActivity.class));
                                        finish();
                                    } else {
                                        Log.d(TAG, "Error password not updated");
                                        Toast.makeText(ChangePasswordActivity.this, "Enter Valid New Password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "Error auth failed");
                            Toast.makeText(ChangePasswordActivity.this, "Current Password Incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}