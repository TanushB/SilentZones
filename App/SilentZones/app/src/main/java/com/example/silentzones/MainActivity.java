package com.example.silentzones;
import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.JsonReader;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.Buffer;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.List;

public class MainActivity extends Activity implements LocationListener {
    protected LocationManager mLocationManager;
    protected LocationListener locationListener;
    protected Context context;
    //LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    TextView text1;
    TextView text2;
    TextView text3;
    public static Location zonelocation;
    Boolean granted = Boolean.FALSE;
    String saved = "";
    AudioManager am;
    String lastSavedZone = "";
    NotificationManager nm;

    private Location getLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            mLocationManager.requestLocationUpdates(provider, 0, 0, this);
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        mLocationManager.requestLocationUpdates(bestLocation.getProvider(), 0, 0, this);
        return bestLocation;
    }

    public String LocationStatus(double lat,double lon) throws IOException, JSONException {
        BufferedReader reader = null;
        text3 = (TextView) findViewById(R.id.text3);
        String response = "Failed!";
        JSONObject data = new JSONObject();
        data.put("x",lat);
        data.put("y",lon);
        String postdata = data.toString();
        URL url = new URL("https://tanushb.pythonanywhere.com/get");
        URLConnection conn = url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(postdata);
        wr.flush();
        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line=reader.readLine())!=null){
            sb.append(line + "\n");
        }
        response = sb.toString();
        reader.close();
        response = response.replaceAll("\\s", "");
        if (response.equals("silent")){
            text3.setText("You are currently in a Silent zone.");
        }
        else if (response.equals("none")){
            text3.setText("You are not in a Silent zone currently.");
        }
        if (!response.equals(lastSavedZone)){
            setAudio(response);
            lastSavedZone=response;
        }
        return response;
    }

    public void setAudio(String action){
        if (action.length()>0){
            int act_len = action.length();
            String print_len = Integer.toString(act_len);
        }
        switch (action){
            case "silent":
            switch (am.getRingerMode()){
                case AudioManager.RINGER_MODE_SILENT: saved="silent";break;
                case AudioManager.RINGER_MODE_NORMAL: saved="ring"; break;
                case AudioManager.RINGER_MODE_VIBRATE: saved="vibrate";break;
            }
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE); break;
            case "none":
            if (saved.length()>0){
                switch(saved){
                    case "silent": am.setRingerMode(AudioManager.RINGER_MODE_SILENT);break;
                    case "vibrate": am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);break;
                    case "ring": am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);break;
                }
            } break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (!nm.isNotificationPolicyAccessGranted()){
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_BACKGROUND_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},1);
        }
        else {
            granted = Boolean.TRUE;
        }
        if (granted) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location lastLocation = getLocation();
            if (lastLocation != null) {
                double strlat = lastLocation.getLatitude();
                double strlong = lastLocation.getLongitude();
                text1.setText(Double.toString(strlat));
                text2.setText(Double.toString(strlong));
                try {
                    String action = LocationStatus(strlat, strlong);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onLocationChanged(Location location){
        if (granted) {
            if (location != null) {
                double strlat = location.getLatitude();
                double strlong = location.getLongitude();
                text1.setText(Double.toString(strlat));
                text2.setText(Double.toString(strlong));
                try {
                    String action = LocationStatus(strlat, strlong);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Location reset = getLocation();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Location reset = getLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Location reset = getLocation();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==1){
            if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                granted = Boolean.TRUE;
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (!nm.isNotificationPolicyAccessGranted()){
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    public static Location getZoneLocation(){
        return zonelocation;
    }

    public void newzone(View v){
        Intent intent = new Intent(this, AddZone.class);
        zonelocation = getLocation();
        startActivity(intent);
    }

    public void viewZones(View v) throws IOException, JSONException {
        zonelocation = getLocation();
        Intent intent = new Intent(this, ViewZones.class);
        startActivity(intent);
    }

    public void delZone(View v){
        //Calendar calendar = Calendar.getInstance();
        //long time1 = calendar.getTimeInMillis();
        zonelocation = getLocation();
        //long time2 = calendar.getTimeInMillis();
        //String msg = String.valueOf(time2-time1);
        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,delZone.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

}
