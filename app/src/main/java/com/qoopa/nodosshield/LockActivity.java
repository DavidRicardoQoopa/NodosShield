package com.qoopa.nodosshield;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class LockActivity extends Activity {

    public Context ctx = this;
    private WindowManager winManager;
    private ImageView verifyImage;
    private LinearLayout wifiManager;
    private LinearLayout primer_intento;
    private ImageView ok;
    public String code;
    public String id;
    public String message;
    public String codigoAux;
    private String nombre_foto = "";
    private boolean Conf_Wifi = false;
    LinearLayout bloqueo;// = (LinearLayout) findViewById(R.id.teclado);
    ImageView boton_verificar;

    LinearLayout backgroundOne, backgroundTwo, backgroundThree, backgroundFour, FormularioBloqueo;
    int backgroundNumber = 6;

    String result;
    RelativeLayout wrapperView;
    JSONParser jsonParser = new JSONParser();
    Typeface tf1;
    Typeface tf2;

    String serverHandlerURL = "https://www.nodos.com.co/universal_handler.php";

    CaptureImage capture;

    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("***-***", "onReceive = "+intent.getAction());
            Log.e("***-***", "EEXXTTRRAASS = " + intent.getExtras());

            try {
                if (intent.getAction().equals("com.qoopa.nodosshield")) {
                    if (intent.getExtras() == (null)) {
                        Log.e("***-***", "001");
                        finish();
                    } else {
                        Log.e("***-***", "Before onPause");
                        Log.e("***-***", "002");
                        //onPause();
                        onStop();
                        //onResume();
                        Log.e("***-***","After onPause");
                        //onResume();
                    }
                }
            } catch (Exception e) {
                LogUtil.printFullError("BROADCAST RECEIVER", "EXCEPCION", e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_teclado);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        Intent i = getIntent();
        nombre_foto = i.getExtras().getString("Nombre Imagen");

        final ImageView ingrese = (ImageView) findViewById(R.id.ingrese);
        ingrese.setVisibility(View.GONE);

        bloqueo = (LinearLayout) findViewById(R.id.teclado);

        //bloqueo = (LinearLayout) findViewById(R.id.teclado);

        try {
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction("com.qoopa.nodosshield");
            mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        } catch (Exception e) {
            LogUtil.printFullError("LOCK ACTIVITY", "EXCEPTION ON BROADCAST", e);
        }

        try {

            bloqueo.setVisibility(View.VISIBLE);
            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, //frente a todo
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //la muestra cuando esta bloqueda
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL //no touch
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, //pantalla completa
                    PixelFormat.TRANSLUCENT);
            this.winManager = ((WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE));
            this.wrapperView = new RelativeLayout(getBaseContext());
            getWindow().setAttributes(localLayoutParams);
            View.inflate(this, R.layout.content_teclado, this.wrapperView);
            //View.inflate(this, Integer.parseInt(WifiManager.ACTION_PICK_WIFI_NETWORK), this.wrapperView);
            this.winManager.addView(this.wrapperView, localLayoutParams);

        } catch (Exception e) {
            LogUtil.printFullError("LOCK ACTIVITY", "EXCEPTION ON CREATE LOCK WINDOW", e);
        }

        //*************************************************************************************************************

        backgroundOne = (LinearLayout) wrapperView.findViewById(R.id.fondo_1);
        backgroundTwo = (LinearLayout) wrapperView.findViewById(R.id.fondo_2);
        backgroundThree = (LinearLayout) wrapperView.findViewById(R.id.fondo_3);
        backgroundFour = (LinearLayout) wrapperView.findViewById(R.id.fondo_4);
        FormularioBloqueo = (LinearLayout) wrapperView.findViewById(R.id.Formulario_Bloqueo);

        wifiManager = (LinearLayout) wrapperView.findViewById(R.id.pantalla_Wifi_2);
        final ImageView boton_wifi = (ImageView) wrapperView.findViewById(R.id.BotonWifi);
        boton_wifi.setVisibility(View.VISIBLE);
        boton_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("*******", "Click Listener Ok.");
                finish();
                Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent); //getApplicationContext().startService(intent);
                //startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
