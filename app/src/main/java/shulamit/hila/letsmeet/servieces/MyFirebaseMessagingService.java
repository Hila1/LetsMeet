package shulamit.hila.letsmeet.servieces;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import shulamit.hila.letsmeet.R;
import shulamit.hila.letsmeet.activities.MainActivity;
import shulamit.hila.letsmeet.activities.SavesActivity;

import static android.support.constraint.Constraints.TAG;
import static shulamit.hila.letsmeet.activities.HistoryActivity.HISTORY_DB;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String USERS = "users";
    private String SHARED_PREFS = "user_details";
    private static final String NOTIFICATION_CHANNEL_ID1 = "shulamit.hila.letsmeet.shareLocation";
    private static final String NOTIFICATION_CHANNEL_ID2 = "shulamit.hila.letsmeet.getLocation";
    private static final String NOTIFICATION_CHANNEL_ID3 = "shulamit.hila.letsmeet.acceptSharing";
    private static final String NOTIFICATION_CHANNEL_ID4 = "shulamit.hila.letsmeet.declineSharing";
    private NotificationManager notificationManager;
    private FirebaseFirestore db;

    public MyFirebaseMessagingService() {
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        showNotification(remoteMessage.getData());


        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void showNotification(Map<String,String>data) {

        String channelId = data.get("channelId");
        switch (channelId) {
            case "1":
                showNotification1(data);
                break;
            case "2":
                showNotification2(data);//to change inside
                break;
            case "3":
                showNotification3(data);
                break;

        }
        createChannels();
    }

    private void showNotification1(Map<String,String>data) {
        String latLng = data.get("latLng");
        String body = data.get("title");
        String title = "You got new location!";
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        int notificationId = new Random().nextInt();
        Intent intentGo = new Intent(this, MainActivity.class);
        intentGo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intentGo.putExtra("goMTitle", body);
        intentGo.putExtra("goMLatLng", latLng);
        intentGo.putExtra("notificationId", notificationId);
        intentGo.putExtra("shouldNavigate",true);

        PendingIntent actionIntentGo= PendingIntent.getActivity(this,0,intentGo,PendingIntent.FLAG_ONE_SHOT);
        // put extra for saves activity
        Intent intentSave = new Intent(this, SavesActivity.class);
        intentSave.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intentSave.putExtra("saveMTitle", body);
        intentSave.putExtra("saveMLatLng", latLng);
        intentSave.putExtra("notificationId", notificationId);
        intentSave.putExtra("shouldAddNewLocation",true);
        PendingIntent actionIntentSave= PendingIntent.getActivity(this,0,intentSave,PendingIntent.FLAG_ONE_SHOT);

        addLocationToHistoryTable(body, latLng);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID1);
        notification.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_launcher,"go there",actionIntentGo)
                .addAction(R.mipmap.ic_launcher,"save location",actionIntentSave);
        notificationManager.notify(notificationId, notification.build());
    }

    private void addLocationToHistoryTable(String body, String latLng) {
        String [] temp = new String[2];
        temp = latLng.split(",");
        String lat,lng;
        lat = temp[0];
        lng = temp[1];
        //save in the database
        saveInDB(body,lat,lng);
    }

    private void saveInDB(String name, String lat, String lng) {
        // Get the contact name and email entered
        // Execute SQL statement to insert new data
        SQLiteDatabase historyDB = openOrCreateDatabase(HISTORY_DB, MODE_PRIVATE, null);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        name = name + "\ndate: " + dateFormat.format(date);
        // Execute SQL statement to insert new data
        String sqlHistory = "INSERT INTO historyPoints (name, lat,lng) VALUES ('" + name + "', '" + lat +"', '" + lng + "');";
        historyDB.execSQL(sqlHistory);
    }

    private void showNotification2(Map<String,String>data) {


        String myToken = data.get("userToken");
        String userName = data.get("userName");
        String body = data.get("title");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        int notificationId = new Random().nextInt();
        Intent intentAccept = new Intent(this, MyIntentServiceAccept.class);
        intentAccept.putExtra("senderName", userName);
        intentAccept.putExtra("senderToken",myToken);
        intentAccept.putExtra("notificationId", notificationId);
        intentAccept.putExtra("isUserAccepted", "Accept");
        PendingIntent actionIntentAccept = PendingIntent.getService(this,0,intentAccept,0);//change

        Intent intentDecline = new Intent(this, MyIntentServiceDecline.class);
        intentDecline.putExtra("senderToken", myToken);
        intentDecline.putExtra("senderName", userName);
        intentDecline.putExtra("notificationId",notificationId);
        intentDecline.putExtra("isUserAccepted", "Decline");
        PendingIntent actionIntentDecline = PendingIntent.getService(this,0,intentDecline,0);//change

      // put extra for saves activity

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID2);
        notification.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .addAction(R.mipmap.ic_launcher,"Accept",actionIntentAccept)
                .addAction(R.mipmap.ic_launcher,"Deny",actionIntentDecline);
        notificationManager.notify(notificationId, notification.build());
    }
    private void showNotification3(Map<String,String>data) {
        int notificationId = new Random().nextInt();

        String body = data.get("message");
        String title = data.get("title");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notificationId",notificationId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID3);
        notification.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent);
        notificationManager.notify(notificationId, notification.build());
    }
    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel1 = new NotificationChannel(NOTIFICATION_CHANNEL_ID1,"Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel1.setDescription("EDMT Channel");
            notificationChannel1.enableLights(true);
            notificationChannel1.setLightColor(Color.BLUE);
            notificationChannel1.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel1.enableLights(true);
            notificationManager.createNotificationChannel(notificationChannel1
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel2 = new NotificationChannel(NOTIFICATION_CHANNEL_ID2,"Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel2.setDescription("EDMT Channel");
            notificationChannel2.enableLights(true);
            notificationChannel2.setLightColor(Color.BLUE);
            notificationChannel2.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel2.enableLights(true);
            notificationManager.createNotificationChannel(notificationChannel2
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel3 = new NotificationChannel(NOTIFICATION_CHANNEL_ID3,"Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel3.setDescription("EDMT Channel");
            notificationChannel3.enableLights(true);
            notificationChannel3.setLightColor(Color.BLUE);
            notificationChannel3.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel3.enableLights(true);
            notificationManager.createNotificationChannel(notificationChannel3
            );

        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Map<String, Object> number = new HashMap<>();
        number.put("token",token);

        db.collection(USERS).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .update(number)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

    }
}
