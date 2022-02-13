package com.example.annotationdemo;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.BindView;
import com.example.arouter_annotation.Parameter;
import com.example.arouter_api.BindViewManager;
import com.example.arouter_api.ParameterManager;
import com.example.common.base.BaseActivity;
import com.example.common.order.drawable.OrderDrawable;
import com.example.common.order.user.IUser;

@ARouter(path = "/app/MyActivity")
public class MyActivity extends BaseActivity {

    @Parameter
    String name = " ";

    @Parameter
    int age = 18;

    @BindView(R.id.tv)
    TextView mTextView;

    @BindView(R.id.img)
    ImageView mImageView;


    @Parameter(name = "/order/getDrawable")
    OrderDrawable orderDrawable; // 公共基础库common

    @Parameter(name = "/order/getUserInfo")
    IUser iUser; // 公共基础库common


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        ParameterManager.getInstance().loadParameter(this);
        BindViewManager.getInstance().bindView(this);

        mTextView.setText(name + " <=====> " + age);

        // app模块本来就可以直接加载其他模块的资源   personal
        // 拿到 order模块的图片 在app模块展示
        int drawableId = orderDrawable.getDrawable();
        ImageView img = findViewById(R.id.img);
        img.setImageResource(drawableId);

        // 我输出 order模块的Bean休息
        Log.d("-----> ：", "order的Bean onCreate: " +  iUser.getUserInfo().toString());
    }
}








