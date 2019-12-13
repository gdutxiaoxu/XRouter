package com.xj.xrouter.api;

import java.util.Map;


public interface IRouterMap {

    void handleMap(Map<String, Class<?>> routers);

}
