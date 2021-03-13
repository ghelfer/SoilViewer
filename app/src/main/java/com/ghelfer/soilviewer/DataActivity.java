package com.ghelfer.soilviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class DataActivity extends AppCompatActivity {

    Menu menu;
    private ListView listView;
    List<Map<String, String>> lista;
    SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        listView = (ListView) findViewById(R.id.listView);

        lista = new ArrayList<>();
        adapter = new SimpleAdapter(this, lista,
                R.layout.list_item, new String[]{"id", "descr", "sample", "dt"},
                new int[]{R.id.id, R.id.descr, R.id.sample, R.id.dt});
        listView.setAdapter(adapter);

        buscaDados();
    }

    ProgressDialog dialog;

    private void buscaDados() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Version", "1.0");
        client.get(Tools.ACCESS_URL + "/ws.php?today", new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                dialog = ProgressDialog.show(DataActivity.this, "Aguarde",
                        "Baixando dados...");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    try {
                        String result = new String(response, "UTF-8");
                        JSONObject obj = new JSONObject(result);
                        JSONArray array = obj.getJSONArray("sample");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject json = array.getJSONObject(i);
                            Double om = Double.parseDouble(json.get("om").toString());
                            Double clay = Double.parseDouble(json.get("clay").toString());
                            String sample = json.get("sample").toString();
                            String id = json.get("id").toString();
                            String dt = json.get("dt").toString();

                            Map<String, String> mapa = new HashMap<>();
                            mapa.put("id", id);
                            mapa.put("descr", String.format(Locale.US, "O.M.: %.2f  Clay: %.1f", om, clay));
                            mapa.put("sample", sample);
                            mapa.put("dt", dt);
                            lista.add(mapa);
                        }
                        adapter.notifyDataSetChanged();


                    } catch (Exception ex) {
                        MessageBox("General Exception: " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    MessageBox("General Exception: " + ex.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //showProgress(false);
                MessageBox(error.getLocalizedMessage());
            }

            @Override
            public void onFinish() {
                dialog.dismiss();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_data, menu);
        return true;
    }

    boolean notify = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            buscaDados();
            return true;
        } else {
            if (id == R.id.action_not) {
                notify = !notify;
                if (notify) {
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_notif));
                    //startService(i);
                    criarNotificação();

                } else {
                    menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_notif_none));
                    //stopService(i);

                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void criarNotificação() {

        int id = 1;
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Version", "1.0");
        client.get("http://mlservices.top:8000/?type=last", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    String result = new String(response, "UTF-8");
                    JSONObject obj = new JSONObject(result);
                    JSONArray array = obj.getJSONArray("sample");
                    JSONObject json = array.getJSONObject(0);
                    Double om = Double.parseDouble(json.get("om").toString());
                    Double clay = Double.parseDouble(json.get("clay").toString());
                    String sample = json.get("sample").toString();
                    String id = json.get("id").toString();
                    String dt = json.get("dt").toString();

                    String text = String.format(Locale.US, "O.M.: %.2f  Clay: %.2f", om, clay);
                    criarNotificação(dt, sample, text, id);


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

    private void criarNotificação(String... values) {
        int icone = R.drawable.ic_lab;
        long data = System.currentTimeMillis();

        Intent i = new Intent(getApplicationContext(), DataActivity.class);
        //i.putExtra("temp", temp);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
        NotificationCompat.Builder build =
                new NotificationCompat.Builder(getApplicationContext(), "channel_id");

        Notification not = build.setContentIntent(pi).setSmallIcon(icone)
                .setAutoCancel(false).setTicker(values[0]).setContentTitle(values[1])
                .setContentText(values[2]).setWhen(data).build();
        not.flags = Notification.FLAG_AUTO_CANCEL;
        not.defaults |= Notification.DEFAULT_VIBRATE;
        not.defaults |= Notification.DEFAULT_LIGHTS;
        not.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(Integer.parseInt(values[3]), not);

    }

    private void MessageBox(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}