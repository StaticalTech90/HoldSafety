package com.example.holdsafety;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ReportsActivity extends AppCompatActivity {
    RecyclerView recyclerViewReports;
    String[] reportID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        recyclerViewReports = findViewById(R.id.recyclerviewReports);
        reportID = getResources().getStringArray(R.array.reportID);

        ReportAdapter reportAdapter = new ReportAdapter(this, reportID);
        recyclerViewReports.setAdapter(reportAdapter);
        recyclerViewReports.setLayoutManager(new LinearLayoutManager(this));
    }
}