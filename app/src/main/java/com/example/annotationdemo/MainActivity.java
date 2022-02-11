package com.example.annotationdemo;

import android.os.Bundle;
import android.view.View;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_api.RouterManager;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.skip_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterManager.getInstance()
                        .build("/app/MyActivity")
                        .withString("name", "XaoMing")
                        .withInt("age", 18)
                        .navigation(MainActivity.this);
            }
        });
    }
}