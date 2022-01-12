package com.example.holdsafety;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ReportsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    RecyclerView recyclerViewReports;
    String[] reportID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        recyclerViewReports = findViewById(R.id.recyclerviewReports);
        reportID = getResources().getStringArray(R.array.reportID);

        ReportAdapter reportAdapter = new ReportAdapter(this, reportID);
        recyclerViewReports.setAdapter(reportAdapter);
        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
    }


//    public void place(){
//        FirebaseFirestore.getInstance()
//                .collection("emergencyContacts")
//                .document(user.getUid()).collection("contacts").get()
//                .addOnCompleteListener(task -> {
//
//                    if (task.isSuccessful()) {
//                        //FOR EACH
//                        //GET ALL ID
//                        for (QueryDocumentSnapshot contactSnap : task.getResult()) {
//
//                            String brgyID = contactSnap.getId();
//                            //GET CONTACT DETAILS
//                            String email = contactSnap.getString("email");
//                            String firstName = contactSnap.getString("firstName");
//                            String lastName = contactSnap.getString("lastName");
//                            String mobileNumber = contactSnap.getString("mobileNumber");
//                            String relation = contactSnap.getString("relation");
//
//                            //DECLARE THE LAYOUT - CARDVIEW
//                            View detailsView = getLayoutInflater().inflate(R.layout.activity_select_contact, null, false);
//
//                            //DECLARE TEXTS AND BUTTONS
//                            TextView txtContactName = detailsView.findViewById(R.id.txtContactName);
//                            ImageView btnUpdate = detailsView.findViewById(R.id.updateContact);
//                            ImageView btnDelete = detailsView.findViewById(R.id.btnRemoveAccount);
//
//                            //SET TEXTS
//                            txtContactName.setText(firstName+" "+lastName);
//
//                            //SET DETAILS ONCLICK LISTENER
//                            btnUpdate.setOnClickListener(view -> {
//                                //SHOW DETAILS DIALOG
//                                Dialog dialog = new Dialog(BrgyLocationsActivity.this);
//                                View dialogView = getLayoutInflater().inflate(R.layout.layout_brgy_details, null, false);
//                                dialog.setContentView(dialogView);
//                                dialog.setCancelable(false);
//
//                                //DECLARE DIALOG VIEWS
//                                TextView txtDetailsLocation = dialogView.findViewById(R.id.txtDetailsLocation);
//                                Button btnOk = dialogView.findViewById(R.id.btnOk);
//
//                                //SET TEXT AND ON CLICK LISTENER
//                                txtDetailsLocation.setText(brgyLat + ", " + brgyLon);
//                                btnOk.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        //DISMISS DIALOG
//                                        dialog.dismiss();
//                                    }
//                                });
//
//                                dialog.show();
//
//                            });
//
//                            //ADD TO LINEAR
//                            linearLayoutBrgy.addView(detailsView);
//                        }
//
//                    } else {
//                        //NO DATA AVAILABLE
//                        Toast.makeText(this, "No Brgy. Available", Toast.LENGTH_SHORT).show();
//                    }
//
//                });
//    }
}