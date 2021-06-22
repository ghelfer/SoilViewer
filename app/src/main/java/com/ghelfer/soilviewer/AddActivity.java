package com.ghelfer.soilviewer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class AddActivity extends AppCompatActivity {

    LocationManager locationManager;
    String latitude, longitude;
    EditText txtLat, txtLon, txtName, txtOm, txtClay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        txtLat = findViewById(R.id.txtlat);
        txtLon = findViewById(R.id.txtlon);
        txtName = findViewById(R.id.txtname);
        txtOm = findViewById(R.id.txtom);
        txtClay = findViewById(R.id.txtclay);

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


        //Data e Hora
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format( c.getTime() );

        //Banco de dados
        DatabaseHelper helper= new DatabaseHelper(this);
        SQLiteDatabase db= helper.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put("name", txtName.getText().toString());
        values.put("latitude",  txtLat.getText().toString() );
        values.put("longitude",  txtLon.getText().toString() );
        values.put("dt",  formattedDate );
        long resultado= db.insert("soil", null, values);
        if( resultado != -1 ){
            Toast.makeText(this, "Salvo com sucesso!!", Toast.LENGTH_SHORT).show();
            clean();
        }else{
            Toast.makeText(this, "Erro ao salvar registro!",
                    Toast.LENGTH_SHORT).show();
        }
        helper.close();


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