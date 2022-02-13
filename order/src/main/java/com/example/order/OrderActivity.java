package com.example.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.Parameter;

@ARouter(path = "/order/OrderActivity")
public class OrderActivity extends AppCompatActivity {
    @Parameter
    String name = " ";

    @Parameter
    int age = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
    }
}