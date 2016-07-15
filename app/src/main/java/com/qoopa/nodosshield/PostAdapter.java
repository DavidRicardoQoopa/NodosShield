package com.qoopa.nodosshield;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by claudia.canon on 13/06/2016.
 */
public class PostAdapter extends ArrayAdapter {

    // Atributos
    private RequestQueue requestQueue;


    private static final String URL_BASE = "http://162.248.52.99/push_ventana/New/php";
    private static final String URL_JSON = "/reg_disp.php";
    private static final String URL_JSON_STATE = "/state_message.php";
    private static final String TAG = "PostAdapter";

    public PostAdapter(Context context, final String token, final String key_app, final String email, final String estado,final String message_id) {
        super(context,0);

        // Crear nueva cola de peticiones
        requestQueue= Volley.newRequestQueue(context);

        // Nueva peticiÃ³n JSONObject
        if(estado==null){
            StringRequest jsArrayRequest = new StringRequest(Request.Method.POST, URL_BASE + URL_JSON, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject objeto= jsonArray.getJSONObject(0);
                        Log.e(TAG, "Response: "+ objeto);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error de parsing: "+ e.getMessage());
                    }
                    notifyDataSetChanged();
                }


            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "Error Respuesta en JSON: " + error.getMessage());

                        }
                    }
            ){
                @Override
                protected Map<String, String> getParams() {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("key_app", "AIzaSyCD5SzA_hI3dGcUa5GbjMSArnK5s7K4HEg"); //"AIzaSyBRsZmbiw3EnxJJZjERXmQ-KYQkOmgT8jE");
                    params.put("key_disp", token);
                    params.put("email", "correo");
                    return params;
                }
            };

            // AÃ±adir peticiÃ³n a la cola
            requestQueue.add(jsArrayRequest);
        }else{
            StringRequest jsArrayRequest = new StringRequest(Request.Method.POST, URL_BASE + URL_JSON_STATE, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject objeto= jsonArray.getJSONObject(0);
                        Log.e(TAG, "Response: "+ objeto);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error de parsing: "+ e.getMessage());
                    }
                    notifyDataSetChanged();
                }


            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "Error Respuesta en JSON: " + error.getMessage());

                        }
                    }
            ){
                @Override
                protected Map<String, String> getParams() {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("estado", estado);
                    params.put("id_message", message_id);
                    params.put("key_disp", token);
                    return params;
                }
            };
            // AÃ±adir peticiÃ³n a la cola
            requestQueue.add(jsArrayRequest);
        }



    }



}

