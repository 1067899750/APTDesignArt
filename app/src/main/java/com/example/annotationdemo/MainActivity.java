package com.example.annotationdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.BindView;
import com.example.arouter_api.BindViewManager;
import com.example.arouter_api.RouterManager;
import com.example.common.base.BaseActivity;
import com.example.common.bean.Student;

@ARouter(path = "/app/MainActivity")
public class MainActivity extends BaseActivity {

    @BindView(R.id.skip_activity)
    Button skipBtn;

    @BindView(R.id.skip_order_activity)
    Button skipOrderBtn;

    @BindView(R.id.skip_personal_activity)
    Button skipPersonalBtn;

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


        skipOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterManager.getInstance()
                        .build("/order/OrderActivity")
                        .withString("name", "XaoMing")
                        .withInt("age", 18)
                        .navigation(MainActivity.this);
            }
        });


        skipPersonalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Student student = new Student("大大", "男", 99);
                RouterManager.getInstance()
                        .build("/personal/PersonalActivity")
                        .withString("name", "XaoMing")
                        .withSerializable("student", student)
                        .withInt("age", 18)
                        .navigation(MainActivity.this);
            }
        });
    }


}