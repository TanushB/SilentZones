package com.example.silentzones;

import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class delZone extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCircleClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_del_zone);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            renderZones();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        Location loc = MainActivity.getZoneLocation();
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(loc.getLatitude(), loc.getLongitude());
        mMap.addMarker(new MarkerOptions().position(sydney).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18));
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
    public void renderZones() throws JSONException, IOException {
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(c, 20));
        removeZone(c);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng ml = marker.getPosition();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ml, 20));
        if (marker.getTitle().equals("Center")){
            removeZone(ml);
        }
        else{
            Toast.makeText(getApplicationContext(), "Click on a Circle or a blue marker to select a zone.", Toast.LENGTH_LONG).show();
        }
        return false;
    }
    public void removeZone(LatLng zone){
        final LatLng zn = zone;
        System.out.println("Called!");
        Snackbar snackbar = Snackbar.make(mapFragment.getView(),"Remove Silent Zone?", 60000);
        snackbar.setAction("Remove", new View.OnClickListener() {
            @Override
          public void onClick(View v) {
              try {
                  URL url = new URL("https://tanushb.pythonanywhere.com/remove");
                  URLConnection conn = url.openConnection();
                  conn.setDoOutput(true);
                  conn.setDoInput(true);
                  JSONObject jsonObject = new JSONObject();
                  jsonObject.put("x", zn.latitude);
                  jsonObject.put("y", zn.longitude);
                  OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
                  osw.write(jsonObject.toString());
                  osw.flush();
                  BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                  StringBuilder sb = new StringBuilder();
                  String line = "";
                  while ((line=br.readLine())!=null){
                      sb.append(line);
                  }
                  String resp = sb.toString();
                  if (resp.equals("success")){
                      finish();
                      Toast.makeText(getApplicationContext(),"Zone removed successfully", Toast.LENGTH_SHORT).show();
                  }
                  else if (resp.equals("failure")){
                      Toast.makeText(getApplicationContext(), "The system failed to remove the zone :(", Toast.LENGTH_SHORT).show();
                  }
              }
              catch (Exception e){
                  Toast.makeText(getApplicationContext(), "Connection Failed", Toast.LENGTH_SHORT).show();
              }

            }
        });
        snackbar.show();
    }
}