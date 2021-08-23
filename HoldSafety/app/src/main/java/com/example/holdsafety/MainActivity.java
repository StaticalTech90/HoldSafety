package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
<<<<<<< HEAD
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){
            setContentView(R.layout.activity_account_details);
        }
=======
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//
//        if(currentUser!=null){
//            setContentView(R.layout.activity_account_details);
//        }
>>>>>>> a0e652d2cdcb3b2e96daa42d9046600539dc7d1b
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void userLogin(View view){
        Toast.makeText(getApplicationContext(), "Login", Toast.LENGTH_LONG).show();
    }

    public void userLoginWithGoogle(View view){
        Toast.makeText(getApplicationContext(), "Login With Google", Toast.LENGTH_LONG).show();
    }

    public void userSignUp(View view){
        Intent intent = new Intent (this, Register1Activity.class);
        startActivity(intent);
    }




}