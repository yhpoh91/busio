package net.walklight.busio.utils;

import android.util.Log;

import net.walklight.busio.Constant;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yeehuipoh on 8/15/15.
 */
public class BusManager {
    public void getBusStops(final Callback callback){
        WebTool.getHtmlInBackground(Constant.BUSSTOP_API, new ArrayList<NameValuePair>(), "", "", new WebCallback() {
            @Override
            public void run(String response) {
                JSONArray responseJSON = new JSONArray();
                try {
                    responseJSON = new JSONArray(response);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.callback(responseJSON);
            }
        });
    }

    public void getBusRoute(String busStop1, String busStop2, String busService, final Callback callback){
        String url = Constant.BUS_API + busService + "/" + busStop1 + "/" + busStop2;
        WebTool.getHtmlInBackground(url, new ArrayList<NameValuePair>(), "", "", new WebCallback() {
            @Override
            public void run(String response) {
                JSONArray responseJSON = new JSONArray();
                try {
                    responseJSON = new JSONArray(response);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.callback(responseJSON);
            }
        });
    }

    public void getBusListFromBusStops(final String busStop1, final String busStop2, final Callback callback){
        WebTool.getHtmlInBackground(Constant.BUS_BUSSTOP_API, new ArrayList<NameValuePair>(), "", "", new WebCallback() {
            @Override
            public void run(String response) {

                List<String> availableBuses = new ArrayList<String>();
                try {
                    JSONObject responseJSON = new JSONObject(response);
//                    Log.d("BUS", response);
                    if(responseJSON.has(busStop1) && responseJSON.has(busStop2)){
                        JSONArray busStop1Buses = responseJSON.getJSONArray(busStop1);
                        JSONArray busStop2Buses = responseJSON.getJSONArray(busStop2);
                        int index1 = 0;
                        int index2 = 0;

                        while(index1 < busStop1Buses.length() && index2 < busStop2Buses.length()){
                            if(busStop1Buses.getString(index1).equals(busStop2Buses.getString(index2))){
                                availableBuses.add(busStop1Buses.getString(index1));
                                index1++;
                                index2++;
                            }
                            else if(busStop1Buses.getString(index1).compareTo(busStop2Buses.getString(index2)) < 0){
                                index1++;
                            }
                            else{
                                index2++;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.callback(availableBuses);
            }
        });
    }
}
