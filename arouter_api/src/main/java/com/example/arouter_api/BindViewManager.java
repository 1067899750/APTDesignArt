package com.example.arouter_api;

import android.app.Activity;
import android.util.LruCache;

/**
 * 控件的 加载管理器
 */
public class BindViewManager {
    private static BindViewManager instance;

    // LRU缓存 key=类名      value=参数加载接口
    private LruCache<String, BindViewGet> mCache;

    // 为了这个效果：MainActivity + $$Parameter
    private static final String FILE_SUFFIX_NAME = "$$BindView";


    public static BindViewManager getInstance() {
        if (instance == null){
            synchronized (BindViewManager.class){
                if (instance == null){
                    instance = new BindViewManager();
                }
            }
        }
        return instance;
    }

    private BindViewManager() {
        mCache = new LruCache<>(100);
    }


    public void bindView(Activity activity) {
        // className == MainActivity
        String className = activity.getClass().getName();
        BindViewGet bindViewGet = mCache.get(className);// 先从缓存里面拿 如果有  如果没有
        if (null == bindViewGet) { // 缓存里面没东东   提高性能
            // 拼接 如：MainActivity + $$BindView
            // 类加载 MainActivity + $$BindView

            try {
                // 类加载 MainActivity + $$BindView
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                // 用接口 parameterLoad = 接口的实现类 MainActivity
                bindViewGet = (BindViewGet) aClass.newInstance();

                // 保存到缓存
                mCache.put(className, bindViewGet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 最终的执行  会执行我们生成的类
        bindViewGet.bindView(activity);
    }

}








