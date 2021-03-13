package com.ghelfer.soilviewer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import cz.msebera.android.httpclient.Header;

public class AddActivity extends AppCompatActivity {

    LocationManager locationManager;
    String latitude, longitude;
    EditText txtLat, txtLon, txtName, txtOm, txtClay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        txtLat = findViewById(R.id.lat);
        txtLon = findViewById(R.id.lon);
        txtName = findViewById(R.id.name);
        txtOm = findViewById(R.id.om);
        txtClay = findViewById(R.id.clay);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGPS();
            } else {
                getLocation();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            return;
        }
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (locationGPS != null) {
            double lat = locationGPS.getLatitude();
            double lon = locationGPS.getLongitude();
            latitude = String.valueOf(lat);
            longitude = String.valueOf(lon);

            txtLat.setText(latitude);
            txtLon.setText(longitude);

        } else {
            Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
        }

    }

    public void inputClick(View view) {

        RequestParams params = new RequestParams();
        params.add("lat", txtLat.getText().toString());
        params.add("lon", txtLon.getText().toString());
        params.add("name", txtName.getText().toString());
        params.add("om", txtOm.getText().toString());
        params.add("clay", txtClay.getText().toString());

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Version", "1.0");
        client.post(Tools.ACCESS_URL + "/ws.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    String result = new String(response, "UTF-8");
                    MessageBox(result);
                    clean();
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

    private void clean() {
        String c = "";
        txtLat.setText(c);
        txtLon.setText(c);
        txtName.setText(c);
        txtOm.setText(c);
        txtClay.setText(c);
    }

    private void MessageBox(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}