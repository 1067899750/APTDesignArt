package com.example.annotationdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.arouter_annotation.ARouter;
import com.example.arouter_annotation.BindActivity;

import com.example.annotation.BindId;
import com.example.arouter_annotation.bean.RouterBean;
import com.example.arouter_api.ARouterPath;
import com.example.new_modular_customarouter.ARouter$$Group$$app;
import com.example.new_modular_customarouter.ARouter$$Path$$my;
import com.pu.dataBinding.FindViewUtil;

import java.util.Map;

@ARouter(path = "/main/MainActivity")
public class MainActivity extends BaseActivity {
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
                ARouter$$Group$$app group$$personal = new ARouter$$Group$$app();
                Map<String, Class<? extends ARouterPath>> groupMap = group$$personal.getGroupMap();
                Class<? extends ARouterPath> myClass = groupMap.get("my");

                try {
                    ARouter$$Path$$my path = (ARouter$$Path$$my) myClass.newInstance();
                    Map<String, RouterBean> pathMap = path.getPathMap();
                    RouterBean bean = pathMap.get("/my/MyActivity");

                    if (bean != null) {
                        Intent intent = new Intent(MainActivity.this, bean.getMyClass());
                        startActivity(intent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}