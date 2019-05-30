package com.alex.deu;

import android.content.Context;
import android.os.AsyncTask;
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
import java.util.ArrayList;
import java.util.Calendar;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class RegisterActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private static final int m = 9000, n = 3;
    private static final int delay = 10000;
    // Para 10000 microseconds con 6000 muestras (m) tenemos 1 minutos de datos (100 muestras por s)
    // Para DELAY_GAME con 3000 muestras (m), una cada 20,000 micro.s, tenemos 1 minutos de datos
    // Para DELAY_NORMAL con 300 muestras (m), una  cada 200,000 micro.s, tenemos 1 minutos de datos


    private SensorManager sensorManager;
    private Sensor mAcce, mGyro, mGravity, mMag, mRotVector;
    private TextView textData;
    private EditText editText;


    private ArrayList<float[]> gravity_data;//Almacenamiento de datos de Gravity
    private float[] gravity = null;//Almacena la ultima actualizacion del sensor de gravedad

    private ArrayList<float[]> magnetic_data; // ALmecena todos los datos del sensor Magnetic Field
    private float[] magnetic;

    private float[][] acc_data;
    private int acc_data_line = 0;
    private float[][] lin_acc_data;
    //Proyeccion de la aceleración lineal sobre el vector gravedad
    private ArrayList<float[]> acc_proj_data;

    private float[][] gyr_data;
    private int gyr_data_line = 0;
    //Proyección del giroscopio sobre gravedad
    private ArrayList<float[]> gyr_proj_data;

    /**
     * Matriz de rotacion
     */
    private float[] rotMatrix = new float[9];
    private ArrayList<float[]> rotMatrix_data;//se almacenan todos los cambios de la matriz


    /**
     * Inicializa las variables globales
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Log.d(TAG, "onCreate: Inicializando sensores");


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAcce = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mRotVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        textData = findViewById(R.id.textView_accData);

        editText = findViewById(R.id.editText_file_name);


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
        gravity_data = new ArrayList<>();//float[]
        magnetic = new float[3];
        magnetic_data = new ArrayList<>();//float[]
        acc_proj_data = new ArrayList<>();//float[]
        gyr_proj_data = new ArrayList<>();//float[]
        rotMatrix_data = new ArrayList<>();//float[]


        //Mostrar lista de ficheros guardados
        String text = "";
        int i;
        for (i = 0; i < fileList().length; i++) {
            text += fileList()[i] + "\n";
        }
        textData.setText(text);

    }

    /**
     * Al pulsar boton START activa los escuchadores de los sensores
     */
    public void startRegister(View view) {
        Toast toast = Toast.makeText(this, "START: Registrando actividad de sensores", Toast.LENGTH_SHORT);
        toast.show();


        if (mAcce != null) {
            acc_data_line = 0;
            sensorManager.registerListener(this, mAcce, delay);
            Log.d(TAG, "startRegister: Registrando el sensor ACCELEROMETER");
        } else {
            Log.d(TAG, "startRegister: ACCELEROMETER NO DISPONIBLE");
        }
        if (mGyro != null) {
            gyr_data_line = 0;
            sensorManager.registerListener(this, mGyro, delay);
            Log.d(TAG, "startRegister: Registrando el sensor GYROSCOPE");
        } else {
            Log.d(TAG, "startRegister: GYROSCOPE NO DISPONIBLE");
        }

        if (mMag != null) {
            sensorManager.registerListener(this, mMag, delay);
            Log.d(TAG, "startRegister: Registrando el sensor MAGNETIC FIELD");
        } else {
            Log.d(TAG, "startRegister: MAGNETIC FIELD NO DISPONIBLE");
        }
        /*
        if (mRotVector != null) {
            sensorManager.registerListener(this, mRotVector, delay);
            Log.d(TAG, "startRegister: Registrando el sensor ROTATION VECTOR");
        } else {
            Log.d(TAG, "startRegister: ROTATION VECTOR NO DISPONIBLE");
        }
        */

        if (mGravity != null) {
            //sensorManager.registerListener(this, mGravity, delay);
            sensorManager.registerListener(this, mGravity, delay);
            Log.d(TAG, "startRegister: Registrando el sensor GRAVITY");
        } else {
            Log.d(TAG, "startRegister: Sensor GRAVITY NO DISPONIBLE");
        }

    }


    /**
     * Desactiva los escuchadores de los sensores, deja de guardar susdatos y reinicia los vectores de datos
     */
    public void stopRegister(View view) {
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        sensorManager.unregisterListener(this, mGravity);
        sensorManager.unregisterListener(this, mMag);
        //sensorManager.unregisterListener(this, mRotVector);
        Log.d(TAG, "stopRegister: listeners unregistered");
        //Log.d(TAG, "stopRegister: "+ dataToString(acc_data));
        //textData.setText("Datos guardados");



        if (acc_data != null) {
            Log.d(TAG, "Creating FILE acc_data...");
            makeDataFile(sensorDataToString(acc_data), 1);
            acc_data = null;
        }
        if (gyr_data != null) {
            Log.d(TAG, "Creating FILE gyr_data...");
            makeDataFile(sensorDataToString(gyr_data), 2);
            gyr_data = null;
        }
        if (lin_acc_data != null) {
            Log.d(TAG, "Creating FILE lin_acc_data...");
            makeDataFile(sensorDataToString(lin_acc_data), 3);
            lin_acc_data = null;
        }

//        if (a_z != null) {
//            // Creacion del String de a_z para escribir en fichero
//            String azStr;
//            int iter = 0;
//            azStr = Float.toString(a_z[0]);
//            while (iter < acc_data_line - 1) {
//                iter++;
//                azStr += "\n" + Float.toString(a_z[iter]);
//            }
//            // Crear fichero
//            makeDataFile(azStr, 4);
//        }
//        if (a_z_lin != null) {
//            // Creacion del String de a_z para escribir en fichero
//            String azLinStr;
//            int iter = 0;
//            azLinStr = Float.toString(a_z_lin[0]);
//            while (iter < acc_data_line - 1) {
//                iter++;
//                azLinStr += "\n" + Float.toString(a_z_lin[iter]);
//            }
//            // Crear fichero
//            makeDataFile(azLinStr, 5);
//        }
//        if (g_z != null) {
//            // Creacion del String g_z para escribir en fichero
//            String gzStr;
//            int iter = 0;
//            gzStr = Float.toString(g_z[0]);
//            while (iter < gyr_data_line - 1) {
//                iter++;
//                gzStr += "\n" + Float.toString(g_z[iter]);
//            }
//            // Creacion de fichero
//            makeDataFile(gzStr, 6);
//        }


        ////ArrayList<Float[]> a String


        //Log.d(TAG, "GRAVITY DATA: \n" + listToString(gravity_data));
        //Log.d(TAG, "LINEAR ACC PROJ DATA: \n" + listToString(acc_proj_data));
        //Crear ficheros con los datos y resetear listas
        if (acc_proj_data != null) {
            Log.d(TAG, "Creating FILE acc_proj_data...");
            makeDataFile(listToString(acc_proj_data), 4);
            acc_proj_data.clear();
        }
        if (gyr_proj_data != null) {
            Log.d(TAG, "Creating FILE gyr_proj_data...");
            makeDataFile(listToString(gyr_proj_data), 5);
            gyr_proj_data.clear();
        }

        if (gravity_data != null) {
            gravity_data.clear();
        }

        if (magnetic_data != null) {
            magnetic_data.clear();
        }
        if (rotMatrix_data != null) {
            Log.d(TAG, "Creating FILE rotMatrix_data...");
            String matrixString = listToString(rotMatrix_data);
            makeDataFile(matrixString, 6);
            //Log.d(TAG, "Matriz de rotacion:\n" + matrixString);
            //rotMatrix_data.clear();
        }


        //Mostrar lista de ficheros guardados
        String text = "";
        int i;
        for (i = 0; i < fileList().length; i++) {
            text += fileList()[i] + "\n";
        }
        textData.setText(text);

        Toast toast = Toast.makeText(this, "Stop Register", Toast.LENGTH_SHORT);
        toast.show();


    }


    /**
     * Recibe un ArrayList cuyos elementos son arrays de float[]
     *
     * @return string listo para imprimir en un fichero
     */
    public String listToString(ArrayList<float[]> list) {
        String str = "";
        if (list != null) {
            if (list.size() > 0) {

                for (int r = 0; r < list.size(); r++) {
                    for (int i = 0; i < list.get(r).length - 1; i++) {
                        str += list.get(r)[i] + ", ";
                    }
                    str += list.get(r)[list.get(r).length - 1] + "\n";
                }
                return str;
            }
        }
        return "";
    }

    /**
     * Recibe los datos en formato string y los guarda en un fichero
     */
    public void makeDataFile(String data, int tipo) {
        // Tipo 1: acelerometro
        // Tipo 2: giroscopio
        // Tipo 3: acelerometro lineal
        // Tipo 4: proyecccion de aceleracion lineal sobre vector gravedad (acc_proj_data)
        // TIpo 5: proyeccion de giroscopio sobre vector gravedad (gyr_proj_data)
        // TIpo 6: matriz de rotacion (rotMatrix_data)

        String extension = ".txt";

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

        // Eleccion del nombre del fichero segun su tipo
        if (edit_name.equals("")) {
            switch (tipo) {
                case 1:
                    filename = "acc_" + date + extension;
                    break;
                case 2:
                    filename = "gyr_" + date + extension;
                    break;
                case 3:
                    filename = "lin_" + date + extension;
                    break;
                case 4:
                    filename = "az_" + date + extension;
                    break;
                case 5:
                    filename = "gz_" + date + extension;
                    break;
                case 6:
                    filename = "rotMat_" + date + extension;
                    break;
                default:
                    filename = date + extension;
                    break;
            }
        } else {
            switch (tipo) {
                case 1:
                    filename = "acc_" + edit_name + extension;
                    break;
                case 2:
                    filename = "gyr_" + edit_name + extension;
                    break;
                case 3:
                    filename = "lin_" + edit_name + extension;
                    break;
                case 4:
                    filename = "az_" + edit_name + extension;
                    break;
                case 5:
                    filename = "gz_" + edit_name + extension;
                    break;
                case 6:
                    filename = "rotMat_" + edit_name + extension;
                    break;

                default:
                    filename = date + extension;
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

        Log.d(TAG, "\n...DONE: " + directory + "/" + filename);
    }


    /**
     * Recibe los datos brutos de los sensores en un array 2D
     * y les da formato de String para grabarlos en fichero posteriormente
     */
    public String sensorDataToString(float[][] datos) {
        String stringData = "";
        int i;
        for (i = 0; i < m; i++) {

            if (datos[i][0] == 0f) {
                return stringData;
            } else {
                if (i == 0) {
                    stringData += datos[i][0] + "\t" +
                            datos[i][1] + "\t" +
                            datos[i][2] + "\t" +
                            datos[i][3];
                } else {
                    stringData += "\n" +
                            datos[i][0] + "\t" +
                            datos[i][1] + "\t" +
                            datos[i][2] + "\t" +
                            datos[i][3];
                }
            }
        }
        return stringData;
    }


    /**
     * Elimina el ultimo fichero creado
     */
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


    /**
     * Se ejetuca al cambiar el valor de los sensores
     * Almacena los datos de sensores en arrays
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float mod_grav = 1;

        switch (event.sensor.getType()) {


            case Sensor.TYPE_MAGNETIC_FIELD:
                /*
                  // float rotationMatrix = new float[9];
                  // Update rotation matrix, which is needed to update orientation angles.
                  // SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, mMagnetometerReading);
                * */
                magnetic = event.values;
                //magnetic_data.add(magnetic_data.size(), event.values);

                //Log.d(TAG, "magnetic_data.size(): " + magnetic_data.size());
                //Log.d(TAG, "Magnetic timestamp: " + event.timestamp);

                boolean check;
                check = SensorManager.getRotationMatrix(rotMatrix, null, gravity, magnetic);
                if (check) {
                    Log.d(TAG, "rotMatrix: \n" + rotMatrix[0] + ", " + rotMatrix[1] + ", " + rotMatrix[2] + "\n"+
                            rotMatrix[3] + ", " + rotMatrix[4] + ", " + rotMatrix[5] + "\n"+
                            rotMatrix[6] + ", " + rotMatrix[7] + ", " + rotMatrix[8]);
                    //TODO: añadir timestamp a rotMatrix. Buscar posicion que no se usa para meterlo dentro del array
                    rotMatrix_data.add(rotMatrix_data.size(), rotMatrix);
                }

                break;

            case Sensor.TYPE_GRAVITY:
                //Log.d(TAG, "Gravity Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2]);
                gravity = event.values;
                //gravity_data.add(gravity_data.size(), event.values);

                //Log.d(TAG, "gravity_data.size(): " + gravity_data.size());
                //Log.d(TAG, "Gravity timestasmp: " + event.timestamp);
                break;

            case Sensor.TYPE_ACCELEROMETER:
                //Log.d(TAG, "Acce Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2] + " TS:" + event.timestamp);
                if (acc_data_line < m) {
                    acc_data[acc_data_line][0] = event.values[0];
                    acc_data[acc_data_line][1] = event.values[1];
                    acc_data[acc_data_line][2] = event.values[2];
                    acc_data[acc_data_line][3] = event.timestamp;

                /*
                CALCULO DE ACELERACION LINEAL
                Fuente: https://developer.android.com/guide/topics/sensors/sensors_motion#java
                */
                /*
                    // Suavizado de la gravedad
                    // In this example, alpha is calculated as t / (t + dT),
                    // where t is the low-pass filter's time-constant and
                    // dT is the event delivery rate.
                    final float alpha = 0.8f;
                    // Isolate the force of gravity with the low-pass filter.
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];


                    // Remove the gravity contribution with the high-pass filter.
                    lin_acc_data[acc_data_line][0] = event.values[0] - gravity[0];
                    lin_acc_data[acc_data_line][1] = event.values[1] - gravity[1];
                    lin_acc_data[acc_data_line][2] = event.values[2] - gravity[2];
                    lin_acc_data[acc_data_line][3] = event.timestamp;
                */

                    double gx, gy, gz;
                    gx = (double) gravity[0];
                    gy = (double) gravity[1];
                    gz = (double) gravity[2];
                    // Modulo del vector gravedad
                    mod_grav = (float) sqrt(pow(gx, 2) + pow(gy, 2) + pow(gz, 2));
                    // Proyeccion de la aceleracion sobre gravedad
                    if (mod_grav > 0) {
                        /*
                         * Calculo de la proyeccion de la aceleracion sobre el eje de gravedad
                         * */
                        // producto escalar: a·b = a1*b1 + a2*b2 + a3*b3
                        // Proyeccion de la aceleracion lineal sobre gravedad

                        float lin_accelerometer_projection = (
                                (lin_acc_data[acc_data_line][0] * gravity[0]) +
                                        (lin_acc_data[acc_data_line][1] * gravity[1]) +
                                        (lin_acc_data[acc_data_line][2] * gravity[2])) / mod_grav;

                        acc_proj_data.add(acc_proj_data.size(), new float[]{lin_accelerometer_projection, event.timestamp});
                    } else {
                        //TODO:
                        // si por alguna razon el modulo de la gravedad es 0...

                    }
                    //Log.d(TAG, "SENSORDATA GRAV: " + mod_grav);
                    //Log.d(TAG, "SENSORDATA LIN_ACC: " + "x:" + lin_acc_data[acc_data_line][0] + " Y:" + lin_acc_data[acc_data_line][1] + " Z:" + lin_acc_data[acc_data_line][2]);

                    acc_data_line++;
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                //Log.d(TAG, "Gyro Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2]);
                if (gyr_data_line < m) {
                    gyr_data[gyr_data_line][0] = event.values[0];
                    gyr_data[gyr_data_line][1] = event.values[1];
                    gyr_data[gyr_data_line][2] = event.values[2];

                    // Modulo del vector gravedad
                    double gx, gy, gz;
                    gx = (double) gravity[0];
                    gy = (double) gravity[1];
                    gz = (double) gravity[2];
                    mod_grav = (float) sqrt(pow(gx, 2) + pow(gy, 2) + pow(gz, 2));
                    if (mod_grav > 0) {

                        // ArrayList para almacenar los valores de la proyeccion del gyr sobre gravity
                        float gyroscope_projection = ((event.values[0] * gravity[0]) +
                                (event.values[1] * gravity[1]) +
                                (event.values[2] * gravity[2])) / mod_grav;

                        gyr_proj_data.add(gyr_proj_data.size(), new float[]{gyroscope_projection, event.timestamp});

                    } else {
                        //TODO:
                        // Si por alguna razon el modulo de la gravedad es cero...
                    }


                    gyr_data_line++;
                }
                break;
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /**
     * Al salir de la actividad desactiva los escuchadores
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        sensorManager.unregisterListener(this, mGravity);
        sensorManager.unregisterListener(this, mMag);
    }

    public static class CreateFiles extends AsyncTask<String, Void, Void>{


        @Override
        protected Void doInBackground(String... data) {


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
