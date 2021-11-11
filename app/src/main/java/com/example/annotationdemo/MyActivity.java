package com.example.annotationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.annotation.BindView;
import com.example.annotation.SkipPager;

@BindView(value = "my_activity")
@SkipPager(value = "MyActivity")
public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }
}