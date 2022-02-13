package com.example.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.Parameter;
import com.example.arouter_api.ParameterManager;

@ARouter(path = "/personal/PersonalActivity")
public class PersonalActivity extends AppCompatActivity {
    @Parameter
    String name = " ";

    @Parameter
    int age = 18;

    TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
        ParameterManager.getInstance().loadParameter(this);

        mTextView = findViewById(R.id.per_tv);
        mTextView.setText(name + " ==== " + age);
    }
}













