package com.qoopa.nodosshield;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

public class Transparent extends AppCompatActivity {

    private static final String TAG = "MyFirebaseIIDService";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);

        Log.d(TAG, "From: ");
        Bundle extras = getIntent().getExtras();
        String url = extras.getString("url");
        String not_abrir = extras.getString("notificacion");
        new PostAdapter(this, FirebaseInstanceId.getInstance().getToken(),null,null,"2",not_abrir);
        Log.d(TAG, "From: " + url);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
        finish();
    }
}
