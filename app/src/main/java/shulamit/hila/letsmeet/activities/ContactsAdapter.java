package shulamit.hila.letsmeet.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import shulamit.hila.letsmeet.R;
import shulamit.hila.letsmeet.moduls.Person;
import shulamit.hila.letsmeet.servieces.MySingleton;

import static android.content.Context.MODE_PRIVATE;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private static final String USERS = "users";
    private final FirebaseFirestore db;
    private Context context;
    private ArrayList<Person> contacts;
    private ArrayList<String> users;
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAkFCmCqM:APA91bEvR1nquPLEnb5CgQkyb-Oz3qeJ2_RZlQO4XE9Goa-bUnkAGlK83ZZLfOAx3enAI_6PXYltXeIqWdXrGAcDTxD507MnLJzM2knagH-Gyqr1jmwOn4oNn3oDSjY6DYjq9zC53LE_";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";
    String topic;
    private String SHARED_PREFS = "user_details";
    public ContactsAdapter(ArrayList<Person> contacts, Context context, ArrayList<String> users) {
        this.contacts = contacts;
        this.users = users;
        db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        viewHolder.name.setText(contacts.get(position).getName());
        viewHolder.phoneNumber.setText(accurateNumber(contacts.get(position).getNumber()));


    }

    /***
     * if user register to the app return true
     * @param number
     * @return
     */
    private boolean checkIfUserIsRegister(String number) {
        if (users.contains(number))
            return true;
        return false;
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView phoneNumber;
        RelativeLayout xmlContactsItem;
        Button btnShareLocation;
        Button btnGetLocation;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txv_contact_name);
            phoneNumber = itemView.findViewById(R.id.txv_phone_number);
            xmlContactsItem = itemView.findViewById(R.id.item_contacts);
            btnShareLocation = itemView.findViewById(R.id.btn_share_location);
            btnGetLocation = itemView.findViewById(R.id.btn_ask_for_location);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        /**
         * on click on share location notification is send
         */
        (holder).btnShareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = (holder).phoneNumber.getText().toString();
                SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                String name   = sharedPreferences.getString(LoginActivity.USER_NAME, "");

                Location location = MainActivity.getOriginLocation();
                if (location == null) {
                    Toast.makeText(context,"no location", Toast.LENGTH_SHORT).show();
                    return;
                }
                Double lat = location.getLatitude();
                Double lng = location.getLongitude();
                db.collection(USERS).whereEqualTo("number", number).
                        get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    topic = (String) document.getData().get("token");

                                    String title = name +" shared location with you!";
                                    String message = lat + "," + lng;

                                    JSONObject notification = new JSONObject();
                                    JSONObject notificationBody = new JSONObject();
                                    try {
                                        notificationBody.put("title", title);//init the JSON file to channel 2
                                        notificationBody.put("latLng", message);
                                        notificationBody.put("channelId","1" );

                                        notification.put("to", topic);
                                        notification.put("data", notificationBody);
                                    } catch (JSONException e) {
                                        Log.e(TAG, "onCreate: " + e.getMessage());
                                    }
                                    sendNotification(notification);
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        }
                        else {}
                    }
                });
            }
        });
        /**
         *  on click on get location notification is send
         */
        (holder).btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = (holder).phoneNumber.getText().toString();
                SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                String name   = sharedPreferences.getString(LoginActivity.USER_NAME, "");;
                String myToken   = sharedPreferences.getString(LoginActivity.USER_TOKEN, "");;

                db.collection(USERS).whereEqualTo("number", number).
                        get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    String topic = (String) document.getData().get("token");

                                    String title = name +" want to know your location";

                                    JSONObject notification = new JSONObject();
                                    JSONObject notificationBody = new JSONObject();
                                    try {
                                        notificationBody.put("title", title);
                                        notificationBody.put("channelId","2" );//int the JSON file to channel 2
                                        notificationBody.put("userToken",myToken );
                                        notificationBody.put("userName" ,name);

                                        notification.put("to", topic);
                                        notification.put("data", notificationBody);
                                    } catch (JSONException e) {
                                        Log.e(TAG, "onCreate: " + e.getMessage());
                                    }
                                    sendNotification(notification);
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        }
                        else {}
                    }
                });
            }

        });
    }



    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (!checkIfUserIsRegister((holder).phoneNumber.getText().toString())) {
        }
    }

    private String accurateNumber(String number) {
        if (number.charAt(0) != '+') {
            number = "+972" + number.substring(1);
        }
        number = number.replaceAll("-","");
        number = number.replaceAll(" ","");
        return number;
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
                        Log.i(TAG, "onErrorResponse: Didn't work"+error.getMessage());
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
