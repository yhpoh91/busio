package net.walklight.busio;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.walklight.busio.utils.BusManager;
import net.walklight.busio.utils.BusStop;
import net.walklight.busio.utils.BusStopAdapter;
import net.walklight.busio.utils.Callback;
import net.walklight.busio.utils.GPSTracker;
import net.walklight.busio.utils.TrackingCallback;
import net.walklight.busio.utils.WebTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BusRouteActivity extends AppCompatActivity {
    private ListView listView;
    private Context context;
    private BusStopAdapter adapter;

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

    public Location getCurrentLocation(){
        GPSTracker gpsTracker = new GPSTracker(context);
        Location location = gpsTracker.getLocation();
        return location;
    }

    public void startGPSTrackingThread(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                TrackingCallback callback = new TrackingCallback();

                while(!callback.isDoneTracking()){
                    adapter.updateBusStops(getCurrentLocation(), callback);

                    final int position = adapter.getCurrentItem();
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(position >= 0) {
                                listView.smoothScrollToPosition(position);
                            }
                        }
                    });
                }

                // Notify User
                Runnable notificationRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Vibrate
                        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                        if(vibrator.hasVibrator()){
                            vibrator.vibrate(Constant.VIRBATION_LENGTH);
                        }

                        // Ring
                    }
                };

                ((Activity) context).runOnUiThread(notificationRunnable);
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }
}
