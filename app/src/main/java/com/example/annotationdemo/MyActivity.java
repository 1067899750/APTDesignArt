package com.example.annotationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.Parameter;

@ARouter(path = "/my/MyActivity")
public class MyActivity extends BaseActivity {

    @Parameter
    String name = " ";

    @Parameter
    int age = 18;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

    }
}