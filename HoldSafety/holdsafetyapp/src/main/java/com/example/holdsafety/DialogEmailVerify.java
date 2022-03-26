package com.example.holdsafety;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DialogEmailVerify {
    private Context context;
    private Dialog dialog;
    View dialogView;

    public EditText etCode;
    public Button btnSubmit;
    public TextView timeRemaining, verifyDesc;

    //Constructor for OTP dialog box, make another constructor if this class is to be used for other dialog boxes
    public DialogEmailVerify(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialogView = ((Activity)context).getLayoutInflater().inflate(R.layout.dialog_verification, null, false);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);

        etCode = dialogView.findViewById(R.id.txtCode);
        btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        timeRemaining = dialogView.findViewById(R.id.txtTimeRemaining);
        verifyDesc = dialogView.findViewById(R.id.lblVerifyDesc);
    }

    public void showDialog() {
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }
}
