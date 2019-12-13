package com.xj.xrounterdemo;

import android.app.Application;
import android.content.Context;


/**
 * Created by jun xu on 2019-11-12.
 */

public class RouterApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        XRouter.getInstance().init();
    }
}
