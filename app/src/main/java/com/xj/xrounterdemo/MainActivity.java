package com.xj.xrounterdemo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.xj.xrouter.annotation.Router;
import com.xj.xrouter.api.RouterCallback;
import com.xj.xrouter.api.XRouter;

import androidx.appcompat.app.AppCompatActivity;

@Router(path = "activity/main")
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        XRouter.getInstance().add("activity/three", ThreeActivity.class);
    }

    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                XRouter.getInstance().build("activity/one").navigation(this);
                break;
            case R.id.button_my_library:
                XRouter.getInstance().build("my/activity/main", new RouterCallback() {
                    @Override
                    public boolean beforeOpen(Context context, Uri uri) {
                        Log.i(TAG, "beforeOpen: uri=" + uri);
                        return false;
                    }

                    @Override
                    public void afterOpen(Context context, Uri uri) {
                        Log.i(TAG, "afterOpen: uri=" + uri);

                    }

                    @Override
                    public void notFind(Context context, Uri uri) {
                        Log.i(TAG, "notFind: uri=" + uri);

                    }

                    @Override
                    public void error(Context context, Uri uri, Throwable e) {
                        Log.i(TAG, "error: uri=" + uri + ";e=" + e);
                    }
                }).navigation(this);
                break;

            case R.id.btn_3:
                XRouter.getInstance().build("activity/three").navigation(this);
                break;

            default:
                break;
        }

    }
}
