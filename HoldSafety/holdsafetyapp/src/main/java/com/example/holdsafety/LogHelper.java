package com.example.holdsafety;

import android.content.Context;

import androidx.annotation.NonNull;

public class LogHelper {
    Context applicationContext;

    public LogHelper(@NonNull Context context) {
        context = applicationContext.getApplicationContext();
    }

    public void LogAction(){

    }
}
