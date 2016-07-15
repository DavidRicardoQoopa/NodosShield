package com.qoopa.nodosshield;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageActivity extends AppCompatActivity {

    Typeface tf1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent i = getIntent();
        String message;
        try {
            message = i.getExtras().getString("Message");
        } catch (Exception e) {
            message = "Jueputa! Funcione!!!";
        }

        Log.e("Message_activity",message);
        final TextView et1 = (TextView)findViewById(R.id.mensajes);
        tf1 = Typeface.createFromAsset(getAssets(), "fonts/OSWALD-BOLDITALIC.TTF");
        et1.setText(message);
        et1.setTypeface(tf1);
        ImageView imageView = (ImageView)findViewById(R.id.verificar);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
