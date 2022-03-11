package com.example.holdsafety;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class LogHelper {
    Calendar calendar;
    Date date;
    Timestamp timestamp;
    Context context;

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user;
    Activity activity;
    HashMap <String, Object> logMap = new HashMap<>();

    public LogHelper(Context c, FirebaseAuth fAuth,
                     FirebaseUser fUser, Activity classActivity){
        context = c;
        mAuth = fAuth;
        user = fUser;
        activity = classActivity;
    }

    protected void saveToFirebase(String action, String result, String description){
        calendar = Calendar.getInstance();
        date = new  Date();
        timestamp = new Timestamp(date.getTime());

        logMap.put("Activity", activity.getLocalClassName());
        logMap.put("Action", action);
        logMap.put("Result", result);
        logMap.put("Description", description);
        logMap.put("Timestamp", timestamp);

        db.collection("logs").document(String.valueOf(calendar.get(Calendar.DATE)))
                .collection(user.getUid()).document(action).set(logMap)
            .addOnSuccessListener(aVoid -> {
                Log.i(TAG, "logging success");
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error writing document", e);
            });
    }
}