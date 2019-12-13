package com.xj.xrouter.api;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class XRouter {

    private static final String TAG = "ARouter";

    private static final XRouter instance = new XRouter();

    private static Map<String, Class<?>> routeMap = new HashMap<>();
    private boolean loaded;

    private XRouter() {
    }

    public static XRouter getInstance() {
        return instance;
    }

    public void init() {
        if (loaded) {
            return;
        }
//        RouterInit.init();
        loaded = true;
    }

    public void add(String path, Class<? extends Activity> clz) {
        routeMap.put(path, clz);
        Log.i(TAG, "add: routeMap=" + routeMap);
    }

    public void add(Map<String, Class<? extends Activity>> routeMap) {
        if (routeMap != null) {
            this.routeMap.putAll(routeMap);
        }
    }

    public void add(IRouterMap iRouterMap) {
        if (iRouterMap != null) {
            iRouterMap.handleMap(routeMap);
        }
    }


    public Map<String, Class<?>> getMap() {
        return routeMap;
    }

    public Postcard build(String path) {
        return build(path, null);
    }

    public Postcard build(String path, RouterCallback routerCallback) {
        Class<?> aClass = routeMap.get(path);
        if (aClass == null) {
            return new Postcard(Uri.parse(path), null, routerCallback);
        }
        return new Postcard(Uri.parse(path), aClass.getName(), routerCallback);
    }


}
