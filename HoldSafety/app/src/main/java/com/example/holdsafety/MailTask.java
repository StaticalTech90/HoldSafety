package com.example.holdsafety;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

public class MailTask extends AsyncTask {

    private ProgressDialog statusDialog;
    private Activity sendMailActivity;

    public MailTask(Activity activity) {
        sendMailActivity = activity;

    }

    //LIBRARY LINK
    //https://code.google.com/archive/p/javamail-android/downloads

    //NEEDS LESS SECURE APP
    //https://myaccount.google.com/lesssecureapps?pli=1&rapt=AEjHL4OjA8svjuyhOsu93E_Ucur6JnTV2LPZSOEwxEOQ-OzOL6Koqe8eSBC_-_4XjRz9rlTkKnqfT04IwRnNdsIYra8GfIkmow

    protected void onPreExecute() {
        statusDialog = new ProgressDialog(sendMailActivity);
        statusDialog.setMessage("Getting ready...");
        statusDialog.setIndeterminate(false);
        statusDialog.setCancelable(false);
        statusDialog.show();
    }

    @Override
    protected Object doInBackground(Object... args) {
        try {
            Log.i("MailTask", "About to instantiate GMail...");
            publishProgress("Processing input....");
            Mail androidEmail = new Mail(args[0].toString(),
                    args[1].toString(), (List) args[2], args[3].toString(),
                    args[4].toString());
            publishProgress("Preparing mail message....");
            androidEmail.createEmailMessage();
            publishProgress("Sending email....");
            androidEmail.sendEmail();
            publishProgress("Email Sent.");
            Log.i("MailTask", "Mail Sent.");
        } catch (Exception e) {
            publishProgress(e.getMessage());
            Log.e("MailTask", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void onProgressUpdate(Object... values) {
//        statusDialog.setMessage(values[0].toString());

    }

    @Override
    public void onPostExecute(Object result) {
        statusDialog.dismiss();
    }

}