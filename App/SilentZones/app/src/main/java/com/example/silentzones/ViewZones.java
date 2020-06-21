package com.example.silentzones;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class ViewZones extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCircleClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //long time1 = System.nanoTime();
        setContentView(R.layout.activity_view_zones);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //long time2 = System.nanoTime();
        //long taken = (time2 - time1)/1000;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        Location currloc = MainActivity.getZoneLocation();
        LatLng sydney = new LatLng(currloc.getLatitude(), currloc.getLongitude());
        mMap.addMarker(new MarkerOptions().position(sydney).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,20));
        try {
            getZones();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getZones() throws JSONException, IOException {
        URL url = new URL("https://tanushb.pythonanywhere.com/");
        URLConnection conn = url.openConnection();
        conn.setDoInput(true);
        InputStreamReader isr = new InputStreamReader(conn.getInputStream());
        BufferedReader rdr = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line = "";
        while ((line=rdr.readLine())!=null){
            sb.append(line);
        }
        rdr.close();
        String finaldata = sb.toString();
        JSONObject jsonObject = new JSONObject(finaldata);
        JSONArray jsonArray = jsonObject.getJSONArray("array");
        int iters = jsonArray.length();
        if (iters>0){
        for (int i = 0; i<iters; i++) {
            JSONObject currzone = jsonArray.getJSONObject(i);
            double centerx = currzone.getDouble("centerx");
            double centery = currzone.getDouble("centery");
            double radius = currzone.getDouble("radius");
            LatLng center = new LatLng(centerx, centery);
            mMap.addCircle(new CircleOptions().center(center).radius(radius).fillColor(0x557f7fff)).setClickable(true);
            mMap.addMarker(new MarkerOptions().position(center).title("Center").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        mMap.setOnCircleClickListener(this);
        mMap.setOnMarkerClickListener(this);
        }
        else {
            Toast.makeText(getApplicationContext(), "No silent zones added yet!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCircleClick(Circle circle) {
        LatLng c = circle.getCenter();
        //String msg = String.valueOf(c);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(c,20));
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng pos = marker.getPosition();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 20));
        return false;
    }
}


