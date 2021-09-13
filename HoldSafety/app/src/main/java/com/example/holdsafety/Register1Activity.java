package com.example.holdsafety;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Register1Activity extends AppCompatActivity {

    private EditText etLastName;
    private EditText etFirstName;
    private EditText etMiddleName;
    private EditText etBirthdate;
    private Spinner spinnerSex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register1);

        etLastName = findViewById(R.id.txtLastName);
        etFirstName = findViewById(R.id.txtFirstName);
        etMiddleName = findViewById(R.id.txtMiddleName);
        etBirthdate = findViewById(R.id.txtBirthDate);
        spinnerSex = findViewById(R.id.txtSex);

        String[] sex = new String[]{"Sex", "M", "F"};
        List<String> sexList = new ArrayList<>(Arrays.asList(sex));

        ArrayAdapter<String> spinnerSexAdapter = new ArrayAdapter<String>(this, R.layout.spinner_sex, sexList){
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
                } else{
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

    public void userRegisterNext(View view) throws ParseException {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        String date = etBirthdate.getText().toString();
        String valid = "01-01-2002"; //age restriction
        Date validDate = dateFormat.parse(valid);

        String sLastName = etLastName.getText().toString();
        String sFirstName = etFirstName.getText().toString();
        String sMiddleName = etMiddleName.getText().toString();
        String sSex = spinnerSex.getSelectedItem().toString();
        String sDate = etBirthdate.getText().toString();

        try{
            Date parsedDate = dateFormat.parse(date);
            assert parsedDate != null;
            if(parsedDate.after(validDate)){
                etBirthdate.setHint("Please enter valid birthdate (mm-dd-yyyy)");
                etBirthdate.setError("Please enter valid birthdate (mm-dd-yyyy)");
            } else {
                if(TextUtils.isEmpty(etLastName.getText())){
                    etLastName.setHint("please enter last name");
                    etLastName.setError("please enter last name");
                } else if(TextUtils.isEmpty(etFirstName.getText())){
                    etFirstName.setHint("please enter first name");
                    etFirstName.setError("please enter first name");
                } else if(TextUtils.isEmpty(etBirthdate.getText())){
                    etBirthdate.setHint("please enter birthdate (mm-dd-yyyy)");
                    etBirthdate.setError("please enter birthdate (mm-dd-yyyy)");
                } else if(spinnerSex.getSelectedItem().equals("Sex")){
                    ((TextView)spinnerSex.getSelectedView()).setError("please select sex");
                } else {
                    //send data over to register2 activity
                    //not the best practice but fragments are tricky
                    Intent intent = new Intent (getApplicationContext(), Register2Activity.class);
                    intent.putExtra("lastName", sLastName);
                    intent.putExtra("firstName", sFirstName);
                    intent.putExtra("middleName", sMiddleName);
                    intent.putExtra("sex", sSex);
                    intent.putExtra("date", sDate);
                    intent.putExtra("birthDate", sDate);
                    startActivity(intent);
                }
            }
        } catch(ParseException pe){
            etBirthdate.setHint("Please enter valid birthdate");
            etBirthdate.setError("Please enter valid birthdate");
        }
    }

    public void goBack(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(intent);
    }

}