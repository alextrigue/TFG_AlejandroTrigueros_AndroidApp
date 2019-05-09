package com.alex.deu;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import static java.lang.Math.pow;
import static java.lang.StrictMath.sqrt;

public class StatisticsActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = StatisticsActivity.class.getSimpleName();

    private SensorManager sensorManager;
    private Sensor mAcce, mGyro, mGrav;
    private static final int delay = SensorManager.SENSOR_DELAY_GAME;
    //20.000 microsegundos -> 50 muestras/segundo
    // Para 10000 microseconds -> 100 muestras/segundo

    private double intervalo = 2;// 2 segundos
    private double muestras_seg = 1 / delay;
    private int data_size = (int) (intervalo * muestras_seg);//Tamaño del array para calcular estadisticos

    private TextView tv_med, tv_std, tv;

    // guarda los valores x, y, z cada ez que cambia el sensor grav
    private float[] gravityValues = new float[3];
    private ArrayList<Double> array_az = new ArrayList<Double>(data_size);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAcce = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGrav = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        tv_med = findViewById(R.id.tv_med);
        tv_std = findViewById(R.id.tv_std);
        tv = findViewById(R.id.tv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAcce != null) {
            sensorManager.registerListener(this, mAcce, delay);
            Log.d(TAG, "onResume: Registrando el sensor acelerometro");
        } else {
            tv.setText(tv.getText().toString() + "\nAcc no disponible");
        }
        if (mGrav != null) {
            sensorManager.registerListener(this, mGrav, delay);
            Log.d(TAG, "onResume: Registrando el sensor gravedad");
        } else {
            tv.setText(tv.getText().toString() + "\nGrav no disponible");
        }
        if (mGyro != null) {
            sensorManager.registerListener(this, mGyro, delay);
            Log.d(TAG, "onResume: Registrando el sensor giroscopio");
        } else {
            tv.setText(tv.getText().toString() + "\nGyro no disponible");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, mGrav);
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                /*ACCELEROMETER*/
                //Log.d(TAG, "ACCE Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2] + " TS:" + event.timestamp);

                /*
                 * CALCULO DE ACELERACION LINEAL
                 * Fuente: https://developer.android.com/guide/topics/sensors/sensors_motion#java
                 * */
                /* Suavizado de la gravedad
                 * In this example, alpha is calculated as t / (t + dT),
                 * where t is the low-pass filter's time-constant and
                 * dT is the event delivery rate.
                 * */
                final float alpha = 0.8f;
                // Isolate the force of gravity with the low-pass filter.
                gravityValues[0] = alpha * gravityValues[0] + (1 - alpha) * event.values[0];
                gravityValues[1] = alpha * gravityValues[1] + (1 - alpha) * event.values[1];
                gravityValues[2] = alpha * gravityValues[2] + (1 - alpha) * event.values[2];
                //Remove the gravity contribution with the high-pass filter.
                float[] linear_acc = new float[3];
                linear_acc[0] = event.values[0] - gravityValues[0];
                linear_acc[1] = event.values[1] - gravityValues[1];
                linear_acc[2] = event.values[2] - gravityValues[2];

                /*
                 * CALCULO DE LA PROYECCION DE LA ACELERACION SOBRE EL VECTOR DE GRAVEDAD
                 *
                 * array_az
                 *
                 * producto escalar: a·b = a1*b1 + a2*b2 + a3*b3
                 * */

                double mod_grav = sqrt(pow(gravityValues[0], 2) + pow(gravityValues[1], 2) + pow(gravityValues[2], 2));

                if (mod_grav != 0) {
                    array_az.add(
                            (double) ((linear_acc[0] * gravityValues[0]) +
                                    (linear_acc[1] * gravityValues[1]) +
                                    (linear_acc[2] * gravityValues[2])) / mod_grav
                    );
                    Log.d(TAG, "Array AZ .add(): " + array_az.toString());
                }else {
                    array_az.add(0.0);

                }


                break;
            case Sensor.TYPE_GYROSCOPE:
                /*GYROSCOPE*/
                //Log.d(TAG, "GYRO Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2] + " TS:" + event.timestamp);

                break;
            case Sensor.TYPE_GRAVITY:
                /*GRAVITY*/
                gravityValues[0] = event.values[0];// X
                gravityValues[1] = event.values[1];// Y
                gravityValues[2] = event.values[2];// Z
                //Log.d(TAG, "GRAVITY Changed: X:" + gravityValues[0] + " Y:" + gravityValues[1] + " Z:" + gravityValues[2]);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
