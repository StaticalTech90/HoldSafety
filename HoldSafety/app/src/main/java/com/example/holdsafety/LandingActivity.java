package com.example.holdsafety;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LandingActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    Button btnSafetyButton;
    TextView seconds, description;
    private int timer;
    long remainTime;

    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int LOCATION_REQ_CODE = 1000;
    private static final int GPS_REQ_CODE = 1001;
    private static final int SEND_SMS_REQ_CODE = 1002;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        mAuth = FirebaseAuth.getInstance();

        Intent intent = new Intent(LandingActivity.this, RecordingCountdownActivity.class);

        //FLPC DECLARATION
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        seconds = findViewById(R.id.countdown);
        btnSafetyButton = findViewById(R.id.btnSafetyButton);

        //handle method for holdsafety button
        btnSafetyButton.setOnTouchListener(new View.OnTouchListener() {

            //Declare timer instance
            CountDownTimer cTimer = new CountDownTimer(2000, 1000) {

                //wait for timer to countdown
                public void onTick(long millisUntilFinished) {
                    remainTime = millisUntilFinished / 1000;
                    seconds.setText(Long.toString(remainTime));
                }

                //timer executes this code once finished
                public void onFinish() {
                    //Toast.makeText(getApplicationContext(), "2 seconds finished", Toast.LENGTH_SHORT).show();
                    getCurrentLocation();
                    //startActivity(intent);
                }
            };

            private long firstTouchTS = 0;

            //onTouch handles the long press events (press -> cancel; hold<2 -> cancel; hold>2 -> proceed)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //if button pressed down
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    this.firstTouchTS = System.currentTimeMillis();
                    //start
                    cTimer.start();
                } else if (event.getAction() == MotionEvent.ACTION_UP) { //button released
                    int timer = (int) (System.currentTimeMillis() - this.firstTouchTS) / 1000; //2 second timer

                    //for debug
                    //Toast.makeText(getApplicationContext(), "Time Pressed: " + timer, Toast.LENGTH_SHORT).show();

                    //cancels countdown; invalidates startActivity
                    cTimer.cancel();

                    //reset seconds
                    seconds.setText(Long.toString(remainTime));
                }
                return false;
            }
        });
    }

    public void menuRedirect(View view) {
        startActivity(new Intent(LandingActivity.this, MenuActivity.class));
    }

    //not working right now
    //start timer function
//    public void startTimer(CountDownTimer cTimer) {
//        seconds = findViewById(R.id.countdown); //kunin seconds
//
//        //make initialize the thing
//        cTimer = new CountDownTimer(2000, 1000) {
//            public void onTick(long millisUntilFinished) {
//                long remainTime = millisUntilFinished/1000;
//                seconds.setText(Long.toString(remainTime));
//            }
//            public void onFinish() {
//                //Toast.makeText(getApplicationContext(), "2 seconds finished", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getApplicationContext(), RecordingCountdownActivity.class);
//                startActivity(intent);
//            }
//        };
//
//        //start of timer
//        cTimer.start();
//    }

    //cancel timer
    public void cancelTimer(CountDownTimer cTimer) {
        if (cTimer != null) {
            Toast.makeText(getApplicationContext(), "Cancel", Toast.LENGTH_SHORT).show();
            cTimer.cancel();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            //DENIED LOCATION PERMISSION
            Log.d("location permission", "Please Grant Location Permission");
            //SHOW PERMISSION
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQ_CODE);
            return;
        }
//        else if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
//            //DENIED LOCATION PERMISSION
//            Log.d("location permission", "Please Grant SMS Permission");
//            //SHOW PERMISSION
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.SEND_SMS},
//                    SEND_SMS_REQ_CODE);
//            return;
//        }

//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "Please Grant Location Permission", Toast.LENGTH_SHORT).show();
//            return;
//        }

        //PERMISSION GRANTED
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(task -> {

                    Location location = task.getResult();
                    if (location == null) {
                        //TODO DISPLAY GPS DIALOG
                        showGPSDialog();

                    } else {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                        try {
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 2);
                            String address = addresses.get(1).getAddressLine(0);

                            Toast.makeText(this, location.getLatitude() + "," + location.getLatitude(), Toast.LENGTH_SHORT).show();

                            getEstablishmentsLocations(location, address);
                            //TODO COMPARE LOCATION TO CONTACTS
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQ_CODE) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //DENIED ONCE
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQ_CODE);
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
//            else {
//                Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                Uri uri = Uri.fromParts("package", getPackageName(), null);
//                settingsIntent.setData(uri);
//                startActivity(settingsIntent);
//                Toast.makeText(this, "Please Grant Location Permission.", Toast.LENGTH_SHORT).show();
//            }

        }
