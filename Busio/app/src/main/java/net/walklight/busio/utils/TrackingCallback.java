package net.walklight.busio.utils;

/**
 * Created by yeehuipoh on 8/15/15.
 */
public class TrackingCallback implements Callback {
    private boolean doneTracking;

    public TrackingCallback() {
        doneTracking = false;
    }

    @Override
    public void callback(Object object) {
        doneTracking = true;
    }

    public boolean isDoneTracking() {
        return doneTracking;
    }
}
