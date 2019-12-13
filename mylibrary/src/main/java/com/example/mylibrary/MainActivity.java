package com.example.mylibrary;

import android.os.Bundle;

import com.xj.xrouter.annotation.Module;
import com.xj.xrouter.annotation.Router;

import androidx.appcompat.app.AppCompatActivity;

@Router(path = "my/activity/main")
@Module("moudle1")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);
    }
}
