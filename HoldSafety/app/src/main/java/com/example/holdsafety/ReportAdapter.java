package com.example.holdsafety;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportHolder> {
    String id[];
    Context context;

    public ReportAdapter(Context ct, String reportID[]){
        id = reportID;
        context = ct;
    }

    @NonNull
    @Override
    public ReportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater reportInflater = LayoutInflater.from(context);
        View view = reportInflater.inflate(R.layout.report_row, parent, false);
        return new ReportHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportHolder holder, int position) {
        holder.textViewReportID.setText(id[position]);
    }

    @Override
    public int getItemCount() {
        return id.length;
    }

    public class ReportHolder extends RecyclerView.ViewHolder{
        TextView textViewReportID;
        public ReportHolder(@NonNull View itemView) {
            super(itemView);
            textViewReportID = itemView.findViewById(R.id.txtReportID);
        }
    }
}
