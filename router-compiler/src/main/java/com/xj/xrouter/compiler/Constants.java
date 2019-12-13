package com.xj.xrouter.compiler;

public class Constants {

    public static final String CLASSNAME_APPLICATION = "android.app.Application";
    public static final String CLASSNAME_ACTIVITY = "android.app.Activity";

    public static final String[] FILTER_PREFIX = new String[]{
            "com.android",
            "android",
            "java",
            "javax",
    };

    public static String ROUTE_CLASS_PACKAGE = "com.xj.xrouter.apt";
    public static String ROUTE_INTERFACE_NAME = "com.xj.xrouter.api.IRouterMap";
    public static final String OPTION_MODULE_NAME = "moduleName";
    public static final String IROUTERMAP_HANDLEMAP = "handleMap";
}
