package com.example.annotationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.BindView;
import com.example.arouter_annotation.Parameter;
import com.example.arouter_api.BindViewManager;
import com.example.arouter_api.ParameterManager;

@ARouter(path = "/app/MyActivity")
public class MyActivity extends BaseActivity {

    @Parameter
    String name = " ";

    @Parameter
    int age = 18;

    @BindView(R.id.tv)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        ParameterManager.getInstance().loadParameter(this);
        BindViewManager.getInstance().bindView(this);

        mTextView.setText(name + " <=====> " + age);

    }
}