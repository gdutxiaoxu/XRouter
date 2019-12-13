package com.xj.xrounterdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.xj.xrouter.annotation.Router;

import androidx.appcompat.app.AppCompatActivity;

@Router(path = "activity/two")
public class TwoActivity extends AppCompatActivity {

    public static final String KEY_RESULT = "key_result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
    }

    public void onButtonClick(View view) {
        Intent data = new Intent();
        data.putExtra(KEY_RESULT, "OK");
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
