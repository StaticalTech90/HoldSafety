package com.example.holdsafety;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    FirebaseFirestore db;

    LinearLayout reportView;
    String reportID, location, date, barangay, evidence;
    Boolean isCoordinated;
//    RecyclerView recyclerViewReports;
//    String[] reportID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        db = FirebaseFirestore.getInstance();

        reportView = findViewById(R.id.linearReportList);

        listMyReports();
    }

    public void listMyReports() {
        db.collection("reportUser").document(userID).collection("reportDetails").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                for(QueryDocumentSnapshot reportSnap : task.getResult()) {

                    reportID = reportSnap.getId();
                    Toast.makeText(this, reportID, Toast.LENGTH_SHORT).show();
                    location = reportSnap.getString("Lat") + ", " + reportSnap.getString("Lon");
                    date = reportSnap.getString("Report Date");
                    barangay = reportSnap.getString("Barangay");
                    //currently hardcoded values
                    isCoordinated = false;
                    evidence = reportSnap.getString("Video Link");

                    View displayReportView = getLayoutInflater().inflate(R.layout.report_row, null, false);

                    TextView txtReportID = displayReportView.findViewById(R.id.txtReportID);
                    TextView txtReportLocation = displayReportView.findViewById(R.id.txtReportLocation);
                    TextView txtDateAndTime = displayReportView.findViewById(R.id.txtDateAndTime);
                    TextView txtBarangay = displayReportView.findViewById(R.id.txtBarangay);
                    TextView txtCoordinated = displayReportView.findViewById(R.id.txtCoordinated);
                    TextView txtEvidence = displayReportView.findViewById(R.id.txtEvidence);

                    txtReportID.setText(reportID);
                    txtReportLocation.setText(location);
                    txtDateAndTime.setText(date);
                    txtBarangay.setText(barangay);
                    txtCoordinated.setText(isCoordinated.toString());
                    txtEvidence.setText(evidence);

                    reportView.addView(displayReportView);

                }
            }
        });
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