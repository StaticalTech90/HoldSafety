package com.example.holdsafety;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    String userID;
    FirebaseFirestore db;

    LinearLayout reportView;
    String reportID, location, date, barangay, evidence;
    String longitude, latitude;
//    RecyclerView recyclerViewReports;
//    String[] reportID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        assert user != null;
        userID = user.getUid();
        db = FirebaseFirestore.getInstance();

        reportView = findViewById(R.id.linearReportList);

        //getGeoLoc();
        listMyReports();
    }

    public void listMyReports() {
        db.collection("reports").whereEqualTo("User ID", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for(QueryDocumentSnapshot reportSnap : task.getResult()) {
                        reportID = reportSnap.getId();
                        //Toast.makeText(ReportsActivity.this, reportID, Toast.LENGTH_SHORT).show();

                        latitude = reportSnap.getString("Lat");
                        longitude = reportSnap.getString("Lon");
                        location = getGeoLoc(latitude, longitude);
                        date = reportSnap.getString("Report Date");
                        barangay = reportSnap.getString("Barangay");
                        evidence = reportSnap.getString("Evidence");

                        View displayReportView = getLayoutInflater().inflate(R.layout.report_row, null, false);

                        TextView txtReportID = displayReportView.findViewById(R.id.txtReportID);
                        TextView txtReportLocation = displayReportView.findViewById(R.id.txtReportLocation);
                        TextView txtDateAndTime = displayReportView.findViewById(R.id.txtDateAndTime);
                        TextView txtBarangay = displayReportView.findViewById(R.id.txtBarangay);
                        TextView txtEvidence = displayReportView.findViewById(R.id.txtEvidence);

                        txtReportID.setText(reportID);
                        txtReportLocation.setText(location);
                        txtDateAndTime.setText(date);
                        txtBarangay.setText(barangay);
                        txtEvidence.setText(evidence);

                        reportView.addView(displayReportView);
                    }
                } else {
                    Log.w("Reports", "Error getting documents: ", task.getException());
                }
            }
        });

        /*
        db.collection("reportUser").document(userID).collection("reportDetails").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                for(QueryDocumentSnapshot reportSnap : task.getResult()) {
                    reportID = reportSnap.getId();
                    Toast.makeText(this, reportID, Toast.LENGTH_SHORT).show();

                    latitude = reportSnap.getString("Lat");
                    longitude = reportSnap.getString("Lon");
                    location = getGeoLoc(latitude, longitude);
                    date = reportSnap.getString("Report Date");
                    barangay = reportSnap.getString("Barangay");
                    evidence = reportSnap.getString("Evidence");



                    View displayReportView = getLayoutInflater().inflate(R.layout.report_row, null, false);

                    TextView txtReportID = displayReportView.findViewById(R.id.txtReportID);
                    TextView txtReportLocation = displayReportView.findViewById(R.id.txtReportLocation);
                    TextView txtDateAndTime = displayReportView.findViewById(R.id.txtDateAndTime);
                    TextView txtBarangay = displayReportView.findViewById(R.id.txtBarangay);
                    TextView txtEvidence = displayReportView.findViewById(R.id.txtEvidence);

                    txtReportID.setText(reportID);
                    txtReportLocation.setText(location);
                    txtDateAndTime.setText(date);
                    txtBarangay.setText(barangay);
                    txtEvidence.setText(evidence);

                    reportView.addView(displayReportView);
                }
            }
        });
        */
    }

    public String getGeoLoc(String latitude, String longitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Double doubleLat = Double.parseDouble(latitude.trim());
        Double doubleLong = Double.parseDouble(longitude.trim());
        try {
            List<Address> addresses = geocoder.getFromLocation(doubleLat, doubleLong, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("User Report Address", strReturnedAddress.toString());
            } else {
                Log.w("User Report Address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("User Report Address", "Cannot get Address!");
        }
        return strAdd;
        //textViewBrgyAddress.setText(strAdd);
        //Toast.makeText(getApplicationContext(), "Address: " + strAdd, Toast.LENGTH_SHORT).show();

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