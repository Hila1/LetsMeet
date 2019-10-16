package shulamit.hila.letsmeet.servieces;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import shulamit.hila.letsmeet.activities.LoginActivity;

public class MyIntentServiceDecline extends IntentService {


    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAkFCmCqM:APA91bEvR1nquPLEnb5CgQkyb-Oz3qeJ2_RZlQO4XE9Goa-bUnkAGlK83ZZLfOAx3enAI_6PXYltXeIqWdXrGAcDTxD507MnLJzM2knagH-Gyqr1jmwOn4oNn3oDSjY6DYjq9zC53LE_";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;
    private String userAccepted;
    private String senderToken;
    private String SHARED_PREFS = "user_details";

    public MyIntentServiceDecline() {
        super("MyIntentServiceDecline");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int notificationId= intent.getIntExtra("notificationId",0);
        userAccepted = intent.getStringExtra("isUserAccepted");
        senderToken = intent.getStringExtra("senderToken");
        notificationManager.cancel(notificationId);

        if(!userAccepted.equals("Accept")){
            sendDeclineNotification();
        }
        notificationManager.cancel(notificationId);
    }

    private void sendDeclineNotification() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String name   = sharedPreferences.getString(LoginActivity.USER_NAME, "");

        TOPIC = senderToken;
        NOTIFICATION_TITLE = name + " denied your location request";
        NOTIFICATION_MESSAGE  = "";

        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("title", NOTIFICATION_TITLE);
            notificationBody.put("message", NOTIFICATION_MESSAGE);
            notificationBody.put("channelId","3" );

            notification.put("to", TOPIC);
            notification.put("data", notificationBody);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage());
        }
        sendNotification(notification);
    }
    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);

    }
}
