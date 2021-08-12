package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

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