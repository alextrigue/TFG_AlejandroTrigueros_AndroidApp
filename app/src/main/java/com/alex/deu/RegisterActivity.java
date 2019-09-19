package com.alex.deu;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class RegisterActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    Context mContext;


    private static final String TAG = RegisterActivity.class.getSimpleName();

    //private static final int m = 9000;//n = 3;
    private static final int delay = 20000;
    // Para 10000 microseconds con 6000 muestras (m) tenemos 1 minutos de datos (100 muestras por s)
    // Para DELAY_GAME con 3000 muestras (m), una cada 20,000 micro.s, tenemos 1 minutos de datos
    // Para DELAY_NORMAL con 300 muestras (m), una  cada 200,000 micro.s, tenemos 1 minutos de datos


    private SensorManager sensorManager;
    private Sensor mAcce, mGyro, mGravity, mMag;
    private TextView textData, textSpeed;
    private EditText editText;
    private Button start, stop;


    // GRAVITY
    //Almacena la ultima actualizacion del sensor de gravedad
    private float[] gravity = null;

    // MAGNETIC FIELD
    // Almecena los datos del sensor Magnetic Field
    private float[] magnetic;

    // ACCELEROMETER
    //private float[][] acc_data_array;
    private ArrayList<float[]> acc_data;
    //private int acc_data_line = 0;


    //Proyeccion de la aceleración lineal sobre el vector gravedad
    private ArrayList<float[]> acc_proj_data;

    // GYROSCOPE
    private ArrayList<float[]> gyr_data;
    //private int gyr_data_line = 0;
    //Proyección del giroscopio sobre gravedad
    private ArrayList<float[]> gyr_proj_data;

    // ORIENTATION
    float[] orientation;
    private ArrayList<float[]> orientation_data;//se almacenan todos los cambios de la matriz


    /**
     * Matriz de rotacion
     */
    private float[] rotMatrix;



    private String provider;
    private LocationManager locationManager;
    private Location location;
    private static final int REQUEST_LOCATION = 111;
    private float speed;

    public Context getContext() {
        return this.mContext = this;
    }

    /**
     * Inicializa las variables globales
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        speed = 0.0f;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Log.d(TAG, "onCreate: Inicializando sensores");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAcce = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        textData = findViewById(R.id.textView_accData);
        textSpeed = findViewById(R.id.textView_speed);
        editText = findViewById(R.id.editText_file_name);
        start = findViewById(R.id.button_start);
        stop = findViewById(R.id.button_stop);
        stop.setEnabled(false);
        //SENSOR_DELAY_NORMAL 200.000 microseconds
        //SENSOR_DELAY_UI 60.000 microseconds
        //SENSOR_DELAY_GAME 20.000 microseconds
        //SENSOR_DELAY_FASTEST 0 microseconds
        //Se puede inidicar el delay en microseconds tambien

        //acc_data_array = new float[m][n + 1];//la cuarta columna es el timestamp
        //lin_acc_data = new float[m][n + 1];
        //gyr_data_array = new float[m][n + 1];

        acc_data = new ArrayList<>();//float[]
        gyr_data = new ArrayList<>();//float[]

        gravity = new float[3];
        magnetic = new float[3];
        acc_proj_data = new ArrayList<>();//float[]
        gyr_proj_data = new ArrayList<>();//float[]
        rotMatrix = new float[9];
        orientation_data = new ArrayList<>();//float[]
        orientation = new float[3];

        //Mostrar lista de ficheros guardados
        refreshFileList();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Verificar si hay permisos de localizacion asignados
        if (checkLocationPermission())
            Log.d(TAG, "Permisos de localización disponibles.");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Al terminar la aplicacion se desactivan los escuchadores y las actualizaciones de localización
     */
    @Override
    protected void onDestroy() {
        sensorManager.unregisterListener(this, mMag);
        sensorManager.unregisterListener(this, mGravity);
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        locationManager.removeUpdates(this);
        super.onDestroy();
    }

    /**
     * Actualiza el valor text del TextView textData para mostrar la lista de ficheros generados
     */
    public void refreshFileList() {
        StringBuilder text = new StringBuilder();
        int i;
        for (i = 0; i < fileList().length; i++) {
            text.append(fileList()[i]).append("\n");
        }
        textData.setText(text.toString());
    }

    /**
     * Al pulsar boton START activa los escuchadores de los sensores
     */
    public void startRegister(View view) {
        Toast toast = Toast.makeText(this,
                "START: Registrando actividad de sensores", Toast.LENGTH_SHORT);
        toast.show();
        start.setEnabled(false);
        stop.setEnabled(true);

        textData.setText(getString(R.string.data_reg_warn));

        if (mAcce != null) {
            //acc_data_line = 0;
            sensorManager.registerListener(this, mAcce, delay);
            Log.d(TAG, "startRegister: Registrando el sensor ACCELEROMETER");
        } else {
            Log.d(TAG, "startRegister: ACCELEROMETER NO DISPONIBLE");
        }
        if (mGyro != null) {
            //gyr_data_line = 0;
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

        if (mGravity != null) {
            //sensorManager.registerListener(this, mGravity, delay);
            sensorManager.registerListener(this, mGravity, delay);
            Log.d(TAG, "startRegister: Registrando el sensor GRAVITY");
        } else {
            Log.d(TAG, "startRegister: Sensor GRAVITY NO DISPONIBLE");
        }
    }


    /**
     * Desactiva los escuchadores de los sensores y se inicia la generación de archivos
     */
    public void stopRegister(View view) {
        sensorManager.unregisterListener(this, mMag);
        sensorManager.unregisterListener(this, mGravity);
        sensorManager.unregisterListener(this, mAcce);
        sensorManager.unregisterListener(this, mGyro);
        Log.d(TAG, "stopRegister: listeners unregistered");
        //start.setEnabled(true);
        stop.setEnabled(false);

        if (!orientation_data.isEmpty()) {
            new CreateFiles().execute("Creando archivos...");
        }
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
            toast = Toast.makeText(this, "Fichero eliminado", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            toast = Toast.makeText(this, "Ya se han eliminaod todos los archivos.", Toast.LENGTH_SHORT);
            toast.show();
        }
        //
        // Actializar lista de ficheros guardados
        refreshFileList();
    }

    /**
     * Se ejetuca al cambiar el valor de los sensores
     * Almacena los datos de sensores en ArrayList
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float mod_grav;
        float ts;

        switch (event.sensor.getType()) {
            default:
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                ts = event.timestamp;
                magnetic = event.values;
                //magnetic_data.add(magnetic_data.size(), event.values);
                //Log.d(TAG, "magnetic_data.size(): " + magnetic_data.size());
                //Log.d(TAG, "Magnetic timestamp: " + event.timestamp);
                // ROTATION MATRIX
                boolean check = SensorManager.getRotationMatrix(rotMatrix, null, gravity, magnetic);
                if (check) {
                    // Si se ha obtenido de forma correcta la matriz de rotacion
                    SensorManager.getOrientation(rotMatrix, orientation);
                    //Log.d(TAG, "Orientation: " + orientation[0] + ", " + orientation[1] + ", " + orientation[2]);
                    orientation_data.add(new float[]{
                            orientation[0],
                            orientation[1],
                            orientation[2],
                            ts,
                            speed
                    });
                }
                break;

            case Sensor.TYPE_GRAVITY:
                //Log.d(TAG, "Gravity Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2]);
                gravity = event.values;
                break;

            case Sensor.TYPE_ACCELEROMETER:
                ts = event.timestamp;
                //Log.d(TAG, "Acce Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2] + " TS:" + event.timestamp);
                /*
                    acc_data_array[acc_data_line][0] = event.values[0];
                    acc_data_array[acc_data_line][1] = event.values[1];
                    acc_data_array[acc_data_line][2] = event.values[2];
                    acc_data_array[acc_data_line][3] = event.timestamp;
                */
                acc_data.add(new float[]{
                        event.values[0],
                        event.values[1],
                        event.values[2],
                        ts
                });

                /*
                CALCULO DE ACELERACION LINEAL
                Fuente: https://developer.android.com/guide/topics/sensors/sensors_motion#java
                */
                float[] linear_acceleration = new float[3];
                linear_acceleration[0] = event.values[0] - gravity[0];
                linear_acceleration[1] = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];



                // Modulo del vector gravedad
                mod_grav = (float) sqrt(pow(gravity[0], 2) + pow(gravity[1], 2) + pow(gravity[2], 2));
                // Proyeccion de la aceleracion sobre gravedad

                if (mod_grav == 0) {
                    //Log.d(TAG, "ATENCION: Modulo vector gravedad igual a 0: forzando mod_grav = 9.8");
                    mod_grav = 9.8f;
                }
                /*
                 * Calculo de la proyeccion de la aceleracion sobre el eje de gravedad
                 * */
                // producto escalar: a·b = a1*b1 + a2*b2 + a3*b3
                // Proyeccion de la aceleracion lineal sobre gravedad

                float lin_accelerometer_projection = (
                        (linear_acceleration[0] * gravity[0]) +
                                (linear_acceleration[1] * gravity[1]) +
                                (linear_acceleration[2] * gravity[2])) / mod_grav;

                acc_proj_data.add(new float[]{
                        lin_accelerometer_projection,
                        ts
                });


                //Log.d(TAG, "SENSORDATA GRAV: " + mod_grav);
                //Log.d(TAG, "SENSORDATA LIN_ACC: " + "x:" + lin_acc_data[acc_data_line][0] + " Y:" + lin_acc_data[acc_data_line][1] + " Z:" + lin_acc_data[acc_data_line][2]);
                break;

            case Sensor.TYPE_GYROSCOPE:
                ts = event.timestamp;
                //Log.d(TAG, "Gyro Changed: X:" + event.values[0] + " Y:" + event.values[1] + " Z:" + event.values[2]);
                /*
                gyr_data_array[gyr_data_line][0] = event.values[0];
                gyr_data_array[gyr_data_line][1] = event.values[1];
                gyr_data_array[gyr_data_line][2] = event.values[2];
*/
                gyr_data.add(new float[]{
                        event.values[0],
                        event.values[1],
                        event.values[2],
                        ts
                });

                // Modulo del vector gravedad
                mod_grav = (float) sqrt(pow(gravity[0], 2) + pow(gravity[1], 2) + pow(gravity[2], 2));

                if (mod_grav == 0) {
                    //Log.d(TAG, "ATENCION: Modulo vector gravedad igual a 0: forzando mod_grav = 9.8");
                    mod_grav = 9.8f;
                }

                // ArrayList para almacenar los valores de la proyeccion del gyr sobre gravity
                float gyroscope_projection = ((event.values[0] * gravity[0]) +
                        (event.values[1] * gravity[1]) +
                        (event.values[2] * gravity[2])) / mod_grav;
                gyr_proj_data.add(gyr_proj_data.size(), new float[]{
                        gyroscope_projection,
                        ts
                });


                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /**
     * Al cambiar la ubicacion se extrae la velocidad de depslazamiento
     * El valor original está en m/s y se pasa a K/h
     */
    @Override
    public void onLocationChanged(Location loc) {
        speed = loc.getSpeed() * 3600 / 1000; // Velocidad en kilometros por hora
        location = loc;
        Log.d(TAG, "Location: " + location + "\nSpeed: " + speed);
        String str = String.format(Locale.getDefault(), "%s %.1f", "Velocidad (km/h):", speed);
        textSpeed.setText(str);
        //Toast.makeText(getContext(),"Speed: " + speed, Toast.LENGTH_SHORT).show();
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

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostrar mensaje explicando porqué necesitamos permisos de localizacion
                Toast toast = Toast.makeText(this,
                        getString(R.string.mensajeSolicitudPermisosLocalizacion), Toast.LENGTH_LONG);
                toast.show();
                Log.d(TAG, getString(R.string.mensajeSolicitudPermisosLocalizacion));
            }

            // Solicitar permisos de localización --> implementar onRequestPermissionsResult()
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION,},
                    REQUEST_LOCATION);

            return false;
        } else {
            Log.d(TAG, "Permisos de localización concedidos, obteniendo localización...");
            provider = locationManager.getBestProvider(new Criteria(), false);
            //Obtener localización
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, 150, 0, this);
                // minTime en milisegundos
                // minDistance en metros
                location = locationManager.getLastKnownLocation(provider);
                //onLocationChanged(location);
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permisos de localización concedidos, obteniendo localización...");
                // Permisos concedidos
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    provider = locationManager.getBestProvider(new Criteria(), false);
                    //Solicitar localizacion
                    if (provider != null) {
                        locationManager.requestLocationUpdates(provider, 150, 0, this);
                        // minTime en milisegundos
                        // minDistance en metros
                        location = locationManager.getLastKnownLocation(provider);
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    } else {
                        Log.e(TAG, "Error obteniendo localización. Error Provider.");
                    }
                }
            } else {
                // Si el usuario no concede acceso a los servicios de localización,
                // la actividad termina su ejecición
                Log.e(TAG, "No se han concedido permisos para obtener localización.");
                Toast toast = Toast.makeText(this,
                        getString(R.string.mensajeSolicitudPermisosLocalizacion), Toast.LENGTH_LONG);
                toast.show();
                finish();
            }
        }
    }


    /**
     * Clase Async Task para la generación de los ficheros
     * sin parar el hilo de ejecucion principal de la app
     */
    @SuppressLint("StaticFieldLeak")
    public class CreateFiles extends AsyncTask<String, Integer, Integer> {
        private static final String TAG = "CreateFiles";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textData.setText(getString(R.string.file_reg_warn));
        }

        @Override
        protected Integer doInBackground(String... strings) {
            Log.d(TAG, strings[0]);
            String acc_str,
                    gyr_str,
                    acc_proj_str,
                    gyr_proj_str,
                    orientation_str;

            if (acc_data != null) {
                Log.d(TAG, "Creating FILE acc_data...");
                acc_str = listToString(acc_data);
                makeDataFile(acc_str, 1);
                acc_data.clear();
                publishProgress();
            }
            if (gyr_data != null) {
                Log.d(TAG, "Creating FILE gyr_data...");
                gyr_str = listToString(gyr_data);
                makeDataFile(gyr_str, 2);
                //gyr_data_array = null;
                gyr_data.clear();
                publishProgress();
            }

            if (acc_proj_data != null) {
                Log.d(TAG, "Creating FILE acc_proj_data...");
                acc_proj_str = listToString(acc_proj_data);
                makeDataFile(acc_proj_str, 4);
                acc_proj_data.clear();
                publishProgress();
            }
            if (gyr_proj_data != null) {
                Log.d(TAG, "Creating FILE gyr_proj_data...");
                gyr_proj_str = listToString(gyr_proj_data);
                makeDataFile(gyr_proj_str, 5);
                gyr_proj_data.clear();
                publishProgress();
            }

            if (orientation_data != null) {
                Log.d(TAG, "Creating FILE orientation_data...");
                orientation_str = listToString(orientation_data);
                makeDataFile(orientation_str, 8);
                orientation_data.clear();
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //Mostrar lista de ficheros guardados
            refreshFileList();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            //Mostrar lista de ficheros guardados
            refreshFileList();
            start.setEnabled(true);

        }

        /**
         * Recibe un ArrayList cuyos elementos son arrays de float[]
         *
         * @return string listo para imprimir en un fichero
         */
        private String listToString(ArrayList<float[]> list) {
            StringBuilder str = new StringBuilder();
            if (list != null) {
                if (list.size() > 0) {
                    for (int r = 0; r < list.size(); r++) {
                        for (int i = 0; i < list.get(r).length - 1; i++) {
                            str.append(list.get(r)[i]).append(", ");
                        }
                        str.append(list.get(r)[list.get(r).length - 1]).append("\n");
                    }
                    return str.toString();
                }
            }
            return "";
        }

        /**
         * Recibe los datos en formato string y los guarda en un fichero
         */
        private void makeDataFile(String data, int tipo) {
            // Tipo 1: acelerometro
            // Tipo 2: giroscopio
            // Tipo 3: acelerometro lineal
            // Tipo 4: proyecccion de aceleracion lineal sobre vector gravedad (acc_proj_data)
            // Tipo 5: proyeccion de giroscopio sobre vector gravedad (gyr_proj_data)
            // Tipo 6: matriz de rotacion (rotMatrix_data)
            // Tipo 7: timestamp de las matriz de rotacion
            // Tipo 8: orientacion

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

            String filename;
            String name;

            // Eleccion del nombre del fichero segun su tipo
            if (editText.getText().toString().equals("")) {
                name = year + "-" + month + "-" + day + "_" + hour + "-" + min + "-" + sec;
            } else {
                name = editText.getText().toString();
            }

            switch (tipo) {
                case 1:
                    filename = "acc_" + name + extension;
                    break;
                case 2:
                    filename = "gyr_" + name + extension;
                    break;
                case 3:
                    filename = "lin_" + name + extension;
                    break;
                case 4:
                    filename = "az_" + name + extension;
                    break;
                case 5:
                    filename = "gz_" + name + extension;
                    break;
                case 6:
                    filename = "rotMat_" + name + extension;
                    break;
                case 7:
                    filename = "rotMat_ts_" + name + extension;
                    break;
                case 8:
                    filename = "orientation_" + name + extension;
                    break;
                default:
                    filename = "000" + name + extension;
                    break;
            }

            File directory = getContext().getFilesDir();
            new File(directory, filename);//Crear fichero
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
    }
}
