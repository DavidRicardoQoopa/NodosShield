package com.qoopa.nodosshield;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.iid.FirebaseInstanceId;

public class ImagePush extends Activity {
    private NetworkImageView mNetworkImageView;
    private ImageLoader mImageLoader;
    private static final String TAG = "LoadImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_push);

        mNetworkImageView = (NetworkImageView) findViewById(R.id.networkImageView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Instantiate the RequestQueue.
        Bundle extras = getIntent().getExtras();
        String url = extras.getString("url");
        String not_abrir = extras.getString("notificacion");
        new PostAdapter(this, FirebaseInstanceId.getInstance().getToken(),null,null,"2",not_abrir);
        Log.d(TAG, "From: " + url);
        mImageLoader = ImageVolley.getInstance(this.getApplicationContext()).getImageLoader();
        //Image URL - This can point to any image file supported by Android

        mImageLoader.get(url, ImageLoader.getImageListener(mNetworkImageView, R.mipmap.ic_launcher, android.R.drawable.ic_dialog_alert));
        mNetworkImageView.setImageUrl(url, mImageLoader);
    }
}
