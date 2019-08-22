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

import java.text.DecimalFormat;

import static com.alex.deu.R.string.sensor_no_disp;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = SensorActivity.class.getSimpleName();

    // Declaración de variables globales
    private SensorManager sensorManager;
    private Sensor mAcce, mGyro, mMagno, mLight, mStepCounter, mGravity,
            mHumi, mTemp, mPressure, mProx;
    private TextView xAccValue, yAccValue, zAccValue,
            xGyroValue, yGyroValue, zGyroValue,
            xMagValue, yMagValue, zMagValue,
            xGravValue, yGravValue, zGravValue,
            light, step, humi, temp, press, prox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        Log.d(TAG, "onCreate: Inicializando sensores");

        //Inicialización de las variables globales

        xAccValue = findViewById(R.id.xAccValue);
        yAccValue = findViewById(R.id.yAccValue);
        zAccValue = findViewById(R.id.zAccValue);

        xGyroValue = findViewById(R.id.xGyroValue);
        yGyroValue = findViewById(R.id.yGyroValue);
        zGyroValue = findViewById(R.id.zGyroValue);

        xMagValue = findViewById(R.id.xMagValue);
        yMagValue = findViewById(R.id.yMagValue);
        zMagValue = findViewById(R.id.zMagValue);

        xGravValue = findViewById(R.id.xGravValue);
        yGravValue = findViewById(R.id.yGravValue);
        zGravValue = findViewById(R.id.zGravValue);

        light = findViewById(R.id.light);
        step = findViewById(R.id.steps);
        humi = findViewById(R.id.humi);
        temp = findViewById(R.id.temp);
        press = findViewById(R.id.press);
        prox = findViewById(R.id.prox);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAcce = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagno = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mHumi = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        mTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mProx = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Presenta los datos de cada sensor en los correspondientes TextView
     * */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        String str;
        DecimalFormat formato4 = new DecimalFormat("0.0000");//4 decimales

        if (sensor.getType() == Sensor.TYPE_GRAVITY) {
            Log.d(TAG, "GRAVITY: X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
            str = "X: " + formato4.format(sensorEvent.values[0]);
            xGravValue.setText(str);
            str = "Y: " + formato4.format(sensorEvent.values[1]);
            yGravValue.setText(str);
            str = "Z: " + formato4.format(sensorEvent.values[2]);
            zGravValue.setText(str);
        }
        if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            Log.d(TAG, "VECTOR: X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
        }
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG, "ACCELEROMETER: X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);
            str = "X: " + formato4.format(sensorEvent.values[0]);
            xAccValue.setText(str);
            str = "Y: " + formato4.format(sensorEvent.values[1]);
            yAccValue.setText(str);
            str = "Z: " + formato4.format(sensorEvent.values[2]);
            zAccValue.setText(str);
        }
        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Log.d(TAG, "GYROSCOPE: X:" + sensorEvent.values[0] + " Y:" + sensorEvent.values[1] + " Z:" + sensorEvent.values[2]);

            str = "X: " + formato4.format(sensorEvent.values[0]);
            xGyroValue.setText(str);
            str = "Y: " + formato4.format(sensorEvent.values[1]);
            yGyroValue.setText(str);
            str = "Z: " + formato4.format(sensorEvent.values[2]);
            zGyroValue.setText(str);

            if (sensorEvent.values[2] > 0.5f) {
                getWindow().getDecorView().setBackgroundColor(Color.RED);
            } else if (sensorEvent.values[2] < -0.5f) {
                getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
            } else if (-0.5f < sensorEvent.values[2] && 0.5f > sensorEvent.values[2]) {
                getWindow().getDecorView().setBackgroundColor(Color.WHITE);
            }
        }
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            str = "X: " + formato4.format(sensorEvent.values[0]);
            xMagValue.setText(str);
            str = "Y: " + formato4.format(sensorEvent.values[1]);
            yMagValue.setText(str);
            str = "Z: " + formato4.format(sensorEvent.values[2]);
            zMagValue.setText(str);
        }
        if (sensor.getType() == Sensor.TYPE_LIGHT) {
            str = "X: " + sensorEvent.values[0];
            light.setText(str);
        }
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            str = "" + new DecimalFormat("0").format(sensorEvent.values[0]);
            step.setText(str);
        }
        if (sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
            str = "" + sensorEvent.values[0];
            humi.setText(str);
        }
        if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            str = "" + sensorEvent.values[0];
            temp.setText(str);
        }
        if (sensor.getType() == Sensor.TYPE_PRESSURE) {
            str = "" + formato4.format(sensorEvent.values[0]);
            press.setText(str);
        }
        if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
            str = "" + sensorEvent.values[0];
            prox.setText(str);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Registramos un escuchador a cada sensor para obtener sus datos
        // Previa verificación de su correcta inicialización
        if (mGravity != null) {
            sensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor de gravedad");
        } else {
            Log.d(TAG, "onResume: Gravity sensor NO DISPONIBLE");
        }
        if (mAcce != null) {
            sensorManager.registerListener(this, mAcce, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor acelerometro");
        } else {
            xAccValue.setText(sensor_no_disp);
            yAccValue.setText(sensor_no_disp);
            zAccValue.setText(sensor_no_disp);
        }
        if (mGyro != null) {
            sensorManager.registerListener(this, mGyro, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor giroscopio");
        } else {
            xGyroValue.setText(sensor_no_disp);
            yGyroValue.setText(sensor_no_disp);
            zGyroValue.setText(sensor_no_disp);
        }
        if (mMagno != null) {
            sensorManager.registerListener(this, mMagno, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor de campo magnetico");
        } else {
            xMagValue.setText(sensor_no_disp);
            yMagValue.setText(sensor_no_disp);
            zMagValue.setText(sensor_no_disp);
        }
        if (mLight != null) {
            sensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor de luminosidad");
        } else {
            light.setText(sensor_no_disp);
        }
        if (mStepCounter != null) {
            sensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor podometro");
        } else {
            step.setText(sensor_no_disp);
        }
        if (mHumi != null) {
            sensorManager.registerListener(this, mHumi, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor de humedad relativa");
        } else {
            humi.setText(sensor_no_disp);
        }
        if (mTemp != null) {
            sensorManager.registerListener(this, mTemp, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor de temperatura");
        } else {
            temp.setText(sensor_no_disp);
        }
        if (mPressure != null) {
            sensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor de presion");
        } else {
            press.setText(sensor_no_disp);
        }
        if (mProx != null) {
            sensorManager.registerListener(this, mProx, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "onResume: Registrando el sensor de proximidad");
        } else {
            prox.setText(sensor_no_disp);
        }
    }

    /**
     * Desactiva los escuchadores de los sensores cuando la aplicación pasa a segundo plano
     * */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, mGravity);
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        sensorManager.unregisterListener(this, mMagno);
        sensorManager.unregisterListener(this, mLight);
        sensorManager.unregisterListener(this, mStepCounter);
        sensorManager.unregisterListener(this, mPressure);
        sensorManager.unregisterListener(this, mTemp);
        sensorManager.unregisterListener(this, mHumi);
        sensorManager.unregisterListener(this, mProx);
    }
}
