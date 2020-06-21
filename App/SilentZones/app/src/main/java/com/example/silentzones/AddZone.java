package com.example.silentzones;

import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class AddZone extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    double diam;
    EditText InputDiameter;
    Circle circleZone;
    Marker marker;
    double rad;
    LatLng center;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zone);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    public void onMapClick(LatLng click){
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(click,20));
        marker.setPosition(click);
        marker.setTitle("Center");
        center = click;
        circleZone.setCenter(click);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        rad = 25;
        Location markerloc = MainActivity.getZoneLocation();
        Double zonelat = markerloc.getLatitude();
        Double zonelon = markerloc.getLongitude();
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(zonelat, zonelon);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,20));
        CircleOptions co = new CircleOptions();
        co.center(sydney);
        center = sydney;
        co.radius(rad);
        circleZone = mMap.addCircle(co);
        mMap.setOnMapClickListener(this);
    }

    public void onDraw(View v){
        InputDiameter = (EditText) findViewById(R.id.diameter);
        String temp = InputDiameter.getText().toString();
        if (temp.length()>0){
            diam = Double.valueOf(temp);
            rad = diam/2.0;
            circleZone.setRadius(rad);
        }
    }

    public void onSubmit(View view) throws JSONException, IOException {
        JSONObject adddata = new JSONObject();
        adddata.put("centerx", center.latitude);
        adddata.put("centery", center.longitude);
        adddata.put("radius", rad);
        String addpost = adddata.toString();
        URL posturl = new URL("https://tanushb.pythonanywhere.com/add");
        URLConnection postconn = posturl.openConnection();
        postconn.setDoOutput(true);
        postconn.setDoInput(true);
        OutputStreamWriter ows = new OutputStreamWriter(postconn.getOutputStream());
        ows.write(addpost);
        ows.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(postconn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line=reader.readLine())!=null){
            sb.append(line);
        }
        reader.close();
        String success = sb.toString();
        success.replaceAll("\\s", "");
        if (success.equals("Success!")){
            Toast.makeText(getApplicationContext(), "Uploaded!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
