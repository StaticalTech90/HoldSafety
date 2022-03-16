package com.example.holdsafety;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    String userID;
    FirebaseFirestore db;

    ImageView btnBack;
    LinearLayout reportView;
    String reportID, location, date, barangay, evidence;
    String longitude, latitude;
    TextView lblReportsCount, lblNoReports;
    int count;

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
        lblReportsCount = findViewById(R.id.lblNumOfReports);
        lblNoReports = findViewById(R.id.lblNoReports);
        btnBack = findViewById(R.id.backArrow);

        btnBack.setOnClickListener(view -> goBack());

        listMyReports();
    }

    public void listMyReports() {
        db.collection("reports").whereEqualTo("User ID", userID).orderBy("Report Date", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for(QueryDocumentSnapshot reportSnap : task.getResult()) {
                            reportID = reportSnap.getId();
                            count++;
                            //Toast.makeText(ReportsActivity.this, reportID, Toast.LENGTH_SHORT).show();

                            latitude = reportSnap.getString("Lat");
                            longitude = reportSnap.getString("Lon");
                            location = getGeoLoc(latitude, longitude);
                            date = reportSnap.getTimestamp("Report Date").toDate().toString();
                            barangay = reportSnap.getString("Nearest Barangay");
                            evidence = reportSnap.getString("Evidence");

                            View displayReportView = getLayoutInflater().inflate(R.layout.report_row, null, false);

                            TextView txtReportID = displayReportView.findViewById(R.id.txtReportID);
                            TextView txtReportLocation = displayReportView.findViewById(R.id.txtReportLocation);
                            TextView txtDateAndTime = displayReportView.findViewById(R.id.txtDateAndTime);
                            TextView txtBarangay = displayReportView.findViewById(R.id.txtBarangay);
                            TextView txtEvidence = displayReportView.findViewById(R.id.txtEvidence);

                        txtReportID.setText(reportID);
                        txtReportLocation.setText(location.trim());
                        txtDateAndTime.setText(date);
                        txtBarangay.setText(barangay);
                        txtEvidence.setText(evidence);

                            reportView.addView(displayReportView);
                        }
                        lblReportsCount.setText("Number of Reports: " + count);

                        if(count==0){
                            lblNoReports.setVisibility(View.VISIBLE);
                        } else {
                            lblNoReports.setVisibility(View.GONE);
                        }
                    } else {
                        Log.w("Reports", "Error getting documents: ", task.getException());
                    }
                });
    }

    public String getGeoLoc(String latitude, String longitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        double doubleLat = Double.parseDouble(latitude.trim());
        double doubleLong = Double.parseDouble(longitude.trim());
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

    private void goBack(){
        finish();
    }
}