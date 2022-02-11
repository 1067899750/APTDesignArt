package com.example.annotationdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.arouter.ARouter$$Group$$app;
import com.example.arouter.ARouter$$Path$$my;
import com.example.arouter_annotation.ARouter;

import com.example.arouter_annotation.bean.RouterBean;
import com.example.arouter_api.ARouterPath;



import java.util.Map;

@ARouter(path = "/main/MainActivity")
public class MainActivity extends BaseActivity {

//    @Parameter
//    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.skip_activity).setOnClickListener(new View.OnClickListener() {
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