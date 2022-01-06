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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth mAuth;
    FirebaseUser user;
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 308;
    EditText txtEmailOrMobileNum, txtPassword;
    Button btnLogin;
    TextView txtToggle, txtForgotPassword;
    SignInButton btnGoogle;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef;

    GoogleSignInClient gsc;
    GoogleSignInOptions gso;

    private GoogleApiClient googleApiClient;
    private static final int SIGN_IN_REQUEST_CODE = 1000;

    
    @Override
    protected void onStart() {
        super.onStart();

        // check if user is already logged in
        user = mAuth.getCurrentUser();
        if(user != null) {
            // TODO: Home screen for already logged-in users
            //Intent intent = new Intent(getApplicationContext(), MenuActivity.java)
            //startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Declare authentication
        mAuth = FirebaseAuth.getInstance();
        createRequest();

        findViewById(R.id.btnLoginWithGoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLoginWithGoogle(v);
            }
        });

        txtEmailOrMobileNum = findViewById(R.id.txtEmailOrMobileNum);
        txtPassword = findViewById(R.id.txtCurrentPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtToggle = findViewById(R.id.txtToggle);
        txtForgotPassword = findViewById(R.id.lblForgotPassword);

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

        // Forgot Password button
        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go to the necessary screen
                Intent resetPasswordIntent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(resetPasswordIntent);
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

    private void updateUI(FirebaseUser user){
        Toast.makeText(getApplicationContext(), "Update UI", Toast.LENGTH_SHORT).show();
        if(user!=null){
            //Login with Google Successful, There's an account present
            //setContentView(R.layout.activity_landing);
            //Toast.makeText(getApplicationContext(), "User: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();


            //TODO: User is sent to landing activity directly if account already exists
            docRef = db.collection("users").document(user.getUid());

            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()) {
                        String lastName = documentSnapshot.getString("LastName");
                        String firstName = documentSnapshot.getString("FirstName");
                        String sex = documentSnapshot.getString("Sex");
                        String currentMobileNumber = documentSnapshot.getString("MobileNumber");

                        if(lastName != "" && firstName != "" && sex != "" && currentMobileNumber != "") { //Account is complete in db
                            Intent landingPage = new Intent (MainActivity.this, LandingActivity.class);
                            startActivity(landingPage);
                        } else { // Account needs to be completed
                            Intent completeGoogleRegistration = new Intent(MainActivity.this, RegisterGoogleActivity.class);
                            startActivity(completeGoogleRegistration);
                        }

                    }
                }
            });
        }
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
        Intent intent = new Intent (this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(getApplicationContext(), "On Activity Result", Toast.LENGTH_SHORT).show();

        //The real Google SignIn method. idk what the if-statement above does.
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Toast.makeText(getApplicationContext(), "123", Toast.LENGTH_SHORT).show();
            try {
                Toast.makeText(getApplicationContext(), "Try", Toast.LENGTH_SHORT).show();
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_SHORT).show();
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithCredential:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);

                                    //Display Google Data
                                    Toast.makeText(getApplicationContext(), "Login With Google Success: " + user.getDisplayName(), Toast.LENGTH_SHORT).show();

                                    //Put Google Data in Firestore
                                    GoogleSignInAccount account;
                                    account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

                                    Map<String, Object> docUsers = new HashMap<>();
                                    String email = user.getEmail();
                                    docUsers.put("ID", user.getUid());
                                    docUsers.put("isVerified", false);
                                    docUsers.put("LastName", account.getFamilyName());
                                    docUsers.put("FirstName", account.getGivenName());
                                    docUsers.put("MiddleName", "");
                                    docUsers.put("Sex", "");
                                    docUsers.put("BirthDate", "");
                                    docUsers.put("MobileNumber", "");
                                    docUsers.put("Email", account.getEmail()); // the only thing we get from the db for RegisterGoogleActivity

                                    db.collection("users").document(user.getUid()).set(docUsers)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(getApplicationContext(), "Successful db input", Toast.LENGTH_SHORT).show();
                                                    //Log.d(TAG, "DocumentSnapshot successfully written!");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(), "Unsuccessful", Toast.LENGTH_SHORT).show();
                                                    //Log.w(TAG, "Error writing document", e);
                                                }
                                            });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                                    //Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                                    updateUI(null);
                                    Toast.makeText(getApplicationContext(), "Login With Google Failed", Toast.LENGTH_SHORT).show();
                                }

                                // ...
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

    private void completeProfile() { // currently unused
        //String email = task.getResult().getString("Email");

        Intent completeGoogleRegistration = new Intent(this, RegisterGoogleActivity.class);
        startActivity(completeGoogleRegistration);
    }
}