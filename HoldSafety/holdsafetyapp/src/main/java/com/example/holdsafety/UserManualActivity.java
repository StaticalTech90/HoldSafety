package com.example.holdsafety;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;

public class UserManualActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manual);
        String pdf = getIntent().getStringExtra("pdf_url");

        WebView userManualWebview = findViewById(R.id.userManualWebview);

        userManualWebview.setWebViewClient(new WebViewClient());
        userManualWebview.loadUrl("https://docs.google.com/gview?embedded=true&url=" + pdf);

        ImageView btnBack = findViewById(R.id.backArrow);
        btnBack.setOnClickListener(view -> goBack());

    }

    private void goBack() {
        finish();
    }
}