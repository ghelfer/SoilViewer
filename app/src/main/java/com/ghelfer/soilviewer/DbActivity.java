package com.ghelfer.soilviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class DbActivity extends AppCompatActivity {

    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        listView = findViewById(R.id.listView);

        retrieveLocal();
    }

    private void retrieveLocal() {

        DatabaseHelper helper= new DatabaseHelper(this);
        SQLiteDatabase db= helper.getReadableDatabase();

        List<Map<String,String>> list = new ArrayList<>();
        String query = "select * from soil order by id desc";
        Cursor res = db.rawQuery( query, null,null );
        res.moveToFirst();
        while(res.isAfterLast() == false) {
            String id = res.getString(res.getColumnIndex("id"));
            String name = res.getString(res.getColumnIndex("name"));
            String lat = res.getString(res.getColumnIndex("lat"));
            String lon = res.getString(res.getColumnIndex("lon"));
            String dt = res.getString(res.getColumnIndex("dt"));

            Map<String,String> hash = new HashMap<>();
            hash.put("id", id);
            hash.put("name", name);
            hash.put("lat", lat);
            hash.put("lon", lon);
            hash.put("dt", dt);
            list.add(hash);
            res.moveToNext();
        }
        SimpleAdapter adapter = new SimpleAdapter(this,list,R.layout.list_db,
                new String[]{"id", "name", "lat", "lon", "dt"},
                new int[]{R.id.id, R.id.name, R.id.lat, R.id.lon, R.id.dt});
        listView.setAdapter(adapter);

        helper.close();

    }


    private void sync(String lat, String lon, String name) {
        RequestParams params = new RequestParams();
        params.add("lat", lat);
        params.add("lon", lon);
        params.add("name", name);
        params.add("om", "0");
        params.add("clay", "0");

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Version", "1.0");
        client.post(Tools.ACCESS_URL + "/ws.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    String result = new String(response, "UTF-8");
                    MessageBox(result);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cloud) {
            upload();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void upload() {
    }

    private void MessageBox(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}