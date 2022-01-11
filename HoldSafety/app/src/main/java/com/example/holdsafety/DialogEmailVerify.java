package com.example.holdsafety;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//NOTE: Ginawa ko design neto para reusable by other activities just in case, para d lang pang OTP
public class DialogEmailVerify {
    private Context context;
    private Dialog dialog;
    View dialogView;

    public EditText etCode;
    public Button btnSubmit;
    //private Button btnCancel;
    public TextView timeRemaining;

    //Constructor for OTP dialog box, make another constructor if this class is to be used for other dialog boxes
    public DialogEmailVerify(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialogView = ((Activity)context).getLayoutInflater().inflate(R.layout.dialog_verification, null, false);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);

        etCode = dialogView.findViewById(R.id.txtCode);
        btnSubmit = dialogView.findViewById(R.id.btnSubmit);
        //btnCancel = dialogView.findViewById(R.id.btnCancel);
        timeRemaining = dialogView.findViewById(R.id.txtTimeRemaining);
    }

    public void showDialog() {
        dialog.show();
    }

    public void dismissDialog() {
        dialog.dismiss();
    }
}