//                FormularioBloqueo.setVisibility(View.GONE);
//                backgroundOne.setVisibility(View.GONE);
//                backgroundTwo.setVisibility(View.GONE);
//                backgroundThree.setVisibility(View.GONE);
//                backgroundFour.setVisibility(View.GONE);


                //wifiManager.setVisibility(View.VISIBLE);
                //finish(); // habilita la pantalla y botones!!!

                Log.e("***12345***", "LA - Ingresa en el metodo que quiero que me active de nuevo el bloqueo!!!.");
                //LockService.PruebaBloqueo();

//                final Calendar c = Calendar.getInstance();
//                String tempFile = (c.get(Calendar.YEAR) + "" + "-" + (c.get(Calendar.MONTH) + 1) + "" + "-" + c.get(Calendar.DAY_OF_MONTH) + "" + "_" + c.get(Calendar.HOUR_OF_DAY) + "" + "-" + c.get(Calendar.MINUTE) + "" + "-" + c.get(Calendar.SECOND)) + "(front)" + ".jpg";
//                Intent i = new Intent(ctx, LockActivity.class);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                i.putExtra("Nombre Imagen", tempFile);
//                LogUtil.printError("LOCK ACTIVITY", "INICIANDO: " + tempFile);
//                startActivity(i);

//                //Ayuda de Esteban
//                Intent intent2 = new Intent(LockActivity.this, LockActivity.class);
//                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent2);

