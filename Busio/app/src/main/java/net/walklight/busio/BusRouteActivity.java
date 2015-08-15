package net.walklight.busio;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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
import java.util.Calendar;
import java.util.List;

public class BusRouteActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private ListView listView;
    private TextView tvGPS;
    private Context context;
    private BusStopAdapter adapter;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private TrackingCallback callback;
    private boolean cancelTracking = false;
//    private GPSTrackingThread gpsTrackingThread;
//    private GPSTracker gpsTracker = null;

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route);

        this.context = this;
        listView = (ListView) findViewById(R.id.lv_bus_routes);
        tvGPS = (TextView) findViewById(R.id.tv_gps);

        callback = new TrackingCallback();

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
        Location location = new Location("");
        return location;
    }

    public void startGPSTrackingThread(){
//        gpsTrackingThread = new GPSTrackingThread(this);
//        gpsTrackingThread.startThread();

//        Log.i("BRA", "Tracking started");

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Toast.makeText(context, "Google Location Service created", Toast.LENGTH_SHORT).show();

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(500);

        googleApiClient.connect();
        Toast.makeText(context, "Tracking started", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(googleApiClient != null && googleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        cancelTracking = true;
//        gpsTrackingThread.stopThread();
//        Log.i("BRA", "Tracking stopped");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
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

    public TextView getTvGPS() {
        return tvGPS;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("GGPS", "Location services connected.");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    public void handleNewLocation(final Location location){
        if(!callback.isDoneTracking() && !cancelTracking) {
            adapter.updateBusStops(location, callback);
            ((Activity) this).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();

                    int position = adapter.getCurrentItem();
                    if (position >= 0) {
                        listView.smoothScrollToPosition(position);
                    }

                    Calendar c = Calendar.getInstance();
                    int seconds = c.get(Calendar.SECOND);
                    ((BusRouteActivity) context).getTvGPS().setText("GPS" + Integer.toString(seconds) + " : " + Double.toString(location.getLongitude()) + ", " + Double.toString(location.getLatitude()));
                    Log.i("GGPS", "GPS" + Integer.toString(seconds) + " : " + Double.toString(location.getLongitude()) + ", " + Double.toString(location.getLatitude()));
                }
            });
        }
        else if(cancelTracking){
            cancelTracking = false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("GGPS", "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("GGPS", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
}
