package com.qoopa.nodosshield;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

/**
 * Created by diego.saavedra on 16/10/2015.
 */
public class LockService extends Service {

    public Context ctx;
    private Double latitude = Double.NaN;
    private Double longitude = Double.NaN;
    private static String deviceId;
    private static final String deviceIdFilePath = "DeviceId.id";
    private static final String loginIdFilePath = "LoginId.id";
    //private final String batchFilePath = "batch.txt";
    private static final String serverHandlerURL = "https://www.nodos.com.co/universal_handler.php";
    private static final String serverPendingURL = "https://www.nodos.com.co/php/event/pending.php";
    private static final String serverConfirmURL = "https://www.nodos.com.co/php/event/confirm.php";
    private Window window; //Necessary
    private String alarm;
    private String screen;
    private String delete;
    public static String state = "UNLOCKED";
    public static String CodigoBloqueo = "";
    public static String id;
    public boolean tracking = false;
    public Socket mSocket;
    private static LockService instance;
    private int versionUltima = 0;
    private int versionAnterior = 0;
    private String downloadURLUpdate = "";
    static JSONParser jsonParser = new JSONParser();
    String bateria;
    private static Integer idLogin;
    public String message;
    public int CheckStateDelayInt = 300000; //150;
    public int deviceinfodelayint = 18000000; //600; //3600
    public int geoDataDelayInt = 3600000; //3600;
    public int trackDataDelayInt = 30000; //300;
    public int offlinePoliciesDelayInt = 900000; //300; //3600
    public int PriorityQueueInt = 1000; //300; //3600 // PENDIENTE cola de proridades
    public static boolean PriorityQueueBool = false, PQ1 = false, PQ2 = false, PQ3 = false; // 1-Unlook -> Geolock -> Geounlock // PENDIENTE cola de proridades
    public static boolean geolock; // PENDIENTE

    private String tempFile;
    private boolean isGeolock = false;

    private String latitudePolitic = "";
    private String longitudePolitic = "";
    private String radiusPolitic = "";

    BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            try {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                bateria = (String.valueOf(level));
            } catch (Exception e) {
                LogUtil.printError("Error", "LockService - mBatInfoReceiver - onReceive");
                LogUtil.printFullError("Error", "LockService - mBatInfoReceiver - onReceive", e);
            }
        }
    };

    //****************************** Getters ******************************

    public static String getState() {
        return state;
    }

    public static String getCodigoBloqueo() {
        //= Codigo;
        return CodigoBloqueo;
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static LockService getInstance() {
        return instance;
    }

    //****************************** Setters ******************************

    public static void setState(String newState) {
        state = newState;
    }

    public static void setCodigoBloqueo(String Codigo) {
        Log.e("Seguimiento", "Codigo de Desbloqueo: " + Codigo);
        CodigoBloqueo = Codigo;
    }

    //****************************** Others ******************************

    public static void ActValuesPQ(String Nombre, String valor) {

        switch (Nombre) {
            case "PriorityQueueBool":
                if (valor.equals("true")) PriorityQueueBool = true;
                else if (valor.equals("false")) PriorityQueueBool = false;
                else LogUtil.printError("Error","Error validando PQB");
                break;
            case "PQ1":
                if (valor.equals("true")) PQ1 = true;
                else if (valor.equals("false")) PQ1 = false;
                else LogUtil.printError("Error","Error validando PQ1");
                break;
            case "PQ2":
                if (valor.equals("true")) PQ2 = true;
                else if (valor.equals("false")) PQ2 = false;
                else LogUtil.printError("Error","Error validando PQ2");
                break;
            case "PQ3":
                if (valor.equals("true")) PQ3 = true;
                else if (valor.equals("false")) PQ3 = false;
                else LogUtil.printError("Error","Error validando PQ3");
                break;
            default:
                LogUtil.printError("Error","Error validando PQ");
                break;

        }

//        PriorityQueueBool = true;
//        PQ1 = true;
//        PQ2 = true;
//        PQ3 = true;
    }

    public static void ReUnlock() {
        PriorityQueueBool = true;
        PQ1 = true;
//        //**************************************
//        //Pruebas Database
//        Database mydb = new Database(getApplicationContext());
//        mydb.abrir();
//        try {
//            mydb.actualizarQueue("PriorityQueueBool","true");// consultarLocalizacion();
//            mydb.actualizarQueue("PQ1","true");
//        } catch (Exception e) {
//            LogUtil.printError("Error", "LockService - ReUnlock");
//            LogUtil.printFullError("Error", "LockService - ReUnlock", e);
//        }
//        mydb.cerrar();
//        //**************************************
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Exception e) {
            LogUtil.printError("BATERIA", "EXCEPCION: SIN SEÑAL ");
            LogUtil.printError("Error", "LockService - onCreate");
            LogUtil.printFullError("Error", "LockService - onCreate", e);
        }

//        //Pruebas Database
//        Database mydb = new Database(ctx);
//        mydb.abrir();
//        String[][] datos = new String[0][];
//        try {
//            datos = mydb.consultarQueue();// consultarLocalizacion();
//        } catch (Exception e) {
//            LogUtil.printError("Error", "LockService - onCreate - tryPrueba");
//            LogUtil.printFullError("Error", "LockService - onCreate - tryPrueba", e);
//            //e.printStackTrace();
//        }
//        mydb.cerrar();
//
//        //datos = mydb.consultarLocalizacion(); D  consultarQueue
//                //
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, START_STICKY, startId);
        try {
            LogUtil.printDebug("SERVICIO", "INICIANDO SERVICIO (LockService)");
            start();
        } catch (Exception e) {
            LogUtil.printError("Error", "LockService - onStartCommand");
            LogUtil.printFullError("Error", "LockService - onStartCommand", e);
        }
        return START_REDELIVER_INTENT; //return START_STICKY;
    }

    @Override
    public void onDestroy() { // Pendiente: Revisar si aqui se reinicia el Servicio

        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();

        LogUtil.printError("SERVICIO", "SE DETUVO");
        LogUtil.printDebug("Error", "LockService - onDestroy");

        try { // Try01
            LogUtil.printError("Error", "LockService - onDestroy - Try01");
            LogUtil.printError("SERVICIO", "REINICIANDO");

            unregisterReceiver(this.mBatInfoReceiver);
            sendBroadcast(new Intent("RestartService"));
            startService(new Intent(getApplicationContext(), LockService.class));

        } catch (Exception e) {
            LogUtil.printError("Error", "LockService - onCreate");
            LogUtil.printFullError("Error", "LockService - onCreate", e);
            LogUtil.printError("SERVICIO", "ERROR REINICIANDO");
            Toast.makeText(this, "Service re run failed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void start() {
        //Log.e("*******", "GetDistancia: " + getDistance(latitude, longitude, Double.parseDouble(latitudePolitic), Double.parseDouble(longitudePolitic)));
        try { //Try01
            ctx = this;
        } catch (Exception e) {
            LogUtil.printFullError("START", "EXCEPCION OBTENIENDO CONTEXTO", e);
        }

        try { //Try02
            LogUtil.printError("START", "INICIANDO LOCATION MANAGER");
            final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            Log.e("***SEG***", "Llamando a locationListener");
            //Log.e("***SEG***", "Dist: " + getDistance(latitude, longitude, 0, 0));
            ;
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location loc) {
                    Log.e("***SEG***", "onLocationChange");
                    Log.e("***SEG***", "Llamando a getDistance 01");
                    if (latitude.equals(Double.NaN) || longitude.equals(Double.NaN)) {
                        Log.e("***SEG***", "Double.NaN");
                        latitude = loc.getLatitude();
                        longitude = loc.getLongitude();
                        Log.e("***123***", "lat y long: " + latitude + " " + longitude);

                    } else if (getDistance(latitude, longitude, loc.getLatitude(), loc.getLongitude()) > 20) { //Estaba en 50, no se porque...
                        Log.e("***SEG***", "getDistance > 20");
                        latitude = loc.getLatitude();
                        longitude = loc.getLongitude();
                        Log.e("Latitude1", latitude.toString());
                        Log.e("Longitude1", longitude.toString());

                        //SaveLocation pendiente de confirmación
                        //saveLocation();
                    } else {
                        Log.e("***SEG***", "NO ENTRO EN NINGUN IF");
//                        latitude = loc.getLatitude();
//                        longitude = loc.getLongitude();
//                        Log.e("Latitude1", latitude.toString());
//                        Log.e("Longitude1", longitude.toString());
                    }

                    //saveLocation(); //Estaba aquí pero manda posicion cada segundo y no deberia.
                    Log.e("Location", "Latitude: " + loc.getLatitude() + " Longitude:" + loc.getLongitude() + " Provider: " + loc.getProvider());
                    Log.e("DISTANCE", "r: " + radiusPolitic + " " + "lat: " + latitudePolitic + " " + "long: " + longitudePolitic);

                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Log.e("GPS", "DISABLED");

                        Intent intent = new Intent(ctx, GPSActivation.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    if (!latitudePolitic.equals("")) {
                        Log.e("***SEG***", "Llamando a getDistance 02");
                        Log.e("DISTANCE", "r: " + radiusPolitic + " ?: d:" + getDistance(latitude, longitude, Double.parseDouble(latitudePolitic), Double.parseDouble(longitudePolitic)));
                        Log.e("***SEG***", "Llamando a getDistance 03");
                        if (getDistance(latitude, longitude, Double.parseDouble(latitudePolitic), Double.parseDouble(longitudePolitic)) > Double.parseDouble(radiusPolitic)) {
                            if (!isGeolock) { //unlocked
                                Log.e("GEOLOCK", "LOCKING");
                                startLockActivity();//Lock
                                new SendGeolock().execute();
                                isGeolock = true;
                                if (!isThereActiveInternetConnection()) {
                                    //PQ1 = true; //1-Unlook
                                    PQ2 = true; //2-Geolock
                                    //PQ3 = true; //3-Geounlock
                                    PriorityQueueBool = true;

                                    //**************************************
                                    //Pruebas Database
                                    Database mydb = new Database(getApplicationContext());
                                    mydb.abrir();
                                    try {
                                        mydb.actualizarQueue("PriorityQueueBool","true");// consultarLocalizacion();
                                        mydb.actualizarQueue("PQ2","true");
                                    } catch (Exception e) {
                                        LogUtil.printError("Error", "LockService - start");
                                        LogUtil.printFullError("Error", "LockService - start", e);
                                    }
                                    mydb.cerrar();
                                    //**************************************
                                }
                            }
                        } else {
                            if (isGeolock) { //locked
                                Log.e("GEOLOCK", "UNLOCKING");
                                stopLockActivity();
                                new SendGeounlock().execute();
                                isGeolock = false;//unlock
                                if (!isThereActiveInternetConnection()) {
                                    //PQ1 = true; //1-Unlook
                                    //PQ2 = true; //2-Geolock
                                    PQ3 = true; //3-Geounlock
                                    PriorityQueueBool = true;

                                    //**************************************
                                    //Pruebas Database
                                    Database mydb = new Database(getApplicationContext());
                                    mydb.abrir();
                                    try {
                                        mydb.actualizarQueue("PriorityQueueBool","true");// consultarLocalizacion();
                                        mydb.actualizarQueue("PQ3","true");
                                    } catch (Exception e) {
                                        LogUtil.printError("Error", "LockService - start");
                                        LogUtil.printFullError("Error", "LockService - start", e);
                                    }
                                    mydb.cerrar();
                                    //**************************************
                                }
                            }
                        }
                    } else {
                        if (isGeolock) { //locked
                            Log.e("GEOLOCK", "UNLOCKING");
                            stopLockActivity();
                            new SendGeounlock().execute();
                            isGeolock = false;//unlock
                            if (!isThereActiveInternetConnection()) {
                                //PQ1 = true; //1-Unlook
                                //PQ2 = true; //2-Geolock
                                PQ3 = true; //3-Geounlock
                                PriorityQueueBool = true;

                                //**************************************
                                //Pruebas Database
                                Database mydb = new Database(getApplicationContext());
                                mydb.abrir();
                                try {
                                    mydb.actualizarQueue("PriorityQueueBool","true");// consultarLocalizacion();
                                    mydb.actualizarQueue("PQ3","true");
                                } catch (Exception e) {
                                    LogUtil.printError("Error", "LockService - start");
                                    LogUtil.printFullError("Error", "LockService - start", e);
                                }
                                mydb.cerrar();
                                //**************************************
                            }
                        }
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.e("GPS", "ENABLED");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.e("GPS", "DISABLED");
                    Intent intent = new Intent(getApplicationContext(), GPSActivation.class);
                    //startActivity(intent);//MUERE CUANDO QUITO EL GPS
                }

            };

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 2, locationListener); //5 SEC OR 1 M
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 2, locationListener); //5 SEC OR 1 M

//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 2, locationListener); //60 SEC OR 2 M
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 2, locationListener); //60 SEC OR 2 M
        } catch (Exception e) {
            LogUtil.printFullError("START", "EXCEPCION EN LOCATION MANAGER", e);
        }

        new RegisterUserIdAsyncTask().execute(idLogin);
        contDown();
    }

    public class RegisterUserIdAsyncTask extends AsyncTask<Integer, Integer, JSONObject> {

        @Override
        protected JSONObject doInBackground(Integer... params) {
            getLoginIdFromFile(getApplicationContext());
            if (deviceId == null || deviceId.length() != 20) {
                try {
                    if (idLogin != 0) { // Entra cuando no esta registrado
                        ownerRegistration(idLogin);
                    } else { // Ya esta registrado el Dispositivo y se loguea Solo en el server.
                        getDeviceIdFromServer();
                    }
                } catch (Exception e) {
                    LogUtil.printFullErrorInit("", "", e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            continueService();
        }
    }

    public float getBatteryLevel() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    private void startMessageActivity() {
        LogUtil.printError("MESSAGE ACTIVITY", "INICIANDO");
        try {
            Intent i = new Intent(ctx, MessageActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("Message", message);
            startActivity(i);
        } catch (Exception e) {
            LogUtil.printFullError("MESSAGE ACTIVITY", "EXCEPCION", e);
        }
    }

    private void startPhotoActivity() {
        try {
            final Calendar c = Calendar.getInstance();
            Intent i = new Intent(ctx, PhotoActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LogUtil.printError("PHOTO ACTIVITY", "INICIANDO: ");
            startActivity(i);

        } catch (Exception e) {
            LogUtil.printFullError("PHOTO ACTIVITY", "EXCEPCION", e);
        }
    }

    private void startRegCallsActivity() {
        try {
            final Calendar c = Calendar.getInstance();
            Intent i = new Intent(ctx, RegisterCallActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LogUtil.printError("REGISTER CALL ACTIVITY", "INICIANDO: ");
            startActivity(i);

        } catch (Exception e) {
            LogUtil.printFullError("REGISTER CALL ACTIVITY", "EXCEPCION", e);
        }
    }

    private void startLockActivity() {

        try {
            final Calendar c = Calendar.getInstance();
            tempFile = (c.get(Calendar.YEAR) + "" + "-" + (c.get(Calendar.MONTH) + 1) + "" + "-" + c.get(Calendar.DAY_OF_MONTH) + "" + "_" + c.get(Calendar.HOUR_OF_DAY) + "" + "-" + c.get(Calendar.MINUTE) + "" + "-" + c.get(Calendar.SECOND)) + "(front)" + ".jpg";
//            Log.e("ctx", ctx.toString());
            Intent i = new Intent(ctx, LockActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("Nombre Imagen", tempFile);
            LogUtil.printError("LOCK ACTIVITY", "INICIANDO: " + tempFile);
            startActivity(i);

        } catch (Exception e) {
            LogUtil.printFullError("LOCK ACTIVITY", "EXCEPCION", e);
        }
    }

    private void startAlarm() {
        LogUtil.printError("ALARM ACTIVITY", "INICIANDO");

        try {
            final Calendar c = Calendar.getInstance();
            tempFile = (c.get(Calendar.YEAR) + "" + "-" + (c.get(Calendar.MONTH) + 1) + "" + "-" + c.get(Calendar.DAY_OF_MONTH) + "" + "_" + c.get(Calendar.HOUR_OF_DAY) + "" + "-" + c.get(Calendar.MINUTE) + "" + "-" + c.get(Calendar.SECOND)) + "(front)" + ".jpg";
            Intent i = new Intent(ctx, AlarmService.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("Nombre Imagen", tempFile);
            startActivity(i);
        } catch (Exception e) {
            LogUtil.printFullError("ALARM ACTIVITY", "EXCEPCION", e);
        }
    }

    private void stopLockActivity() {
        try {
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
            localBroadcastManager.sendBroadcast(new Intent("com.qoopa.nodosshield"));
            Log.e("LOCK ACTIVITY", "DETENIENDO");
        } catch (Exception e) {
            LogUtil.printFullErrorInit("LOCK ACTIVITY", "EXCEPCION", e);
        }
    }

    private void continueService() {

        LogUtil.printError("HILOS", "INICIANDO HILOS");
        startSocket();
        new GetStatusAndMessageThread().start();
        new UploadGeoDataThread().start();
        new GetOfflinePoliciesThread().start();
        new GetDelaysAsyncTask().execute();
        new UploadDataAsyncTask().execute();
        new PriorityQueueThread().start();
        new EventsPendingThread().start();
        try {
            Log.e("***Seg***", "Entrando RegistroApps()");
            RegistroApps registro = new RegistroApps();

            //ConsultAppUpdate consultar = new ConsultAppUpdate();
            registro.execute();
        } catch (Exception e) {
            Log.e("***Seg***", "Error RegistroApps()");
            e.printStackTrace();
        }
        //new RestartServiceManually().start();
    }

    private void startSocket() {

        try {
            LogUtil.printError("SOCKET", "STARTING");

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            IO.setDefaultSSLContext(sc);
            HttpsURLConnection.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());

            IO.Options opts = new IO.Options();
            opts.forceNew = false;
            opts.reconnection = true;
            opts.secure = true;
            opts.sslContext = sc;

            mSocket = IO.socket("https://162.248.52.99:8080", opts);
            //mSocket = IO.socket("http://162.248.52.99:8081");
            LogUtil.printError("SOCKET", "STARTING OFF");
            mSocket.off("alarm message");
            mSocket.off("terminal state");
            mSocket.off("block message");
            mSocket.off("unlock message");
            mSocket.off("terminal unlock");
            mSocket.off("track position message");
            mSocket.off("update policies message");
            LogUtil.printError("SOCKET", "STARTING ON");
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectTimeOut);
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
//                    LogUtil.printError("SOCKET", "UP");
                    Log.e("SOCKET", "UP");
                    JSONObject data = new JSONObject();
                    try {
                        data.put("terminal", deviceId);
                        LogUtil.LogEnvioDatos("Socket", data.toString());//SeguimientoDatos
                        mSocket.emit("terminal handshake message", data);
                    } catch (Exception e) {
                        LogUtil.printFullError("SOCKET", "ON CALL EMITTER", e);
                    }
                }
            });

            mSocket.on("update policies message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LogUtil.LogEnvioDatos("SocketR", "update policies message");//SeguimientoDatos
                    new GetOfflinePoliciesAsyncTask().execute();
                    //Log.e("***Seg***","update policies message");
                }
            });

            mSocket.on("photo message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

//                    //Pruebas Database
//                    Database mydb = new Database(getApplicationContext());
//                    mydb.abrir();
//                    String[][] datos = new String[0][];
//                    try {
//                        datos = mydb.consultarQueue();// consultarLocalizacion();
//                    } catch (Exception e) {
//                        LogUtil.printError("Error", "LockService - onCreate - tryPrueba");
//                        LogUtil.printFullError("Error", "LockService - onCreate - tryPrueba", e);
//                        //e.printStackTrace();
//                    }
//                    mydb.cerrar();


                    LogUtil.LogEnvioDatos("SocketR", "photo message");//SeguimientoDatos
                    //new GetOfflinePoliciesAsyncTask().execute();
                    Log.e("**---**", "on photo message");

                    startPhotoActivity();

//                    String nombre_foto = "PRUEBA_DAVID";
//                    try {
//                        capture2 = new CaptureImage();
//                        capture2.setNombre_imagen(nombre_foto);
//                        LogUtil.printError("NOMBRE FOTO", "" + nombre_foto);
//                        capture2.camWeb();
//                    } catch (Exception e) {
//                        //e.printStackTrace();
//                        Log.e("*******", "SCREENSHOT EXCEPTION");
//                        Log.e("*******", "SCREENSHOT EXCEPTION");
//                        Log.e("*******", "SCREENSHOT EXCEPTION");
//                        Log.e("*******", "SCREENSHOT EXCEPTION");
//                        Log.e("*******", "SCREENSHOT EXCEPTION");
//                    }

//                    capture = new CaptureImage();
//                    capture.setNombre_imagen(nombre_foto);
//                    LogUtil.printError("NOMBRE FOTO", "" + nombre_foto);
//                    capture.cam();
                }
            });

            mSocket.on("alarm message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LogUtil.printError("SOCKET", "ALARM MESSAGE");

//                    //**************************************
//                    //Pruebas Database
//                    Database mydb = new Database(getApplicationContext());
//                    mydb.abrir();
//                    String[][] datos = new String[0][];
//                    try {
//                        mydb.actualizarQueue("PriorityQueueBool","true");// consultarLocalizacion();
//                        mydb.actualizarQueue("PQ1","true");
//                        mydb.actualizarQueue("PQ2","true");
//                        //mydb.actualizarQueue("","true");
//                    } catch (Exception e) {
//                        LogUtil.printError("Error", "LockService - onCreate - tryPrueba");
//                        LogUtil.printFullError("Error", "LockService - onCreate - tryPrueba", e);
//                        //e.printStackTrace();
//                    }
//                    mydb.cerrar();
//                    //**************************************

                    JSONObject data = (JSONObject) args[0];
                    try {
                        alarm = "ALARMED";
                        id = data.getString("id");
                        LogUtil.printError("ALARM", "ID" + id);
                        data.put("state", alarm);
                        data.put("id", id);
                        data.put("terminal", deviceId);
                        LogUtil.LogEnvioDatos("Socket", data.toString());//SeguimientoDatos
                        mSocket.emit("terminal send state", data);

                    } catch (Exception e) {
                        LogUtil.printFullError("SOCKET", "ON CALL ALARM MSG", e);
                    }
                    startAlarm();
                }
            });

            mSocket.on("block message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LogUtil.printError("SOCKET", "LOCK MESSAGE");

                    if (state.equals("UNLOCKED")) {
                        state = "LOCKED";

                        JSONObject data = (JSONObject) args[0];
                        JSONObject data1 = (JSONObject) args[0];

                        try {
                            id = data.getString("id");
                            LogUtil.printError("LOCK", "ID" + id);
                            data1.put("id", id);
                            data1.put("state", state);
                            data1.put("terminal", deviceId);
                            LogUtil.printError("STATE : ", state);
                            LogUtil.LogEnvioDatos("Socket", data1.toString());//SeguimientoDatos
                            mSocket.emit("terminal send state", data1);
                        } catch (Exception e) {
                            LogUtil.printFullError("SOCKET", "ON CALL LOCK MSG", e);
                        }
                        startLockActivity();
                    }
                }
            });

            mSocket.on("unlock message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LogUtil.printError("SOCKET", "UNLOCK MESSAGE");
                    Log.e("STATE ?", ": " + state);
                    //if (state.equals("LOCKED")) {
                    state = "UNLOCKED";
                    stopLockActivity();
                    //}

                    JSONObject data = (JSONObject) args[0];
                    try {

                        id = data.getString("id");
                        LogUtil.printError(" UNLOCK", "ID" + id);
                        data.put("id", id);
                        data.put("state", state);
                        data.put("terminal", deviceId);
                        LogUtil.printError("STATE : ", state);
                        LogUtil.LogEnvioDatos("Socket", data.toString());//SeguimientoDatos
                        mSocket.emit("terminal send state", data);

                    } catch (Exception e) {
                        LogUtil.printFullError("SOCKET", "ON CALL LOCK MSG", e);
                    }
                }
            });

            mSocket.on("terminal state", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    LogUtil.printError("SOCKET", "TERMINAL STATE MESSAGE");
                    LogUtil.LogEnvioDatos("SocketR", "terminal state");//SeguimientoDatos
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String auxState = data.getString("state");
                        id = data.getString("id");

                        if (auxState.equals("LOCKED")) {
                            if (state.equals("UNLOCKED")) {
                                startLockActivity();
                            }
                            state = "LOCKED";

                        } else if (auxState.equals("UNLOCKED")) {
                            if (state.equals("LOCKED")) {
                                stopLockActivity();
                            }
                            state = "UNLOCKED";
                        }
                    } catch (Exception e) {
                        LogUtil.printFullError("SOCKET", "ON TERMINAL STATE MSG", e);
                    }
                }
            });

            mSocket.on("shutdown message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LogUtil.LogEnvioDatos("SocketR", "shutdown message");//SeguimientoDatos
                }
            });

            mSocket.on("message message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LogUtil.printError("SOCKET", "ON NORMAL MESSAGE");
                    JSONObject data = (JSONObject) args[0];
                    try {
//                        Log.e("Seguimiento", "Antes de true");
//                        PriorityQueueBool = true;
//                        Log.e("Seguimiento", "Despues de true");
                        id = data.getString("id");
                        message = data.getString("message");
                        startMessageActivity();
                        Log.e("states_message", state);
                        Log.e("id_message", id);
                        Log.e("message", message);
                        LogUtil.printError("MESSAGE : ", message);
                        data.put("id", id);
                        data.put("state", state);
                        data.put("terminal", deviceId);
                        LogUtil.LogEnvioDatos("Socket", data.toString());//SeguimientoDatos
                        mSocket.emit("terminal send state", data);
                    } catch (Exception e) {
                        LogUtil.printFullError("SOCKET", "ON NORMAL MSG", e);
                    }
                }
            });
