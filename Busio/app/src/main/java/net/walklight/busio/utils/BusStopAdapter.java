package net.walklight.busio.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.location.Location;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import net.walklight.busio.Constant;
import net.walklight.busio.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yeehuipoh on 8/15/15.
 */
public class BusStopAdapter extends BaseAdapter {
    private List<BusStop> busStopList;
    private Context context;


    public BusStopAdapter(Context context) {
        busStopList = new ArrayList<>();
        this.context = context;
    }

    public void updateBusStops(Location currentLocation, Callback bellPressCallback){

        for(BusStop busStop : busStopList){
            busStop.updateStatus(currentLocation);
        }

        BusStop busStop = busStopList.get(busStopList.size() - 1);
        if(busStop.arriving(currentLocation)){
            bellPressCallback.callback(busStop);
        }
    }

    @Override
    public int getCount() {
        return busStopList.size();
    }

    @Override
    public Object getItem(int i) {
        return busStopList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = layoutInflater.inflate(R.layout.busstop_list_item, null);

        TextView tvName = (TextView) rootView.findViewById(R.id.tv_busstop_list_item_name);
        TextView tvId = (TextView) rootView.findViewById(R.id.tv_busstop_list_item_id);
        ImageView ivStatus = (ImageView) rootView.findViewById(R.id.iv_busstop_list_item_status);

        Resources resources = context.getResources();
        float pixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, resources.getDisplayMetrics());
        Shape oval = new OvalShape();
        oval.resize(pixel, pixel);
        ShapeDrawable shapeDrawable = new ShapeDrawable(oval);


        BusStop busStop = busStopList.get(i);
        tvName.setText(busStop.getName());
        tvId.setText(busStop.getName());

        int status = busStop.getStatus();

        if(status == Constant.AWAY) {
            shapeDrawable.getPaint().setColor(Color.TRANSPARENT);
        }
        else if(status == Constant.APPROACHING) {
            shapeDrawable.getPaint().setColor(Color.YELLOW);
        }
        else if(status == Constant.ARRIVED) {
            shapeDrawable.getPaint().setColor(Color.GREEN);
        }
        else if(status == Constant.PASSED) {
            shapeDrawable.getPaint().setColor(Color.BLUE);
        }

        int sdk = android.os.Build.VERSION.SDK_INT;
        ivStatus.setBackgroundDrawable(shapeDrawable);

        return rootView;
    }

    public void add(BusStop busStop){
        busStopList.add(busStop);
        notifyDataSetChanged();
    }

    public int getCurrentItem(){
        int index = -1;

        for(int i = 0; i < busStopList.size(); i++){
            BusStop busStop = busStopList.get(i);
            if(busStop.getStatus() == Constant.APPROACHING || busStop.getStatus() == Constant.ARRIVED){
                index = i;
                break;
            }
        }

        return index;
    }
}
