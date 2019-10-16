package shulamit.hila.letsmeet.servieces;


import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "mFirebaseIIDService";
    private FirebaseFirestore db;
    private String USERS ="users";

    @Override
    public void onTokenRefresh() {

        /*
          This method is invoked whenever the token refreshes
          OPTIONAL: If you want to send messages to this application instance
          or manage this apps subscriptions on the server side,
          you can send this token to your server.
        */
        String token = FirebaseInstanceId.getInstance().getToken();

        // Once the token is generated, subscribe to topic with the userId
        FirebaseMessaging.getInstance().subscribeToTopic(token);
           Log.i(TAG, "onTokenRefresh completed with token: " + token);
    }
}