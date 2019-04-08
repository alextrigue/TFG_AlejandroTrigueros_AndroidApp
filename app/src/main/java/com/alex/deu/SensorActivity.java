package com.alex.deu;

import android.graphics.Color;
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
    private Sensor mAcce, mGyro, mMagno, mLight, mStepCounter, mLinAcce, mGravity, rVector;
    private final int sensor_delay = 500000;//500.000 microseconds
    //private Sensor  mHumi, mTemp, mPressure;

    TextView xValue, yValue, zValue,
            xGyroValue, yGyroValue, zGyroValue,
            xMagnoValue, yMagnoValue, zMagnoValue,
            light,
            step;
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
        mLinAcce = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //mHumi = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        //mTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        //mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        /* GRAVITY SENSOR */
        mGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        rVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);



    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;

        if(sensor.getType() == Sensor.TYPE_GRAVITY){
            Log.d(TAG, "GRAVITY: X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
        }

        if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            Log.d(TAG, "VECTOR: X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
        }


        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG, "ACCELEROMETER: X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
            xValue.setText("X: " + sensorEvent.values[0]);
            yValue.setText("Y: " + sensorEvent.values[1]);
            zValue.setText("Z: " + sensorEvent.values[2]);
        }
        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Log.d(TAG, "GYROSCOPE: X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
            xGyroValue.setText("X: " + sensorEvent.values[0]);
            yGyroValue.setText("Y: " + sensorEvent.values[1]);
            zGyroValue.setText("Z: " + sensorEvent.values[2]);

            if(sensorEvent.values[2] > 0.5f){
                getWindow().getDecorView().setBackgroundColor(Color.RED);
            }else if(sensorEvent.values[2] < -0.5f){
                getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
            }else if(-0.5f < sensorEvent.values[2] && 0.5f > sensorEvent.values[2]){
                getWindow().getDecorView().setBackgroundColor(Color.WHITE);
            }

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


        if(rVector != null){
            sensorManager.registerListener(this, rVector, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_STATUS_ACCURACY_LOW);
            Log.d(TAG, "onResume: Registrando Rotation Vector");
        }else{
            Log.d(TAG, "onResume: Rotation Vector NO DISPONIBLE");
        }

        if(mGravity != null){
            sensorManager.registerListener(this, mGravity, sensor_delay);
            Log.d(TAG, "onResume: Registrando el sensor de gravedad");
        }else{
            Log.d(TAG, "onResume: Gravity sensor NO DISPONIBLE");
        }

        if (mLinAcce!= null) {
            sensorManager.registerListener(this, mLinAcce, sensor_delay);
            Log.d(TAG, "onResume: Registrando el sensor acelerometro lineal");
        }else{
            Log.d(TAG, "onResume: Linear Acceleration NO DISPONIBLE");

        }

        if (mAcce!= null) {
            sensorManager.registerListener(this, mAcce, sensor_delay);
            Log.d(TAG, "onResume: Registrando el sensor acelerometro");
        }else{
            xValue.setText("No disponible");
            yValue.setText("No disponible");
            zValue.setText("No disponible");
        }
        if (mGyro!= null) {
            sensorManager.registerListener(this, mGyro, sensor_delay);
            Log.d(TAG, "onResume: Registrando el sensor giroscopio");
        }else{
            xGyroValue.setText("No disponible");
            yGyroValue.setText("No disponible");
            zGyroValue.setText("No disponible");
        }
        if (mMagno!= null) {
            sensorManager.registerListener(this, mMagno, sensor_delay);
            Log.d(TAG, "onResume: Registrando el sensor de campo magnetico");
        }else{
            xMagnoValue.setText("No disponible");
            yMagnoValue.setText("No disponible");
            zMagnoValue.setText("No disponible");
        }
        if (mLight!= null) {
            sensorManager.registerListener(this, mLight, sensor_delay);
            Log.d(TAG, "onResume: Registrando el sensor de luminosidad");
        }else{
            light.setText("No disponible");
        }
        if (mStepCounter!= null) {
            sensorManager.registerListener(this, mStepCounter, sensor_delay);
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
        sensorManager.unregisterListener(this, rVector);
        sensorManager.unregisterListener(this, mGravity);
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        sensorManager.unregisterListener(this, mMagno);
        sensorManager.unregisterListener(this, mLight);
        sensorManager.unregisterListener(this, mStepCounter);

    }
}
