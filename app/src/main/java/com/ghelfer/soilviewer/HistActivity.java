package com.ghelfer.soilviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class HistActivity extends AppCompatActivity  {

    LinearLayout edit;
    ListView listView;
    String id = "0";
    EditText om, clay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hist);

        edit = findViewById(R.id.edit);
        edit.setVisibility(View.GONE);

        om = findViewById(R.id.om);
        clay = findViewById(R.id.clay);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                edit.setVisibility(View.VISIBLE);
                TextView txtId = view.findViewById(R.id.id);
                id = txtId.getText().toString();
                TextView txtOm = view.findViewById(R.id.om);
                om.setText(txtOm.getText().toString());
                TextView txtClay = view.findViewById(R.id.clay);
                clay.setText(txtClay.getText().toString());
            }
        });
        buscaDados();
    }

    private void buscaDados() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Tools.ACCESS_URL + "/ws.php?soil", new AsyncHttpResponseHandler() {
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
            List<Map<String,String>> list = new ArrayList<>();
            JSONObject obj = new JSONObject(result);
            JSONArray array = obj.getJSONArray("result");
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                Map<String,String> map = new HashMap<>();
                String id = json.get("id").toString();
                String om = json.get("om").toString();
                String clay = json.get("clay").toString();
                String lat = json.get("lat").toString();
                String lon = json.get("lon").toString();
                String name = json.get("name").toString();
                String dt = json.get("dt").toString();
                map.put("id", id);
                map.put("om", om);
                map.put("clay", clay);
                map.put("lat", lat);
                map.put("lon", lon);
                map.put("name", name);
                map.put("dt", dt);
                list.add(map);
            }
            SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.list_all,
                    new String[]{"id","om","clay","lat","lon","name","dt"},
                    new int[]{R.id.id,R.id.om,R.id.clay,R.id.lat,R.id.lon,R.id.name, R.id.dt});
            listView.setAdapter(adapter);

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("DATA", ex.getMessage());
        }
    }

    public void updateClick(View view) {
        if (om.getText().toString().isEmpty() ||  clay.getText().toString().isEmpty())
            return;
        String update = "id=" + id + "&om=" + om.getText().toString() + "&clay=" + clay.getText().toString();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Tools.ACCESS_URL + "/ws.php?" + update, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    String result = new String(response, "UTF-8");
                    MessageBox(result);
                    buscaDados();
                    edit.setVisibility(View.GONE);
                } catch (Exception ex) {
                    MessageBox("General Exception: " + ex.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                MessageBox(error.getLocalizedMessage());
            }
        });
    }


    private void MessageBox(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_local, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_local) {
            startActivity(new Intent(this, DbActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}