package com.alex.deu;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class TurnActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private long i_interval;

    private static final String TAG = MainActivity.class.getSimpleName();

    // Elementos UI
    private Button start_button, stop_button;
    private TextView velocity_tv, anglechange_tv, radio_tv;

    // Sensores
    private static final int SENSOR_DELAY = 20000; //microsegundos
    private SensorManager sensorManager;
    private Sensor mAccelerometer, mGyroscope, mGravity, mMagneticField;

    // Variables globales para datos de los sensores
    private float[] gravity;
    private ArrayList<Float> az;
    private ArrayList<Float> gz;
    private ArrayList<Float> azimuth;
    private ArrayList<Long> timestamp;

    // Localizacion
    private String provider;
    private LocationManager locationManager;
    private float speed;
    private ArrayList<Float> velocity;
    private Location location;
    private static final int REQUEST_LOCATION = 111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_turn);

        start_button = findViewById(R.id.start);
        stop_button = findViewById(R.id.stop);
        velocity_tv = findViewById(R.id.velocity_value);
        anglechange_tv = findViewById(R.id.anglechange_value);
        radio_tv = findViewById(R.id.radio_value);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        gravity = new float[3];
        az = new ArrayList<>();
        gz = new ArrayList<>();
        azimuth = new ArrayList<>();
        timestamp = new ArrayList<>();
        velocity = new ArrayList<>();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkLocationPermission()) {
            Log.d(TAG, "Permisos de localización disponibles.");
        }
    }

    @Override
    protected void onDestroy() {
        startStop(stop_button);
        super.onDestroy();
    }

    /**
     * Metodos de SensorEventListener
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float mod_grav;
        long ts;

        switch (sensorEvent.sensor.getType()) {

            case Sensor.TYPE_MAGNETIC_FIELD:
                //Log.d(TAG, "Magnetic:" + sensorEvent.values[0]);
                ts = sensorEvent.timestamp;
                float[] R = new float[9];
                float[] orientation = new float[3];
                if (SensorManager.getRotationMatrix(R, null, gravity, sensorEvent.values)) {
                    // si se obtiene correctamente la matriz de rotacion, obtenemos orientacion
                    SensorManager.getOrientation(R, orientation);
                    //nos quedamos con componente 1 de orientacion: azimuth
                    azimuth.add(orientation[0]);
                    velocity.add(speed);
                    timestamp.add(ts);
                    if (timestamp.size() == 1) {
                        i_interval = ts;
                    }

                    if (ts - i_interval >= 1000000000) {
                        /*
                        Log.d(TAG, "Length Az: " + az.size());
                        Log.d(TAG, "Length Gz: " + gz.size());
                        Log.d(TAG, "Length Azimuth: " + azimuth.size());
                        Log.d(TAG, "Length Velocity: " + velocity.size());
                        */
                        //i_interval = ts;
                        //Log.d(TAG,"AZ: " + az_array[10].toString());
                        //Object[] objectArray = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
                        new TurnDetection().execute(
                                az.toArray(),
                                gz.toArray(),
                                azimuth.toArray(),
                                velocity.toArray(),
                                timestamp.toArray());
                        az.clear();
                        gz.clear();
                        azimuth.clear();
                        velocity.clear();
                        timestamp.clear();

                    }
                }



                /*
                Si ha pasado un segundo iniciar los calculos para la deteccion de giro en Turn Detection
                pasar los datos necesarios
                resetear variables globales
                */

                break;

            case Sensor.TYPE_GRAVITY:
                //Log.d(TAG, "Gravity: " + sensorEvent.values[0] + ", " + sensorEvent.values[1] + ", " + sensorEvent.values[2]);
                gravity = sensorEvent.values;
                break;

            case Sensor.TYPE_ACCELEROMETER:
                // Log.d(TAG, "Accelerometer:" + sensorEvent.values[0]);
                float[] linear_acceleration = new float[3];
                linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
                linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
                linear_acceleration[2] = sensorEvent.values[2] - gravity[2];

                // Modulo del vector gravedad
                mod_grav = (float) sqrt(pow(gravity[0], 2) + pow(gravity[1], 2) + pow(gravity[2], 2));
                // Proyeccion de la aceleracion sobre gravedad
                //Log.d(TAG, "mod_grav = " + mod_grav);

                if (mod_grav == 0) {
                    //Log.d(TAG, "ATENCION: Modulo vector gravedad igual a 0: forzando mod_grav = 9.8");
                    mod_grav = 9.8f;
                }
                /*
                 * Calculo de la proyeccion de la aceleracion sobre el eje de gravedad
                 * */
                // producto escalar: a·b = a1*b1 + a2*b2 + a3*b3
                // Proyeccion de la aceleracion lineal sobre gravedad

                az.add(((linear_acceleration[0] * gravity[0]) +
                        (linear_acceleration[1] * gravity[1]) +
                        (linear_acceleration[2] * gravity[2])) / mod_grav);


                break;

            case Sensor.TYPE_GYROSCOPE:
                //Log.d(TAG, "Gyroscope:" + sensorEvent.values[0]);
                // Modulo del vector gravedad
                mod_grav = (float) sqrt(pow(gravity[0], 2) + pow(gravity[1], 2) + pow(gravity[2], 2));
                //Log.d(TAG, "mod_grav = " + mod_grav);

                // Proyeccion de la aceleracion sobre gravedad
                if (mod_grav == 0) {
                    //Log.d(TAG, "ATENCION: Modulo vector gravedad igual a 0: forzando mod_grav = 9.8");
                    mod_grav = 9.8f;
                }

                gz.add(((sensorEvent.values[0] * gravity[0]) +
                        (sensorEvent.values[1] * gravity[1]) +
                        (sensorEvent.values[2] * gravity[2])) / mod_grav);

                break;

            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public void startStop(View view) {

        if (view.getId() == R.id.start) {
            Snackbar.make(findViewById(R.id.constraint_layout), "START", Snackbar.LENGTH_LONG).show();
            registerSensors();
            start_button.setEnabled(false);
            stop_button.setEnabled(true);
        }
        if (view.getId() == R.id.stop) {
            Snackbar.make(findViewById(R.id.constraint_layout), "STOP", Snackbar.LENGTH_LONG).show();
            unregisterSensors();
            start_button.setEnabled(true);
            stop_button.setEnabled(false);
            velocity_tv.setText("");
            anglechange_tv.setText("");
        }
    }

    public void registerSensors() {
        //Toast.makeText(this, "Sensores registrados", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Registrando listeners de sensores...");
        sensorManager.registerListener(this, mAccelerometer, SENSOR_DELAY);
        sensorManager.registerListener(this, mGyroscope, SENSOR_DELAY);
        sensorManager.registerListener(this, mGravity, SENSOR_DELAY);
        sensorManager.registerListener(this, mMagneticField, SENSOR_DELAY);
    }

    public void unregisterSensors() {
        sensorManager.unregisterListener(this, mAccelerometer);
        sensorManager.unregisterListener(this, mGyroscope);
        sensorManager.unregisterListener(this, mGravity);
        sensorManager.unregisterListener(this, mMagneticField);
    }

    /**
     * Metodos de LocationManager
     */
    @Override
    public void onLocationChanged(Location location) {
        speed = location.getSpeed(); // Velocidad en metros por segundo
        //Log.d(TAG, "Location: " + location + "\nSpeed: " + speed);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    /**
     * Verifica si los permisos estan concedidos. Si no lo estan los solicita
     * <p>
     * <p>
     * Fuente: https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime
     */
    public boolean checkLocationPermission() {
        if (
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //...Mostrar algun mensaje explicando porqué necesitamos permisos de localizacion
                Log.d(TAG, "Explanation: se requiere acceso a la ubicacion.");
            } else {
                // Solicitar permisos de localización --> implementar onRequestPermissionsResult()
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION,},
                        REQUEST_LOCATION);
            }
            return false;
        } else {
            Log.d(TAG, "Permisos de localización disponibles, obteniendo localización...");
            provider = locationManager.getBestProvider(new Criteria(), false);
            //Solicitar localizacion
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, 200, 0, this);
                //minTime en milisegundos
                // minDistance en metros
                location = locationManager.getLastKnownLocation(provider);
                return true;

            } else {
                Log.e(TAG, "Error obteniendo localizaión. Error de provider.");
                return false;
            }
        }
    }

    /**
     * Este método se ejecuta al responder a la solicitud de permisos de localización
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permisos de localización obtenidos, obteniendo localización...");
                //Acciones necesarias si aplica

                // permission was granted, yay! Do the
                // location-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    provider = locationManager.getBestProvider(new Criteria(), false);
                    //Solicitar localizacion
                    if (provider != null) {
                        locationManager.requestLocationUpdates(provider, 200, 0, this);
                        //minTime en milisegundos
                        // minDistance en metros
                        location = locationManager.getLastKnownLocation(provider);

                    } else {
                        Log.e(TAG, "Error obteniendo localizaión. Error de Provider.");
                    }
                }

            } else {
                Log.e(TAG, "No hay permisos para obtener localización.");
                //Acciones necesarias si aplica
            }
        }
    }


    /**
     * Clase AsyncTask
     */
    public class TurnDetection extends AsyncTask<Object[], Integer, Integer> {

        private double angle_change;
        private double vel_med;
        private double radio_giro;


        @Override
        protected Integer doInBackground(Object[]... sensorData) {

            long tic = System.nanoTime();

            double[] az = objectToDoubletArray(sensorData[0]);
            double[] gz = objectToDoubletArray(sensorData[1]);
            double[] azi = objectToDoubletArray(sensorData[2]);
            double[] vel = objectToDoubletArray(sensorData[3]);
            Object[] ts = sensorData[4];
            double t = (double)((long)ts[ts.length-1] - (long)ts[0]);

            double[] azi_rect = rectAzimuth(azi);
            angle_change = (azi_rect[azi_rect.length - 1] - azi_rect[0]) * 180 / Math.PI;
            vel_med = media(vel);
            radio_giro = radioGiro(angle_change,vel_med,t);



            long toc = (System.nanoTime() - tic);
            Log.d(TAG, "Tiempo de ejecución de TurnDetection (ns): " + toc);
            return null;
        }

        private double[] objectToDoubletArray(Object[] objects) {
            double[] doubles = new double[objects.length];
            for (int i = 0; i < objects.length; i++) {
                doubles[i] = (double) (float) objects[i];
            }
            //Log.d(TAG, "Datos: " + Arrays.toString(floats) + "\nSize: " + floats.length);
            return doubles;
        }

        private double media(double[] datos) {
            double result = 0;
            for (double dato : datos) {
                result += dato;
            }
            return result / datos.length;
        }


        private double[] rectAzimuth(double[] azimuth) {
            int i = 0;
            int l = azimuth.length;
            while (i < l) {
                azimuth[i] += Math.PI;
                i++;
            }
            double margen_rect = 0.97 * 2 * Math.PI;
            for (i = 1; i < l; i++) {
                if ((azimuth[i] - azimuth[i - 1]) < -margen_rect) {
                    for (int r = i; r < l; r++) {
                        azimuth[r] = azimuth[r] + (2 * Math.PI);
                    }
                }
                if ((azimuth[i] - azimuth[i - 1]) > margen_rect) {
                    for (int r = i; r < l; r++) {
                        azimuth[r] = azimuth[r] - (2 * Math.PI);
                    }
                }
            }
            return azimuth;
        }

        private double radioGiro(double angle_change, double speed, double time) {
            double r;
            double umbral_angulo = 12;
            time = time/1000000000; // de nanosegundos a segundos
            // Si el cambio de angulo es de 12 grados o mas, caluculamos el raid ode giro
            // Sirve para evitar el calculo de radios de giro demasiado grandes
            // que pueden interpretarse como ruido al andar en linea recta
            if (angle_change > umbral_angulo){
                // velocidad en m/s
                // tiempo en segundos
                // angulo de giro en radianes
                // longitudes en metros
                double longitud = speed * time;// metros
                r = longitud / angle_change;
            }else{
                r = 0;
            }
            return r;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            velocity_tv.setText("" + vel_med);
            anglechange_tv.setText("" + angle_change);
            radio_tv.setText(""+radio_giro);
            Log.d(TAG, "\nVelocidad: " + vel_med + "\nAngleChange/sec: " + angle_change + "\nRadio giro: " + radio_giro);
        }
    }
}
