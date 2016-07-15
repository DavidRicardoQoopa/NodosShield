package com.qoopa.nodosshield;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AlarmService extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    CaptureImage capture;
    Typeface tf1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_service);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        Intent i = getIntent();
        final String nombre_foto = i.getExtras().getString("Nombre Imagen");

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(true);
        mediaPlayer = MediaPlayer.create(AlarmService.this, R.raw.alarm);
        mediaPlayer.start();

        tf1 = Typeface.createFromAsset(getAssets(), "fonts/Champagne & Limousines.ttf");

        TextView tx = (TextView) findViewById(R.id.Textview);
        TextView tx_ = (TextView) findViewById(R.id.textview_1);
        String timeStamp = new SimpleDateFormat("hh:mm a").format(new Date());

        Log.e("Hora", timeStamp);
        Log.e("Calendario", currentDate());
        tx.setTypeface(tf1);
        tx_.setTypeface(tf1);
        tx.setText(timeStamp);
        tx_.setText(currentDate());

        new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            }
        }.start();

        AudioManager am = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        final Vibrator vibrator = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);
        vibrator.vibrate(mediaPlayer.getDuration());
        Log.e("Alarm", "alarm");

        final ImageView desactivar = (ImageView) findViewById(R.id.desactivar_alarma);
        desactivar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    vibrator.cancel();
                    mediaPlayer.stop();

                    capture = new CaptureImage();
                    capture.setNombre_imagen(nombre_foto);
                    capture.cam();
                    capture.uploadFile("/mnt/sdcard/MyCameraApp");

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("SCREENSHOT", "EXCEPTION");
                }
                finish();
            }
        });
    }

    public static String currentDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
