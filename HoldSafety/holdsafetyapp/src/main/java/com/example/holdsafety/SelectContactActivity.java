package com.example.holdsafety;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SelectContactActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;

    ImageView btnBack;
    LinearLayout contactsView;
    String[] contactName;
    TextView lblContactCount, lblNoContacts;
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        contactsView = findViewById(R.id.linearContactList);

        ContactAdapter contactAdapter = new ContactAdapter(this, contactName);
        lblContactCount = findViewById(R.id.lblNumOfContacts);
        lblNoContacts = findViewById(R.id.lblNoContactsDesc);
        btnBack = findViewById(R.id.backArrow);

        btnBack.setOnClickListener(view -> goBack());

        getContacts();
        getContactCount();
    }

    @SuppressLint("SetTextI18n")
    private void getContacts() {
        FirebaseFirestore.getInstance()
                .collection("emergencyContacts")
                .document(user.getUid()).collection("contacts").get()
                .addOnCompleteListener(task -> {
                    Intent updateContact = new Intent(this, UpdateContactActivity.class);
                    if (task.isSuccessful()) {
                        //FOR EACH
                        //GET ALL ID
                        for (QueryDocumentSnapshot contactSnap : task.getResult()) {
                            //GET CONTACT DETAILS
                            String email = contactSnap.getString("email");
                            String firstName = contactSnap.getString("firstName");
                            String lastName = contactSnap.getString("lastName");
                            String mobileNumber = contactSnap.getString("mobileNumber");
                            String relation = contactSnap.getString("relation");
                            String documentId = contactSnap.getId();

                            //DECLARE THE LAYOUT - CARDVIEW
                            View detailsView = getLayoutInflater().inflate(R.layout.contact_row, null, false);

                            //DECLARE TEXTS AND BUTTONS
                            TextView txtContactName = detailsView.findViewById(R.id.txtContactName);
                            ImageView btnUpdate = detailsView.findViewById(R.id.updateContact);
                            ImageView btnDelete = detailsView.findViewById(R.id.btnRemoveAccount);

                            //SET TEXTS
                            txtContactName.setText(firstName+" "+lastName);

                            //SET DETAILS ONCLICK LISTENER
                            btnUpdate.setOnClickListener(view -> {
                                Toast.makeText(this, email, Toast.LENGTH_SHORT).show();
                                //PREPARE DATA TO PASS
                                updateContact.putExtra("email", email);
                                updateContact.putExtra("firstName", firstName);
                                updateContact.putExtra("lastName", lastName);
                                updateContact.putExtra("mobileNumber", mobileNumber);
                                updateContact.putExtra("relation", relation);
                                updateContact.putExtra("documentId", documentId);

                                startActivity(updateContact);
                            });

                            btnDelete.setOnClickListener(view -> {
                                AlertDialog.Builder dialogRemoveAccount;
                                dialogRemoveAccount = new AlertDialog.Builder(SelectContactActivity.this);
                                dialogRemoveAccount.setTitle("Confirm Deletion");
                                dialogRemoveAccount.setMessage("Are you sure you want to delete this contact?");

                                dialogRemoveAccount.setPositiveButton("Delete", (dialog, which) -> {
                                        db.collection("emergencyContacts")
                                                .document(user.getUid())
                                                .collection("contacts")
                                                .document(documentId)
                                                .delete();
                                        //Reload Activity After deleting contact
                                        finish();
                                        startActivity(getIntent());
                                });

                                dialogRemoveAccount.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                                AlertDialog alertDialog = dialogRemoveAccount.create();
                                alertDialog.show();
                            });
                            //ADD TO LINEAR
                            contactsView.addView(detailsView);
                        }
                    } else {
                        //NO DATA AVAILABLE
                        Toast.makeText(this, "No Contacts Available", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getContactCount(){
        FirebaseFirestore.getInstance()
                .collection("emergencyContacts")
                .document(user.getUid()).collection("contacts").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot contactSnap : task.getResult()) {
                            count++;
                        }
                        lblContactCount.setText("Number of Contacts: "+ count + "/5");

                        if(count==0) {
                            lblNoContacts.setVisibility(View.VISIBLE);
                        } else {
                            lblNoContacts.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(this, "No Contacts Available", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goBack(){
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}