//                LockService pru = new LockService();
                //pru.PruebaBloqueo();

                new PermisosWifi().execute();


                Log.e("***12345***", "LA - Ingresa en el metodo que quiero que me active de nuevo el bloqueo!!!.");

                Log.e("*******","Click Listener Ok.");
            }
        });

        final ImageView boton_wifi_back = (ImageView) wrapperView.findViewById(R.id.BotonRegreso);
        boton_wifi_back.setVisibility(View.VISIBLE);
        boton_wifi_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("*******", "Click Listener wifiManager Ok.");

                wifiManager.setVisibility(View.GONE);
                FormularioBloqueo.setVisibility(View.VISIBLE);

                if(backgroundNumber == 1){
                    backgroundOne.setVisibility(View.VISIBLE);
                    backgroundTwo.setVisibility(View.GONE);
                    backgroundThree.setVisibility(View.GONE);
                    backgroundFour.setVisibility(View.GONE);
                } else if(backgroundNumber == 2){
                    backgroundOne.setVisibility(View.GONE);
                    backgroundTwo.setVisibility(View.VISIBLE);
                    backgroundThree.setVisibility(View.GONE);
                    backgroundFour.setVisibility(View.GONE);

                } else if(backgroundNumber == 3){
                    backgroundOne.setVisibility(View.GONE);
                    backgroundTwo.setVisibility(View.GONE);
                    backgroundThree.setVisibility(View.VISIBLE);
                    backgroundFour.setVisibility(View.GONE);
                } else if(backgroundNumber == 4){
                    backgroundOne.setVisibility(View.GONE);
                    backgroundTwo.setVisibility(View.GONE);
                    backgroundThree.setVisibility(View.GONE);
                    backgroundFour.setVisibility(View.VISIBLE);
                } else {
                    backgroundOne.setVisibility(View.GONE);
                    backgroundTwo.setVisibility(View.GONE);
                    backgroundThree.setVisibility(View.GONE);
                    backgroundFour.setVisibility(View.VISIBLE);
                }

                Log.e("*******","Click Listener wifiManager Ok.");
            }
        });

        //*************************************************************************************************************


        boton_verificar = (ImageView) wrapperView.findViewById(R.id.verificar);
        boton_verificar.setVisibility(View.VISIBLE);
        boton_verificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new CheckCodeAsyncTask().execute();

                try {
                    capture = new CaptureImage();
                    capture.setNombre_imagen(nombre_foto);
                    LogUtil.printError("NOMBRE FOTO", "" + nombre_foto);
                    capture.cam();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("SCREENSHOT", "EXCEPTION");
                }
            }
        });




        TextView textoCod = (TextView) wrapperView.findViewById(R.id.texto_codigo_unlock);

        tf2 = Typeface.createFromAsset(getAssets(), "fonts/XBall.ttf");
        textoCod.setTypeface(tf2);
        textoCod.setVisibility(View.VISIBLE);

        final EditText et1 = (EditText) wrapperView.findViewById(R.id.contraseña1);
        et1.setTypeface(tf2);
        et1.setVisibility(View.VISIBLE);



        int min = 1;
        int max = 4;
        Random r = new Random();
        backgroundNumber = r.nextInt(max - min + 1) + min;
        Log.e("RANDOM",": "+backgroundNumber);

        if(backgroundNumber == 1){
            backgroundOne.setVisibility(View.VISIBLE);
            backgroundTwo.setVisibility(View.GONE);
            backgroundThree.setVisibility(View.GONE);
            backgroundFour.setVisibility(View.GONE);
        } else if(backgroundNumber == 2){
            backgroundOne.setVisibility(View.GONE);
            backgroundTwo.setVisibility(View.VISIBLE);
            backgroundThree.setVisibility(View.GONE);
            backgroundFour.setVisibility(View.GONE);

        } else if(backgroundNumber == 3){
            backgroundOne.setVisibility(View.GONE);
            backgroundTwo.setVisibility(View.GONE);
            backgroundThree.setVisibility(View.VISIBLE);
            backgroundFour.setVisibility(View.GONE);
        } else if(backgroundNumber == 4){
            backgroundOne.setVisibility(View.GONE);
            backgroundTwo.setVisibility(View.GONE);
            backgroundThree.setVisibility(View.GONE);
            backgroundFour.setVisibility(View.VISIBLE);
        } else {
            Log.e("RANDOM",": ???");
        }

        new GetMessageAsyncTask().execute();
    }

    protected void onDestroy() {
        try {
            this.winManager.removeView(this.wrapperView);
            this.wrapperView.removeAllViews();
            super.onDestroy();
            mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            LogUtil.printFullError("LOCK ACTIVITY", "EXCEPTION ON DESTROY", e);
        }
    }

    private class GetMessageAsyncTask extends AsyncTask<String, String, JSONObject> {

        TextView mensajeTextView = (TextView) wrapperView.findViewById(R.id.mensaje);
        TextView textoCod = (TextView) wrapperView.findViewById(R.id.texto_codigo_unlock);
        final ImageView boton_verificar = (ImageView) wrapperView.findViewById(R.id.verificar);
        final EditText et1 = (EditText) wrapperView.findViewById(R.id.contraseña1);
        boolean geoLock = false;

        @Override
        protected JSONObject doInBackground(String... params) {

            List<NameValuePair> params2 = new ArrayList<NameValuePair>();
            params2.add(new BasicNameValuePair("option", "get_status"));
            params2.add(new BasicNameValuePair("terminal", LockService.getDeviceId()));

            try {
                LogUtil.LogEnvioDatos(serverHandlerURL, params2.toString());//SeguimientoDatos
                JSONObject json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params2);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
                Log.e("Unlock_", json.toString());
                Log.e("Unlock_", json.getString("code"));
                LockService.setCodigoBloqueo(json.getString("code"));

                message = json.getString("message");
                Log.e("MENSAJE",""+ message);
                if(message.equals("Terminal bloqueada por localización")|| code.equals("0")){
                    geoLock = true;
                }
                code = json.getString("code");

                id = json.getString("id");
                codigoAux = code;

//                Database mybd = new Database(getApplicationContext());
//                mybd.abrir();
//                mybd.registrarEstado(code, "LOCKED");
//                mybd.cerrar();

                Log.e("Message", "" + message);
                Log.e("Code", "" + code);
                Log.e("CodigoAux", codigoAux);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            mensajeTextView.setText(message);
            mensajeTextView.setTypeface(tf1);
            if(geoLock){
                textoCod.setVisibility(View.GONE);
                et1.setVisibility(View.GONE);
                boton_verificar.setVisibility(View.GONE);
            }
        }
    }

    private class CheckCodeAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            try {
                verifyImage = (ImageView) wrapperView.findViewById(R.id.verificar_codigo);
                primer_intento = (LinearLayout) wrapperView.findViewById(R.id.primer_intento);
                //cerrar = (ImageView) wrapperView.findViewById(R.id.cerrar);
                ok = (ImageView) wrapperView.findViewById(R.id.ok);

                verifyImage.setVisibility(View.VISIBLE);
                primer_intento.setVisibility(View.GONE);
                ok.setVisibility(View.GONE);
                //cerrar.setVisibility(View.GONE);

                final EditText et1 = (EditText) wrapperView.findViewById(R.id.contraseña1);
                result = et1.getText().toString();
                et1.setTypeface(tf1);
                LogUtil.printError("UNLOCK REVISION", "CODIGO INGRESADO: " + result);
                LogUtil.printError("UNLOCK REVISION", "CODIGO DE DESBLOQUEO: " + codigoAux);

                //if (!isOnline()) {
                    LogUtil.printError("UNLOCK REVISION", "DISPOSITIVO ESTA OFFLINE -->"+result+":::"+LockService.getCodigoBloqueo());

                    if (result.equals(LockService.getCodigoBloqueo())) {

                        LogUtil.printError("UNLOCK REVISION", "CODIGOS COINCIDEN");
                        Database mybd = new Database(getApplicationContext());
                        mybd.abrir();
                        mybd.borrarEstado();
                        mybd.registrarEstado(code, "UNLOCKED");
                        mybd.cerrar();
                        this.cancel(true);
                        finish();
                        if ((!isOnline())) {
                            //if ("FAIL".equals(data.getString("result"))) {
                                LockService.ReUnlock();
                                //PQ1 = true; //1-Unlook
                                //PQ2 = true; //2-Geolock
                                //PQ3 = true; //3-Geounlock
                                //PriorityQueueBool = true;
                            //}
                        }
                    } else {
                        LogUtil.printError("UNLOCK REVISION", "Codigos diferentes -->"+result+":::"+LockService.getCodigoBloqueo());
                    }
                //}
//                else {
//                    LogUtil.printDebug("UNLOCK REVISION", "DISPOSITIVO ESTA ONLINE");
//                }
            } catch (Exception e) {
                LogUtil.printFullError("UNLOCK REVISION", "EXCEPCION ON PRE EXECUTE", e);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            List<NameValuePair> params2 = new ArrayList<NameValuePair>();
            params2.add(new BasicNameValuePair("option", "unlock"));
            params2.add(new BasicNameValuePair("terminal", LockService.getDeviceId()));
            params2.add(new BasicNameValuePair("code", result));

            //if ((isOnline())) {
                //LogUtil.printError("UNLOCK REVISION", "DISPOSITIVO ESTA ONLINE");
                try {
                    LogUtil.LogEnvioDatos(serverHandlerURL, params2.toString());//SeguimientoDatos
                    JSONObject data = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params2);
                    LogUtil.LogEnvioDatos("Respuesta", data.toString());//SeguimientoDatos
                    // LogUtil.printError("UNLOCK REVISION", "REQUEST: " + data.toString());
                    LockService.setState(data.getString("state"));
                    LockService.setCodigoBloqueo(result);

                } catch (Exception e) {
                    LockService.ReUnlock();
                    //e.printStackTrace();
                    LogUtil.printFullError("UNLOCK REVISION", "EXCEPCION REQUEST", e);
                }

            //}

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                verifyImage.setVisibility(View.GONE);

                if (LockService.getState().equals("LOCKED")) {
                    LogUtil.printError("UNLOCK REVISION", "MANTENIENDO PANTALLA DE BLOQUEO");
                    primer_intento.setVisibility(View.VISIBLE);
                    ok.setVisibility(View.VISIBLE);

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            primer_intento.setVisibility(View.GONE);
                            ok.setVisibility(View.GONE);
                        }
                    });
                } else {
                    LogUtil.printError("UNLOCK REVISION ", "ESCONDIENDO PANTALLA DE BLOQUEO");
                    finish();
                }
            } catch (Exception e) {
                LogUtil.printFullErrorInit("UNLOCK REVISION", "EXCEPCION POST EXECUTE", e);
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private class PermisosWifiThread extends Thread {

        @Override
        public void run() {
            //while (true) {
            try {
                Log.e("***456***","");
                this.sleep(5000);
                Log.e("***456***","");
            } catch (Exception ex) {
                LogUtil.printFullError("TRACKING", "INTERRUPT EXCEPCION ", ex);
            }
            try {
                final Calendar c = Calendar.getInstance();
                String tempFile = (c.get(Calendar.YEAR) + "" + "-" + (c.get(Calendar.MONTH) + 1) + "" + "-" + c.get(Calendar.DAY_OF_MONTH) + "" + "_" + c.get(Calendar.HOUR_OF_DAY) + "" + "-" + c.get(Calendar.MINUTE) + "" + "-" + c.get(Calendar.SECOND)) + "(front)" + ".jpg";
                Intent i = new Intent(ctx, LockActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("Nombre Imagen", tempFile);
                LogUtil.printError("LOCK ACTIVITY", "INICIANDO: " + tempFile);
                startActivity(i);
            } catch (Exception e) {
                LogUtil.printFullError("Error", "Error Bloqueando el dispositivo", e);
            }
            //}
        }
    }

    private class PermisosWifiBoton extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {

            try {
                Log.e("***456***","BotonWifi");
                Thread.sleep(15000);
                Log.e("***456***","BotonWifi");
            } catch(InterruptedException e) {}

            return null;
        }
    }

    private class PermisosWifi extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("***456***", "pre David Trompa");
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            Log.e("***456***", "post David Trompa");
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            Log.e("***456***","antes try David Trompa");
            try {
                Log.e("***456***","David Trompa");
                Thread.sleep(15000);
                Log.e("***456***","David Trompa");
            } catch(InterruptedException e) {
                Log.e("***456***","Error David Trompa");
            }

            final Calendar c = Calendar.getInstance();
            String tempFile = (c.get(Calendar.YEAR) + "" + "-" + (c.get(Calendar.MONTH) + 1) + "" + "-" + c.get(Calendar.DAY_OF_MONTH) + "" + "_" + c.get(Calendar.HOUR_OF_DAY) + "" + "-" + c.get(Calendar.MINUTE) + "" + "-" + c.get(Calendar.SECOND)) + "(front)" + ".jpg";
            Intent i = new Intent(ctx, LockActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("Nombre Imagen", tempFile);
            LogUtil.printError("LOCK ACTIVITY", "INICIANDO: " + tempFile);
            startActivity(i);

            return null;
        }
    }

    private void startMessageActivity() {

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
//                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
//                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

//        try {
//            LogUtil.printError("LOCK ACTIVITY", "001");
//
//
//            //finish();
//
//            //bloqueo = (LinearLayout) findViewById(R.id.teclado);
//            bloqueo.setVisibility(View.GONE);
//            LogUtil.printError("LOCK ACTIVITY", "002");
//            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR, //frente a todo
//                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //la muestra cuando esta bloqueda
//                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL //no touch
//                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, //pantalla completa
//                    PixelFormat.TRANSLUCENT);
//            LogUtil.printError("LOCK ACTIVITY", "003");
//            this.winManager = ((WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE));
//            this.wrapperView = new RelativeLayout(getBaseContext());
//            LogUtil.printError("LOCK ACTIVITY", "004");
//            getWindow().setAttributes(localLayoutParams);
//            View.inflate(this, R.layout.activity_message, this.wrapperView);
//            //View.inflate(this, Integer.parseInt(WifiManager.ACTION_PICK_WIFI_NETWORK), this.wrapperView);
//            this.winManager.addView(this.wrapperView, localLayoutParams);
//            LogUtil.printError("LOCK ACTIVITY", "005");
//
//        } catch (Exception e) {
//            LogUtil.printFullError("LOCK ACTIVITY", "EXCEPTION ON CREATE LOCK WINDOW", e);
//        }

        //
        // bloqueo = (LinearLayout) findViewById(R.id.teclado);
//        bloqueo.setVisibility(View.INVISIBLE);
//
//        LogUtil.printError("LOCK ACTIVITY", "INICIANDO WIFI");
//        try {
//            LogUtil.printError("LOCK ACTIVITY", "001");
//            Intent i2 = new Intent(ctx, MessageActivity.class);
//            LogUtil.printError("LOCK ACTIVITY", "002");
//            i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            LogUtil.printError("LOCK ACTIVITY", "003");
//            //i2.putExtra("Message", message);
//            LogUtil.printError("LOCK ACTIVITY", "004");
//            startActivity(i2);
//            LogUtil.printError("LOCK ACTIVITY", "005");
//        } catch (Exception e) {
//            LogUtil.printFullError("LOCK ACTIVITY", "EXCEPCION", e);
//        }

        try {

            //mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
            //IntentFilter mIntentFilter = new IntentFilter();
            //mIntentFilter.addAction("com.qoopa.nodosshield.Wifi");
            //mLocalBroadcastManager.sendBroadcast(new Intent ("com.qoopa.nodosshield.Wifi"));

//            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
//            localBroadcastManager.sendBroadcast(new Intent("com.qoopa.nodosshield.Wifi"));

            final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
            Intent inte = new Intent("com.qoopa.nodosshield");
            inte.putExtra("Lugar","Wifi");
            localBroadcastManager.sendBroadcast(inte);

            Log.e("LOCK ACTIVITY", "PROBANDO");
        } catch (Exception e) {
            LogUtil.printFullErrorInit("LOCK ACTIVITY", "EXCEPCION", e);
        }
    }



