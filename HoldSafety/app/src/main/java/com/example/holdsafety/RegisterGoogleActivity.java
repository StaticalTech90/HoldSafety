package com.example.holdsafety;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegisterGoogleActivity extends AppCompatActivity {
    public Uri imageURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_google);

        Spinner spinnerSex = (Spinner) findViewById(R.id.txtSex);
        String[] sex = new String[]{"Sex", "M", "F"};
        List<String> sexList = new ArrayList<>(Arrays.asList(sex));

        ArrayAdapter<String> spinnerSexAdapter = new ArrayAdapter<String>(this, R.layout.spinner_sex, sexList){
            @Override
            public boolean isEnabled(int position){
                if(position == 0){
                    return false;
                }

                else{
                    return true;
                }
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
        };

        spinnerSexAdapter.setDropDownViewResource(R.layout.spinner_sex);
        spinnerSex.setAdapter(spinnerSexAdapter);

        spinnerSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    public void userRegister(View view){
        Toast.makeText(getApplicationContext(), "Register with Google", Toast.LENGTH_SHORT).show();
    }

    public void uploadID(View view){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageURI = data.getData();
            Toast.makeText(getApplicationContext(), "Selected " + imageURI, Toast.LENGTH_SHORT).show();
            //uploadPicture();
        }
    }
}