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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class RegisterActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "RegisterActivity";

    private static final int m = 3000, n = 3;
    private static final int delay = 10000;
    // Para 10000 microseconds con 6000 muestras (m) tenemos 1 minutos de datos (100 muestras por s)
    // Para DELAY_GAME con 3000 muestras (m), una cada 20,000 micro.s, tenemos 1 minutos de datos
    // Para DELAY_NORMAL con 300 muestras (m), una  cada 200,000 micro.s, tenemos 1 minutos de datos

    private SensorManager sensorManager;
    private Sensor mAcce, mGyro, mGravity;
    private TextView textData;
    private EditText editText;

    private float[][] acc_data = null;
    private int acc_data_line = 0;

    private float[][] lin_acc_data = null;

    private float[] gravity = null;


    private float[][] gyr_data = null;
    private int gyr_data_line = 0;

    private float[] a_z = null;
    private float[] a_z_lin = null;

    public void startRegister(View view) {
        Toast toast = Toast.makeText(this, "START: Registrando actividad de sensores", Toast.LENGTH_SHORT);
        toast.show();

        //SENSOR_DELAY_NORMAL 200.000 microseconds
        //SENSOR_DELAY_UI 60.000 microseconds
        //SENSOR_DELAY_GAME 20.000 microseconds
        //SENSOR_DELAY_FASTEST 0 microseconds
        //Se puede inidicar el delay en microseconds tambien

        //acc_data = new float[m][n];
        acc_data = new float[m][n + 1];//la cuarta columna es el timestamp
        lin_acc_data = new float[m][n + 1];
        gyr_data = new float[m][n + 1];
        gravity = new float[3];
        a_z = new float[m];
        a_z_lin = new float[m];

        if (mGravity != null) {
            sensorManager.registerListener(this, mGravity, delay);
            Log.d(TAG, "startRegister: Registrando el sensor ade Gravedad");
        } else {
            Log.d(TAG, "startRegister: Sensor de Gravedad NO DISPONIBLE");
        }
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
        sensorManager.unregisterListener(this, mGravity);
        Log.d(TAG, "stopRegister: listeners unregistered");
        //Log.d(TAG, "stopRegister: "+ dataToString(acc_data));
        //textData.setText("Datos guardados");

        if (acc_data != null) {
            makeDataFile(sensorDataToString(acc_data), 1);
            acc_data = null;
        }
        if (gyr_data != null) {
            makeDataFile(sensorDataToString(gyr_data), 2);
            gyr_data = null;
        }
        if (lin_acc_data != null) {
            makeDataFile(sensorDataToString(lin_acc_data), 3);
            lin_acc_data = null;
        }
        if (a_z != null) {
            // Creacion del String de a_z para escribir en fichero
            String azStr;
            int iter = 0;
            azStr = Float.toString(a_z[0]);
            while (iter < acc_data_line) {
                iter++;
                azStr += "\n" + Float.toString(a_z[iter]);
            }
            // Crear fichero
            makeDataFile(azStr, 4);
        }
        if (a_z_lin != null) {
            // Creacion del String de a_z para escribir en fichero
            String azStr = "";
            int iter = 0;
            azStr = Float.toString(a_z_lin[0]);
            while (iter < acc_data_line) {
                iter++;
                azStr += "\n" + Float.toString(a_z_lin[iter]);
            }
            // Crear fichero
            makeDataFile(azStr, 5);
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
        // Tipo 1: acelerometro
        // Tipo 2: giroscopio
        // Tipo 3: acelerometro lineal
        // Tipo 4: proyecccion de aceleracion sobre vector gravedad a_z
        // Tipo 5: proyecccion de aceleracion lineal sobre vector gravedad a_z_lin


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
        String filename;

        String edit_name = editText.getText().toString();

        if (edit_name.equals("")) {
            switch (tipo) {
                case 1:
                    filename = "acc_" + date + ".txt";
                    break;
                case 2:
                    filename = "gyr_" + date + ".txt";
                    break;
                case 3:
                    filename = "lin_" + date + ".txt";
                    break;
                case 4:
                    filename = "az_" + date + ".txt";
                    break;
                case 5:
                    filename = "azlin_" + date + ".txt";
                    break;
                default:
                    filename = date + ".txt";
                    break;
            }
        } else {
            switch (tipo) {
                case 1:
                    filename = "acc_" + edit_name + ".txt";
                    break;
                case 2:
                    filename = "gyr_" + edit_name + ".txt";
                    break;
                case 3:
                    filename = "lin_" + edit_name + ".txt";
                    break;
                case 4:
                    filename = "az_" + edit_name + ".txt";
                    break;
                case 5:
                    filename = "azlin_" + edit_name + ".txt";
                    break;
                default:
                    filename = date + ".txt";
                    break;
            }
        }


        //Crear fichero
        File directory = this.getFilesDir();
        File file = new File(directory, filename);
        FileOutputStream outputStream;

        //Escribir datos en el fichero
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            // Escritura del String de datos (String data) en el fichero de texto
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "stopRegister: Creating FILE... DONE" + directory + "/" + filename);
    }

    public String sensorDataToString(float datos[][]) {
        String stringData = "";
        int i;
        for (i = 0; i < m; i++) {

            if (datos[i][0] == 0f) {
                return stringData;
            } else {
                // TODO: Cambiar concatenacion de strings por StringBuilder()
                if (i == 0) {
                    stringData += Float.toString(datos[i][0]) + "\t" +
                            Float.toString(datos[i][1]) + "\t" +
                            Float.toString(datos[i][2]) + "\t" +
                            Float.toString(datos[i][3]);
                } else {
                    stringData += "\n" +
                            Float.toString(datos[i][0]) + "\t" +
                            Float.toString(datos[i][1]) + "\t" +
                            Float.toString(datos[i][2]) + "\t" +
                            Float.toString(datos[i][3]);
                }
            }
        }
        return stringData;
    }

    public void deleteLastFile(View view) {
        Toast toast;
        //toast = Toast.makeText(this, "Borando...", Toast.LENGTH_SHORT);
        //toast.show();
        if (fileList().length > 0) {
            int last = fileList().length - 1;
            this.deleteFile(fileList()[last]);

            toast = Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT);
            toast.show();
        } else {
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
        mGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        textData = findViewById(R.id.textView_accData);

        editText = findViewById(R.id.editText_file_name);

        //Mostrar lista de ficheros guardados
        String text = "";
        int i;
        for (i = 0; i < fileList().length; i++) {
            text += fileList()[i] + "\n";
        }
        textData.setText(text);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_GRAVITY) {
            Log.d(TAG, "Gravity Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2]);
            gravity[0] = event.values[0];
            gravity[1] = event.values[1];
            gravity[2] = event.values[2];
        }

        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG, "Acce Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2] + " TS:" + event.timestamp);
            if (acc_data_line < m) {
                acc_data[acc_data_line][0] = event.values[0];
                acc_data[acc_data_line][1] = event.values[1];
                acc_data[acc_data_line][2] = event.values[2];
                acc_data[acc_data_line][3] = event.timestamp;

                /*
                CALCULO DE ACELERACION LINEAL
                Fuente: https://developer.android.com/guide/topics/sensors/sensors_motion#java
                */


                // In this example, alpha is calculated as t / (t + dT),
                // where t is the low-pass filter's time-constant and
                // dT is the event delivery rate.
                final float alpha = 0.2f;
                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];


                // Remove the gravity contribution with the high-pass filter.
                lin_acc_data[acc_data_line][0] = event.values[0] - gravity[0];
                lin_acc_data[acc_data_line][1] = event.values[1] - gravity[1];
                lin_acc_data[acc_data_line][2] = event.values[2] - gravity[2];
                lin_acc_data[acc_data_line][3] = event.timestamp;




                /*
                 * Calculo de la proyeccionm de la aceleracion sobre el eje de gravedad
                 * */
                // producto escalar: a·b = a1*b1 + a2*b2 + a3*b3

                double gx, gy, gz;
                gx = (double) gravity[0];
                gy = (double) gravity[1];
                gz = (double) gravity[2];

                float mod_grav = (float) sqrt(pow(gx, 2) + pow(gy, 2) + pow(gz, 2));

                a_z[acc_data_line] = (event.values[0] * gravity[0] + event.values[1] * gravity[1] +
                        event.values[2] * gravity[2]) / mod_grav;


                a_z_lin[acc_data_line] = (lin_acc_data[acc_data_line][0] * gravity[0] + lin_acc_data[acc_data_line][1] * gravity[1] +
                        lin_acc_data[acc_data_line][2] * gravity[2]) / mod_grav;

                Log.d(TAG, "GRAV: " + mod_grav);
                Log.d(TAG, "LIN_ACC: " + "x:" + lin_acc_data[acc_data_line][0] + " Y:" + lin_acc_data[acc_data_line][1] + " Z:" + lin_acc_data[acc_data_line][2]);
                Log.d(TAG, "AZ: " + a_z[acc_data_line]);
                Log.d(TAG, "AZ_LIN: " + a_z_lin[acc_data_line]);

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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        sensorManager.unregisterListener(this, mGravity);

    }
}
