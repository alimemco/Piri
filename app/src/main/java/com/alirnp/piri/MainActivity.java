package com.alirnp.piri;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity
implements OnSuccessListener{

    private TextView textView ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermission();
    }

    private void initViews() {
         textView = findViewById(R.id.activity_main_textView);
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED ||

                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_SMS)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS},
                    Constants.MY_PERMISSIONS_REQUEST);

        } else {
            getContacts();
            getSms();
        }

    }

    private void getSms() {
        new GetSmsTask(this,this).execute();
    }

    private void getContacts() {
        new GetContactsTask(this).execute();
    }





    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {


        for (int i = 0; i < permissions.length; i++) {

            boolean holeIsOpen = grantResults[i] == PackageManager.PERMISSION_GRANTED;

            if (holeIsOpen) {
                if (permissions[i].equals(Manifest.permission.READ_CONTACTS)) {
                    getContacts();
                    Log.i(Constants.TAG, "onRequestPermissionsResult: READ CONTACT");
                } else if (permissions[i].equals(Manifest.permission.READ_SMS)) {
                    getSms();
                    Log.i(Constants.TAG, "onRequestPermissionsResult: READ SMS");
                }
            }
        }


    }

    @Override
    public void OnSuccess(boolean success, Constants.State state) {
        StringBuilder sb = new StringBuilder();
        sb.append("success = ").append(success)
                .append(" ; state = ")
                .append(state == Constants.State.SMS ? "SMS": "CONTACTS");

        textView.setText(sb.toString());
    }
}
