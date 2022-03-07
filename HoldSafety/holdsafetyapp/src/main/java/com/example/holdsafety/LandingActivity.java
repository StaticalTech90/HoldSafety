package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.ImageView;
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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class LandingActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    DocumentReference docRef;
    CollectionReference docRefBrgy;
    FirebaseUser user;

    private static final String CAPABILITY_WATCH_APP = "send_signal";
    String userID, isFromWidget, nearestBrgy;
    String lastName, firstName, mobileNumber, googleMapLink, txtMessage, emailMessage;
    Boolean isVerified;
    String brgyName, brgyCity, brgyEmail, brgyMobileNumber;
    String coordsLon, coordsLat;
    String reportID;

    Button btnSafetyButton;
    ImageView btnBluetooth, btnMenu;
    TextView seconds, description;
    private int timer;
    long remainTime;
    Map<String, Object> docDetails = new HashMap<>(); // for reports in db
    HashMap<String, String> evidenceLinkRequirements = new HashMap<>(); // for vid in db

    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int LOCATION_REQ_CODE = 1000;
    private static final int GPS_REQ_CODE = 1001;
    private static final int SEND_SMS_REQ_CODE = 1002;
    private final int CAMERA_REQ_CODE = 1003;
    private final int AUDIO_REQ_CODE = 1004;
    private final int STORAGE_WRITE_REQ_CODE = 1005;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        //Get logged in user
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("users").document(userID);
        docRefBrgy = db.collection("barangay");

        isFromWidget = getIntent().getStringExtra("isFromWidget");

        //Receive message
        Wearable.getMessageClient(LandingActivity.this).addListener(messageEvent -> {
            byte compareID = 1;
            int result = Byte.compare(messageEvent.getData()[0], compareID);

            Log.d("SIGNAL", "message:  " + messageEvent.getData()[0]);

            if(result == 0) {
                Log.d("SIGNAL", "INSERT REPORT METHOD HERE");
                getCurrentLocation();
            } else {
                Log.d("SIGNAL", "FUCK");
            }
        });

        //Get available nodes
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(LandingActivity.this)
                .getCapability(CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE);

        capabilityInfoTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                CapabilityInfo capabilityInfo = task.getResult();
                Set<Node> nodes = capabilityInfo.getNodes();
            } else {
                Log.d("SIGNAL", "Capability request failed to return any results.");
            }

        });

        //wifi and loc status
        statusCheck();

        //FLPC DECLARATION
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        seconds = findViewById(R.id.countdown);
        description = findViewById(R.id.description);

        btnSafetyButton = findViewById(R.id.btnSafetyButton);
        btnBluetooth = findViewById(R.id.btnBluetooth);
        btnMenu = findViewById(R.id.btnMenu);

        btnSafetyButton.setOnTouchListener(new View.OnTouchListener() {
            //Declare timer instance
            CountDownTimer cTimer = new CountDownTimer(2000, 1000) {

                //wait for timer to countdown
                public void onTick(long millisUntilFinished) {
                    remainTime = millisUntilFinished / 1000;
                    description.setVisibility(View.INVISIBLE);
                    seconds.setVisibility(View.VISIBLE);
                    seconds.setText(Long.toString(remainTime+1));
                }

                //timer executes this code once finished
                public void onFinish() {
                    getCurrentLocation();
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
                    description.setVisibility(View.VISIBLE);
                    seconds.setVisibility(View.INVISIBLE);

                    //cancels countdown; invalidates startActivity
                    cTimer.cancel();

                    //reset seconds
                    seconds.setText(Long.toString(timer));
                }
                return false;
            }
        });

        //handle method for holdsafety widget
        if(isFromWidget!=null && isFromWidget.equals("true")){
            getCurrentLocation();
        }

        btnBluetooth.setOnClickListener(v -> setUpBluetooth());
        btnMenu.setOnClickListener(v -> menuRedirect());
    }

    public void setUpBluetooth() {
        //TODO: bluetooth connection
    }

    public void menuRedirect() {
        startActivity(new Intent(LandingActivity.this, MenuActivity.class));
    }

    //cancel timer
    public void cancelTimer(CountDownTimer cTimer) {
        if (cTimer != null) {
            cTimer.cancel();
        }
    }

    //checks required permissions onStart()
    private void setPermissions(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            //DENIED LOCATION PERMISSION
            Log.d("location permission", "Please Grant Location Permission");
            //SHOW PERMISSION
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQ_CODE);
        } else if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
            //DENIED LOCATION PERMISSION
            Log.d("location permission", "Please Grant SMS Permission");
            //SHOW PERMISSION
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SEND_SMS_REQ_CODE);
        } else if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
            //DENIED AUDIO PERMISSION
            Log.d("audio permission", "Please Grant Audio Permission");
            //SHOW PERMISSION
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_REQ_CODE);
        } else if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            //DENIED STORAGE PERMISSION
            Log.d("storage permission", "Please Grant Storage Permission");
            //SHOW PERMISSION
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_WRITE_REQ_CODE);
        } else if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ){
            //DENIED CAMERA PERMISSION
            Log.d("camera permission", "Please Grant Camera Permission");
            //SHOW PERMISSION
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.CAMERA}, CAMERA_REQ_CODE);
        }
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
                setPermissions();
            }
            else {
                Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                settingsIntent.setData(uri);
                startActivity(settingsIntent);
                Toast.makeText(this, "Please Grant Location Permission.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SEND_SMS_REQ_CODE) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                //DENIED ONCE
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        SEND_SMS_REQ_CODE);
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                setPermissions();
            } else {
                Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                settingsIntent.setData(uri);
                startActivity(settingsIntent);
                Toast.makeText(this, "Please Grant SMS Permission.", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == AUDIO_REQ_CODE){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                ActivityCompat.requestPermissions(this, permissions, AUDIO_REQ_CODE);
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED){
                setPermissions();
            } else {
                Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                settingsIntent.setData(uri);
                startActivity(settingsIntent);
                Toast.makeText(this, "Please Grant Audio Permission.", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == STORAGE_WRITE_REQ_CODE){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                ActivityCompat.requestPermissions(this, permissions, STORAGE_WRITE_REQ_CODE);
            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                //Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();

            } else {
                Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                settingsIntent.setData(uri);
                startActivity(settingsIntent);
                Toast.makeText(this, "Please Grant Storage Permission.", Toast.LENGTH_SHORT).show();
            }
        }
//        else if(requestCode == CAMERA_REQ_CODE){
//            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
//                ActivityCompat.requestPermissions(this, permissions, CAMERA_REQ_CODE);
//            } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                    == PackageManager.PERMISSION_GRANTED){
//
//            }
//            else {
//                Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                Uri uri = Uri.fromParts("package", getPackageName(), null);
//                settingsIntent.setData(uri);
//                startActivity(settingsIntent);
//                Toast.makeText(this, "Please Grant Camera Permission.", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQ_CODE) {
            if (resultCode == RESULT_OK) { //USER PRESSED OK
                getCurrentLocation();
            } else { //USER PRESSED NO THANKS
                showGPSDialog();
            }
        }
    }

    //GET USER CURRENT LOCATION
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location == null) {
                        //TODO DISPLAY GPS DIALOG
                        showGPSDialog();
                    } else {
                        String address="";
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null) {
                                Address returnedAddress = addresses.get(0);
                                StringBuilder strReturnedAddress = new StringBuilder("");

                                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                                }
                                address = strReturnedAddress.toString();
                                Log.w("User Address", address);
                            } else {
                                Log.w("User Address", "No Address returned!");
                            }
                            //String address = addresses.get(1).getAddressLine(0);
                            //Toast.makeText(this, "Current Location: " + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            coordsLat = Double.toString(location.getLatitude());
                            coordsLon = Double.toString(location.getLongitude());
                            docDetails.put("Lat", coordsLat);
                            docDetails.put("Lon", coordsLon);


                            getNearestBrgyLocation(location, address);
                            //TODO COMPARE LOCATION TO CONTACTS
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void showGPSDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> locationSettingsResponseTask = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());

        locationSettingsResponseTask.addOnCompleteListener(task -> {
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

    private void getNearestBrgyLocation(Location location, String address) {
        //GET ESTABLISHMENTS LOCATIONS
        FirebaseFirestore.getInstance()
                .collection("barangay")
                .get()
                .addOnCompleteListener(task -> {
                    HashMap<String, Object> hashBrgys = new HashMap<>();

                    for (QueryDocumentSnapshot brgySnap : task.getResult()) {
                        HashMap<String, Object> brgySnapHash = new HashMap<>();
                        String brgyID = brgySnap.getId();

                        String lat = brgySnap.getString("Latitude");
                        String lon = brgySnap.getString("Longitude");

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

                            if(nearestDistance == 0){ //FIRST KEY
                                nearestDistance = distance;
                                nearestBrgySnap = brgySnap;
                            } else if(distance < nearestDistance){
                                nearestDistance = distance;
                                nearestBrgySnap = brgySnap;
                            }
                            nearestBrgy = nearestBrgySnap.getString("Barangay");
                        }
                    }

                    //Enable wifi when button is held
                    //WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    //wifiManager.setWifiEnabled(true);

                    String nearestBrgyID = nearestBrgySnap.getId();
                    sendAlertMessage(location, address, nearestBrgyID);
                });
    }

    private void sendAlertMessage(Location location, String address, String nearestBrgyID){
        //Get User Info
        isVerified = false;
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                lastName = documentSnapshot.getString("LastName");
                firstName = documentSnapshot.getString("FirstName");
                mobileNumber = documentSnapshot.getString("MobileNumber");
                isVerified = documentSnapshot.getBoolean("isVerified");

                //Declare and initialize alert message contents
                googleMapLink = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();


                //Only send msg to barangay if verified
                if(isVerified){
                    //get nearest brgy details
                    docRefBrgy.document(nearestBrgyID).get().addOnSuccessListener(brgySnapshot -> {
                        //Toast.makeText(getApplicationContext(), "Inside Doc Ref", Toast.LENGTH_LONG).show();
                        if(brgySnapshot.exists()){
                            brgyName = brgySnapshot.getString("Barangay");
                            brgyCity = brgySnapshot.getString("City");
                            brgyEmail = brgySnapshot.getString("Email");
                            brgyMobileNumber = brgySnapshot.getString("MobileNumber");
                            //Toast.makeText(getApplicationContext(), "Brgy Name [Inside docRefBrgy]: " + brgyName, Toast.LENGTH_LONG).show();

                            txtMessage = "[EMERGENCY] HoldSafety Alert Message! \n\nUser Details:" +
                                    "\nFull Name: " + lastName + ", " + firstName +
                                    "\nPhone Number: " + mobileNumber +
                                    "\nLocation: " + address +
                                    "\nPlease go here immediately: " + googleMapLink +
                                    "\n\nAlert Message for Barangay: " + brgyName;

                            emailMessage = "[EMERGENCY] HoldSafety Alert Message! <br /><br />User Details:" +
                                    "<br />Full Name: " + lastName + ", " + firstName +
                                    "<br />Phone Number: " + mobileNumber +
                                    "<br />Location: " + address +
                                    "<br />Please go here immediately: " + googleMapLink +
                                    "<br /><br />Alert Message for Barangay: " + brgyName;
                            if (brgyMobileNumber != null) {
                                registerReceiver(new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        switch (getResultCode()) {
                                            case Activity.RESULT_OK:
                                                //MESSAGE SENT
                                                break;
                                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                break;
                                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                break;
                                            case SmsManager.RESULT_ERROR_NULL_PDU:
                                                break;
                                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                break;
                                        }
                                    }
                                }, new IntentFilter("SMS_SENT"));

                                ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
                                PendingIntent sentPI = PendingIntent.getBroadcast(LandingActivity.this,
                                        SEND_SMS_REQ_CODE, new Intent("SMS_SENT"), 0);

                                //Send SMS
                                try {
                                    SmsManager manager = SmsManager.getDefault();
                                    //For long messages to work
                                    ArrayList<String> msgArray = manager.divideMessage(txtMessage);

                                    for (int i = 0; i < msgArray.size(); i++) {
                                        sentPendingIntents.add(i, sentPI);
                                    }
                                    docDetails.put("Barangay", nearestBrgy);
                                    manager.sendMultipartTextMessage(brgyMobileNumber, null, msgArray, sentPendingIntents, null);
                                    Toast.makeText(getApplicationContext(), "Text Sent to Brgy", Toast.LENGTH_LONG).show();
                                } catch (Exception ex) {
                                    Toast.makeText(getApplicationContext(), "SMS Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                                    ex.printStackTrace();
                                }
                            }

                            if (brgyEmail != null) {
                                //Send Email
                                String username = "holdsafety.ph@gmail.com";
                                String password = "HoldSafety@4qmag";
                                String subject = "[EMERGENCY] Alert Message - HoldSafety";

                                List<String> recipients = Collections.singletonList(brgyEmail);
                                //email of sender, password of sender, list of recipients, email subject, email body
                                new MailTask(LandingActivity.this).execute(username, password, recipients, subject, emailMessage);

                                Toast.makeText(LandingActivity.this, "Email Sent to Brgy", Toast.LENGTH_LONG).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "No barangay", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "You're not yet verified. Can't send alert to barangay", Toast.LENGTH_LONG).show();
                }

            }
            else{
                Toast.makeText(getApplicationContext(), "No current user", Toast.LENGTH_LONG).show();
            }
        });


        //Get user's emergency contacts
        FirebaseFirestore.getInstance()
                .collection("emergencyContacts")
                .document(userID)
                .collection("contacts")
                .get()
                .addOnCompleteListener(task -> {
                    //Get each contact details
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        String contactID = snapshot.getId();
                        String contactMobileNumber = snapshot.getString("mobileNumber");
                        String contactFirstName = snapshot.getString("firstName");
                        String contactLastName = snapshot.getString("lastName");
                        String contactEmail = snapshot.getString("email");

                        txtMessage = "[EMERGENCY] HoldSafety Alert Message! \n\nUser Details:" +
                                "\nFull Name: " + lastName + ", " + firstName +
                                "\nPhone Number: " + mobileNumber +
                                "\nLocation: " + address +
                                "\nPlease go here immediately: " + googleMapLink +
                                "\n\nAlert Message for: " + contactFirstName + " " + contactLastName;

                        emailMessage = "[EMERGENCY] HoldSafety Alert Message! <br /><br />User Details:" +
                                "<br />Full Name: " + lastName + ", " + firstName +
                                "<br />Phone Number: " + mobileNumber +
                                "<br />Location: " + address +
                                "<br />Please go here immediately: " + googleMapLink +
                                "<br /><br />Alert Message for: " + contactFirstName + " " + contactLastName;

                        //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                        //Send Text Message
                        if (contactMobileNumber != null) {
                            registerReceiver(new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    switch (getResultCode()) {
                                        case Activity.RESULT_OK: //MESSAGE SENT
                                            break;
                                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                            break;
                                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                                            break;
                                        case SmsManager.RESULT_ERROR_NULL_PDU:
                                            break;
                                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                                            break;
                                    }
                                }
                            }, new IntentFilter("SMS_SENT"));

                            ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
                            PendingIntent sentPI = PendingIntent.getBroadcast(LandingActivity.this,
                                    SEND_SMS_REQ_CODE, new Intent("SMS_SENT"), 0);

                            //Send SMS
                            try {
                                SmsManager manager = SmsManager.getDefault();
                                //For long messages to work
                                ArrayList<String> msgArray = manager.divideMessage(txtMessage);

                                for (int i = 0; i < msgArray.size(); i++) {
                                    sentPendingIntents.add(i, sentPI);
                                }
                                manager.sendMultipartTextMessage(contactMobileNumber, null, msgArray, sentPendingIntents, null);
                                //Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
                            } catch (Exception ex) {
                                Toast.makeText(getApplicationContext(), "SMS Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                                ex.printStackTrace();
                            }
                        }

                        if (contactEmail != null) {
                            //Send Email
                            String username = "holdsafety.ph@gmail.com";
                            String password = "HoldSafety@4qmag";
                            String subject = "[EMERGENCY] Alert Message - HoldSafety";

                            List<String> recipients = Collections.singletonList(contactEmail);
                            //email of sender, password of sender, list of recipients, email subject, email body
                            new MailTask(LandingActivity.this).execute(username, password, recipients, subject, emailMessage);
                        }
                    }
                    saveToDB();
                });
    }

    private void saveToDB() {
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        docRef = db.collection("users").document(userID);

        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("FirstName");
                        String lastName = documentSnapshot.getString("LastName");
                        docDetails.put("FirstName", firstName);
                        docDetails.put("LastName", lastName);
                        //docDetails.put("Barangay", nearestBrgy);
                        docDetails.put("Report Date", timestamp);
                        docDetails.put("Evidence", "");

                        db = FirebaseFirestore.getInstance();

                        //GET THE ID OF THE REPORT TO BE SAVED IN DB
                        DocumentReference docRefDetails = db.collection("reports").document();
                        reportID = docRefDetails.getId();
                        Log.d("DocID", "documentId: " + reportID);

                        //putExtras for evidence push to reportId
                        Intent audioRecordIntent = new Intent(this, AudioRecordingActivity.class);
                        audioRecordIntent.putExtra("reportId", reportID);
                        audioRecordIntent.putExtra("userId", userID);

                        Intent videoRecordIntent = new Intent(this, VideoRecordingActivity.class);
                        videoRecordIntent.putExtra("reportId", reportID);
                        videoRecordIntent.putExtra("userId", userID);

                        evidenceLinkRequirements.put("userID", userID);
                        evidenceLinkRequirements.put("nearestBrgy", nearestBrgy);
                        evidenceLinkRequirements.put("reportID", reportID);

                        //ADD TO GENERAL REPORTS COLLECTION
                        //docDetails.put("Barangay", nearestBrgy); adding of brgy will happen if nagsend ng alert msg sakanila
                        docDetails.put("User ID", userID);
                        db.collection("reports").document(reportID).set(docDetails)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "General Report saved to brgy-sorted DB!"))
                                .addOnFailureListener(e -> Log.w(TAG, "General Report saving to Error!!", e));

                        Intent recordingCountdown = new Intent(LandingActivity.this, RecordingCountdownActivity.class);
                        recordingCountdown.putExtra("evidenceLinkRequirements", evidenceLinkRequirements);
                        startActivity(recordingCountdown);
                        finish();
                    }
                })
                .addOnFailureListener(documentSnapshot -> {
                    docDetails.put("FirstName", "");
                    docDetails.put("LastName", "");

                    //docDetails.put("Barangay", nearestBrgy);
                    docDetails.put("Report Date", timestamp);

                    db = FirebaseFirestore.getInstance();

                    //GET THE ID OF THE REPORT TO BE SAVED IN DB
                    DocumentReference docRefDetails = db.collection("reports").document();
                    reportID = docRefDetails.getId();
                    Log.d("DocID", "documentId: " + reportID);

                    //ADD TO GENERAL COLLECTION USING THE SAME ID
                    db.collection("reports").document(reportID).set(docDetails)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "General Report saved to brgy-sorted DB!"))
                            .addOnFailureListener(e -> Log.w(TAG, "General Report saving to Error!!", e));

                });
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else if (wifiManager.getWifiState() == 1){
            buildAlertMessageNoWifi();
        }
    }

    private void buildAlertMessageNoWifi() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Wifi seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setPermissions();
    }
}