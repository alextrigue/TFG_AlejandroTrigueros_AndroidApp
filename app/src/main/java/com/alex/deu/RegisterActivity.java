package com.alex.deu;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "RegisterActivity";

    private SensorManager sensorManager;
    private Sensor mAcce, mGyro;
    private TextView textData;

    private static final int m = 3000, n = 3;
    private final int delay = SensorManager.SENSOR_DELAY_GAME;
    // Para DELAY_GAME con 3000 muestras (m) cada 20,000 micro.s, tenemos 1 minutos de datos
    // Para DELAY_NORMAL con 300 muestras (m) cada 200,000 micro.s, tenemos 1 minutos de datos

    private float[][] acc_data = null;
    private int acc_data_line = 0;

    private float[][] gyr_data = null;
    private int gyr_data_line = 0;

    public void startRegister(View view) {
        Toast toast = Toast.makeText(this, "START: Registrando actividad de sensores", Toast.LENGTH_SHORT);
        toast.show();

        //SENSOR_DELAY_NORMAL 200.000 microseconds
        //SENSOR_DELAY_UI 60.000 microseconds
        //SENSOR_DELAY_GAME 20.000 microseconds
        //SENSOR_DELAY_FASTEST 0 microseconds
        //Se puede inidicar el delay en microseconds tambien

        acc_data = new float[m][n];
        gyr_data = new float[m][n];

        if (mAcce != null) {
            acc_data_line = 0;
            sensorManager.registerListener(this, mAcce, delay);
            Log.d(TAG, "startRegister: Registrando el sensor acelerometro");
        } else {
            Log.d(TAG, "startRegister: Accelerometro NO DISPONIBLE");
        }
        if (mGyro != null) {
            gyr_data_line = 0;
            sensorManager.registerListener(this, mGyro, delay);
            Log.d(TAG, "startRegister: Registrando el sensor giroscopio");
        } else {
            Log.d(TAG, "startRegister: Giroscopio NO DISPONIBLE");
        }

    }

    public void stopRegister(View view) {
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        Log.d(TAG, "stopRegister: listeners unregistered");
        //Log.d(TAG, "stopRegister: "+ dataToString(acc_data));
        //textData.setText("Datos guardados");

        if (acc_data != null) {
            makeDataFile(dataToString(acc_data), 1);
            acc_data = null;
        }
        if (gyr_data != null) {
            makeDataFile(dataToString(gyr_data), 2);
            gyr_data = null;
        }

        //Mostrar lista de ficheros guardados
        String text = "";
        int i;
        for (i = 0; i < fileList().length; i++) {
            text += fileList()[i] + "\n";
        }
        textData.setText(text);

    }

    public void makeDataFile(String data, int tipo) {
        // Tipo 1 == acelerometro
        // Tipo 2 == giroscopio
        // Tipo 3 == acelerometro lineal


        Log.d(TAG, "stopRegister: Creating FILE");
        //String content = "HELLO WORLD!!";
        //String filename = "hola.txt";
        //String numFile = Integer.toString(System.currentTimeMillis());

        //Crear nombre del nuevo fichero
        Calendar c = Calendar.getInstance();
        int sec = c.get(Calendar.SECOND);
        int min = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = 1 + c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        String date = year + "-" + month + "-" + day + "_" + hour + "-" + min + "-" + sec;
        String filename = "";

        if (tipo == 1) {
            filename = "acc_" + date + ".txt";
        } else if (tipo == 2) {
            filename = "gyr_" + date + ".txt";
        }

        //Crear fichero
        File directory = this.getFilesDir();
        File file = new File(directory, filename);
        FileOutputStream outputStream;

        //Escribir datos en el fichero
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "stopRegister: Creating FILE... DONE" + directory + "/" + filename);
    }

    public String dataToString(float datos[][]) {
        String text = "";
        int i;
        for (i = 0; i < m; i++) {

            if (datos[i][0] == 0f) {
                return text;
            } else {
                if (i == 0) {
                    text += Float.toString(datos[i][0]) + "\t" +
                            Float.toString(datos[i][1]) + "\t" +
                            Float.toString(datos[i][2]);
                } else {
                    text += "\n" + Float.toString(datos[i][0]) + "\t" +
                            Float.toString(datos[i][1]) + "\t" +
                            Float.toString(datos[i][2]);
                }
            }
        }
        return text;
    }

    public void deleteLastFile(View view) {
        Toast toast;
        //toast = Toast.makeText(this, "Borando...", Toast.LENGTH_SHORT);
        //toast.show();
        if(fileList().length > 0) {
            int last = fileList().length - 1;
            this.deleteFile(fileList()[last]);

            toast = Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT);
            toast.show();
        }else{
            toast = Toast.makeText(this, "fileList() Empty", Toast.LENGTH_SHORT);
            toast.show();
        }
        //Mostrar lista de ficheros guardados
        String text = "";
        int i;
        for (i = 0; i < fileList().length; i++) {
            text += fileList()[i] + "\n";
        }
        textData.setText(text);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.d(TAG, "onCreate: Inicializando sensores");


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAcce = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        textData = findViewById(R.id.textView_accData);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG, "Acce Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2]);
            if (acc_data_line < m) {
                acc_data[acc_data_line][0] = event.values[0];
                acc_data[acc_data_line][1] = event.values[1];
                acc_data[acc_data_line][2] = event.values[2];
                acc_data_line++;
            }
        }
        if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Log.d(TAG, "Gyro Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2]);
            if (gyr_data_line < m) {
                gyr_data[gyr_data_line][0] = event.values[0];
                gyr_data[gyr_data_line][1] = event.values[1];
                gyr_data[gyr_data_line][2] = event.values[2];
                gyr_data_line++;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
    }
}
