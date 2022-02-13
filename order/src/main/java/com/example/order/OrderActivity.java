package com.example.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.Parameter;
import com.example.arouter_api.ParameterManager;

@ARouter(path = "/order/OrderActivity")
public class OrderActivity extends AppCompatActivity {
    @Parameter
    String name = " ";

    @Parameter
    int age = 18;

    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        ParameterManager.getInstance().loadParameter(this);

        mTextView = findViewById(R.id.order_tv);
        mTextView.setText(name + " ==== " + age);
    }
}







