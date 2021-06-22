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
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
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

    private CombinedChart chart;
    List<BarEntry> entries1;
    List<Entry> entries2,entries3,entries4,entries5,entries6,entries7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_data);

        chart = findViewById(R.id.chart1);

        chart.getDescription().setEnabled(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setHighlightFullBarEnabled(false);

        // draw bars behind lines
        chart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        buscaDados("today");
    }

    private LineData generateLineData() {
        LineData d = new LineData();

        LineDataSet set2 = new LineDataSet(entries2, "Air temp");
        set2.setColor(Color.RED);
        set2.setLineWidth(2.5f);
        set2.setDrawCircles(false);
        //set2.setFillColor(Color.rgb(240, 238, 70));
        set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set2.setDrawValues(false);
        //set2.setValueTextSize(10f);
        //set2.setValueTextColor(Color.rgb(255, 0, 0));
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet set3 = new LineDataSet(entries3, "Humidity");
        set3.setColor(Color.BLUE);
        set3.setLineWidth(2.5f);
        set3.setDrawCircles(false);
        //set3.setFillColor(Color.rgb(240, 238, 70));
        set3.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set3.setDrawValues(false);
        //set3.setValueTextSize(10f);
        //set3.setValueTextColor(Color.rgb(0, 0, 255));
        set3.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet set4 = new LineDataSet(entries4, "Soil temp");
        set4.setColor(Color.rgb(255, 128, 0));
        set4.setLineWidth(2.5f);
        set4.setDrawCircles(false);
        //set4.setFillColor(Color.rgb(240, 238, 70));
        set4.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set4.setDrawValues(false);
        //set4.setValueTextSize(10f);
        //set4.setValueTextColor(Color.rgb(255, 128, 0));
        set4.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet set5 = new LineDataSet(entries5, "Moisture");
        set5.setColor(Color.rgb(0, 128, 225));
        set5.setLineWidth(2.5f);
        set5.setDrawCircles(false);
        //set5.setFillColor(Color.rgb(240, 238, 70));
        set5.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set5.setDrawValues(false);
        //et5.setValueTextSize(10f);
        //set5.setValueTextColor(Color.rgb(0, 128, 255));
        set5.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet set6 = new LineDataSet(entries6, "Wind");
        set6.setColor(Color.rgb(0, 128, 0));
        set6.setLineWidth(2.5f);
        set6.setDrawCircles(false);
        //set6.setFillColor(Color.rgb(240, 238, 70));
        set6.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set6.setDrawValues(false);
        //set6.setValueTextSize(10f);
        //set6.setValueTextColor(Color.rgb(0, 128, 0));
        set6.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineDataSet set7 = new LineDataSet(entries7, "Pressure");
        set7.setColor(Color.rgb(0, 0, 0));
        set7.setLineWidth(2.5f);
        set7.setDrawCircles(false);
        //set7.setFillColor(Color.rgb(240, 238, 70));
        set7.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set7.setDrawValues(false);
        //set7.setValueTextSize(10f);
        //set7.setValueTextColor(Color.rgb(0, 0, 0));
        set7.setAxisDependency(YAxis.AxisDependency.LEFT);

        d.addDataSet(set2);
        d.addDataSet(set3);
        d.addDataSet(set4);
        d.addDataSet(set5);
        d.addDataSet(set6);
        d.addDataSet(set7);

        return d;

    }

    private BarData generateBarData() {
        BarDataSet set1 = new BarDataSet(entries1, "Lux");
        set1.setColor(Color.rgb(60, 220, 78));
        set1.setValueTextColor(Color.rgb(60, 220, 78));
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        float groupSpace = 0.06f;
        float barSpace = 0.02f; // x2 dataset
        float barWidth = 0.45f; // x2 dataset
        // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 0));
        BarDataSet set2 = new BarDataSet(entries, "");

        BarData d = new BarData(set1,set2);
        d.setBarWidth(barWidth);

        // make this BarData object grouped
        d.groupBars(0, groupSpace, barSpace); // start at x = 0

        return d;
    }


    private void buscaDados(String t) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Version", "1.0");
        client.get(Tools.ACCESS_URL + "/ws.php?" + t, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                MessageBox("Wait... downloading data!");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                try {
                    try {
                        String result = new String(response, "UTF-8");
                        popupLists(result);

                    } catch (Exception ex) {
                        MessageBox("General Exception: " + ex.getMessage());
                    }
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

    private void popupLists(String res) {
        entries1 = new ArrayList<>();
        entries2 = new ArrayList<>();
        entries3 = new ArrayList<>();
        entries4 = new ArrayList<>();
        entries5 = new ArrayList<>();
        entries6 = new ArrayList<>();
        entries7 = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(res);
            JSONArray array = obj.getJSONArray("result");
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                //"id","airtemp","humidity","pressure","soil_temp","moisture"
                // "co2","lux","wind","gust","cpu"
                String id = json.get("id").toString();
                String a = json.get("air_temp").toString();
                Float airtemp = Float.parseFloat(a);
                String b = json.get("humidity").toString();
                Float humidity = Float.parseFloat(b);
                String c = json.get("pressure").toString();
                Float pressure = Float.parseFloat(c);
                String d = json.get("soil_temp").toString();
                Float soil_temp = Float.parseFloat(d);
                String e = json.get("moisture").toString();
                Float moisture = Float.parseFloat(e);
                String f = json.get("lux").toString();
                Float lux = Float.parseFloat(f);
                String g = json.get("wind").toString();
                Float wind = Float.parseFloat(g);
                String h = json.get("gust").toString();
                Float gust = Float.parseFloat(h);
      
                entries1.add(new BarEntry(i, lux/10));
                entries2.add(new Entry(i, airtemp));
                entries3.add(new Entry(i, humidity));
                entries4.add(new Entry(i, soil_temp));
                entries5.add(new Entry(i, moisture));
                entries6.add(new Entry(i, wind));
                entries7.add(new Entry(i, pressure/10));
            }

            Legend l = chart.getLegend();
            l.setWordWrapEnabled(true);
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            l.setDrawInside(false);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setDrawGridLines(false);
            rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setDrawGridLines(false);
            leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
            xAxis.setAxisMinimum(0f);
            xAxis.setGranularity(1f);

            CombinedData data = new CombinedData();

            data.setData(generateBarData());
            data.setData(generateLineData());

            xAxis.setAxisMaximum(data.getXMax() + 0.25f);

            chart.setData(data);
            chart.invalidate();

        }catch (Exception ex){
            MessageBox("Error: " + ex.getMessage());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_data, menu);
        return true;
    }

    boolean notify = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            buscaDados("today");
            return true;
        }
        if (id == R.id.action_last) {
            buscaDados("last");
            return true;
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