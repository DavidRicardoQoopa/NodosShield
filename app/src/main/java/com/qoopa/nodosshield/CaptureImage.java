package com.qoopa.nodosshield;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by diego.saavedra on 15/02/2016.
 */
public class CaptureImage {

    public Camera mCamera;
    public int serverResponseCode;
    public ProgressDialog dialog;
    public static String uploadFilePath = "/mnt/sdcard/MyCameraApp";
    public String uploadFileName;
    public JSONParser jsonParser;
    public Bitmap thePicture;
    static String photoName;
    private String encodeImage;
    private final String serverHandlerURL = "https://www.nodos.com.co/universal_handler.php";

    public CaptureImage() {
        jsonParser = new JSONParser();
        uploadFileName = new String();
        thePicture = null;
        dialog = null;
        serverResponseCode = 0;
    }

    public void cam() {
        Log.e("CAM", "INICIO");

        mCamera = getCameraInstance();
        Log.e("CAM", "INSTANCE TAKEN");

        mCamera.startPreview();
        Log.e("CAM", "PREVIEW TAKEN");

        final Camera.PictureCallback mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.e("PICTURE", "TAKEN");

                thePicture = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix m = new Matrix();
                m.postRotate(-90);
                thePicture = Bitmap.createBitmap(thePicture, 0, 0, thePicture.getWidth(), thePicture.getHeight(), m, true);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                thePicture = Bitmap.createScaledBitmap(thePicture, 300, 400, false);
                thePicture.compress(Bitmap.CompressFormat.JPEG, 20, bos);
                byte[] b = bos.toByteArray();
                encodeImage = Base64.encodeToString(b, Base64.DEFAULT);

                try {

                    new Thread(new Runnable() {
                        public void run() {
                            uploadFile(uploadFilePath + "" + uploadFileName);
                        }
                    }).start();

                } catch (Exception ex) {
                    Log.e("CAMERA", ex.getMessage(), ex);
                } finally {
                    Log.e("CAM", "before camera release");
                    camera.stopPreview();
                    camera.setPreviewCallback(null);
                    camera.release();
                    camera = null;
                    Log.e("CAM", "after camera release");
                }
            }
        };

        try {
            SurfaceTexture st = new SurfaceTexture(-1);
            mCamera.setPreviewTexture(st);
            mCamera.startPreview();

            Log.e("CAM", "BEFORE TAKE PICTURE");
            //mCamera.takePicture(null, null, mPicture);
            //Looper.prepare();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("CAM", "Antesitos..........");
                    mCamera.takePicture(null, null, mPicture);
                }
            }, 1000);
            Log.e("CAM", "IN TAKE PICTURE");

        } catch (Exception e) {
            LogUtil.printFullErrorInit("TAKE PICTURE", "EXCEPTION", e);
        }
    }

    public void camWeb() {
        Log.e("CAM", "INICIO");

        mCamera = getCameraInstance();
        Log.e("CAM", "INSTANCE TAKEN");

        mCamera.startPreview();
        Log.e("CAM", "PREVIEW TAKEN");

        final Camera.PictureCallback mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.e("PICTURE", "TAKEN");

                thePicture = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix m = new Matrix();
                m.postRotate(-90);
                thePicture = Bitmap.createBitmap(thePicture, 0, 0, thePicture.getWidth(), thePicture.getHeight(), m, true);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                thePicture = Bitmap.createScaledBitmap(thePicture, 300, 400, false);
                thePicture.compress(Bitmap.CompressFormat.JPEG, 20, bos);
                byte[] b = bos.toByteArray();
                encodeImage = Base64.encodeToString(b, Base64.DEFAULT);

                try {

                    new Thread(new Runnable() {
                        public void run() {
                            uploadFile(uploadFilePath + "" + uploadFileName);
                        }
                    }).start();

                } catch (Exception ex) {
                    Log.e("CAMERA", ex.getMessage(), ex);
                } finally {
                    Log.e("CAM", "before camera release");
                    camera.stopPreview();
                    camera.setPreviewCallback(null);
                    camera.release();
                    camera = null;
                    Log.e("CAM", "after camera release");
                }
            }
        };

        try {
            SurfaceTexture st = new SurfaceTexture(-1);
            mCamera.setPreviewTexture(st);
            mCamera.startPreview();

            Log.e("CAM", "BEFORE TAKE PICTURE");
            //mCamera.takePicture(null, null, mPicture);
            Looper.prepare();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.e("CAM", "Antesitos..........");
                    mCamera.takePicture(null, null, mPicture);
                }
            }, 1000);
            Log.e("CAM", "IN TAKE PICTURE");

        } catch (Exception e) {
            LogUtil.printFullErrorInit("TAKE PICTURE", "EXCEPTION", e);
        }
    }

    private Camera getCameraInstance() {
        Camera camera;
        try {

            releaseCameraAndPreview();

            int cameraType = 0;
            int cameras = Camera.getNumberOfCameras();
            Camera.CameraInfo info = new Camera.CameraInfo();
            for (int i = 0; i < cameras; i++) {
                Camera.getCameraInfo(i, info);
                LogUtil.printError("CAMERA", ": FOR: " + info.facing);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraType = 10;
                }
            }
            if (cameraType == 10) {
                LogUtil.printError("CAMERA", ": TYPE: " + Camera.CameraInfo.CAMERA_FACING_FRONT);
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        } catch (Exception e) {
            LogUtil.printFullErrorInit("CAMERA ERROR ", "EXCEPTION ON PICTURE", e);

            camera = null;
        }

        return camera;
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            Log.e("CAMERANOTNULL", "RELEASED");
            mCamera.release();
            mCamera = null;
        } else {
            Log.e("CAMERANULL", "1");
        }

    }

    public int uploadFile(String sourceFileUri) {
        new SendEncodedImageAsyncTask().execute();
        return serverResponseCode;
    }

    public void setNombre_imagen(String nombre_imagen) {
        photoName = nombre_imagen;
    }

    public class SendEncodedImageAsyncTask extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("SEND ENCODE", "INIT");
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject json1 = null;

            Log.e("SCREEN SHOT ", "UPLOADING ENCODING");
            Log.e("ID DEVICE SCREEN", "" + LockService.getDeviceId());
            Log.e("ID message", "" + LockService.id);
            try {
                ArrayList params = new ArrayList();

                params.add(new BasicNameValuePair("option", "upload_image"));
                params.add(new BasicNameValuePair("terminal", LockService.getDeviceId()));
                params.add(new BasicNameValuePair("id", LockService.id));
                params.add(new BasicNameValuePair("image", encodeImage));
                LogUtil.LogEnvioDatos(serverHandlerURL, params.toString());//SeguimientoDatos
                json1 = jsonParser.makeHttpRequest(serverHandlerURL, "POST", params);
                LogUtil.LogEnvioDatos("Respuesta", json1.toString());//SeguimientoDatos
                LogUtil.printError("RESPONSE ENCODING ", "" + json1);

            } catch (Exception e) {
                LogUtil.printFullErrorInit("SCREENSHOT", "UPLOADING EXCEPCION", e);
            }

            return json1;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            Log.e("IMAGE", "SENT");
        }
    }

}
