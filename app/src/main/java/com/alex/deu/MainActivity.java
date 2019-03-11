package com.alex.deu;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG =
            MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void startSensorActivity(View view) {
        Log.d(LOG_TAG, "Sensor Activity started!!");
        Intent intent = new Intent(this, SensorActivity.class);
        startActivity(intent);

    }

    public void startRegisterActivity(View view) {
        Log.d(LOG_TAG, "Register Activity started!!");
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

    }
}
