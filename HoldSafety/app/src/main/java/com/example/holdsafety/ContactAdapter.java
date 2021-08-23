package com.example.holdsafety;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactHolder> {
    String contactName[];
    Context context;

    public ContactAdapter(Context ct, String name[]){
        context = ct;
        contactName = name;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater contactInflater = LayoutInflater.from(context);
        View view = contactInflater.inflate(R.layout.contact_row, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ContactHolder holder, int position) {
        holder.textViewContactName.setText(contactName[position]);

        holder.contactLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UpdateContactActivity.class);
                intent.putExtra("contactName", contactName[position]);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactName.length;
    }

    public class ContactHolder extends RecyclerView.ViewHolder{
        TextView textViewContactName;
        ConstraintLayout contactLayout;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            textViewContactName = itemView.findViewById(R.id.txtContactName);
            contactLayout = itemView.findViewById(R.id.layoutContact);
        }
    }
}
