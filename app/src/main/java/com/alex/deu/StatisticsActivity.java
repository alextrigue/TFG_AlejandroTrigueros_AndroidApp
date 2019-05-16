package com.alex.deu;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import static java.lang.Math.pow;
import static java.lang.StrictMath.sqrt;

public class StatisticsActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = StatisticsActivity.class.getSimpleName();

    private SensorManager sensorManager;
    private Sensor mAcce, mGyro, mGrav;
    private static final int delay = 20000;
    //20.000 microsegundos -> 50 muestras/segundo
    // Para 10000 microseconds -> 100 muestras/segundo


    private int data_size;//Tamaño del array para calcular estadisticos

    private TextView tv_med, tv_std, tv;

    // guarda los valores x, y, z cada ez que cambia el sensor grav
    private float[] gravityValues;
    private double[] array_az;
    private int az_index;

    private double[] array_gz;
    private int gz_index;

    private ArrayList<Double> a_medias, a_desv_tipicas, g_medias, g_desv_tipicas;


    /**
     * Inicializacion de sensores y elementos view
     */
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

    /**
     * Al iniciar activity regustrar escuchadores de sensores
     */
    public void startStatistics(View view) {
        if (mAcce != null) {
            sensorManager.registerListener(this, mAcce, delay);
            Log.d(TAG, "START: Registrando el sensor acelerometro");
        } else {
            tv.setText(tv.getText().toString() + "\nAcc no disponible");
        }
        if (mGrav != null) {
            sensorManager.registerListener(this, mGrav, delay);
            Log.d(TAG, "START: Registrando el sensor gravedad");
        } else {
            tv.setText(tv.getText().toString() + "\nGrav no disponible");
        }
        if (mGyro != null) {
            sensorManager.registerListener(this, mGyro, delay);
            Log.d(TAG, "START: Registrando el sensor giroscopio");
        } else {
            tv.setText(tv.getText().toString() + "\nGyro no disponible");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //delay = 20000;
        //20.000 microsegundos -> 50 muestras/segundo
        double intervalo = 4;//segundos
        double delay_seg = (double) delay / 1000000;//pasar de microsegundos a segundos
        double muestras_seg = 1 / ((double) delay_seg);
        data_size = (int) (intervalo * muestras_seg);//Tamaño del array para calcular estadisticos


        // guarda los valores x, y, z cada ez que cambia el sensor grav
        gravityValues = new float[3];
        array_az = new double[data_size];
        az_index = 0;

        array_gz = new double[data_size];
        gz_index = 0;

        a_medias = new ArrayList<Double>();
        a_desv_tipicas = new ArrayList<Double>();
        g_medias = new ArrayList<Double>();
        g_desv_tipicas = new ArrayList<Double>();
    }

    /**
     * Apagar escuchadores
     */
    public void stopStatistics(View view) {
        sensorManager.unregisterListener(this, mGrav);
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        Log.d(TAG, "STOP: Unregistering sensor listeners");


        makeStatisticsFiles();



        /*
         * Resetear variables
         * */
        gravityValues = new float[3];
        array_az = new double[data_size];
        az_index = 0;

        array_gz = new double[data_size];
        gz_index = 0;

        a_medias = new ArrayList<Double>();
        a_desv_tipicas = new ArrayList<Double>();
        g_medias = new ArrayList<Double>();
        g_desv_tipicas = new ArrayList<Double>();
    }


    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, mGrav);
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        Log.d(TAG, "onPause: Unregistering sensor listeners");

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        double mod_grav = 0;
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
                mod_grav = sqrt(pow((double) gravityValues[0], 2) + pow((double) gravityValues[1], 2) + pow((double) gravityValues[2], 2));
                if (mod_grav != 0) {
                    /*
                     * Si mod_grav distinito de cero podemos calcular la proyeccion en el vector gravedad
                     * como vector unitario dividiendo por el modulo del vector gravedad
                     * */
                    array_az[az_index] = (double) ((linear_acc[0] * gravityValues[0]) +
                            (linear_acc[1] * gravityValues[1]) +
                            (linear_acc[2] * gravityValues[2])) / mod_grav;
                    az_index++;
                } else {
                    /*
                     * Si mod_grav es 0 el valor de la proyección es 0
                     * */
                    array_az[az_index] = 0;
                    az_index++;
                }
                /*
                 * Si se llena el array se calculan los estadisticos y resetea el array
                 * */
                if (array_az[data_size - 1] != 0) {
                    double med = media(array_az);
                    Log.d(TAG, "Media ACC: " + med);
                    double desv = desvTipica(array_az, med);
                    Log.d(TAG, "Desviacion Tipica ACC: " + desv);
                    array_az = new double[data_size];
                    az_index = 0;

                    a_medias.add(med);
                    a_desv_tipicas.add(desv);
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                /*GYROSCOPE*/
                //Log.d(TAG, "GYRO Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2] + " TS:" + event.timestamp);
                mod_grav = sqrt(pow((double) gravityValues[0], 2) + pow((double) gravityValues[1], 2) + pow((double) gravityValues[2], 2));
                if (mod_grav != 0) {
                    /*
                     * Si mod_grav distinito de cero podemos calcular la proyeccion en el vector gravedad
                     * como vector unitario dividiendo por el modulo del vector gravedad
                     * */
                    array_gz[gz_index] = (double) ((event.values[0] * gravityValues[0]) +
                            (event.values[1] * gravityValues[1]) +
                            (event.values[2] * gravityValues[2])) / mod_grav;
                    gz_index++;
                } else {
                    /*
                     * Si mod_grav es 0 el valor de la proyección es 0
                     * */
                    array_gz[gz_index] = 0;
                    gz_index++;
                }
                /*
                 * Si se llena el array se calculan los estadisticos y resetea el array
                 * */
                if (array_gz[data_size - 1] != 0) {

                    double med = media(array_gz);
                    Log.d(TAG, "Media GYRO: " + med);

                    double desv = desvTipica(array_gz, med);
                    Log.d(TAG, "Desviacion Tipica GYRO: " + desv);
                    array_gz = new double[data_size];
                    gz_index = 0;

                    g_medias.add(med);
                    g_desv_tipicas.add(desv);

                }
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

    /**
     * @return Calculo de la media de un array double
     */
    public double media(double[] data) {
        int index = 0;
        double sum = 0;
        while (index < data.length) {
            sum += data[index];
            index++;
        }
        return sum / data.length;
    }

    /**
     * @param data array double
     * @param med  media de data
     * @return desviacion tipica de array double
     */
    public double desvTipica(double[] data, double med) {
        /*
         * La varianza es la media de las desviaciones respecto a la media al cuadrado
         * */
        double varianza;
        int n = data.length;
        int i = 0;
        double sum = 0;
        if (med != 0) {
            while (i < data.length) {
                sum += pow(data[i] - med, 2);
                i++;
            }
            varianza = sum / n;
            // devuelve la raiz cuadrada de la varianza, es decir, la desviacion tipica
            return sqrt(varianza);
        } else {
            // si la media no se pasa como 0 se calcula aqui
            med = media(data);
            while (i < data.length) {
                sum += pow(data[i] - med, 2);
                i++;
            }
            varianza = sum / n;
            // devuelve la raiz cuadrada de la varianza, es decir, la desviacion tipica
            return sqrt(varianza);
        }
    }

    public double mediana(double[] data) {

        return 0;
    }

    //mediana

    // coef de Pearson desvTipica/media

    // coef de apertura max/min del conjunto de datos

    //simetrias/asimetrias en distribucion estatistica


    // covarianzas


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public void makeStatisticsFiles() {
        Log.d(TAG, "makeStatisticsFiles: a_medias.size = " + a_medias.size() +
                ", a_desv_tipicas.size = " + a_desv_tipicas.size());
        Log.d(TAG, "makeStatisticsFiles: g_medias.size = " + g_medias.size() +
                ", g_desv_tipicas.size = " + g_desv_tipicas.size());
        //Log.d(TAG, "MEDIAS: " + a_medias.toString());
        //Log.d(TAG, "DESV TIPICAS: " + a_desv_tipicas.toString());

        Calendar c = Calendar.getInstance();
        int sec = c.get(Calendar.SECOND);
        int min = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = 1 + c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);
        String date = year + "-" + month + "-" + day + "_" + hour + "-" + min + "-" + sec;
        String a_m, a_d, g_m, g_d;
        a_m = "a_med_"+date+".txt";
        g_m = "g_med_"+date+".txt";
        a_d = "a_des_"+date+".txt";
        g_d = "g_des_"+date+".txt";

        String str = "[0,1,2]";
        str = str.substring(1, str.length()-1);
        Log.d(TAG, "SUBSTRING: " + str);

        //TODO: quitar corchetes a los strings usando substring

        writeFile(a_m, a_medias.toString());
        writeFile(a_d, a_desv_tipicas.toString());
        writeFile(g_m, g_medias.toString());
        writeFile(g_d, g_desv_tipicas.toString());

    }

    public void writeFile(String filename, String text){
        //Crear fichero
        File directory = this.getFilesDir();
        File file = new File(directory, filename);
        FileOutputStream outputStream;

        //Escribir datos en el fichero
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            // Escritura del String de datos (String data) en el fichero de texto
            outputStream.write(text.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "FILE created:\t" + directory + "/" + filename);
    }



}
