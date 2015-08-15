package net.walklight.busio;

/**
 * Created by yeehuipoh on 8/15/15.
 */
public class Constant {
    public final static String BUSSTOP_API = "http://tutturu.walklight.net/utils/bus/test/busstops/";
    public final static String BUS_API = "http://tutturu.walklight.net/utils/bus/route_bus_stops/";
    public final static String BUS_BUSSTOP_API = "http://tutturu.walklight.net/utils/bus/test/bus_busstops";

    public final static float ARRIVING_DISTANCE = 50;
    public final static float APPROACHING_DISTANCE = 150;

    public final static int AWAY = 0;
    public final static int APPROACHING = 1;
    public final static int ARRIVED = 2;
    public final static int PASSED = 3;

    public final static int VIRBATION_LENGTH = 1000;
}
