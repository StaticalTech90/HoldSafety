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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;
    LogHelper logHelper;
    EditText txtEmailOrMobileNum, txtPassword;
    Button btnLogin;
    TextView txtToggle, btnForgotPass, btnSignUp;
    ImageView btnMenu;
    SignInButton btnGoogle;
    String isFromWidget;

    FirebaseFirestore db;
    FirebaseUser user;
    DocumentReference docRef;

    GoogleSignInClient gsc;
    GoogleSignInOptions gso;

    CollectionReference colRef;

    private GoogleApiClient googleApiClient;
    private static final int SIGN_IN_REQUEST_CODE = 1000;

    protected void onStart() {
        super.onStart();

        //check if user is already logged in
        if(user != null) {
            Intent landing = new Intent(LoginActivity.this, LandingActivity.class);
            isFromWidget = getIntent().getStringExtra("isFromWidget");

            //handle method for holdsafety widget
            if(isFromWidget != null && isFromWidget.equals("true")) {
                isFromWidget = null;
                landing.putExtra("isFromWidget", "true");
            }

            startActivity(landing);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Declare authentication
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        colRef = db.collection("users");
        logHelper = new LogHelper(LoginActivity.this, mAuth, user,this);

        txtEmailOrMobileNum = findViewById(R.id.txtEmailOrMobileNum);
        txtPassword = findViewById(R.id.txtCurrentPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPass = findViewById(R.id.btnForgotPassword);
        btnSignUp = findViewById(R.id.lblRegister);
        txtToggle = findViewById(R.id.txtToggle);
        btnMenu = findViewById(R.id.menu);

        btnGoogle = findViewById(R.id.btnLoginWithGoogle);

        txtToggle.setVisibility(View.GONE);
        txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Configure Google Sign In
        gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("233680747912-m8q45hor79go5n8aqfkuneklnkshudqs.apps.googleusercontent.com")
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(this, gso);

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Google Sign In Button Onclick Listener
        btnGoogle.setOnClickListener(view -> {
            gsc.signOut();
            //Display list of google accounts
            Intent GoogleSignIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(GoogleSignIntent, SIGN_IN_REQUEST_CODE);
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(txtPassword.getText().length() > 0) {
                    txtToggle.setVisibility(View.VISIBLE);
                } else {
                    txtToggle.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        txtToggle.setOnClickListener(view -> {
            if(txtToggle.getText() == "SHOW") {
                txtToggle.setText("HIDE");
                txtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                txtToggle.setText("SHOW");
                txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            txtPassword.setSelection(txtPassword.length());
        });
        btnLogin.setOnClickListener(view -> {
            String email, password;

            email = txtEmailOrMobileNum.getText().toString().trim();
            password = txtPassword.getText().toString();

            if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                txtEmailOrMobileNum.setError("Email is required");
                txtPassword.setError("Password is required");
                return;
            }
            if(TextUtils.isEmpty(email)) {
                txtEmailOrMobileNum.setError("Email is required");
                return;
            }
            if(TextUtils.isEmpty(password)) {
                txtPassword.setError("Password is required");
                return;
            }

            loginUser(email,password);
        });
        btnForgotPass.setOnClickListener(view -> forgotPassword());
        btnSignUp.setOnClickListener(view -> userSignUp());
        btnMenu.setOnClickListener(view -> othersRedirect());
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            colRef.document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
               if(documentSnapshot.exists()) {
                   Boolean isProfileComplete = documentSnapshot.getBoolean("profileComplete");

                   if(isProfileComplete) { //ACCOUNT EXISTS AND COMPLETE
                       Intent landingPage = new Intent (LoginActivity.this, LandingActivity.class);
                       startActivity(landingPage);
                   } else { //ACCOUNT EXISTS, BUT INCOMPLETE
                       completeGoogleProfile();
                   }
               } else { //ACCOUNT DOES NOT EXIST
                   completeGoogleProfile();
               }
            });
            finish();
        }
    }

    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser user = mAuth.getCurrentUser();

                        //pass user to check if it exists in user table
                        assert user != null;

                        checkUserAccount(user);
                    } else {
                        Log.e("check user", "Account does not exist");
                        Toast.makeText(LoginActivity.this, "Account does not exist", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserAccount(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        docRef = db.collection("users").document(user.getUid());

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()) {
                Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, LandingActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("USER", data.toString());

        //GOOGLE SIGN IN
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, task1 -> {
                            if (task1.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i("googlesignin", "signInWithCredential:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                assert user != null;
                                Log.i("googlesignin", "user acc: " + user.getEmail());

                                //Create a new account if it doesn't exist, otherwise continue
                                docRef = colRef.document(user.getUid());

                                docRef.get().addOnSuccessListener(documentSnapshot -> {
                                    if(!documentSnapshot.exists()) {
                                        //Toast.makeText(getApplicationContext(), "Does not exist yet", Toast.LENGTH_SHORT).show();

                                        //ADD KNOWN AND PLACEHOLDER VALUES
                                        HashMap<String, String> googleSignInMap = new HashMap<>();
                                        googleSignInMap.put("lastName", account.getFamilyName());
                                        googleSignInMap.put("firstName", account.getGivenName());
                                        googleSignInMap.put("email", account.getEmail());

                                        Intent googleReg = new Intent(this, RegisterGoogleActivity.class);
                                        googleReg.putExtra("googleSignInMap", googleSignInMap);
                                        startActivity(googleReg);
                                    } else { //ACCOUNT EXISTS, COMPLETE THE PROFILE
                                        Intent landingPage = new Intent (LoginActivity.this, LandingActivity.class);
                                        startActivity(landingPage);
                                        finish();
                                    }
                                });
                            } else {
                                // If sign in fails, display a message to the user.

                                Toast.makeText(LoginActivity.this, "Google Sign In failed", Toast.LENGTH_SHORT).show();
                                Log.e("Credential Sign-in", "signInWithCredential:failure", task1.getException());
                                updateUI(null);
                            }
                        });
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Sign In Failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public void othersRedirect() {
        Intent others = new Intent(getApplicationContext(), OthersActivity.class);
        startActivity(others);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(LoginActivity.this, "Please check your internet connection.", Toast.LENGTH_LONG).show();
    }

    private void completeGoogleProfile() {
        Intent completeGoogleRegistration = new Intent(this, RegisterGoogleActivity.class);
        startActivity(completeGoogleRegistration);
    }

    public void forgotPassword() {
        Intent forgotPass = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(forgotPass);
    }

    public void userSignUp() {
        Intent register = new Intent (this, RegisterActivity.class);
        startActivity(register);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
