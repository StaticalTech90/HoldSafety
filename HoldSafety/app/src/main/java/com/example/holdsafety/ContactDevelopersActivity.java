package com.example.holdsafety;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContactDevelopersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_developers);

        Spinner spinnerAction = findViewById(R.id.txtAction);
        String[] action = new String[]{"Report a bug", "Feedback", "Concern"};
        List<String> actionList = new ArrayList<>(Arrays.asList(action));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner, actionList);
        //set the spinners adapter to the previously created one.
        spinnerAction.setAdapter(adapter);

        /*ArrayAdapter<String> spinnerActionAdapter = new ArrayAdapter<String>(this, R.layout.spinner, actionList){
            @Override
            public boolean isEnabled(int position){
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent){
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                if(position==0){
                    tv.setTextColor(getResources().getColor(R.color.hint_color));
                }

                else{
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };*/

        //spinnerActionAdapter.setDropDownViewResource(R.layout.spinner);
        //spinnerAction.setAdapter(spinnerActionAdapter);

        spinnerAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if(position > 0){
                    // Notify the selected item text
                    Toast.makeText
                            (getApplicationContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void sendMessage(View view){
        Toast.makeText(getApplicationContext(), "Successfully sent a message to developers", Toast.LENGTH_SHORT).show();
    }
}