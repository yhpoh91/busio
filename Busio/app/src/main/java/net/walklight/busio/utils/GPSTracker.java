package net.walklight.busio.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by yeehuipoh on 8/15/15.
 */
public class GPSTracker extends Service implements LocationListener {

    private LocationManager locationManager;
    private Context context;
    private Location location;

    public GPSTracker(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
    }

    public Location getLocation(){

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(isGPSEnabled || isNetworkEnabled){
            if(isNetworkEnabled){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 5, this);
//                Log.d("Location", "Network");
                if(locationManager != null){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            if(isGPSEnabled){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 5, this);
//                Log.d("Location", "GPS");
                if(locationManager != null){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        }

        if(locationManager != null) {
            locationManager.removeUpdates(this);
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("GPS", "Location changed");
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Location getCurrentLocation(){
        return location;
    }
}

