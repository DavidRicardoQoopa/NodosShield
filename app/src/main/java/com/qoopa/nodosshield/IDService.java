package com.qoopa.nodosshield;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.regex.Pattern;

/**
 * Created by claudia.canon on 07/06/2016.
 */
public class IDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    // Atributos
    ArrayAdapter adapter;
    String possibleEmail;
    @Override
    public void onTokenRefresh() {

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Context context = null;
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;

            }
        }
        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        String key_app = FirebaseApp.getInstance().getName();
        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + key_app);
        sendRegistrationToServer(refreshedToken,key_app,possibleEmail);

    }

    private void sendRegistrationToServer(String token,String key_app,String email) {
        new PostAdapter(this,token,key_app,email,null,null);
        //You can implement this method to store the token on your server
        //Not required for current project
    }
}
