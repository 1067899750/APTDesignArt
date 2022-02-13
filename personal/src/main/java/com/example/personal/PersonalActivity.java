package com.example.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.Parameter;

@ARouter(path = "/personal/PersonalActivity")
public class PersonalActivity extends AppCompatActivity {
    @Parameter
    String name = " ";

    @Parameter
    int age = 18;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
    }
}