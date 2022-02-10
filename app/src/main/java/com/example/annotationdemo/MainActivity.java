package com.example.annotationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.arouter_annotation.BindActivity;

import com.example.annotation.BindId;
import com.example.annotation.BindView;
import com.pu.dataBinding.DataBindingUtil;
import com.pu.dataBinding.FindViewUtil;

@BindActivity(value = "MainActivity")
public class MainActivity extends AppCompatActivity {
    @BindId(id = R.id.skip_activity)
    public Button mButton;

    @BindId(id = R.id.tv_name)
    public TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FindViewUtil.init_MainActivity(this);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataBindingUtil.start_my_activity(MainActivity.this, new Bundle());
            }
        });
    }
}