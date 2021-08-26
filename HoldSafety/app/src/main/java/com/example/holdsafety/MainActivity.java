package com.example.holdsafety;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    EditText txtEmailOrMobileNum, txtPassword;
    Button btnLogin;
    FirebaseAuth fAuth;
    TextView txtToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtEmailOrMobileNum = findViewById(R.id.txtEmailOrMobileNum);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtToggle = findViewById(R.id.txtToggle);

        txtToggle.setVisibility(View.GONE);
        txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(txtPassword.getText().length() > 0){
                    txtToggle.setVisibility(View.VISIBLE);
                }
                else{
                    txtToggle.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtToggle.getText() == "SHOW"){
                    txtToggle.setText("HIDE");
                    txtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                else{
                    txtToggle.setText("SHOW");
                    txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                txtPassword.setSelection(txtPassword.length());
            }
        });

        fAuth = FirebaseAuth.getInstance();

        //redirects user to landing page if already logged in
        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;

                email = txtEmailOrMobileNum.getText().toString().trim();
                password = txtPassword.getText().toString();

                if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
                    txtEmailOrMobileNum.setError("Email is required");
                    txtPassword.setError("Password is required");
                    return;
                }

                if(TextUtils.isEmpty(email)){
                    txtEmailOrMobileNum.setError("Email is required");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    txtPassword.setError("Password is required");
                    return;
                }

                loginUser(email,password);
            }
        });
    }

    public void loginUser(String email,String password){
        //authentication
        //wont work idk why
        fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, MenuActivity.class));
                    finish();
                }
                else{
                    Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void userSignUp(View view){
        Intent intent = new Intent (this, Register1Activity.class);
        startActivity(intent);
    }
}