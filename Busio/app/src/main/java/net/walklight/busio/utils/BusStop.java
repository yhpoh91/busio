package net.walklight.busio.utils;

import android.location.Location;

import net.walklight.busio.Constant;

/**
 * Created by yeehuipoh on 8/15/15.
 */
public class BusStop {
    private String number;
    private String name;
    private Location location;
    private float distance;
    private int status;

    public BusStop() {
        location = new Location("");
        name = "Bus Stop";
        number = "00000";
        status = Constant.AWAY;
        distance = 0;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(double latitude, double longitude){
        location.setLatitude(latitude);
        location.setLongitude(longitude);
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public float getDistance() {
        return distance;
    }

    public int getStatus() {
        return status;
    }

    public boolean arriving(Location currentLocation){
        boolean isArriving = false;

        distance = location.distanceTo(currentLocation);
        if(distance < Constant.ARRIVING_DISTANCE){
            isArriving = true;
        }

        return isArriving;
    }

    public boolean approaching(Location currentLocation){
        boolean isApproaching = false;

        distance = location.distanceTo(currentLocation);
        if(distance < Constant.APPROACHING_DISTANCE){
            isApproaching = true;
        }

        return isApproaching;
    }

    public void updateStatus(Location currentLocation){
        if(status == Constant.AWAY) {
            // Check for Approaching
            if (approaching(currentLocation)) {
                status = Constant.APPROACHING;
            }
        }
        else if(status == Constant.APPROACHING){
            // Check for getting into arriving distance
            if(arriving(currentLocation)){
                status = Constant.ARRIVED;
            }
            else if(approaching(currentLocation)){
                status = Constant.APPROACHING;
            }
            else{
                // Out of distance
                status = Constant.AWAY;
            }
        }
        else if(status == Constant.ARRIVED){
            // Check for leaving arriving distance
            if(!arriving(currentLocation)){
                status = Constant.PASSED;
            }
        }
    }
}
