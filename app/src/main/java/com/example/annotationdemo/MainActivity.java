package com.example.annotationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.annotation.BindView;
import com.pu.dataBinding.DataBindingUtil;

@BindView(value = "main_activity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.skip_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBindingUtil.startMyActivity(MainActivity.this, null);
            }
        });
    }
}