package com.example.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.Parameter;
import com.example.arouter_api.ParameterManager;
import com.example.common.bean.Student;
import com.example.common.order.drawable.OrderDrawable;
import com.example.common.order.net.OrderAddress;
import com.example.common.order.net.OrderBean;

import java.io.IOException;

@ARouter(path = "/personal/PersonalActivity")
public class PersonalActivity extends AppCompatActivity {
    @Parameter
    String name = " ";

    @Parameter
    int age = 18;


    @Parameter
    Student student;

    TextView mTextView;

    @Parameter(name = "/order/getDrawable")
    OrderDrawable orderDrawable; // 公共基础库common

    // 拿order模块的 网络请求功能
    @Parameter(name = "/order/getOrderBean")
    OrderAddress orderAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        ParameterManager.getInstance().loadParameter(this);

        mTextView = findViewById(R.id.per_tv);
        mTextView.setText(name + " ==== " + age);


        // app模块本来就可以直接加载其他模块的资源   personal
        // 拿到 order模块的图片 在app模块展示
        int drawableId = orderDrawable.getDrawable();
        ImageView img = findViewById(R.id.img);
        img.setImageResource(drawableId);


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OrderBean orderBean = orderAddress.getOrderBean("aa205eeb45aa76c6afe3c52151b52160", "144.34.161.97");
                    Log.e("-----> ：", "从Personal跨组件到Order，并使用Order网络请求功能：" + orderBean.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // 输出 Student
        Log.d("-----> ：", "我的Personal onCreate 对象的传递:" +  student.toString());
    }
}













