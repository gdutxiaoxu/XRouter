package com.xj.xrounterdemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.xj.xrouter.annotation.Router;
import com.xj.xrouter.api.Callback;
import com.xj.xrouter.api.XRouter;

import androidx.appcompat.app.AppCompatActivity;

@Router(path = "activity/one")
public class OneActivity extends AppCompatActivity {

    private static final String TAG = "OneActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);
    }

    public void onButtonClick(View view) {
        XRouter.getInstance().build("activity/two").navigation(this, new Callback() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                Log.i(TAG, "onActivityResult: requestCode=" + requestCode + ";resultCode=" + resultCode + ";data=" + data);
            }
        });
    }
}
