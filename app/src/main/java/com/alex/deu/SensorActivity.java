package com.alex.deu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

public class SensorActivity extends AppCompatActivity implements SensorEventListener{

    private static final String TAG = "SensorActivity";

    private SensorManager sensorManager;
    private Sensor mAcce, mGyro, mMagno, mLight, mStepCounter;
    //private Sensor  mHumi, mTemp, mPressure;

    TextView xValue, yValue, zValue,
            xGyroValue, yGyroValue, zGyroValue,
            xMagnoValue, yMagnoValue, zMagnoValue,
            light, step;
    //TextView humi, temp, pressure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        Log.d(TAG, "onCreate: Inicializando sensores");

        xValue = findViewById(R.id.xValue);
        yValue = findViewById(R.id.yValue);
        zValue = findViewById(R.id.zValue);

        xGyroValue = findViewById(R.id.xGyroValue);
        yGyroValue = findViewById(R.id.yGyroValue);
        zGyroValue = findViewById(R.id.zGyroValue);

        xMagnoValue = findViewById(R.id.xMagnoValue);
        yMagnoValue = findViewById(R.id.yMagnoValue);
        zMagnoValue = findViewById(R.id.zMagnoValue);

        light = findViewById(R.id.light);

        step = findViewById(R.id.step);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAcce = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagno = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        //mHumi = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        //mTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        //mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG, "onSensorChanged: X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
            xValue.setText("X: " + sensorEvent.values[0]);
            yValue.setText("Y: " + sensorEvent.values[1]);
            zValue.setText("Z: " + sensorEvent.values[2]);
        }
        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            xGyroValue.setText("X: " + sensorEvent.values[0]);
            yGyroValue.setText("Y: " + sensorEvent.values[1]);
            zGyroValue.setText("Z: " + sensorEvent.values[2]);
        }
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            xMagnoValue.setText("X: " + sensorEvent.values[0]);
            yMagnoValue.setText("Y: " + sensorEvent.values[1]);
            zMagnoValue.setText("Z: " + sensorEvent.values[2]);
        }
        if (sensor.getType() == Sensor.TYPE_LIGHT) {
            light.setText("X: " + sensorEvent.values[0]);
        }
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            step.setText("steps: " + sensorEvent.values[0]);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (mAcce!= null) {
            sensorManager.registerListener(this, mAcce, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor acelerometro");
        }else{
            xValue.setText("No disponible");
            yValue.setText("No disponible");
            zValue.setText("No disponible");
        }
        if (mGyro!= null) {
            sensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor giroscopio");
        }else{
            xGyroValue.setText("No disponible");
            yGyroValue.setText("No disponible");
            zGyroValue.setText("No disponible");
        }
        if (mMagno!= null) {
            sensorManager.registerListener(this, mMagno, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor de campo magnetico");
        }else{
            xMagnoValue.setText("No disponible");
            yMagnoValue.setText("No disponible");
            zMagnoValue.setText("No disponible");
        }
        if (mLight!= null) {
            sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor de luminosidad");
        }else{
            light.setText("No disponible");
        }
        if (mStepCounter!= null) {
            sensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor podometro");
        }else{
            step.setText("No disponible");
        }

        //if (mHumi!= null) {
        //    sensorManager.registerListener(this, mHumi, SensorManager.SENSOR_DELAY_NORMAL);
        //    Log.d(TAG, "onResume: Registrando el sensor de humedad relativa");
        //}else{humi.setText("No disponible");}
        //if (mTemp!= null) {
        //    sensorManager.registerListener(this, mTemp, SensorManager.SENSOR_DELAY_NORMAL);
        //    Log.d(TAG, "onResume: Registrando el sensor de temperatura");
        //}else{temp.setText("No disponible");}
        //if (mPressure!= null) {
        //    sensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
        //    Log.d(TAG, "onResume: Registrando el sensor de presion");
        //}else{pressure.setText("No disponible");}
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        sensorManager.unregisterListener(this, mMagno);
        sensorManager.unregisterListener(this, mLight);
        sensorManager.unregisterListener(this, mStepCounter);

    }
}
