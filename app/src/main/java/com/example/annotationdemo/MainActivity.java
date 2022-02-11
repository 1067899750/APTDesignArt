package com.example.annotationdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.BindView;
import com.example.arouter_api.BindViewManager;
import com.example.arouter_api.RouterManager;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends BaseActivity {

    @BindView(R.id.skip_activity)
    Button skipBtn;

    @BindView(R.id.tv_name)
    TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BindViewManager.getInstance().bindView(this);

        skipBtn.setOnClickListener(new View.OnClickListener() {
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