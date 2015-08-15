package net.walklight.busio;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import net.walklight.busio.utils.BusManager;
import net.walklight.busio.utils.BusStop;
import net.walklight.busio.utils.BusStopAdapter;
import net.walklight.busio.utils.Callback;
import net.walklight.busio.utils.GPSTracker;
import net.walklight.busio.utils.GPSTrackingThread;
import net.walklight.busio.utils.TrackingCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BusRouteActivity extends AppCompatActivity {
    private ListView listView;
    private Context context;
    private BusStopAdapter adapter;
    private GPSTrackingThread gpsTrackingThread;
    private GPSTracker gpsTracker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route);

        this.context = this;
        listView = (ListView) findViewById(R.id.lv_bus_routes);

        final Bundle bundle = getIntent().getExtras();
        String source = bundle.getString("source", "10009");
        String destination = bundle.getString("destination", "14039");
        final String busService = bundle.getString("busService", "131M");

        final BusManager busManager = new BusManager();
        busManager.getBusRoute(source, destination, busService, new Callback() {
            @Override
            public void callback(final Object object) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d("BRA", ((JSONArray) object).toString());
                        List<String> busStops = new ArrayList<String>();
                        JSONArray array = (JSONArray) object;

                        adapter = new BusStopAdapter(context);

                        for(int i = 0; i < array.length(); i++){
                            try {
                                JSONObject jsonObject = array.getJSONObject(i);
                                String id = jsonObject.getString("id");
                                String name = jsonObject.getString("name");
                                double latitude = jsonObject.getDouble("y");
                                double longitude = jsonObject.getDouble("x");

                                Location location = new Location("");
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);

                                BusStop busStop = new BusStop();
                                busStop.setName(name);
                                busStop.setNumber(id);
                                busStop.setLocation(location);

                                adapter.add(busStop);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        listView.setAdapter(adapter);

                        startGPSTrackingThread();
                    }
                };

                ((Activity) context).runOnUiThread(runnable);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_bus_route, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Location getLocation(){
        Location location = null;
        if(gpsTracker == null) {
            gpsTracker = new GPSTracker(context);
            location = gpsTracker.getLocation();
        }
        else{
            location = gpsTracker.getCurrentLocation();
        }
        return location;
    }

    public void startGPSTrackingThread(){
        gpsTrackingThread = new GPSTrackingThread(this);
        gpsTrackingThread.startThread();

        Log.i("BRA", "Tracking started");
        Toast.makeText(context, "Tracking started", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gpsTrackingThread.stopThread();
        Log.i("BRA", "Tracking stopped");
    }

    public ListView getListView() {
        return listView;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public BusStopAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(BusStopAdapter adapter) {
        this.adapter = adapter;
    }

    public GPSTrackingThread getGpsTrackingThread() {
        return gpsTrackingThread;
    }

    public void setGpsTrackingThread(GPSTrackingThread gpsTrackingThread) {
        this.gpsTrackingThread = gpsTrackingThread;
    }

    public GPSTracker getGpsTracker() {
        return gpsTracker;
    }

    public void setGpsTracker(GPSTracker gpsTracker) {
        this.gpsTracker = gpsTracker;
    }
}
