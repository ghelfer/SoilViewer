package com.ghelfer.soilviewer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    protected LocationManager locationManager;
    String gps = LocationManager.GPS_PROVIDER;
    String network = LocationManager.NETWORK_PROVIDER;

    protected String latitude = "", longitude = "";
    private final String ACCESS_URL = "http://ghelfer.no-ip.org:8000?type=list";

    boolean gps_enabled = false, network_enabled = false;

    private static int INTERVAL = 1000 * 60; //1 minute
    Handler mHandler = new Handler();
    boolean flag = false, flag2 = false;

    Display display;
    Point size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(gps);
        } catch (Exception ex) {
        }

        try {
            network_enabled = locationManager.isProviderEnabled(network);
        } catch (Exception ex) {
        }

        if (!gps_enabled || !network_enabled) {
            showLocationAlert();
        } else {
            SharedPreferences settings = getSharedPreferences("UserInfo", MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("local", true);
            editor.commit();
        }

        load();
    }

    private void load() {

        if (!isLocationEnabled())
            return;

        Location lastKnownLocation;
        try {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            lastKnownLocation = locationManager.getLastKnownLocation(gps);
            latitude = String.valueOf(lastKnownLocation.getLatitude());
            longitude = String.valueOf(lastKnownLocation.getLongitude());
        } catch (Exception ex) {
        }

        try {
            lastKnownLocation = locationManager.getLastKnownLocation(network);
            latitude = String.valueOf(lastKnownLocation.getLatitude());
            longitude = String.valueOf(lastKnownLocation.getLongitude());
        } catch (Exception ex) {
        }


        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        if (!flag) {
            mHandlerTask.run();
            flag = true;
        }

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 0, 0, locationListenerBest);//2 * 60 * 1000, 10
            MessageBox("Best Provider is " + provider);
        }
    }

    private final LocationListener locationListenerBest = new LocationListener() {
        public void onLocationChanged(Location location) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());

            if (!latitude.isEmpty() && !longitude.isEmpty())
                Log.d("GPS: ", String.format("%.3f", Double.parseDouble(latitude)) + " Lng: " + String.format("%.3f", Double.parseDouble(longitude)));
            else
                Log.d("GPS: ", getResources().getString(R.string.location_wait));

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };


    @Override
    public void onMapReady(GoogleMap gMap) {
        mGoogleMap = gMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json2));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-29.5,-52.5) , 7.0f) );
    }


    private Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            buscaDados();
            //mHandler.postDelayed(mHandlerTask, INTERVAL);

        }
    };

    private void buscaDados() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Version", "1.0");
        client.get(ACCESS_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    String result = new String(response, "UTF-8");
                    populaLista(result);
                } catch (Exception ex) {
                    MessageBox("General Exception: " + ex.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //showProgress(false);
                MessageBox(error.getLocalizedMessage());
            }
        });
    }

    private void populaLista(String result) {
        try {

            JSONObject obj = new JSONObject(result);
            JSONArray array = obj.getJSONArray("sample");
            Log.d("DATA", array.length() + " items");
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                String om = json.get("om").toString();
                String clay = json.get("clay").toString();
                String lat = json.get("lat").toString();
                String lon = json.get("lon").toString();
                Log.d("DATA", json.get("id").toString());
                Log.d("DATA", lat + lon);
                addPushPin(lat, lon, om, clay);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("DATA", ex.getMessage());
        }
    }

    private void addPushPin(String lat, String lon, String om, String clay) {
        double mo = Double.parseDouble(om);
        mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)))
                .icon(BitmapDescriptorFactory.defaultMarker(setCor(mo)))
                .title("O.M.:" + formatDigits(1,om) + " Clay:" + formatDigits(2,clay)));

    }

    private String formatDigits(int ctrl, String val) {
        int idx = val.indexOf(".");
        return ctrl==1?val.substring(0,idx+3)+"%":val.substring(0,idx+2)+"%";
    }

    private float setCor(double mo) {
        if (mo < 1)
            return getMarkerColor("#0000ff");
        else if (mo >= 1 && mo < 2)
            return getMarkerColor("#0091ff");
        else if (mo >= 2 && mo < 3)
            return getMarkerColor("#00ffda");
        else if (mo >= 3 && mo < 4)
            return getMarkerColor("#00ff48");
        else if (mo >= 4 && mo < 5)
            return getMarkerColor("#daff00");
        else if (mo >= 5 && mo < 6)
            return getMarkerColor("#ff9100");
        else if (mo >= 6 && mo < 7)
            return getMarkerColor("#ff0000");
        else
            return getMarkerColor("#ff00ff");
    }

    public float getMarkerColor(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return hsv[0];
    }

    private String ConvertDateTime(String timestamp) {
        //String timestamp = "/Date(1376841597000)/";
        Calendar calendar = Calendar.getInstance();
        String aux = timestamp.replace("/Date(", "").replace(")/", "");
        Long timeInMillis = Long.valueOf(aux);
        calendar.setTimeInMillis(timeInMillis);
        calendar.add(Calendar.HOUR_OF_DAY, -4);
        return new SimpleDateFormat("dd-MM EEE HH:mm").format(calendar.getTime()).toString();
    }

    private String ConvertDateTime2(String d,String m,String a,String h,String n) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(a), Integer.parseInt(m), Integer.parseInt(d), Integer.parseInt(h), Integer.parseInt(n));
        //calendar.add(Calendar.HOUR_OF_DAY, -4);
        return new SimpleDateFormat("dd-MM EEEE HH:mm").format(calendar).toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mHandlerTask);
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }


    private void showLocationAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getResources().getString(R.string.location))
                .setMessage(getResources().getString(R.string.set_location))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        MessageBox(getResources().getString(R.string.location_alert));
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void MessageBox(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mapa, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_lab) {
            startActivity(new Intent(this, DataActivity.class));
            return true;
        }
        if (id == R.id.action_add) {
            startActivity(new Intent(this, AddActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



}


