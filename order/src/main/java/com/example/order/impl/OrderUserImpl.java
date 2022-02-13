package com.example.order.impl;


import com.example.arouter_annotation.ARouter;
import com.example.common.order.user.BaseUser;
import com.example.common.order.user.IUser;
import com.example.order.model.UserInfo;

/**
 * personal模块实现的内容
 */
@ARouter(path = "/order/getUserInfo")
public class OrderUserImpl implements IUser {

    @Override
    public BaseUser getUserInfo() {
        // 我order模块，具体的Bean，由我自己
        UserInfo userInfo = new UserInfo();
        userInfo.setName("Derry");
        userInfo.setAccount("154325354");
        userInfo.setPassword("1234567890");
        userInfo.setVipLevel(999);
        return userInfo;
    }
}
