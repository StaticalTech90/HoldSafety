package com.example.holdsafety;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AccountDetailsActivity extends AppCompatActivity {
    public Uri imageURI;
    FirebaseUser user;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    public void uploadID(View view){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        //deprecated -> wont work
        //startActivityForResult(intent, 1);
    }

    public void accountUpdate(View view){
        //Toast.makeText(getApplicationContext(), "Account Update Attempt", Toast.LENGTH_LONG).show();
        Intent intent = new Intent (this, VerifyUserActivity.class);
        startActivity(intent);
    }

    public void userRegister(View view){
        Toast.makeText(getApplicationContext(), "Register", Toast.LENGTH_SHORT).show();
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

    public void removeAccount(View view){
        user = FirebaseAuth.getInstance().getCurrentUser();

        AlertDialog.Builder dialog;
        dialog = new AlertDialog.Builder(AccountDetailsActivity.this);
        dialog.setTitle("Remove Account");
        dialog.setMessage("Are you sure you want to delete your account? " +
                "Keep in mind that all information and files would be deleted from the system.");

        dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    db.collection("users").document(user.getUid()).delete();

                                    Toast.makeText(AccountDetailsActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(AccountDetailsActivity.this, MainActivity.class));
                                    finish();
                                }
                                else{
                                    Toast.makeText(AccountDetailsActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        dialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }
}