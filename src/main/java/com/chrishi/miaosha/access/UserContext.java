package com.chrishi.miaosha.access;

import com.chrishi.miaosha.domain.MiaoshaUser;

public class UserContext {

    private static ThreadLocal<MiaoshaUser> userThreadLocal = new ThreadLocal<MiaoshaUser>();
    public static void setUser(MiaoshaUser user){
        userThreadLocal.set(user);
    }

    public static MiaoshaUser getUser(){
        return userThreadLocal.get();
    }
}
