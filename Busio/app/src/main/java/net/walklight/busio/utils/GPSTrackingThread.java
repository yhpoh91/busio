package net.walklight.busio.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.os.Vibrator;

import net.walklight.busio.BusRouteActivity;
import net.walklight.busio.Constant;

/**
 * Created by yeehuipoh on 8/15/15.
 */
public class GPSTrackingThread{
    private boolean cancelTracking = false;
    private Runnable runnable;
    private Thread thread;
    private BusRouteActivity context;

    public GPSTrackingThread(final BusRouteActivity context) {
        this.context = context;
        runnable = new Runnable() {
            @Override
            public void run() {
                TrackingCallback callback = new TrackingCallback();
                Looper.prepare();

                while(!callback.isDoneTracking() && !cancelTracking){

                    context.getAdapter().updateBusStops(context.getLocation(), callback);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            context.getAdapter().notifyDataSetChanged();

                            int position = context.getAdapter().getCurrentItem();
                            if (position >= 0) {
                                context.getListView().smoothScrollToPosition(position);
                            }
                        }
                    });

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                // Notify User
                Runnable notificationRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Vibrate
                        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                        if(vibrator.hasVibrator()){
                            vibrator.vibrate(Constant.VIRBATION_LENGTH);
                        }

                        // Ring
                    }
                };

                if(!cancelTracking) {
                    ((Activity) context).runOnUiThread(notificationRunnable);
                }
            }
        };
    }

    public void startThread(){
        thread = new Thread(runnable);
        thread.start();
    }

    public void stopThread(){
        cancelTracking = true;
    }
}