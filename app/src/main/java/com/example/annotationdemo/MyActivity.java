package com.example.annotationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.annotation.BindId;
import com.example.annotation.BindView;
import com.example.annotation.SkipPager;

@BindView(value = "my_activity")
@SkipPager(value = "MyActivity")
public class MyActivity extends AppCompatActivity {
    @BindId(id = R.id.tv)
    public TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
    }
}