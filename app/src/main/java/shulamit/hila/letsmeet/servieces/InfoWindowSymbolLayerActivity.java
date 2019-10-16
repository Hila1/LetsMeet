package shulamit.hila.letsmeet.servieces;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class InfoWindowSymbolLayerActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener  {
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        return;
    }
}
