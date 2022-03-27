package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.content.Intent;
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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePasswordActivity extends AppCompatActivity {
    FirebaseUser user;
    FirebaseAuth mAuth;
    LogHelper logHelper;
    ImageView btnBack;
    Button btnConfirm, btnCancel;
    TextView btnForgotPassword;
    EditText txtCurrentPassword, txtNewPassword, txtConfirmNewPassword;
    TextView txtToggleCurrentPass, txtToggleNewPass, txtToggleConfirmPass;

    String currentPassword, newPassword, confirmNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        logHelper = new LogHelper(this, mAuth, user,this);

        txtCurrentPassword = findViewById(R.id.txtCurrentPassword);
        txtNewPassword = findViewById(R.id.txtNewPassword);
        txtConfirmNewPassword = findViewById(R.id.txtConfirmNewPassword);
        txtToggleCurrentPass = findViewById(R.id.txtToggleCurrentPass);
        txtToggleNewPass = findViewById(R.id.txtToggleNewPass);
        txtToggleConfirmPass = findViewById(R.id.txtToggleConfirmPass);

        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnBack = findViewById(R.id.backArrow);
        
        btnBack.setOnClickListener(view -> goBack());
        btnForgotPassword.setOnClickListener(view -> forgotPassword());

        passwordToggleListeners();

        btnConfirm.setOnClickListener(view -> {
            currentPassword = txtCurrentPassword.getText().toString().trim();
            newPassword = txtNewPassword.getText().toString().trim();
            confirmNewPassword = txtConfirmNewPassword.getText().toString().trim();

            String passRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[-@%}+'!/#$^?:;,(\")~`.=&{><_])(?=\\S+$).{8,}$";
            Pattern passPattern = Pattern.compile(passRegex);
            Matcher passMatcher = passPattern.matcher(newPassword);

            //checks if fields are empty
            if (TextUtils.isEmpty(currentPassword) && TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(confirmNewPassword)) {
                txtCurrentPassword.setError("Field is empty");
                txtNewPassword.setError("Field is empty");
                txtConfirmNewPassword.setError("Field is empty");
                return;
            } else if (TextUtils.isEmpty(currentPassword) && TextUtils.isEmpty(newPassword)) {
                txtCurrentPassword.setError("Field is empty");
                txtNewPassword.setError("Field is empty");
                return;
            } else if (TextUtils.isEmpty(newPassword) && TextUtils.isEmpty(confirmNewPassword)) {
                txtNewPassword.setError("Field is empty");
                txtConfirmNewPassword.setError("Field is empty");
                return;
            } else if (TextUtils.isEmpty(currentPassword) && TextUtils.isEmpty(confirmNewPassword)) {
                txtCurrentPassword.setError("Field is empty");
                txtConfirmNewPassword.setError("Field is empty");
                return;
            } else if (TextUtils.isEmpty(currentPassword)) {
                txtCurrentPassword.setError("Field is empty");
                return;
            } else if (TextUtils.isEmpty(newPassword)) {
                txtNewPassword.setError("Field is empty");
                return;
            } else if (!passMatcher.matches()){
                txtNewPassword.setError("Password must contain the following: " +
                        "\n - At least 8 characters" +
                        "\n - At least 1 digit" +
                        "\n - At least one upper and lower case letter" +
                        "\n - A special character (such as @, #, etc.)" +
                        "\n - No spaces or tabs");
            } else if (TextUtils.isEmpty(confirmNewPassword)) {
                txtConfirmNewPassword.setError("Field is empty");
                return;
            } else if(!newPassword.equals(confirmNewPassword)){
                txtConfirmNewPassword.setError("Password mismatch");
                return;
            } else if(currentPassword.equals(newPassword)){
                txtNewPassword.setError("Password does not change");
            } else {
                changePassword();
            }
        });

        btnCancel.setOnClickListener(view -> {
            startActivity(new Intent(ChangePasswordActivity.this, AccountDetailsActivity.class));
            finish();
        });
    }

    private void passwordToggleListeners() {
        //mask current pass
        txtCurrentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(txtCurrentPassword.getText().length() > 0){
                    txtToggleCurrentPass.setVisibility(View.VISIBLE);
                } else{
                    txtToggleCurrentPass.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtToggleCurrentPass.setOnClickListener(view -> {
            if(txtToggleCurrentPass.getText() == "SHOW"){
                txtToggleCurrentPass.setText("HIDE");
                txtCurrentPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else{
                txtToggleCurrentPass.setText("SHOW");
                txtCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            txtCurrentPassword.setSelection(txtCurrentPassword.length());
        });


        //mask new password
        txtNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(txtNewPassword.getText().length() > 0){
                    txtToggleNewPass.setVisibility(View.VISIBLE);
                } else{
                    txtToggleNewPass.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtToggleNewPass.setOnClickListener(view -> {
            if(txtToggleNewPass.getText() == "SHOW"){
                txtToggleNewPass.setText("HIDE");
                txtNewPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else{
                txtToggleNewPass.setText("SHOW");
                txtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            txtNewPassword.setSelection(txtNewPassword.length());
        });

        //mask confirm pass
        txtConfirmNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(txtConfirmNewPassword.getText().length() > 0){
                    txtToggleConfirmPass.setVisibility(View.VISIBLE);
                } else{
                    txtToggleConfirmPass.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtToggleConfirmPass.setOnClickListener(view -> {
            if(txtToggleConfirmPass.getText() == "SHOW"){
                txtToggleConfirmPass.setText("HIDE");
                txtConfirmNewPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else{
                txtToggleConfirmPass.setText("SHOW");
                txtConfirmNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            txtConfirmNewPassword.setSelection(txtConfirmNewPassword.length());
        });
    }

    public void changePassword(){
        AuthCredential credential = EmailAuthProvider
                .getCredential(user.getEmail(), currentPassword);

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Log.d(TAG, "Password updated");

                                //reset values
                                currentPassword = "";
                                newPassword = "";
                                confirmNewPassword = "";

                                logHelper.saveToFirebase("changePassword", "SUCCESS", "Password Changed");
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(ChangePasswordActivity.this, "Password Changed", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
                                finish();
                            } else {
                                logHelper.saveToFirebase("changePassword", "ERROR", "Enter Valid New Password");

                                Log.d(TAG, "Error password not updated");
                                Toast.makeText(ChangePasswordActivity.this, "Enter Valid New Password", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        logHelper.saveToFirebase("changePassword", "ERROR", "Error auth failed");

                        Log.d(TAG, "Error auth failed");
                        Toast.makeText(ChangePasswordActivity.this, "Current Password Incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void forgotPassword() {
        startActivity(new Intent(ChangePasswordActivity.this, ForgotPasswordActivity.class));
    }

    private void goBack() {
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}