//        else if (requestCode == SEND_SMS_REQ_CODE) {
//
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
//                //DENIED ONCE
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.SEND_SMS},
//                        SEND_SMS_REQ_CODE);
//            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
//                    == PackageManager.PERMISSION_GRANTED) {
//                getCurrentLocation();
//            } else {
//                Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                Uri uri = Uri.fromParts("package", getPackageName(), null);
//                settingsIntent.setData(uri);
//                startActivity(settingsIntent);
//                Toast.makeText(this, "Please Grant SMS Permission.", Toast.LENGTH_SHORT).show();
//            }
//
//        }
    }

    private void showGPSDialog() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> locationSettingsResponseTask = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        locationSettingsResponseTask.addOnCompleteListener(
                task -> {

                    try {
                        LocationSettingsResponse locationSettingsResponse = task.getResult(ApiException.class);
                        getCurrentLocation();
                    } catch (ApiException e) {

                        switch (e.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                ResolvableApiException exception = (ResolvableApiException) e;
                                try {
                                    exception.startResolutionForResult(LandingActivity.this, GPS_REQ_CODE);
                                } catch (IntentSender.SendIntentException sendIntentException) {
                                    sendIntentException.printStackTrace();
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Toast.makeText(LandingActivity.this, "Settings is not available", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_REQ_CODE) {

            if (resultCode == RESULT_OK) {
                //USER PRESSED OK
                getCurrentLocation();

            } else {
                //USER PRESSED NO THANKS
                showGPSDialog();
            }

        }

    }

    private void getEstablishmentsLocations(Location location, String address) {
        //GET ESTABLISHMENTS LOCATIONS
        FirebaseFirestore.getInstance()
                .collection("barangay")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        HashMap<String, Object> hashBrgys = new HashMap<>();

                        for (QueryDocumentSnapshot brgySnap : task.getResult()) {

                            HashMap<String, Object> brgySnapHash = new HashMap<>();

                            String brgyID = brgySnap.getId();

                            String brgyName = brgySnap.getString("Barangay");
                            String city = brgySnap.getString("City");
                            String email = brgySnap.getString("Email");
                            String lat = brgySnap.getString("Latitude");
                            String lon = brgySnap.getString("Longitude");
                            String brgyMNumber = brgySnap.getString("MobileNumber");

                            //COMPARE BRGY LAT & LON TO USER
                            if (lat != null && lon != null) {
                                float[] results = new float[1];
                                Location.distanceBetween(
                                        location.getLatitude(), location.getLongitude(),
                                        Double.parseDouble(lat), Double.parseDouble(lon), results);
                                //FLOAT[] WILL RETURN DISTANCE IN METER
                                float distance = results[0];

                                //ADD TO HASHMAP
                                brgySnapHash.put("brgySnap", brgySnap);
                                brgySnapHash.put("distance", distance);
                                //ADD TO ALL BRGY SNAP
                                hashBrgys.put(brgyID, brgySnapHash);
                            }

                        }

                        //COMPARE DISTANCES BETWEEN BARANGAYS
                        QueryDocumentSnapshot nearestBrgySnap = null;
                        float nearestDistance = 0;
                        for (String key : hashBrgys.keySet()) {

                            HashMap<String, Object> hashDistances = (HashMap<String, Object>) hashBrgys.get(key);
                            if (hashDistances != null) {

                                QueryDocumentSnapshot brgySnap = (QueryDocumentSnapshot) hashDistances.get("brgySnap");
                                float distance = (float) hashDistances.get("distance");

                                if(nearestDistance == 0){
                                    //FIRST KEY
                                    nearestDistance = distance;
                                    nearestBrgySnap = brgySnap;
                                } else if(distance < nearestDistance){
                                    nearestDistance = distance;
                                    nearestBrgySnap = brgySnap;
                                }

                            }
                        }

                        Toast.makeText(LandingActivity.this, "" + nearestBrgySnap.getString("Barangay"), Toast.LENGTH_SHORT).show();
                        //sendLocationToContacts(location, address);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

    }
}