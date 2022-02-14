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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;

    private final static int RC_SIGN_IN = 308;
    EditText txtEmailOrMobileNum, txtPassword;
    Button btnLogin;
    TextView txtToggle;
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
        //Log.d("userSnap", user.getEmail());
        if(user != null) {
            //determineNextActivity(user.getUid(), user.getEmail());
            Intent intent = new Intent(LoginActivity.this, LandingActivity.class);
            isFromWidget = getIntent().getStringExtra("isFromWidget");

            //handle method for holdsafety widget
            if(isFromWidget != null && isFromWidget.equals("true")) {
                intent.putExtra("isFromWidget", "true");
                Toast.makeText(getApplicationContext(), "Inside IF Widget" , Toast.LENGTH_SHORT).show();
            }
            startActivity(intent);
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
        createRequest();

        txtEmailOrMobileNum = findViewById(R.id.txtEmailOrMobileNum);
        txtPassword = findViewById(R.id.txtCurrentPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtToggle = findViewById(R.id.txtToggle);

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
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gsc.signOut();
                //Display list of google accounts
                Intent GoogleSignIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(GoogleSignIntent, SIGN_IN_REQUEST_CODE);
            }
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(txtPassword.getText().length() > 0) {
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

        txtToggle.setOnClickListener(view -> {
            if(txtToggle.getText() == "SHOW") {
                txtToggle.setText("HIDE");
                txtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
            else{
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
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            colRef.document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
               if(documentSnapshot.exists()) {
                   Boolean isProfileComplete = documentSnapshot.getBoolean("profileComplete");

                   if(isProfileComplete) { //ACCOUNT EXISTS AND COMPLETE
                       Log.d("isAccountComplete", "updateUI(): if(): Result: Profile Complete");
                       Intent landingPage = new Intent (LoginActivity.this, LandingActivity.class);
                       startActivity(landingPage);
                   } else { //ACCOUNT EXISTS, BUT INCOMPLETE
                       Log.d("updateUI:elseif()", "user data: " + user.getEmail());
                       Log.d("isAccountComplete", "updateUI(): else if(): Result: Profile exists, incomplete details");
                       completeGoogleProfile();
                   }
               } else { //ACCOUNT DOES NOT EXIST
                   Log.d("updateUI:elseif()", "user data: " + user.getEmail());
                   Log.d("isAccountComplete", "updateUI(): else if(): Result: Profile does not exist, creating...");
                   completeGoogleProfile();
               }

            });
            finish();
        }
    }

    //TODO: MAY KWENTA BA ITO?
    private void createRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
//        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
    }

    public void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mAuth = FirebaseAuth.getInstance();
                            FirebaseUser user = mAuth.getCurrentUser();

                            //pass user to check if it exists in user table
                            assert user != null;
                            checkUserAccount(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
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
            else {
                //account does not exist in users table = you are not a registered user/ you are an admin
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(LoginActivity.this, "You are registered as admin. Login via the ADMIN app.", Toast.LENGTH_LONG).show();
                finish();
                startActivity(getIntent());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getApplicationContext(), "On Activity Result", Toast.LENGTH_SHORT).show();

        //GOOGLE SIGN IN
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Task Successful", Toast.LENGTH_SHORT).show();
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("googlesignin", "signInWithCredential:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                assert user != null;
                                Log.d("googlesignin", "user acc: " + user.getEmail());

                                //Create a new account if it doesn't exist, otherwise continue
                                docRef = colRef.document(user.getUid());

                                docRef.get().addOnSuccessListener(documentSnapshot -> {
                                    if(!documentSnapshot.exists()) {
                                        //ADD KNOWN AND PLACEHOLDER VALUES
                                        Map < String, Object > docUsers = new HashMap<>();
                                        String email = user.getEmail();
                                        docUsers.put("ID", user.getUid());
                                        docUsers.put("isVerified", false);
                                        docUsers.put("LastName", account.getFamilyName());
                                        docUsers.put("FirstName", account.getGivenName());
                                        docUsers.put("MiddleName", "");
                                        docUsers.put("Sex", "");
                                        docUsers.put("BirthDate", "");
                                        docUsers.put("MobileNumber", "");
                                        docUsers.put("Email", account.getEmail());
                                        docUsers.put("profileComplete", false);

                                        //ADD TO DB
                                        db.collection("users").document(user.getUid()).set(docUsers)
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(getApplicationContext(), "Successful db input", Toast.LENGTH_SHORT).show();
                                                    //Log.d(TAG, "DocumentSnapshot successfully written!");
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(getApplicationContext(), "Unsuccessful", Toast.LENGTH_SHORT).show();
                                                    //Log.w(TAG, "Error writing document", e);
                                                });
                                        updateUI(user);
                                    } else { //ACCOUNT EXISTS, COMPLETE THE PROFILE
                                        colRef.document(user.getUid()).get().addOnSuccessListener(existingDocumentSnapshot -> {
                                            Boolean isComplete = documentSnapshot.getBoolean("profileComplete");

                                            while(isComplete == null) {
                                                isComplete = documentSnapshot.getBoolean("profileComplete");
                                            }

                                            if(!isComplete) { //IF PROFILE INCOMPLETE, FILL UP FORM
                                                Log.d("isAccountComplete", "onActivityResult(): else if(): Result: Account exists");
                                                completeGoogleProfile();
                                            } else { //PROFILE COMPLETE, GO TO LANDING
                                                Intent landingPage = new Intent (LoginActivity.this, LandingActivity.class);
                                                startActivity(landingPage);
                                            }
                                        });
                                    }
                                });
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getApplicationContext(), "Failed to sign in w/goog", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "signInWithCredential:failure", task1.getException());
                                //Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        });
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Catch", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public void othersRedirect(View view) {
        Intent intent = new Intent(getApplicationContext(), OthersActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Please check your internet connection.", Toast.LENGTH_LONG).show();
    }

    private void completeGoogleProfile() {
        Intent completeGoogleRegistration = new Intent(this, RegisterGoogleActivity.class);
        startActivity(completeGoogleRegistration);
    }

    public void forgotPassword(View view) {
        startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        finish();
    }

//    public void determineNextActivity(String uid, String email) {
//        colRef.get().addOnCompleteListener(task -> {
//            if(task.isSuccessful()) {
//                for(QueryDocumentSnapshot userSnap : task.getResult()) {
//                    if(userSnap.getString("Email").equals(email)) { //EMAIL IN DB
//                        Log.d("userSnap", "userSnap.getString = " + userSnap.getString("Email"));
//                        Log.d("userSnap", "user's email to match = " + email);
//                        Log.d("userSnap", "Are they equal? Answer: " + userSnap.getString("Email").equals(email));
//                        Boolean isComplete = userSnap.getBoolean("profileComplete");
//
//                        while(isComplete == null) {
//                            isComplete = userSnap.getBoolean("profileComplete");
//                        }
//
//                        if(isComplete != null) {
//                            if(isComplete) { //PROFILE IS COMPLETE, GO TO LANDING ON STARTUP
//                                Log.d("userSnap", "is profile complete? = " + userSnap.getBoolean("profileComplete"));
//                                Intent landingPage = new Intent (LoginActivity.this, LandingActivity.class);
//                                startActivity(landingPage);
//                                Log.d("userSnap", "isAccountComplete result inside if: " + isComplete);
//                            }
//                        }
//                    }
//                }
//            } else {
//                Log.d("isAccountComplete", "Failed to fetch in DB");
//            }
//        });
//    }

    public void userSignUp(View view) {
        Intent intent = new Intent (this, RegisterActivity.class);
        startActivity(intent);
    }
}