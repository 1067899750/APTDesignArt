package com.example.order.impl;


import com.example.arouter_annotation.ARouter;
import com.example.common.order.drawable.OrderDrawable;
import com.example.order.R;

// order 自己决定 自己的暴漏
@ARouter(path = "/order/getDrawable")
public class OrderDrawableImpl implements OrderDrawable {
    @Override
    public int getDrawable() {
        return R.drawable.ic_ac_unit_black_24dp;
    }
}
