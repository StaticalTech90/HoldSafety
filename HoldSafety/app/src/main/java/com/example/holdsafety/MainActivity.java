package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 308;
    EditText txtEmailOrMobileNum, txtPassword;
    Button btnLogin;
    TextView txtToggle;
    
    @Override
    protected void onStart() {
        super.onStart();

        // check if user is already logged in
        user = mAuth.getCurrentUser();
        if(user != null) {
            // TODO Home screen for already logged-in users
            //Intent intent = new Intent(getApplicationContext(), MenuActivity.java)
            //startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        createRequest();

        findViewById(R.id.btnLoginWithGoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLoginWithGoogle(v);
            }
        });

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

        mAuth = FirebaseAuth.getInstance();

        //redirects user to landing page if already logged in
        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), LandingActivity.class));
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

    private void createRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
    }

    public void userLoginWithGoogle(View view) {
        Toast.makeText(getApplicationContext(), "Login With Google", Toast.LENGTH_LONG).show();

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityIfNeeded(signInIntent, RC_SIGN_IN);

    }

    public void loginUser(String email,String password){
        //authentication
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, LandingActivity.class));
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getApplicationContext(), "Google Sign in Failed.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // TODO - Screen change to RegisterGoogleActivity
                            Intent fillDetails = new Intent(getApplicationContext(), RegisterGoogleActivity.class);
                            fillDetails.putExtra("userID", user.getUid());
                            fillDetails.putExtra("userEmail", user.getEmail());

                            startActivity(fillDetails);
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Google Sign in Failed.", Toast.LENGTH_LONG).show();
                            //updateUI(null);
                        }
                    }
                });
    }
}