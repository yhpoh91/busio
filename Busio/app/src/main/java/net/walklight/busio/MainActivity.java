package net.walklight.busio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import net.walklight.busio.utils.BusManager;
import net.walklight.busio.utils.Callback;
import net.walklight.busio.utils.GPSTracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText etSource;
    private EditText etDestination;
    private Button btnSearch;
    private Button btnGPS;
    private ListView lvBuses;
    private ArrayAdapter<String> adapter;
    private Context context;
    private List<String> availableBuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSource = (EditText) findViewById(R.id.et_source);
        etDestination = (EditText) findViewById(R.id.et_destination);
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnGPS = (Button) findViewById(R.id.btn_gps);
        lvBuses = (ListView) findViewById(R.id.lv_buses);

        this.context = this;

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String source = etSource.getText().toString();
                String destination = etDestination.getText().toString();

                BusManager busManager = new BusManager();
                busManager.getBusListFromBusStops(source, destination, new Callback() {
                    @Override
                    public void callback(final Object object) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                availableBuses = (List<String>) object;
                                loadList(availableBuses);
                            }
                        };

                        ((Activity) context).runOnUiThread(runnable);
                    }
                });
            }
        });

        btnGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPSTracker gpsTracker = new GPSTracker(context);
                Location location = gpsTracker.getLocation();
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();

                BusManager busManager = new BusManager();
                busManager.getBusStops(new Callback() {
                    @Override
                    public void callback(Object object) {
                        JSONArray array = (JSONArray) object;
                        String busStopId = null;
                        try {
                            if (array.length() > 0) {
                                JSONObject item = array.getJSONObject(0);

                                Location currentLocation = new Location("");
                                currentLocation.setLatitude(latitude);
                                currentLocation.setLongitude(longitude);

                                Location busStopLocation = new Location("");
                                busStopLocation.setLatitude(item.getDouble("lat"));
                                busStopLocation.setLongitude(item.getDouble("lng"));

                                float shortestDistance = currentLocation.distanceTo(busStopLocation);
                                int shortestDistanceIndex = 0;

                                for (int i = 0; i < array.length(); i++) {
                                    item = array.getJSONObject(i);

                                    busStopLocation = new Location("");
                                    busStopLocation.setLatitude(item.getDouble("lat"));
                                    busStopLocation.setLongitude(item.getDouble("lng"));

                                    float distance = currentLocation.distanceTo(busStopLocation);
                                    if(distance < shortestDistance){
                                        shortestDistance = distance;
                                        shortestDistanceIndex = i;
                                    }
                                }

                                JSONObject nearestBusStop = array.getJSONObject(shortestDistanceIndex);
                                busStopId = nearestBusStop.getString("no");
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                        }

                        final String finalBusStopId = busStopId;

                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if(finalBusStopId != null){
                                    etSource.setText(finalBusStopId);
                                }
                            }
                        };

                        ((Activity) context).runOnUiThread(runnable);
                    }
                });
            }
        });

        lvBuses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String busService = availableBuses.get(i);

                Intent intent = new Intent();
                intent.setClass(context, BusRouteActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("source", etSource.getText().toString());
                bundle.putString("destination", etDestination.getText().toString());
                bundle.putString("busService", busService);
                Log.d("CLICK", busService + etSource.getText() + etDestination.getText());

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    public void loadList(List<String> buses){
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, buses);
        lvBuses.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
