package com.qoopa.nodosshield;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {

    private EditText et;
    private EditText et1;
    private TextView tituloLogin;
    private ImageView registrar;
    public JSONParser jsonParser;
    public String usuario;
    public String pass;
    private String result;
    private Typeface tf1;
    private Typeface tf2;
    private PackageManager packageManager;
    private Integer idLogin;
    private boolean registerOrLoginDone;
    String serverPath = "https://www.nodos.com.co/universal_handler.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        packageManager = getPackageManager();
        registerOrLoginDone = false;

        jsonParser = new JSONParser();
        tf1 = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Cond.otf");
        tf2 = Typeface.createFromAsset(getAssets(), "fonts/XBall.ttf");

        et = (EditText) findViewById(R.id.usuario);
        et.setTypeface(tf1);

        et1 = (EditText) findViewById(R.id.contraseña1);
        et1.setTypeface(tf1);

        tituloLogin = (TextView) findViewById(R.id.textoHeaderLogin);
        tituloLogin.setTypeface(tf2);

        registrar = (ImageView) findViewById(R.id.botonRegresar);
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LogUtil.printDebug("BOTON", "REGISTRAR");

                try {
                    Intent registro = new Intent(LoginActivity.this, Registro.class);
                    startActivity(registro);
                    finish();
                } catch (Exception e) {
                    LogUtil.printFullError("BOTON", "REGISTRAR DE LOGIN", e);
                }
            }
        });

        final ImageView button = (ImageView) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LogUtil.printDebug("BOTON", "INGRESAR");
                new SendInfo().execute();
            }
        });

        final TextView recuperar = (TextView) findViewById(R.id.recuperar);
        recuperar.setTypeface(tf1);
        recuperar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.printDebug("BOTON", "RECUPERAR CREDENCIALES");
                new RecuperarPass().execute();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registerOrLoginDone) {
            ComponentName componentName = new ComponentName(LoginActivity.this, Registro.class);
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 1);
        }
    }

    class SendInfo extends AsyncTask<String, String, String> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setMessage("Iniciando Sesión");
            dialog.show();

            usuario = et.getText().toString();
            pass = et1.getText().toString();

            if (usuario.equals("") || pass.equals("")) {
                LogUtil.printDebug("LOGIN", "CAMPOS VACIOS");
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.error_empty_field), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 4, 4);
                toast.show();
            } else {
                LogUtil.printDebug("LOGIN", "OK: CAMPOS LLENOS ");
            }
        }

        protected String doInBackground(String... args) {
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("option", "login"));
                params.add(new BasicNameValuePair("username", usuario));
                params.add(new BasicNameValuePair("password", pass));
                result = "";
                LogUtil.LogEnvioDatos(serverPath, params.toString());//SeguimientoDatos
                JSONObject json = jsonParser.makeHttpRequest(serverPath, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
                LogUtil.printError("LOGIN ", "RESPUESTA PETICION " + json);
                if (json != null) {
                    result = json.getString("result");
                    Log.e("LOGIN RESULT", json.getString("result"));
                }

                LogUtil.printError("LOGIN ", "RESULT" + result);
                if (result == null) {
                    result = "CONNECTIONERROR";
                    registerOrLoginDone = false;
                } else if (!result.equals("FAIL")) {
                    idLogin = json.getInt("id");
                    LockService.setLoginIdInFile(LoginActivity.this, idLogin.toString());
                    registerOrLoginDone = true;
                } else {
                    result = "ERROR";
                    registerOrLoginDone = false;
                }

            } catch (Exception e) {
                LogUtil.printFullErrorInit("ERROR", "REQUEST LOGIN", e);
                result = "ERRORTIME";
                registerOrLoginDone = false;
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            dialog.dismiss();
            LocationManager lm = null;
            try {
                lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            } catch (Exception e) {
                LogUtil.printFullError("LOGIN", "POST EXECUTE EXCEPCION (getSystemService(LOCATION_SERVICE))", e);
            }

            if (result.equals("OK")) {

                try {
                    Intent intent = new Intent(getApplicationContext(), LockService.class);
                    getApplicationContext().startService(intent);
                    LogUtil.printDebug("LOGIN", "INICIANDO EL SERVICIO LUEGO DE LOGIN");
                    lanzarDialog();
                } catch (Exception e) {
                    LogUtil.printFullError("LOGIN", "POST EXECUTE EXCEPCION", e);
                }

            //} else if (result.equals("ERROR") && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            } else if (result.equals("ERROR")) {
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.error_email_password), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 4, 4);
                toast.show();
            } else if (result.equals("ERRORTIME")) {
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.error_internet_connection), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 4, 4);
                toast.show();

            //} else if (result.equals("CONNECTIONERROR") && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            } else if (result.equals("CONNECTIONERROR")) {
                Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.error_internet_connection), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 4, 4);
                toast.show();
            }
        }
    }

    private boolean turnGPSOn() {

        try {

            Log.e("GPS", "?");

            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//                Log.d("GPS", "APAGADO");
//                // Build the alert dialog
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setTitle("Location Services Not Active");
//                builder.setMessage("Para que esta aplicacion funcione correctamente debe habilitar los servicios de localizacion GPS");
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivity(intent);
//                    }
//                });
//
//                Dialog alertDialog = builder.create();
//                alertDialog.setCanceledOnTouchOutside(false);
//                alertDialog.show();

                return false;

            } else {
                Log.e("GPS", "ENCENDIDO");
                return true;
            }
        } catch (Exception e) {
            LogUtil.printFullError("GPS", "EXCEPCION (APAGADO)", e);
            return false;
        }
    }

    private void lanzarDialog() {
        final Dialog info = new Dialog(LoginActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        info.requestWindowFeature(Window.FEATURE_NO_TITLE);
        info.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        info.setContentView(R.layout.content_terminos);
        info.setCancelable(false);

        ImageView continuar = (ImageView) info.findViewById(R.id.continuar);
        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.printDebug("BOTON", "CONTINUAR");
                try {
                    finish();
                    info.dismiss();
                } catch (Exception e) {
                    LogUtil.printFullError("BOTON", "CONTINUAR", e);
                }
            }
        });

        info.show();
    }

    class RecuperarPass extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            usuario = et.getText().toString();
            Log.e("USUARIO", usuario);
        }

        protected String doInBackground(String... args) {

            try {
                String info = "http://www.nodos.com.co/php/sendPassword.php";
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("email", usuario));

                LogUtil.LogEnvioDatos(serverPath, params.toString());//SeguimientoDatos
                JSONObject json = jsonParser.makeHttpRequest(info, "GET", params);
                LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
                result = json.getString("result");
                Log.e("Result", result);

            } catch (Exception e) {
                result = "ERROR";
            }
            return null;
        }

        protected void onPostExecute(String file_url) {

            if (result.equals("OK")) {

                Toast toast = Toast.makeText(getApplicationContext(), "Se enviara un correo con tu contraseña", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 4, 4);
                toast.show();
            }
        }
    }
}
