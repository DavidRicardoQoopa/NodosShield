package com.qoopa.nodosshield;

/**
 * Created by claudia.canon on 07/06/2016.
 *
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MessageService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData().get("tipo"));

        String titulomsj = remoteMessage.getNotification().getTitle();
        String textomsj = remoteMessage.getNotification().getBody();
        String tipomsj = remoteMessage.getData().get("tipo");
        String urlmsj = remoteMessage.getData().get("url");
        String idmsj = remoteMessage.getData().get("idmsj");

        Log.d(TAG, "id_message FCM: " + idmsj);
        //Calling method to generate notification
        sendNotification(titulomsj,textomsj,tipomsj,urlmsj,idmsj);
        new PostAdapter(this, FirebaseInstanceId.getInstance().getToken(),null,null,"1",idmsj);
    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody, String messageTitle , String tipo, String url,String idmsj) {
        Log.d(TAG, "Tipo: " +tipo);
        if(tipo.equals("1")){

            Intent intent = new Intent(this, Registro.class); // Cambiar segun la clase principal de la aplicacion
            intent.putExtra("notificacion", idmsj);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
        }else{
            if(tipo.equals("2")){
                Intent intent = new Intent(this, Transparent.class); // Cambiar segun la clase principal de la aplicacion
                intent.putExtra("url", url);
                intent.putExtra("notificacion", idmsj);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(0, notificationBuilder.build());
            }else{
                if(tipo.equals("3")){
                    Intent intent = new Intent(this, ImagePush.class);
                    intent.putExtra("url", url);
                    intent.putExtra("notificacion", idmsj);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                            PendingIntent.FLAG_ONE_SHOT);

                    Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(messageTitle)
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    notificationManager.notify(0, notificationBuilder.build());
                }else{
                    Log.d(TAG, "Tipo: " +tipo);
                }
            }
        }

    }
}