//
//            mSocket.on("screenshoot message", new Emitter.Listener() {
//                @Override
//                public void call(Object... args) {
//                    JSONObject data = (JSONObject) args[0];
//                    Date now = new Date();
//                    android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
//                    try {
//                        //startScreenshoot(now);
//                        id = data.getString("id");
//                        screen = "SCREENSHOOT";
//                        Log.e("ID_SCREEN", id);
//                        data.put("id", id);
//                        data.put("state", screen);
//                        data.put("url", "http://www.nodos.com.co/img/terminal_data/" + now + ".jpg");
//                        Log.e("URL", "http://www.nodos.com.co/img/terminal_data/" + now + ".jpg");
//                        LogUtil.LogEnvioDatos("Socket", data.toString());//SeguimientoDatos
//                        mSocket.emit("terminal photo", data);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });

            mSocket.on("track message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LogUtil.printError("SOCKET", "ON TRACKING");
                    tracking = true;
                    TrackThread tt = new TrackThread();
                    tt.start();
                    JSONObject data = (JSONObject) args[0];
                    try {
                        id = data.getString("id");
                        Log.e("id_track", id);
                        data.put("id", id);
                        data.put("state", "TRACKED");
                        data.put("terminal", deviceId);
                        LogUtil.printError("SOCKET", "TRACKING: " + String.valueOf(tracking));
                        LogUtil.LogEnvioDatos("Socket", data.toString());//SeguimientoDatos
                        mSocket.emit("terminal send state", data);
                    } catch (Exception e) {
                        LogUtil.printFullError("SOCKET", "ON TRACK MSG", e);
                    }
                }
            });

            mSocket.on("untrack message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LogUtil.printError("SOCKET", "ON UNTRACKING");
                    tracking = false;
                    JSONObject data = (JSONObject) args[0];
                    try {
                        id = data.getString("id");
                        Log.e("id_track", id);
                        data.put("id", id);
                        data.put("state", "UNTRACKED");
                        data.put("terminal", deviceId);
                        LogUtil.printError("SOCKET", "TRACKING: " + String.valueOf(tracking));
                        LogUtil.LogEnvioDatos("Socket", data.toString());//SeguimientoDatos
                        mSocket.emit("terminal send state", data);
                    } catch (Exception e) {
                        LogUtil.printFullError("SOCKET", "ON TRACK MSG", e);
                    }
                }
            });

            mSocket.on("sleep message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        LogUtil.printError("SOCKET", "ON SLEEP");

                        mSocket.disconnect();
                        mSocket.off("alarm message");
                        mSocket.off("terminal state");
                        mSocket.off("block message");
                        mSocket.off("unlock message");
                        mSocket.off("terminal unlock");
                        mSocket.off("track position message");
                        mSocket.off("untrack message");
                        mSocket.off("sleep message");
                        mSocket.off("shutdown message");
                        mSocket.close();

                        Thread.sleep(60000);
                        LogUtil.printError("SOCKET", ": 60 segundos");
                        LogUtil.LogEnvioDatos("SocketR", "sleep message");//SeguimientoDatos
                        mSocket.connect();

                    } catch (Exception e) {
                        LogUtil.printError("SLEEP", " EXCEPTION ");
                        e.printStackTrace();
                    }
                }

            });

            mSocket.on("delete message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    LogUtil.printError("SOCKET", "ON MESSAGE: DELETE");
                    File sdCardRoot = Environment.getExternalStorageDirectory();
                    File yourDir = new File(String.valueOf(sdCardRoot));
                    for (File f : yourDir.listFiles()) {
                        String name = f.getName();
                        Log.e("Name", name);
                    }
                    delete = "DELETED";
                    JSONObject data = (JSONObject) args[0];
                    try {
                        id = data.getString("id");
                        LogUtil.printError("ID_DELETE", id);
                        data.put("id", id);
                        data.put("state", delete);
                        LogUtil.LogEnvioDatos("Socket", data.toString());//SeguimientoDatos
                        mSocket.emit("terminal send state", data);
                        LogUtil.printError("DELETE FILES: ", delete);

                        String DocJSONArray = data.getString("folders");
                        Log.e("Array", DocJSONArray.toString());

                        String[] strings = DocJSONArray.split(",");
                        for (String element : strings) {

                            if (element.equals("Documents")) {
                                deleteDirectory(new File(Environment.getExternalStorageDirectory() + "/Documents/"));
                                Log.e("Documentos", "Documents");
                            } else if (element.equals("Music")) {
                                deleteDirectory(new File(Environment.getExternalStorageDirectory() + "/Music/"));
                                Log.e("Music", "Music");
                            } else if (element.equals("Pictures")) {
                                deleteDirectory(new File(Environment.getExternalStorageDirectory() + "/Pictures/"));
                                Log.e("Imagenes", "Pictures");
                            } else if (element.equals("WhatsApp")) {
                                deleteDirectory(new File(Environment.getExternalStorageDirectory() + "/Whatsapp/"));
                                Log.e("Whatsapp", "Whatsapp");
                            } else if (element.equals("Download")) {
                                deleteDirectory(new File(Environment.getExternalStorageDirectory() + "/Download/"));
                                Log.e("Descargas", "Download");
                            } else {
                                Log.e("NO ENTRO", "no entro");
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.printFullError("SOCKET", "ON DELETE", e);
                    }
                    return;
                }
            });

        } catch (Exception ex) {
            LogUtil.printFullError("SOCKET", "ON ADDING LISTENERS", ex);
        }

        try {
            LogUtil.printError("SOCKET", "CONNECTING");
            mSocket.connect();

        } catch (Exception e) {

            LogUtil.printFullError("SOCKET", "EXCEPTION ON CONNECT", e);
        }
        try {
            JSONObject data = new JSONObject();
            data.put("terminal", deviceId);

            LogUtil.printError("SOCKET", "PUT DEVICE ID");
        } catch (Exception e) {
            LogUtil.printFullError("SOCKET", "ON PUT DEVICE ID", e);
        }
    }

    private TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    }};

    public static class RelaxedHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e("SOCKET", "CAIDO");
        }
    };

    private Emitter.Listener onConnectTimeOut = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            LogUtil.printError("SOCKET", "TIME OUT ERROR");
        }
    };

    public static String getDeviceIdFromFile(Context ctx) {
        String id = "";
        try {
            FileInputStream fis = ctx.openFileInput(deviceIdFilePath);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line = bufferedReader.readLine();
            if (line != null && line.length() == 20) {
                deviceId = line;
                id = line;
                LogUtil.printError("DEVICE ID", "" + deviceId);
            } else {
                LogUtil.printError("DEVICE ID", "NOT FOUND");
            }

        } catch (Exception ex) {
            LogUtil.printError("DEVICE ID", "FILE NOT FOUND EXCEPTION");
            return id;
        }
        return id;
    }

    public void setDeviceIdInFile() {

        FileOutputStream outputStream;
        try {
            LogUtil.printError("DEVICE ID", "WRITING" + deviceId);
            //File logFile = new File.deviceIdFilePath.createNewFile();
            outputStream = openFileOutput(deviceIdFilePath, Context.MODE_PRIVATE);
            outputStream.write(deviceId.getBytes());
            outputStream.close();
            LogUtil.printError("DEVICE ID", "WRITING" + deviceId);
        } catch (Exception ex) {
            LogUtil.printError("DEVICE ID", "WRITING  EXCEPTION");
        }
    }

    public static void setLoginIdInFile(Context ctx, String id) {

        FileOutputStream outputStream;
        try {
            LogUtil.printError("ID TO WRITE", "" + id);
            outputStream = ctx.openFileOutput(loginIdFilePath, Context.MODE_PRIVATE);
            outputStream.write((id).getBytes());
            outputStream.close();
            LogUtil.printError("LOGIN ID", "WRITING" + idLogin);
        } catch (Exception ex) {
            LogUtil.printError("LOGIN ID", "WRITING EXCEPTION");
        }
    }

    public static int getLoginIdFromFile(Context ctx) {
        int id = 0;
        try {
            FileInputStream fis = ctx.openFileInput(loginIdFilePath);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line = bufferedReader.readLine();
            if (line != null) {
                id = Integer.parseInt(line);
                idLogin = id;
                LogUtil.printError("LOGIN ID", "" + idLogin);
                //ownerRegistered = true;
            } else {
                idLogin = 0;
                //ownerRegistered = false;
                LogUtil.printError("LOGIN ID", "NOT FOUND");
            }

        } catch (Exception ex) {
            LogUtil.printError("LOGIN ID", "FILE NOT FOUND EXCEPTION");
            idLogin = 0;
            return 0;
        }

        return id;
    }

    public int getDeviceIdFromServer() {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();

        List<NameValuePair> params2 = new ArrayList<NameValuePair>();
        params2.add(new BasicNameValuePair("option", "get_id"));
        params2.add(new BasicNameValuePair("address", getWifiMacAddress()));
        params2.add(new BasicNameValuePair("imei", imei));
        try {
            LogUtil.LogEnvioDatos(serverHandlerURL, params2.toString());//SeguimientoDatos
            JSONObject json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params2);
            LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            if (json != null) {
                if ("OK".equals(json.getString("result"))) {
                    if (json.getString("id").length() == 20) {
                        deviceId = json.getString("id");
                        setDeviceIdInFile();
                        LogUtil.printError("DEVICE ID", "" + deviceId);
                    }
                    return 0;
                } else {
                    LogUtil.printError("DEVICE ID", "DEVICE NOT REGISTERED");
                }
            }
        } catch (Exception e) {
            LogUtil.printFullErrorInit("DEVICE ID", "EXCEPCION", e);
        }
        return -1;
    }

    public void ownerRegistration(int ownerId) {

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();

        List<NameValuePair> params2 = new ArrayList<NameValuePair>();
        params2.add(new BasicNameValuePair("option", "register_to_admin"));
        params2.add(new BasicNameValuePair("address", getWifiMacAddress()));
        params2.add(new BasicNameValuePair("name", Build.MODEL));
        params2.add(new BasicNameValuePair("owner", ownerId + ""));
        params2.add(new BasicNameValuePair("imei", imei));

        Log.e("OWNER ", "REGISTER: " + ownerId);
        LogUtil.LogEnvioDatos(serverHandlerURL, params2.toString());//SeguimientoDatos
        JSONObject json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params2);
        LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos

        try {
            if (json.getString("id").length() == 20) {
                deviceId = json.getString("id");
                setDeviceIdInFile();
                LogUtil.printError("DEVICE ID", " : " + deviceId + " response: " + json);

            }
        } catch (Exception e) {
            LogUtil.printFullError("OWNER", "EXCEPCION ON REGISTER", e);
        }
    }

    public static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
            LogUtil.printFullErrorInit("MAC ADDRESS ", "EXCEPTION ON OBTAIN", ex);
        }
        return "";
    }

    public boolean isThereActiveInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.e("Internet Connection", netInfo.toString());

            return true;
        } else {
            Log.e("Internet Connection", "Offline");
            return false;
        }
    }

    public void saveLocation() {

        Log.e("*-*-*-*-*", "Aqui en saveLocation!!!");
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try {
            Database mydb = new Database(ctx);
            mydb.abrir();
            mydb.registrarLocalizacion(latitude + "", longitude + "", date);
            mydb.cerrar();
        } catch (Exception e) {
            LogUtil.printFullError("LOCATION", "EXCEPCION GUARDANDO", e);
        }
    }

    public void checkSIMPolicies() throws Exception {

        Database database = new Database(ctx);
        database.abrir();
        String deviceSim = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getSimSerialNumber();
        boolean lock = false;
        List<String> simList = database.consultarSim();
        database.cerrar();
        if (simList.size() > 0) {
            if (deviceSim == null) {
                Log.e("SIM", "No Sim");
                lock = true;
            } else {
                Log.e("SIM", deviceSim);
                Log.e("SIM LIST", simList.toString());
                if (simList.contains(deviceSim)) {
                    Log.e("SIM", "Sim OK");
                } else {
                    Log.e("SIM", "Sim not registered");
                    lock = true;
                }
            }
        }
        if (lock) {
            if (state.equals("UNLOCKED")) {
                state = "LOCKED";
                startLockActivity();
            }

        } else {
            if (state.equals("LOCKED")) {
                state = "UNLOCKED";
                stopLockActivity();
            }
        }
    }

    public static double getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {

        Log.e("*******", "*****-*****");
        Log.e("***getDistance***", "Lat_a: " + lat_a);
        Log.e("***getDistance***", "Lng_a: " + lng_a);
        Log.e("***getDistance***", "Lat_b: " + lat_b);
        Log.e("***getDistance***", "Lng:b: " + lng_b);

        Location loc1 = new Location("");
        Location loc2 = new Location("");

        loc1.setLatitude(lat_a);
        loc1.setLongitude(lng_a);

        loc2.setLatitude(lat_b);
        loc2.setLongitude(lng_b);
        Log.e("*******", "Antes Distancia Return: ");
        Log.e("*******", "Distancia Return: " + loc1.distanceTo(loc2));
        Log.e("*******", "Despues Distancia Return: ");
        return loc1.distanceTo(loc2);
    }

    public Window getWindow() {
        return window;
    }

    public class UploadGeoDataThread extends Thread {
        @Override
        public void run() {
            //Log.e("*-*-*-*-*-*", "Thread - UploadGeoDataThread 001");
            while (true) {
                //Log.e("*-*-*-*-*-*", "Thread - UploadGeoDataThread 002");
                if (isThereActiveInternetConnection()) { //Con internet envio datos al server
                    Log.e("UPLOAD GEO", "ONLINE");
                    Database mydb = new Database(ctx);
                    mydb.abrir();
                    String[][] datos = new String[0][];
                    try {
                        datos = mydb.consultarLocalizacion();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        for (int i = 0; i < datos.length; i++) {
                            try {
                                List<NameValuePair> params = new ArrayList<NameValuePair>();
                                params.add(new BasicNameValuePair("option", "upload_geodata"));
                                params.add(new BasicNameValuePair("terminal", deviceId));
                                params.add(new BasicNameValuePair("latitude", datos[i][1]));
                                params.add(new BasicNameValuePair("longitude", datos[i][2]));
                                params.add(new BasicNameValuePair("date", datos[i][3]));
                                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                                jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
//                                LogUtil.LogEnvioDatos("Respuesta", params.toString());//SeguimientoDatos

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        LogUtil.printFullError("GEODATA", "EXCEPCION UPLOADING", e);
                    }
                    mydb.borrarLocalizacion();
                    mydb.cerrar();
                } else { //SIN internet guarda datos
                    saveLocation();
                }

                try {
                    //this.sleep(1000); // Pruebas
                    this.sleep(geoDataDelayInt);
                } catch (Exception ex) {
                    LogUtil.printFullError("GEODATA", "INTERRUPT EXCEPCION UPLOADING", ex);
                }
            }
        }
    }

    public class TrackThread extends Thread {

        @Override
        public void run() {
            while (tracking) {
                try {

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("option", "upload_geodata"));
                    params.add(new BasicNameValuePair("terminal", deviceId));
                    params.add(new BasicNameValuePair("latitude", latitude.toString()));
                    LogUtil.printError("LATITUD", " : " + latitude.toString());
                    params.add(new BasicNameValuePair("longitude", longitude.toString()));
                    LogUtil.printError("LONGITUD", " : " + longitude.toString());
                    LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                    jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
//                    LogUtil.LogEnvioDatos("Respuesta", params.toString());//SeguimientoDatos
                } catch (Exception e) {
                    LogUtil.printFullError("TRACKING", "EXCEPCION REQUEST", e);
                }

                try {
                    this.sleep(trackDataDelayInt);
                } catch (Exception ex) {
                    LogUtil.printFullError("TRACKING", "INTERRUPT EXCEPCION ", ex);
                }
            }
        }
    }

    public void sendBatteryLevel() {

        JSONObject json = null;
        try {
            bateria = "" + Math.round(getBatteryLevel());
            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("option", "upload_data"));
            params.add(new BasicNameValuePair("terminal", deviceId));
            params.add(new BasicNameValuePair("data", "Batery"));
            params.add(new BasicNameValuePair("value", bateria));
            params.add(new BasicNameValuePair("type", "SOFTWARE"));
            LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
            json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
            LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void GetStatus() {
        List<NameValuePair> params2 = new ArrayList<NameValuePair>();
        params2.add(new BasicNameValuePair("option", "get_status"));
        params2.add(new BasicNameValuePair("terminal", deviceId));
        Log.e("DEVICE ID SEND", "" + deviceId);
        try {
            LogUtil.LogEnvioDatos(serverHandlerURL, params2.toString());//SeguimientoDatos
            JSONObject json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params2);
            LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            String auxState = json.getString("state");
            if (!(auxState == null)) {

                LogUtil.printError("STATUS", "json: " + json + " state: " + state);

                if ("LOCKED".equals(auxState)) {

                    if (state.equals("UNLOCKED")) {
                        startLockActivity();
                        state = "LOCKED";
                    }

                } else if ("UNLOCKED".equals(auxState)) {
                    if (state.equals("LOCKED")) {
                        stopLockActivity();
                        state = "UNLOCKED";
                    }
                }
            }

            //checkSIMPolicies();
        } catch (Exception e) {
            Log.e("STATUS", "EXCEPTION ON GET", e);
            LogUtil.printFullErrorInit("STATUS", " EXCEPCION ON GET", e);
        }
    }

    public void GetMessage() {
        JSONObject json = null;
        try {
            ArrayList params = new ArrayList();
            params.add(new BasicNameValuePair("option", "get_messages"));
            params.add(new BasicNameValuePair("terminal", deviceId));
            LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
            json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
            LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos

            JSONArray messaArray = json.getJSONArray("messages");

            Log.e("MESSAGES", "GETTING MESSAGES");
            Log.e("MESSAGES", messaArray.toString());
            for (int x = 0; x < messaArray.length(); x++) {
                JSONObject messages = messaArray.getJSONObject(x);
                id = messages.get("id").toString();
                Log.e("ID_ARRAY", id);
                String type = messages.get("type").toString();
                Log.e("TYPE_ARRAY", type);

                if (type.equals("MESSAGE")) {
                    message = messages.get("message").toString();
                    Log.e("MESSAGE_ARRAY", message);
                    startMessageActivity();
                } else if (type.equals("ALARM")) {
                    Log.e("ID ALARM MESSAGE ", id);
                    startAlarm();
                }
            }
        } catch (Exception e) {
            LogUtil.printFullErrorInit("MESSAGES", "EXCEPCION ON GET", e);
        }
    }

    public class SendGeolock extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject json = null;
            try {
                ArrayList params1 = new ArrayList();
                params1.add(new BasicNameValuePair("option", "geolock"));
                params1.add(new BasicNameValuePair("terminal", deviceId));
                LogUtil.LogEnvioDatos(serverHandlerURL, params1.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params1);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
                LogUtil.printError("GEOLOCK", "ON" + json.toString());

                if ("FAIL".equals(json.getString("result"))) {
                    //PQ1 = true; //1-Unlook
                    PQ2 = true; //2-Geolock
                    //PQ3 = true; //3-Geounlock
                    PriorityQueueBool = true;

                    //**************************************
                    //Pruebas Database
                    Database mydb = new Database(getApplicationContext());
                    mydb.abrir();
                    try {
                        mydb.actualizarQueue("PriorityQueueBool","true");// consultarLocalizacion();
                        mydb.actualizarQueue("PQ2","true");
                    } catch (Exception e) {
                        LogUtil.printError("Error", "LockService - ReUnlock");
                        LogUtil.printFullError("Error", "LockService - ReUnlock", e);
                    }
                    mydb.cerrar();
                    //**************************************
                }
            } catch (Exception e) {
                LogUtil.printFullError("GEOLOCK", " EXCEPCION ON SEND LOCK", e);
            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
        }
    }

    public class SendGeounlock extends AsyncTask<String, String, JSONObject> {
        JSONObject json = null;

        @Override
        protected JSONObject doInBackground(String... params) {

            try {
                ArrayList params1 = new ArrayList();
                params1.add(new BasicNameValuePair("option", "geounlock"));
                params1.add(new BasicNameValuePair("terminal", deviceId));
                LogUtil.LogEnvioDatos(serverHandlerURL, params1.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params1);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
                LogUtil.printError("GEOUNLOCK", "SENT" + json.toString());

                if ("FAIL".equals(json.getString("result"))) {
                    //PQ1 = true; //1-Unlook
                    //PQ2 = true; //2-Geolock
                    PQ3 = true; //3-Geounlock
                    PriorityQueueBool = true;

                    //**************************************
                    //Pruebas Database
                    Database mydb = new Database(getApplicationContext());
                    mydb.abrir();
                    try {
                        mydb.actualizarQueue("PriorityQueueBool","true");// consultarLocalizacion();
                        mydb.actualizarQueue("PQ3","true");
                    } catch (Exception e) {
                        LogUtil.printError("Error", "LockService - ReUnlock");
                        LogUtil.printFullError("Error", "LockService - ReUnlock", e);
                    }
                    mydb.cerrar();
                    //**************************************
                }

            } catch (Exception e) {
                LogUtil.printFullError("GEOLOCK", " EXCEPCION ON SEND UNLOCK", e);
            }

            return json;
        }
    }

    public class SendUnlock extends AsyncTask<String, String, JSONObject> {
        JSONObject json = null;

        @Override
        protected JSONObject doInBackground(String... params) {

            List<NameValuePair> params2 = new ArrayList<NameValuePair>();
            params2.add(new BasicNameValuePair("option", "unlock"));
            params2.add(new BasicNameValuePair("terminal", LockService.getDeviceId()));
            params2.add(new BasicNameValuePair("code", CodigoBloqueo));

            try {
                LogUtil.LogEnvioDatos(serverHandlerURL, params2.toString());//SeguimientoDatos
                JSONObject data = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params2);
                LogUtil.LogEnvioDatos("Respuesta", data.toString());//SeguimientoDatos

                if ("FAIL".equals(data.getString("result"))) {
                    PQ1 = true; //1-Unlook
                    //PQ2 = true; //2-Geolock
                    //PQ3 = true; //3-Geounlock
                    PriorityQueueBool = true;

                    //**************************************
                    //Pruebas Database
                    Database mydb = new Database(getApplicationContext());
                    mydb.abrir();
                    try {
                        mydb.actualizarQueue("PriorityQueueBool","true");// consultarLocalizacion();
                        mydb.actualizarQueue("PQ1","true");
                    } catch (Exception e) {
                        LogUtil.printError("Error", "LockService - ReUnlock");
                        LogUtil.printFullError("Error", "LockService - ReUnlock", e);
                    }
                    mydb.cerrar();
                    //**************************************
                }

            } catch (Exception e) {
                //e.printStackTrace();
                LogUtil.printFullError("UNLOCK REVISION", "EXCEPCION REQUEST", e);
            }

            return json;
        }
    }

    public class GetDelaysAsyncTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONObject json = null;
            try {
                ArrayList params = new ArrayList();
                params.add(new BasicNameValuePair("option", "get_delays"));
                params.add(new BasicNameValuePair("terminal", deviceId));
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos

                Log.e("DELAYS", "ON GET" + json.toString());

                String checkStateDelay = json.getString("checkStateDelay");
                CheckStateDelayInt = Integer.parseInt(checkStateDelay) * 1000;


                String deviceInfoDelay = json.getString("deviceInfoDelay");
                deviceinfodelayint = Integer.parseInt(deviceInfoDelay) * 1000;

                String geoDataDelay = json.getString("geoDataDelay");
                geoDataDelayInt = Integer.parseInt(geoDataDelay) * 1000;

                String trackDataDelay = json.getString("trackDataDelay");
                trackDataDelayInt = Integer.parseInt(trackDataDelay) * 1000;

                String offlinePoliciesDelay = json.getString("offlinePoliciesDelay");
                offlinePoliciesDelayInt = Integer.parseInt(offlinePoliciesDelay) * 1000;

            } catch (Exception e) {
                LogUtil.printFullError("DELAYS", " EXCEPCION ON GET", e);
            }
            return json;
        }
    }

    public class GetOfflinePoliciesAsyncTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            Log.e("POLICIES", "GET OFFLINE POLICIES");
            JSONObject json = null;
