package com.qoopa.nodosshield;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class ShutDown extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shut_down);

        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Runtime.getRuntime().exec("su");
                    Runtime.getRuntime().exec("reboot");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }
}