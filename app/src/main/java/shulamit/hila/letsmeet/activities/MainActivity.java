package shulamit.hila.letsmeet.activities;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import shulamit.hila.letsmeet.R;

/**
 * the main activity display the Map
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationEngineListener, PermissionsListener {
    private MapView mapView;
    private MapboxMap map;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private static Location originLocation;
    private PermissionsManager permissionsManager;
    private com.mapbox.geojson.Point originalPosition;
    private com.mapbox.geojson.Point otherUserPosition;
    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MainActivity";
    private TextToSpeech tts;  // for TextToSpeech engine

    public static Location getOriginLocation() {
        return originLocation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);//create the map
        mapView.getMapAsync(this);
        otherUserPosition = com.mapbox.geojson.Point.fromLngLat(35.172306,31.832323);//in the beginning
        initializeTextSpeakEngine();

    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        enableLocation();//check permission
        navigate();//drow route on map if the is a destination
    }

    /**
     * display the route between original location and destination location
     * @param origin the orginal location
     * @param destination the destination location
     */
    private void getRoute(Point origin, Point destination){
        MakeSound();
        NavigationRoute.builder().accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null){
                            Log.e(TAG, "No routes found, check right user and access token");
                            return;
                        }
                        else if (response.body().routes().size() == 0){
                            Log.e(TAG, "no routes found");
                            return;
                        }
                        DirectionsRoute currentRoute = response.body().routes().get(0);//get the first route
                        if (navigationMapRoute != null){//remove old route from map
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null,mapView,map);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG, "Error " + t.getMessage());
                    }
                });
    }

    /**
     * check permission and initialize permission
     */
    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)){
                initializeLocationEngine();
                initializeLocationLayer();
        }
        else{
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * initialize the location, ignore permissions - we already have them
     */
    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine(){
        locationEngine = new LocationEngineProvider(MainActivity.this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation !=null) {
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        }
        else {
            locationEngine.addLocationEngineListener(this);
        }
    }


    /**
     * initialize the location layer, ignore permissions - we already have them
     */
    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer(){
        locationLayerPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);

    }
    private void setCameraPosition(Location location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),150.0));
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onStart() {
        super.onStart();
        if (locationEngine!= null){
            locationEngine.requestLocationUpdates();
        }
        if (locationLayerPlugin != null){
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(locationEngine != null)
            locationEngine.removeLocationUpdates();
        if(locationLayerPlugin != null)
            locationLayerPlugin.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null){
            locationEngine.deactivate();
        }
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // open the relevant dialog / activity according to the menu selection
        switch (item.getItemId()) {
            case R.id.ic_about: {
                openAboutDialog();
                break;
            }
            case R.id.ic_exit: {
                openExitDialog();
                break;
            }
            case R.id.item_contacts: {
                Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.item_history: {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.item_saves: {
                Intent intent = new Intent(MainActivity.this, SavesActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.ic_share:{
                shareApp();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareApp() {
        String message = this.getResources().getString(R.string.share_message);
        Intent shareAppIntent = new Intent(Intent.ACTION_SEND);
        shareAppIntent.setType("text/plain");
        shareAppIntent.putExtra(Intent.EXTRA_TEXT,message);
        shareAppIntent.putExtra(Intent.EXTRA_SUBJECT,"Meet your friend!");
        startActivity(shareAppIntent);
    }

    private void openAboutDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(R.mipmap.ic_launcher_round);
        alertDialog.setTitle(R.string.about_title);
        alertDialog.setMessage(R.string.about);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

    private void openExitDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setIcon(R.mipmap.ic_launcher_round);
        alertDialog.setTitle(R.string.exit_title);
        alertDialog.setMessage(R.string.exit_qustion);
        alertDialog.setCancelable(true);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(1); // destroy this activity
            }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    /*
    * remove the possibility to go back to login activity
     */
    @Override
    public void onBackPressed() { }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) { }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            enableLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode,permissions, grantResults);
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            originLocation = location;
            setCameraPosition(location);
            }
    }

    /**
     * draw location on map
     */
    private  void navigate(){
        //say 'lets go!'


        Intent intent = getIntent();

        boolean shouldNavigate = intent.getBooleanExtra("shouldNavigate",false);
        boolean shouldNavigateFromSaves= intent.getBooleanExtra("shouldNavigateFromSaves",false);

        // is assigned to true if the user has added a new location by notification
        if(shouldNavigate)
            startNavigate(intent);
        if(shouldNavigateFromSaves){
            startNavigateFromSaves(intent);
        }
    }

    private void MakeSound() {

        String textToSpeak = "Lets go!";

        // Speech pitch. 1.0 is the normal pitch, lower values lower the tone of
        // the synthesized voice, greater values increase it.
        tts.setPitch(1.5f);

        // Speech rate. 1.0 is the normal speech rate, lower values slow down
        // the speech (0.5 is half the normal speech rate), greater values
        // accelerate it (2.0 is twice the normal speech rate).
        tts.setSpeechRate(1.2f);

        // speak up the string text
        tts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);

    }

    private void initializeTextSpeakEngine() {
        // init Text To Speech engine
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS)
                {
                    int result = tts.setLanguage(Locale.UK);
                    if (result == TextToSpeech.LANG_MISSING_DATA  || result == TextToSpeech.LANG_NOT_SUPPORTED)
                        Toast.makeText(MainActivity.this,"Error: TextToSpeech Language not supported!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startNavigateFromSaves(Intent intent) {
        NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        double userLat = intent.getDoubleExtra("otherUserLat",0);
        double userLng = intent.getDoubleExtra("otherUserLng",0);
        int notificationId = intent.getIntExtra("notificationId", 0);
        notificationManager.cancel(notificationId);

        try {
            otherUserPosition = com.mapbox.geojson.Point.fromLngLat(userLng,userLat);
            LatLng point = new LatLng(originLocation.getLatitude(), originLocation.getLongitude());
            Marker destinationMarker = map.addMarker(new MarkerOptions().position(point));
            originalPosition = com.mapbox.geojson.Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());
            getRoute(originalPosition, otherUserPosition);

        }
        catch (Exception e){
            Toast.makeText(this,"could not find your location here",Toast.LENGTH_LONG).show();
        }
    }

    private void startNavigate(Intent intent) {
        NotificationManager notificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String body = intent.getStringExtra("goMLatLng");
        int notificationId= intent.getIntExtra("notificationId",0);
        String [] temp = new String[2];
        temp = body.split(",");
        String lat,lng;
        lat = temp[0];
        lng = temp[1];

        notificationManager.cancel(notificationId);
        try {
            otherUserPosition = com.mapbox.geojson.Point.fromLngLat(Double.parseDouble(lng),Double.parseDouble(lat));
            originalPosition = com.mapbox.geojson.Point.fromLngLat(originLocation.getLongitude(), originLocation.getLatitude());
            LatLng point = new LatLng(otherUserPosition.latitude(), otherUserPosition.longitude());
            Marker destinationMarker = map.addMarker(new MarkerOptions().position(point));
            getRoute(originalPosition, otherUserPosition);
        }
        catch (Exception e){
            Toast.makeText(this,"could not find your location",Toast.LENGTH_LONG).show();
        }
    }

}
