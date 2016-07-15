package com.qoopa.nodosshield;
// Listo Try - Log

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Registro extends Activity {

    private RelativeLayout pageOne;
    private RelativeLayout pageTwo;
    private LinearLayout pageThreeRegister;
    private final String registroURL = "https://www.nodos.com.co/php/user/create.php";
    private String nombreEnvio;
    private String apellidoEnvio;
    private String correoEnvio;
    private String passwordEnvio;
    private ImageView indicador1;
    private ImageView indicador2;
    private String result;
    private Integer idLogin;
    private ViewPager viewPager;
    private boolean registerOrLoginDone;
    private PackageManager packageManager;
    JSONParser jsonParser = new JSONParser();

    //********************** Actualizacion Push ************************
    private static final String TAG = "Principal";
    private NetworkImageView mNetworkImageView;
    private ImageLoader mImageLoader;
    //********************** Actualizacion Push ************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new CheckAutoLogin().execute();

        //********************** Actualizacion Push ************************
        Bundle extras = getIntent().getExtras();
        if (extras != null) { //
            String response = extras.getString("payload");
            response = response.replace("[", "");
            response = response.replace("]", "");
            response = response.replace("\\", "");
            String[] valuesProfile = response.split(",");
            LogUtil.printDebug(TAG, "url: " + valuesProfile[1]);
            if (response != null) {
                if (valuesProfile[0].equals("1")) {
                    LogUtil.printDebug(TAG, "Tipo 1: Abrir app");
                    new PostAdapter(this, FirebaseInstanceId.getInstance().getToken(), null, null, "1", valuesProfile[2]);
                    new PostAdapter(this, FirebaseInstanceId.getInstance().getToken(), null, null, "2", valuesProfile[2]);
                } else {
                    if (valuesProfile[0].equals("2")) {
                        new PostAdapter(this, FirebaseInstanceId.getInstance().getToken(), null, null, "1", valuesProfile[2]);
                        new PostAdapter(this, FirebaseInstanceId.getInstance().getToken(), null, null, "2", valuesProfile[2]);
                        valuesProfile[1]=valuesProfile[1].substring(1,valuesProfile[1].length()-1); Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(valuesProfile[1]));
                        this.startActivity(viewIntent);
                    } else {
                        if (valuesProfile[0].equals("3")) {
                            new PostAdapter(this,FirebaseInstanceId.getInstance().getToken(),null,null,"1",valuesProfile[2]);
                            new PostAdapter(this,FirebaseInstanceId.getInstance().getToken(),null,null,"2",valuesProfile[2]);
                            valuesProfile[1]=valuesProfile[1].substring(1,valuesProfile[1].length()- 1);
                            mImageLoader = ImageVolley.getInstance(this.getApplicationContext()).getImageLoader(); //Image URL - This can point to any image file supported by Android
                            Intent intent = new Intent(this, ImagePush.class);
                            intent.putExtra("url", valuesProfile[1]);
                            this.startActivity(intent);
                            this.finish();
                        } else {

                        }
                    }
                }
            } else {
                String not_abrir = extras.getString("notificacion");
                new PostAdapter(this, FirebaseInstanceId.getInstance().getToken(), null, null, "2", not_abrir);
            }
        }
        //********************** Actualizacion Push ************************
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (registerOrLoginDone) {
                LogUtil.printDebug("Registro", "Ocultar_Registro");
                packageManager.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 1);
            }
        } catch (Exception e) {
            LogUtil.printError("Error", "Registro - onDestroy");
            LogUtil.printFullError("Error","Registro - onDestroy",e);
        }
    }

    private class CheckAutoLogin extends AsyncTask<Integer, Integer, JSONObject> {
        int idExist = -1;

        @Override
        protected JSONObject doInBackground(Integer... params) {

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            String info = "https://www.nodos.com.co/universal_handler.php";
            List<NameValuePair> params2 = new ArrayList<NameValuePair>();

            try { //Try01
                params2.add(new BasicNameValuePair("option", "get_id"));
                params2.add(new BasicNameValuePair("address", LockService.getWifiMacAddress()));
                params2.add(new BasicNameValuePair("imei", imei));
            } catch (Exception e) {
                LogUtil.printError("Error", "Registro - CheckAutoLogin - doInBackround - Try01");
                LogUtil.printFullError("Error","Registro - CheckAutoLogin - doInBackround - Try01",e);
            }

            LogUtil.printDebug("IMEI", ""+imei);

            try { //Try02
                if (isThereActiveInternetConnection()) {
                    LogUtil.LogEnvioDatos(info, params2.toString());//SeguimientoDatos
                    JSONObject json = jsonParser.makeHttpRequest(info, "GET", params2);
                    LogUtil.LogEnvioDatos("Respuesta", json.toString());//SeguimientoDatos
                    LogUtil.printDebug("JSON", json.getString("result"));
                    if ("OK".equals(json.getString("result"))) {
                        idExist = 0;
                        LogUtil.printDebug("Device Id", "Device registered");
                    } else {
                        LogUtil.printDebug("Device Id", "Device not registered");
                        idExist = -1;
                    }
                } else {
                    LogUtil.printDebug("Registro", "No hay coneccion a Internet");
                }
            } catch (Exception e) {
                LogUtil.printError("Error", "Registro - CheckAutoLogin - doInBackround - Try02");
                LogUtil.printFullError("Error","Registro - CheckAutoLogin - doInBackround - Try02",e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {

            LogUtil.printDebug("IDEXIST", "" + idExist + " (0 = Si esta Registrado)");
            if (idExist != -1) {
                LogUtil.printDebug("LOGIN AUTO", "OK");
                packageManager = getPackageManager();
                try { //Try01
                    Intent intent = new Intent(getApplicationContext(), LockService.class);
                    getApplicationContext().startService(intent);
                    lanzarDialogContinuar();
                    registerOrLoginDone = true;
                } catch (Exception e) {
                    LogUtil.printError("Error", "Registro - CheckAutoLogin - onPostExecute - Try01");
                    LogUtil.printFullError("Error","Registro - CheckAutoLogin - onPostExecute - Try01",e);
                }
            } else {
                LogUtil.printDebug("LOGIN AUTO", "NO");
                setContentView(R.layout.activity_registro);
                packageManager = getPackageManager();
                registerOrLoginDone = false;
                indicador1 = (ImageView) findViewById(R.id.indicadorAtras);
                indicador2 = (ImageView) findViewById(R.id.indicadorAdelante);
                final RelativeLayout touch = (RelativeLayout) findViewById(R.id.touch);
                final TextView textNext = (TextView) findViewById(R.id.textoSiguiente);
                Typeface tf3 = Typeface.createFromAsset(getAssets(), "fonts/XBall.ttf");
                textNext.setTypeface(tf3);
                viewPager = (ViewPager) findViewById(R.id.pager1);
                viewPager.setAdapter(new MainPagerAdapter());
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        switch (position) {
                            case 0:
                                indicador1.setVisibility(View.INVISIBLE);
                                indicador2.setVisibility(View.VISIBLE);

                                indicador1.setOnClickListener(new View.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(View v) {
                                                                      viewPager.setCurrentItem(0, true);
                                                                  }
                                                              }
                                );

                                indicador2.setOnClickListener(new View.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(View v) {
                                                                      viewPager.setCurrentItem(1, true);
                                                                  }
                                                              }
                                );
                                touch.setClickable(true);
                                touch.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        viewPager.setCurrentItem(1, true);
                                    }
                                });

                                textNext.setVisibility(View.VISIBLE);

                                break;
                            case 1:
                                indicador1.setVisibility(View.VISIBLE);
                                indicador1.setOnClickListener(new View.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(View v) {
                                                                      viewPager.setCurrentItem(0, true);
                                                                  }
                                                              }
                                );
                                indicador2.setVisibility(View.VISIBLE);
                                indicador2.setOnClickListener(new View.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(View v) {
                                                                      viewPager.setCurrentItem(2, true);
                                                                  }
                                                              }
                                );
                                touch.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        viewPager.setCurrentItem(2, true);
                                    }
                                });
                                textNext.setVisibility(View.INVISIBLE);
                                break;
                            default:
                                indicador1.setVisibility(View.GONE);
                                indicador2.setVisibility(View.GONE);
                                textNext.setVisibility(View.INVISIBLE);
                                touch.setClickable(false);

                                break;
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

                //simulated scrolls
                viewPager.setCurrentItem(1, true);
                viewPager.setCurrentItem(0, true);

            }
        }

    }

    private class MainPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            View page = null;
            switch (position) {
                case 0:
                    if (pageOne == null) {
                        pageOne = (RelativeLayout) LayoutInflater.from(Registro.this).inflate(R.layout.page_one, null);
                    }
                    page = pageOne;
                    break;
                case 1:
                    if (pageTwo == null) {
                        pageTwo = (RelativeLayout) LayoutInflater.from(Registro.this).inflate(R.layout.page_two, null);
                    }
                    page = pageTwo;
                    break;
                default:
                    if (pageThreeRegister == null) {
                        pageThreeRegister = (LinearLayout) LayoutInflater.from(Registro.this).inflate(R.layout.page_sign_up, null);
                        initPageThree();

                        ((InputMethodManager) collection.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInputFromInputMethod(collection.getWindowToken(), 0);
                    }
                    page = pageThreeRegister;
                    break;
            }

            ((ViewPager) collection).addView(page, 0);

            return page;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(View collection, int position, Object view) {
            ((ViewPager) collection).removeView((View) view);
        }

        private void initPageThree() {

            Typeface tf1 = Typeface.createFromAsset(getAssets(), "fonts/Champagne & Limousines.ttf");
            Typeface tf3 = Typeface.createFromAsset(getAssets(), "fonts/XBall.ttf");
            Typeface tf4 = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro-Cond.otf");

            TextView headerRegistro = (TextView) pageThreeRegister.findViewById(R.id.textoHeader);
            headerRegistro.setTypeface(tf3);

            TextView acepto = (TextView) pageThreeRegister.findViewById(R.id.aceptotText);//Recordar usar para colocar onClick a Terminos
            acepto.setTypeface(tf4);


            TextView terminosYCondiciones = (TextView) pageThreeRegister.findViewById(R.id.terminos);//Recordar usar para colocar onClick a Terminos
            terminosYCondiciones.setTypeface(tf4);

            final TextView titulo0 = (TextView) pageThreeRegister.findViewById(R.id.textoTituloRegistro0);
            titulo0.setTypeface(tf4);

            final TextView titulo1 = (TextView) pageThreeRegister.findViewById(R.id.textoTituloRegistro1);
            titulo1.setTypeface(tf4);

            final EditText nombre = (EditText) pageThreeRegister.findViewById(R.id.nombre);
            nombre.setTypeface(tf4);

            final EditText correo = (EditText) pageThreeRegister.findViewById(R.id.correo);
            correo.setTypeface(tf4);
            final EditText repeatCorreo = (EditText) pageThreeRegister.findViewById(R.id.repeatCorreo);
            repeatCorreo.setTypeface(tf4);

            final EditText password = (EditText) pageThreeRegister.findViewById(R.id.password);
            password.setTypeface(tf4);

            final EditText repeatPassword = (EditText) pageThreeRegister.findViewById(R.id.repeatPassword);
            repeatPassword.setTypeface(tf4);

            final CheckBox aceptarCheck = (CheckBox) pageThreeRegister.findViewById(R.id.cbAcepto);
            aceptarCheck.setTypeface(tf1);

            final ImageView regresar = (ImageView) pageThreeRegister.findViewById(R.id.botonRegresarRegistro);
            regresar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem(1, true);
                }
            });

            final ImageView enviar = (ImageView) pageThreeRegister.findViewById(R.id.enviar);
            enviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LogUtil.printDebug("REGISTRO", "ENVIANDO DATOS DE REGISTRO");

                    try {
                        String delimit = " ";
                        String toSplit = nombre.getText().toString();
                        String tokens[] = toSplit.split(delimit);
                        LogUtil.printDebug("NUM TOKENS", ": " + tokens.length);

                        if (tokens.length < 2) {
                            Toast.makeText(Registro.this, getString(R.string.error_name), Toast.LENGTH_SHORT).show();
                            LogUtil.printDebug("REGISTRO", "MENOS DE UN NOMBRE Y UN APELLIDO");
                            return;
                        }

                        if (correo.getText().length() == 0 || nombre.getText().length() == 0 || password.getText().length() == 0 || repeatPassword.getText().length() == 0) {
                            Toast.makeText(Registro.this, getString(R.string.error_empty_field), Toast.LENGTH_SHORT).show();
                            LogUtil.printDebug("REGISTRO", "CAMPOS VACIOS");
                            return;
                        }

                        if (!correo.getText().toString().equals(repeatCorreo.getText().toString())) {
                            Toast.makeText(Registro.this, getString(R.string.error_mail_match), Toast.LENGTH_SHORT).show();
                            LogUtil.printDebug("REGISTRO", "CORREOS NO COINCIDEN");
                            return;
                        }

                        if (!password.getText().toString().equals(repeatPassword.getText().toString())) {
                            Toast.makeText(Registro.this, getString(R.string.error_password_match), Toast.LENGTH_SHORT).show();
                            LogUtil.printDebug("REGISTRO", "CONTRASEÑAS NO COINCIDEN");
                            password.setText("");
                            repeatPassword.setText("");
                            return;
                        }

                        if (!isEmailValid(correo.getText().toString())) {
                            Toast.makeText(Registro.this, getString(R.string.error_mail_format), Toast.LENGTH_SHORT).show();
                            LogUtil.printDebug("REGISTRO", "CORREO INVALIDO");
                            correo.setText("");
                            return;
                        }
                        if (!aceptarCheck.isChecked()) {
                            Toast.makeText(Registro.this, getString(R.string.error_terms_and_conditions), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            LogUtil.printDebug("REGISTRO", "DATOS VALIDOS");
                            nombreEnvio = tokens[0];
                            apellidoEnvio = tokens[1];
                        }

                        correoEnvio = correo.getText().toString();
                        passwordEnvio = password.getText().toString();

                    } catch (Exception e) {
                        LogUtil.printError("REGISTRO", "EXCEPCION ANTES DEL REGISTRO");
                        LogUtil.printError("Error", "Registro - MainPagerAdapter - initPageThree");
                        LogUtil.printFullError("Error", "Registro - MainPagerAdapter - initPageThree", e);
                    }

                    new SendNewUser().execute();
                }
            });

            titulo1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LogUtil.printDebug("BOTON", "INGRESAR");
                    try {
                        finish();
                        Intent login = new Intent(Registro.this, LoginActivity.class);
                        startActivity(login);
                    } catch (Exception e) {
                        LogUtil.printFullError("BOTON", "INGRESAR EXCEPCION", e);
                    }

                }
            });

        }
    }

    private class SendNewUser extends AsyncTask<String, String, String> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try { //Try01
                dialog = new ProgressDialog(Registro.this);
                dialog.setMessage("Registrando usuario");
                dialog.show();
            } catch (Exception e) {
                LogUtil.printError("Error", "Registro - SendNewUser - onPreExecute - Try01");
                LogUtil.printFullError("Error", "Registro - SendNewUser - onPreExecute - Try01", e);
            }
        }

        @Override
        protected String doInBackground(String... params) {

            JSONParser parser = new JSONParser();
            result = null;

            try { //Try01
                List<NameValuePair> attrib = new ArrayList<>();
                //name //lastname//email//password//phone1//phone2//FAIL//OK -> ENVIA EL id
                attrib.add(new BasicNameValuePair("name", nombreEnvio));
                attrib.add(new BasicNameValuePair("lastname", apellidoEnvio));
                attrib.add(new BasicNameValuePair("email", correoEnvio));
                attrib.add(new BasicNameValuePair("password", passwordEnvio));
                LogUtil.printDebug("correo", correoEnvio);
                LogUtil.printDebug("pass", passwordEnvio);
                LogUtil.printDebug("nombre", nombreEnvio);
                LogUtil.printDebug("apellido", nombreEnvio);

                LogUtil.LogEnvioDatos(registroURL, attrib.toString());//SeguimientoDatos
                JSONObject object = parser.makeHttpRequest(registroURL, "GET", attrib);
                LogUtil.LogEnvioDatos("Respuesta", object.toString());//SeguimientoDatos

                LogUtil.printDebug("REGISTRO ", "Respuesta a Peticion de Registro: " + object);
                result = object.getString("result");

                if (result.equals("OK")) {
                    registerOrLoginDone = true;
                    idLogin = object.getInt("id");
                    LogUtil.printDebug("REGISTRO", "OK, ID: " + idLogin);

                    try { //Try 01-1
                        LockService.setLoginIdInFile(Registro.this, idLogin.toString());
                    } catch (Exception e) {
                        LogUtil.printError("Error", "Registro - SendNewUser - doInBackground - Try01-1");
                        LogUtil.printFullError("Error", "Registro - SendNewUser - doInBackground - Try01-1", e);
                    }
                } else {
                    LogUtil.printError("REGISTRO", "FALLIDO");
                }
            } catch (Exception ex) {
                LogUtil.printError("Error", "Registro - SendNewUser - doInBackground - Try01");
                LogUtil.printFullError("Error","Registro - SendNewUser - doInBackground - Try01",ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            if (registerOrLoginDone) {
                Toast.makeText(Registro.this, "Usuario ingresado satisfactoriamente", Toast.LENGTH_SHORT).show();
                try { //Try01
                    Intent intent = new Intent(getApplicationContext(), LockService.class);
                    getApplicationContext().startService(intent);
                    LogUtil.printError("REGISTRO", "INICIANDO EL SERVICIO LUEGO DE REGISTRO");
                    lanzarDialogContinuar();
                } catch (Exception e) {
                    LogUtil.printError("Error", "Registro - SendNewUser - onPostExecute - Try01");
                    LogUtil.printFullError("Error","Registro - SendNewUser - onPostExecute - Try01",e);
                }
            } else {
                try { //Try02
                    LogUtil.printError("RESULT FAILED", "" + result);
                    if (result == null) {
                        Toast toastVerificando = Toast.makeText(getApplicationContext(), "Por favor revisa tu conexión a internet", Toast.LENGTH_SHORT);
                        toastVerificando.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 4, 4);
                        toastVerificando.show();
                    } else {
                        Toast toastVerificando = Toast.makeText(getApplicationContext(), "Esta cuenta ya existe, por favor inicia sesión", Toast.LENGTH_SHORT);
                        toastVerificando.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 4, 4);
                        toastVerificando.show();
                    }
                } catch (Exception e) {
                    LogUtil.printError("Error", "Registro - SendNewUser - onPostExecute - Try02");
                    LogUtil.printFullError("Error","Registro - SendNewUser - onPostExecute - Try02",e);
                }
            }
        }
    }

    private void lanzarDialogContinuar() {

        final Dialog info = new Dialog(Registro.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        info.requestWindowFeature(Window.FEATURE_NO_TITLE);
        info.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        info.setContentView(R.layout.content_terminos);
        info.setCancelable(false);

        ImageView continuar = (ImageView) info.findViewById(R.id.continuar);
        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.printDebug("BOTON", "CONTINUAR");
                try { //Try01
                    finish();
                    info.dismiss();
                } catch (Exception e) {
                    LogUtil.printError("Error", "Registro - lanzarDialogContinuar - onClick - Try01");
                    LogUtil.printFullError("Error","Registro - lanzarDialogContinuar - onClick - Try01",e);
                }
            }
        });
        info.show();
    }

    private boolean turnGPSOn() {
        try {
            LogUtil.printDebug("GPS", "REVISANDO GPS");

            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                LogUtil.printError("GPS", "NO TIENE ENCENDIDO EL GPS O LA RED");

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Location Services Not Active");
                builder.setMessage("Para que esta aplicacion funcione correctamente debe habilitar los servicios de localizacion GPS");//Pendiente Mensaje a Mostrar.
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });

                Dialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                Toast.makeText(Registro.this, "Una vez encendido, pulsa la opcion ingresar, YA estás registrado!", Toast.LENGTH_LONG).show();
                return false;
            } else {
                LogUtil.printError("GPS", "ENCENDIDO");
                return true;
            }
        } catch (Exception e) {
            LogUtil.printError("Error", "Registro - turnGPSOn");
            LogUtil.printFullError("Error", "Registro - turnGPSOn", e);
            return false;
        }
    }

    public final static boolean isEmailValid(CharSequence target) {
        try {
            if (TextUtils.isEmpty(target)) {
                return false;
            } else {
                return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
            }
        } catch (Exception e) {
            LogUtil.printError("Error", "Registro - isEmailValid");
            LogUtil.printFullError("Error", "Registro - isEmailValid", e);
            return false;
        }
    }

    public boolean isThereActiveInternetConnection() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                LogUtil.printDebug("Internet Connection", netInfo.toString());
                return true;
            } else {
                LogUtil.printDebug("Internet Connection", "Offline");
                return false;
            }
        } catch (Exception e) {
            LogUtil.printError("Error", "Registro - isThereActiveInternetConnection");
            LogUtil.printFullError("Error", "Registro - isThereActiveInternetConnection", e);
            return false;
        }
    }
}