//            Database database = new Database(ctx);
//            database.abrir();
            try {
                ArrayList params = new ArrayList();
                params.add(new BasicNameValuePair("option", "get_offline_policies"));
                params.add(new BasicNameValuePair("terminal", deviceId));
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos

                JSONArray geoJSONArray = json.getJSONArray("geo");
                LogUtil.printError("GEOPLICIES", "POLICIES: " + json.toString());
                Log.e("POLICIES", "" + json.toString());
                JSONArray simJSONArray = json.getJSONArray("sim");
                JSONArray offlineJSONArray = json.getJSONArray("offline");

                Log.e("GEO LENGTH", "" + geoJSONArray.length());
                for (int x = 0; x < geoJSONArray.length(); x++) {
                    JSONObject geoPolicy = geoJSONArray.getJSONObject(x);
                    String latitude = geoPolicy.get("latitude").toString();
                    String longitude = geoPolicy.get("longitude").toString();
                    String radius = geoPolicy.get("radius").toString();

                    Log.e("LATITUDE POLITIC", "" + latitude);
                    Log.e("LONGITUDE POLITIC", "" + longitude);
                    Log.e("RADIUS POLITIC", "" + radius);

                    latitudePolitic = latitude;
                    longitudePolitic = longitude;
                    radiusPolitic = radius;
                    //database.registrarPolitica(latitude, longitude, "activo", radius);
                }

                if (geoJSONArray.length() == 0) {
                    latitudePolitic = "";
                    longitudePolitic = "";
                    radiusPolitic = "";
                }

                for (int x = 0; x < simJSONArray.length(); x++) {
                    String sim = simJSONArray.getString(x);
                    //database.registrarSim(x + "", sim);
                }

                for (int x = 0; x < offlineJSONArray.length(); x++) {
                    String offline = offlineJSONArray.getString(x);

                    //database.registrarOffline(1 + "", offline);
                }

                //  database.cerrar();
            } catch (Exception e) {
                //database.cerrar();
                LogUtil.printFullError("POLICIES", "EXCEPCION ON GET OFFLINE POLICIES", e);
                e.printStackTrace();
            }

            //asfsfsefsefsdfdsfsdfsdefesfsfsefes  PENDIENTE

            return json;

        }

    }

    public class GetStatusAndMessageThread extends Thread {

        public void run() {

            while (true) {
                if (isThereActiveInternetConnection()) {
                    GetMessage();
                    GetStatus();
                    sendBatteryLevel();
                }
                try {
                    Thread.sleep(CheckStateDelayInt);
                } catch (Exception e) {
                    LogUtil.printFullError("STATUS/MESSAGES", "INTERRUPTION EXCEPCION ON GET THREAD", e);
                }
            }
        }
    }

    public class PriorityQueueThread extends Thread {

        public void run() {
            Log.e("Seguimiento","Iniciando revision de Priority Queue");
            //while (PriorityQueueBool) {

            //**************************************
            //Pruebas Database para cargar datos
            Database mydb2 = new Database(getApplicationContext());
            mydb2.abrir();
            try {
                //Pendiente leer los datos
                mydb2.consultarQueue();
//                mydb2.actualizarQueue("PriorityQueueBool","false");// consultarLocalizacion();
//                mydb2.actualizarQueue("PQ1","false");
//                mydb2.actualizarQueue("PQ2","false");
//                mydb2.actualizarQueue("PQ3","false");
            } catch (Exception e) {
                LogUtil.printError("Error", "LockService - PriorityQueueThread - InicializandoValoresThread");
                LogUtil.printFullError("Error", "LockService - PriorityQueueThread - InicializandoValoresThread", e);
            }
            mydb2.cerrar();
            //**************************************

            while (true) {
                if (PriorityQueueBool) {
//                    if (!isThereActiveInternetConnection()) {
//                        PQ1 = true; //1-Unlook
//                        PQ2 = true; //2-Geolock
//                        PQ3 = true; //3-Geounlock
//                        PriorityQueueBool = true;
//                    }
                    if (isThereActiveInternetConnection()) {
                        //asddasd PENDIENTE
                        if (PQ1) { //1-Unlook
                            //*********************************************************************************************
                            new SendUnlock().execute();
                            //*********************************************************************************************
                            Log.e("Seguimiento", "Cola de prioridades 01 (Unlook)");
                            PQ1 = false;
                        }
                        if (PQ2) { //2-Geolock
                            //*********************************************************************************************
                            new SendGeolock().execute();
                            //*********************************************************************************************
                            Log.e("Seguimiento", "Cola de prioridades 02 (Geolock)");
                            PQ2 = false;
                        }
                        if (PQ3) { //3-Geounlock
                            //*********************************************************************************************
                            new SendGeounlock().execute();
                            //*********************************************************************************************
                            Log.e("Seguimiento", "Cola de prioridades 03 (Geounlock)");
                            PQ3 = false;
                        }
                        if (!PQ1 && !PQ2 && !PQ3) {
                            PriorityQueueBool = false;
                            Log.e("Seguimiento", "Todo en falso!!!");
                            //**************************************
                            //Pruebas Database
                            Database mydb = new Database(getApplicationContext());
                            mydb.abrir();
                            try {
                                mydb.actualizarQueue("PriorityQueueBool","false");// consultarLocalizacion();
                                mydb.actualizarQueue("PQ1","false");
                                mydb.actualizarQueue("PQ2","false");
                                mydb.actualizarQueue("PQ3","false");
                            } catch (Exception e) {
                                LogUtil.printError("Error", "LockService - ReUnlock");
                                LogUtil.printFullError("Error", "LockService - ReUnlock", e);
                            }
                            mydb.cerrar();
                            //**************************************
                        }
                    }
                } else {
                    //Log.e("Seguimiento", "PriorityQueueBool esta en false");
                }
                try {
                    //Thread.sleep(PriorityQueueInt);
                    Thread.sleep(5000);
                } catch (Exception e) {
                    LogUtil.printFullError("PriorirtyQueue", "INTERRUPTION EXCEPCION ON GET THREAD", e);
                }
            }
        }
    }

    public class EventsPendingThread extends Thread {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void run() {
            LogUtil.printError("Seguimiento123", "Iniciando EventsPendingThread");
            LogUtil.printError("Seguimiento123", "Iniciando EventsPendingThread");
            LogUtil.printError("Seguimiento123", "Iniciando EventsPendingThread");
            while (true) {
                JSONObject json = null;
                //JSONObject eventos = null;
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("terminal", deviceId));
                //params.add(new BasicNameValuePair("terminal", "g992C7lw5xRflFhSG64K"));


                LogUtil.printError("Seguimiento123", "antes de hacer las peticioones de enevtos pendientes");
                LogUtil.printError("Seguimiento123", "antes de hacer las peticioones de enevtos pendientes");
                LogUtil.printError("Seguimiento123", "antes de hacer las peticioones de enevtos pendientes");

                try {//try01
                    LogUtil.LogEnvioDatos(serverPendingURL, params.toString());//SeguimientoDatos
                    json = jsonParser.makeHttpRequest(serverPendingURL, "GET", params);
                    LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos


                    LogUtil.printError("Seguimiento123", "Respues: "+json.toString());
                    String jArray = null;
                    JSONArray eventos = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) { //Debe ser una version igual o mayor a 4.3
                        jArray = new String(json.getString("events"));
                        eventos = json.getJSONArray("events");
                    } else {
                        LogUtil.printError("Seguimiento123", "no saca el JsonArray");
                    }
                    LogUtil.printError("Seguimiento123", "Respues: "+eventos.length()+"  "+eventos);
                    LogUtil.printError("Seguimiento123", "Respues: "+eventos.length()+"  "+eventos.toString());
                    LogUtil.printError("Seguimiento123", "Respues: "+json.length()+"  "+jArray.length()+"  "+jArray);
                    if (eventos.length()>0) {
                        for(int i=0; i<eventos.length(); i++){
                            JSONObject EventoIndividual = eventos.getJSONObject(i);
                            String idEvento=null, typeEvento=null, messageEvento=null;
                            idEvento = EventoIndividual.getString("id");
                            typeEvento = EventoIndividual.getString("type");
                            messageEvento = EventoIndividual.getString("message");

                            switch (typeEvento) {
                                case "LOCK":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"LOCKED");
                                    startLockActivity();
                                    LogUtil.printError("Error","EventsPendingThread LOCK");
                                    break;
                                case "UNLOCK":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"UNLOCKED");
                                    stopLockActivity();
                                    LogUtil.printError("Error","EventsPendingThread UNLOCK");
                                    break;
                                case "MESSAGE":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"MESSAGED");
                                    id = idEvento;
                                    message = messageEvento;
                                    startMessageActivity();
                                    LogUtil.printError("Error","EventsPendingThread MESSAGE");
                                    break;
                                case "ALARM":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"ALARMED");
                                    //ConfirmarEventoSinSocket(deviceId,idEvento,"ALARMOFF");
                                    startAlarm();
                                    LogUtil.printError("Error","EventsPendingThread ALARM");
                                    break;
                                case "TRACK":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"TRACKED");
                                    tracking = true;
                                    TrackThread tt = new TrackThread();
                                    tt.start();
                                    LogUtil.printError("Error","EventsPendingThread TRACK");
                                    break;
                                case "UNTRACK":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"UNTRACKED");
                                    tracking = false;
                                    LogUtil.printError("Error","EventsPendingThread UNTRACK");
                                    break;
                                case "SCREENSHOT":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"SCREENSHOTOK");
                                    //ConfirmarEventoSinSocket(deviceId,idEvento,"SCREENSHOTUP");
                                    LogUtil.printError("Error","EventsPendingThread SCREENSHOT");
                                    break;
                                case "SHUTDOWN":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"SHUTDOWNOK");
                                    LogUtil.printError("Error","EventsPendingThread SHUTDOWN");
                                    break;
                                case "RESTART":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"RESTARTED");
                                    LogUtil.printError("Error","EventsPendingThread RESTART");
                                    break;
                                case "PHOTO":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"PHOTOOK");
                                    startPhotoActivity();
                                    //ConfirmarEventoSinSocket(deviceId,idEvento,"PHOTOUP");
                                    LogUtil.printError("Error","EventsPendingThread PHOTO");
                                    break;
                                case "DELETE":
                                    ConfirmarEventoSinSocket(deviceId,idEvento,"DELETED");
                                    LogUtil.printError("Error","EventsPendingThread DELETE");
                                    break;
                                default:
                                    LogUtil.printError("Error","LockService - EventsPendingThread - No coincide en Evento");
                                    LogUtil.printError("Error","Datos Recibidos=> id: "+idEvento+", type: "+typeEvento+", message:"+messageEvento);
                                    break;
                            }
                        }
                    } else {
                        LogUtil.printError("Seguimiento123", "No hay eventos pendientes.");
                    }
                    LogUtil.printError("Seguimiento123", "despues de hacer las peticioones de enevtos pendientes");
                    LogUtil.printError("Seguimiento123", "despues de hacer las peticioones de enevtos pendientes");
                    LogUtil.printError("Seguimiento123", "despues de hacer las peticioones de enevtos pendientes");

                } catch (Exception ex) {
                    LogUtil.printError("Error", "LockService - EventsPendingThread - try01");
                    LogUtil.printFullError("Error", "LockService - EventsPendingThread - try01", ex);
                }

                try {
                    //Thread.sleep(3600000);
                    Thread.sleep(15000);
                } catch (Exception e) {
                    LogUtil.printFullError("EventsPendingThread", "LockService - EventsPendingThread - try:Thread.sleep", e);
                }
            }
        }
    }

    public void ConfirmarEventoSinSocket(String terminal, String idEvent, String type) {
        LogUtil.printError("Error", "LockService - ConfirmarEventoSinSocket");
        LogUtil.printError("Error", "LockService - ConfirmarEventoSinSocket");
        LogUtil.printError("Error", "LockService - ConfirmarEventoSinSocket");
        LogUtil.printError("Error", "ter: "+terminal+", id: "+idEvent+", type: "+type);
        LogUtil.printError("Error", "ter: "+terminal+", id: "+idEvent+", type: "+type);
        LogUtil.printError("Error", "ter: "+terminal+", id: "+idEvent+", type: "+type);
        LogUtil.printError("Error", "LockService - ConfirmarEventoSinSocket");
        LogUtil.printError("Error", "LockService - ConfirmarEventoSinSocket");
        LogUtil.printError("Error", "LockService - ConfirmarEventoSinSocket");

        JSONObject json = null;
        //JSONObject eventos = null;
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        //params.add(new BasicNameValuePair("terminal", deviceId));
        params.add(new BasicNameValuePair("terminal", terminal));
        //params.add(new BasicNameValuePair("terminal", "g992C7lw5xRflFhSG64K"));
        params.add(new BasicNameValuePair("event", idEvent));
        params.add(new BasicNameValuePair("type", type));

        LogUtil.printError("Seguimiento123", "antes de hacer las peticioones de enevtos pendientes");
        LogUtil.printError("Seguimiento123", "antes de hacer las peticioones de enevtos pendientes");
        LogUtil.printError("Seguimiento123", "antes de hacer las peticioones de enevtos pendientes");

        if (isThereActiveInternetConnection()) {
            try {//try01
                LogUtil.LogEnvioDatos(serverConfirmURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverConfirmURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos

                //LogUtil.printError("Seguimiento123", "Respues: " + json.toString());
            } catch (Exception ex) {
                LogUtil.printError("Error", "LockService - ConfirmarEventoSinSocket - try01");
                LogUtil.printFullError("Error", "LockService - ConfirmarEventoSinSocket - try01", ex);
            }
        } else {
            //Pendiente: Aqui se encolan las peticiones
            JSONObject combined = new JSONObject();
            String valor = String.valueOf(combined.length());
            try {
                combined.put(valor, params);
            } catch (JSONException e) {
                //e.printStackTrace();
            }

        }
    }

    public class GetOfflinePoliciesThread extends Thread {
        public void run() {
            while (true) {
                new GetOfflinePoliciesAsyncTask().execute();
                //Log.e("*-*-*-*-*", "Delay GetOfflinePolicies: " + offlinePoliciesDelayInt);
                try {

                    Thread.sleep(offlinePoliciesDelayInt);
                } catch (Exception e) {
                    Log.e("GetOfflinePolThread", "GetOfflinePoliciesThread");
                }
            }
        }
    }

    public class UploadDataAsyncTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONObject json = null;
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("option", "upload_data"));
            params.add(new BasicNameValuePair("terminal", deviceId));
            String version = Build.VERSION.RELEASE;

            LogUtil.printError("DATA", "ON UPLOAD");

            try {
                StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
                long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
                long megAvailable = bytesAvailable / (1024 * 1024);
                params.add(new BasicNameValuePair("data", "AvailableMemory"));
                params.add(new BasicNameValuePair("value", Long.toString(megAvailable)));
                params.add(new BasicNameValuePair("type", "OTHER"));
                LogUtil.printError("MEMORY AVAILABLE : ", megAvailable + "");
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("MEMORY", "EXCEPCION", ex);
            }

            try {
                params.add(new BasicNameValuePair("data", "AndroidVersion"));
                params.add(new BasicNameValuePair("value", version));
                params.add(new BasicNameValuePair("type", "SOFTWARE"));
                LogUtil.printError("ANDROID VERSION : ", version);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("ANDROID VERSION", "EXCEPCION", ex);
            }

            TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String imei = "";
            imei = tManager.getDeviceId();

            try {
                params.add(new BasicNameValuePair("data", "Imei"));
                params.add(new BasicNameValuePair("value", imei));
                params.add(new BasicNameValuePair("type", "OTHER"));
                LogUtil.printError("IMEI : ", imei);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullErrorInit("IMEI", "EXCEPCION", ex);
            }

            try {

                params.add(new BasicNameValuePair("data", "Serial"));
                params.add(new BasicNameValuePair("value", tManager.getDeviceId()));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("SERIAL : ", "" + tManager.getDeviceId());
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos

            } catch (Exception ex) {
                LogUtil.printFullErrorInit("SERIAL", "EXCEPCION", ex);
            }

            try {
                String versionSDK = Build.VERSION.SDK;
                params.add(new BasicNameValuePair("data", "SDKVersion"));
                params.add(new BasicNameValuePair("value", versionSDK));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                Log.e("SDKVersion : ", versionSDK);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {

                PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                String appVersion = pInfo.versionName;

                params.add(new BasicNameValuePair("data", "NodosVersion"));
                params.add(new BasicNameValuePair("value", appVersion));
                params.add(new BasicNameValuePair("type", "SOFTWARE"));
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                String numserial = Build.SERIAL;
                params.add(new BasicNameValuePair("data", "SerialNumber"));
                params.add(new BasicNameValuePair("value", numserial));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("SERIAL ", numserial);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("SERIAL", "EXCEPCION", ex);
            }

            try {
                String manufacturer = Build.MANUFACTURER;
                params.add(new BasicNameValuePair("data", "Manufacturer"));
                params.add(new BasicNameValuePair("value", manufacturer));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("MANUFACTURER :", Build.MANUFACTURER);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("MANUFACTURER", "EXCEPCION", ex);
            }

            try {
                String board = Build.BOARD;
                params.add(new BasicNameValuePair("data", "Board"));
                params.add(new BasicNameValuePair("value", board));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("BOARD : ", Build.BOARD);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("BOARD", "EXCEPCION", ex);
            }

            try {
                String cpuAbi = Build.CPU_ABI;
                params.add(new BasicNameValuePair("data", "CPU_ABI"));
                params.add(new BasicNameValuePair("value", cpuAbi));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("CPU_ABI : ", Build.CPU_ABI);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("CPU", "EXCEPCION", ex);
            }

            try {
                String device = Build.DEVICE;
                params.add(new BasicNameValuePair("data", "Device"));
                params.add(new BasicNameValuePair("value", device));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("Device : ", Build.DEVICE);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("DEVICE", "EXCEPCION", ex);
            }

            try {
                String hardware = Build.HARDWARE;
                params.add(new BasicNameValuePair("data", "Hardware"));
                params.add(new BasicNameValuePair("value", hardware));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("Hardware : ", Build.HARDWARE);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("HARDWARE", "EXCEPCION", ex);
            }

            try {
                String host = Build.HOST;
                params.add(new BasicNameValuePair("data", "Host"));
                params.add(new BasicNameValuePair("value", host));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("Host : ", Build.HOST);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("HOST", "EXCEPCION", ex);
            }

            try {
                String id = Build.ID;
                params.add(new BasicNameValuePair("data", "BuildId"));
                params.add(new BasicNameValuePair("value", id));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("BuildId : ", Build.ID);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("BUILD ID", "EXCEPCION", ex);
            }

            try {
                String model = Build.MODEL;
                params.add(new BasicNameValuePair("data", "Model"));
                params.add(new BasicNameValuePair("value", model));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                Log.e("Model :", Build.MODEL);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("MODEL", "EXCEPCION", ex);
            }

            try {
                String product = Build.PRODUCT;
                params.add(new BasicNameValuePair("data", "Product"));
                params.add(new BasicNameValuePair("value", product));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("Product", Build.PRODUCT);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("PRODUCT", "EXCEPCION", ex);
            }

            try {
                String type = Build.TYPE;
                params.add(new BasicNameValuePair("data", "Type"));
                params.add(new BasicNameValuePair("value", type));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("Type", Build.TYPE);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("TYPE", "EXCEPCION", ex);
            }

            try {
                String user = Build.USER;
                params.add(new BasicNameValuePair("data", "User"));
                params.add(new BasicNameValuePair("value", user));
                params.add(new BasicNameValuePair("type", "HARDWARE"));
                LogUtil.printError("User", Build.USER);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("USER", "EXCEPCION", ex);
            }

            try {
                params.add(new BasicNameValuePair("data", "Batery"));
                params.add(new BasicNameValuePair("value", bateria));
                params.add(new BasicNameValuePair("type", "SOFTWARE"));
                LogUtil.printError("BATERY_ LOG", bateria);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("BATERY LOG", "EXCEPCION", ex);
            }

            try {
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
                params.add(new BasicNameValuePair("data", "IP"));
                params.add(new BasicNameValuePair("value", ip));
                params.add(new BasicNameValuePair("type", "SOFTWARE"));
                LogUtil.printError("IP", ip);
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("IP", "EXCEPCION", ex);
            }

            try {
                String ip = "";
                URL url = new URL("http://api.ipify.org");
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                for (String line; (line = reader.readLine()) != null; ) {
                    ip = line;
                }
                LogUtil.printError("LINE", " : " + ip);
                params.add(new BasicNameValuePair("data", "PublicIP"));
                params.add(new BasicNameValuePair("value", ip));
                params.add(new BasicNameValuePair("type", "OTHER"));


                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
            } catch (Exception ex) {
                LogUtil.printFullError("PUBLIC IP", "EXCEPCION", ex);
            }

            try {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                activityManager.getMemoryInfo(mi);
                long availableMegs = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    availableMegs = mi.totalMem;
                    params.add(new BasicNameValuePair("data", "RAM"));
                    params.add(new BasicNameValuePair("value", Long.toString(availableMegs)));
                    params.add(new BasicNameValuePair("type", "HARDWARE"));
                    LogUtil.printError("RAM", Long.toString(availableMegs));
                    LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                    json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                    LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
                }
            } catch (Exception ex) {
                LogUtil.printFullError("RAM", "EXCEPCION", ex);
            }
            return json;
        }
    }

    public class ConsultAppUpdate extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Log.e("ACTUALIZACION", "CONSULTANDO");
        }

        @Override
        protected Void doInBackground(Void... params) {
            URL url;
            try {
                url = new URL("http://www.nodos.com/shield/version/updatetxt.txt"); //Cambiar cuando se haga la actualizacion real
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setReadTimeout(15 * 1000);
                c.setUseCaches(false);
                c.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                JSONObject json = new JSONObject(stringBuilder.toString());
                Log.e("", "" + json.getInt("versionCode"));
                Log.e("", "" + json.getString("versionName"));
                versionUltima = json.getInt("versionCode");
                downloadURLUpdate = json.getString("downloadURL");

            } catch (MalformedURLException e) {
                LogUtil.printFullError("ACTUALIZACION", "MALFORMED URL EXCEPCION ON CONSULT", e);
            } catch (IOException e) {
//                LogUtil.printFullError("ACTUALIZACION", " IO EXCEPCION", e);
                e.printStackTrace();
            } catch (Exception e) {
                LogUtil.printFullError("ACTUALIZACION", "JSON EXCEPCION", e);
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (versionUltima > versionAnterior) {
                UpdateApp act = new UpdateApp();
                act.setContext(getApplicationContext());
                act.execute(downloadURLUpdate);
            }
        }
    }

    public void contDown() {
        new CountDownTimer(3600000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Log.e("seconds remaining:", "done");

                ConsultAppUpdate consultar = new ConsultAppUpdate();
                consultar.execute();
                contDown();

            }
        }.start();
    }

    public class UpdateApp extends AsyncTask<String, Void, Void> {
        private Context context;

        public void setContext(Context contextf) {
            context = contextf;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... arg0) {
            try {
                URL url = new URL(arg0[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                Log.e("prueba 1", "prueba 1");
                c.connect();
                Log.e("prueba 2", "prueba 2");
                String PATH = "/storage/sdcard0/DCIM/"; //String PATH = "/mnt/sdcard/Download/";
                File file = new File(PATH);
                Log.e("prueba 3", "prueba 3");
                file.mkdirs();
                Log.e("prueba 4", "prueba 4");
                File outputFile = new File(file, "update.apk");
                Log.e("prueba 5", "prueba 5");
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                Log.e("prueba 6", "prueba 6");
                FileOutputStream fos = new FileOutputStream(outputFile);
                Log.e("prueba 7", "prueba 7");
                InputStream is = c.getInputStream();
                Log.e("prueba 8", "prueba 8");
                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File("/storage/sdcard0/DCIM/update.apk")), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                context.startActivity(intent);

            } catch (Exception e) {
                Log.e("UpdateAPP", "Update error! " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                    Log.e("Deletefil", files.toString());
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    //private void RegistroApps() throws PackageManager.NameNotFoundException {
    public class RegistroApps extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //Log.e("***Seg***","Entro RegApps-DoInBack");
            try {
                //Log.e("***Seg***","Enviando uninstall_software");
                List<NameValuePair> paramsB = new ArrayList<NameValuePair>();

                paramsB.add(new BasicNameValuePair("terminal", deviceId));
                paramsB.add(new BasicNameValuePair("option", "uninstall_software"));
                //params.add(new BasicNameValuePair("url", info));


                LogUtil.LogEnvioDatos(serverHandlerURL, paramsB.toString());//SeguimientoDatos
                JSONObject jsonB = jsonParser.makeHttpRequest(serverHandlerURL, "GET", paramsB);
                LogUtil.LogEnvioDatos("Respuesta", jsonB.toString());//SeguimientoDatos
                //Log.e("***json***","Json Result: "+jsonB.getString("result"));
                //Log.e("***json***","Json Error: "+jsonB);
            } catch (Exception e) {
                Log.e("***Error***", "Error enviando uninstall_software");
            }

            final PackageManager pm = getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            List<PackageInfo> packagesInstPac = getPackageManager().getInstalledPackages(0);
            String version = "";
            String name2 = "";
            long installed = 0;

            for (ApplicationInfo packageInfo : packages) {
                //Log.e("*******", "****************************************");
                //Log.e("*******", "****************************************");
                String package_name = packageInfo.packageName;
                try {
                    name2 = (String) pm.getApplicationLabel(pm.getApplicationInfo(package_name, PackageManager.GET_META_DATA));
                    //Log.e("*******", "Name :" + name2);
                    ApplicationInfo appInfo = pm.getApplicationInfo(packageInfo.packageName, 0);
                    String appFile = appInfo.sourceDir;
                    installed = new File(appFile).lastModified();
                    //Log.e("*******", "Date Inst :" + installed);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < packagesInstPac.size(); i++) {
                    PackageInfo p = packagesInstPac.get(i);
                    String prueba = p.applicationInfo.sourceDir;
                    if (packageInfo.sourceDir.contains(prueba)) {
                        version = p.versionName;
                        break;
                    }
                }
                //Log.e("*******", "Nombre: " + name2 + ", Fecha: " + installed + ", Version: " + version);


                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String imei = telephonyManager.getDeviceId();

                try {
                    //Log.e("***Seg***","Enviando upload_software");
                    String info = "https://www.nodos.com.co/universal_handler.php";
                    List<NameValuePair> paramsE = new ArrayList<NameValuePair>();
                    paramsE.add(new BasicNameValuePair("name", name2));
                    //params.add(new BasicNameValuePair("installed", String.valueOf(installed)));
                    paramsE.add(new BasicNameValuePair("vendor", ""));
                    paramsE.add(new BasicNameValuePair("version", version));
                    paramsE.add(new BasicNameValuePair("terminal", deviceId));
                    paramsE.add(new BasicNameValuePair("option", "upload_software"));
                    //params.add(new BasicNameValuePair("url", info));


                    LogUtil.LogEnvioDatos(serverHandlerURL, paramsE.toString());//SeguimientoDatos
                    JSONObject json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", paramsE);
                    LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
                    //Log.e("***json***","Json Result: "+json.getString("result"));
                    //Log.e("***json***","Json Error: "+json.getString("error"));
                } catch (Exception e) {
                    Log.e("***Error***", "Error enviando upload_software");
                }
            }

            try {
                //Log.e("Seguimiento", "Ingresa a Registro de llamadas");
                startRegCallsActivity();
            } catch (Exception e) {
                Log.e("Seguimiento", "Error enviando registro llamadas");
            }


            return null;
        }

    }

//    public class RestartServiceManually extends Thread {
//        public void run() {
//            while (true) {
//
//                try {
//                    LogUtil.printError("SERVICE", "---------IN COUNT REESTART...------------");
//                    Thread.sleep(300000);//3600000
//                    try {
//                        LogUtil.printError("SERVICE", "-------------REESTART-----------------");
//                        stopSelf();
//                        sendBroadcast(new Intent("RestartService"));
//                        startService(new Intent(getApplicationContext(), LockService.class));
//                        //HERE
////
////                        stopSocket();
////                        //GetStatusAndMessageThread.currentThread().interrupt();
////
////                        if (Looper.getMainLooper().getThread() != GetStatusAndMessageThread.currentThread()) {
////                            GetStatusAndMessageThread.currentThread().interrupted();
////                            UploadGeoDataThread.currentThread().interrupted();
////                            CheckGeoPoliciesThread.currentThread().interrupted();
////                            GetOfflinePoliciesThread.currentThread().interrupted();
////                        }
//
//                    } catch (Exception e) {
//                        LogUtil.printFullErrorInit("STOP SERVICE", "EXCEPTION ON STOP", e);
//                    }
//                    continueService();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    //    private void stopSocket() {
//        LogUtil.printError("SOCKET", "MANUAL STOP");
//        try {
//            mSocket.disconnect();
//            mSocket.off("alarm message");
//            mSocket.off("terminal state");
//            mSocket.off("block message");
//            mSocket.off("unlock message");
//            mSocket.off("terminal unlock");
//            mSocket.off("track position message");
//            mSocket.off("untrack message");
//            mSocket.off("sleep message");
//            mSocket.off("shutdown message");
//            mSocket.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}