//    public static Bitmap captureScreen(View v) {
//        Bitmap screenshot = null;
//        try {
//            if (v != null) {
//                screenshot = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
//                Canvas canvas = new Canvas(screenshot);
//                v.draw(canvas);
//            }
//        } catch (Exception e) {
//            Log.d("ScreenShotActivity", "Failed to capture screenshot because:" + e.getMessage());
//        }
//        return screenshot;
//    }

//    public static void saveImage(Bitmap bitmap) throws IOException {
//
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 40, bytes);
//        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "test.png");
//        f.createNewFile();
//        FileOutputStream fo = new FileOutputStream(f);
//        fo.write(bytes.toByteArray());
//        fo.close();
//    }

//    public class UploadPhotosAsyncTask extends AsyncTask<String, String, JSONObject> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected JSONObject doInBackground(String... args) {
//            JSONObject json = null;
//
//            LogUtil.printDebug("SCREEN SHOT ", "UPLOADING");
//            try {
//
//                ArrayList paramsPhoto = new ArrayList();
//                paramsPhoto.add(new BasicNameValuePair("option", "set_image"));
//                //params.add(new BasicNameValuePair("terminal", deviceId));
//                paramsPhoto.add(new BasicNameValuePair("id", id));
//                Log.e("ID SCREEN", "" + id);
//                paramsPhoto.add(new BasicNameValuePair("image", "https://www.nodos.com.co/img/terminal_data/" + nombre_foto));
//                json = jsonParser.makeHttpRequest(serverHandlerURL, "GET", paramsPhoto);
//
//            } catch (Exception e) {
//                LogUtil.printFullError("SCREENSHOT", "UPLOADING EXCEPCION", e);
//            }
//            return json;
//        }
//    }

}

//relativeLayout = (RelativeLayout) wrapperView.findViewById(R.id.nodoSecurity1);
//                relativeLayout.post(new Runnable() {
//                    public void run() {
//
//                        //take screenshot
//                        moveTaskToBack(true);
//                        myBitmap = captureScreen(relativeLayout);
//                        Toast.makeText(getApplicationContext(), "Screenshot captured..!", Toast.LENGTH_LONG).show();
//                        try {
//                            if (myBitmap != null) {
//                                //save image to SD card
//                                saveImage(myBitmap);
//                            }
//                            Toast.makeText(getApplicationContext(), "Screenshot saved..!", Toast.LENGTH_LONG).show();
//                        } catch (IOException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                    }
//                });