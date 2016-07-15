package com.qoopa.nodosshield;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

public class PhotoActivity extends Activity {

//    LinearLayout PhotoWeb;
    CaptureImage capture2;
//    private WindowManager winManager;
//    RelativeLayout wrapperView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        //PhotoWeb = (LinearLayout) findViewById(R.id.Photo);

//        final Calendar c = Calendar.getInstance();
//        tempFile = (c.get(Calendar.YEAR) + "" + "-" + (c.get(Calendar.MONTH) + 1) + "" + "-" + c.get(Calendar.DAY_OF_MONTH) + "" + "_" + c.get(Calendar.HOUR_OF_DAY) + "" + "-" + c.get(Calendar.MINUTE) + "" + "-" + c.get(Calendar.SECOND)) + "(front)" + ".jpg";

//        Calendar c = Calendar.getInstance();
//        String nombre_foto = (c.get(Calendar.YEAR) + "" + "-" + (c.get(Calendar.MONTH) + 1) + "" + "-" + c.get(Calendar.DAY_OF_MONTH) + "" + "_" + c.get(Calendar.HOUR_OF_DAY) + "" + "-" + c.get(Calendar.MINUTE) + "" + "-" + c.get(Calendar.SECOND)) + ".jpg";

        String nombre_foto = "Captura_Pantalla";

        try {
            capture2 = new CaptureImage();
            capture2.setNombre_imagen(nombre_foto);
            LogUtil.printError("NOMBRE FOTO", "" + nombre_foto);
            capture2.cam();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SCREENSHOT", "EXCEPTION");
        }
    }
}
