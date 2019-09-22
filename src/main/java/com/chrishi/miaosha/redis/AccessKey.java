package com.chrishi.miaosha.redis;


public class AccessKey extends BasePrefix{
    public AccessKey(String prefix) {
        super(prefix);
    }

    public AccessKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

//    public static AccessKey access = new AccessKey(5,"access");

    public static AccessKey withExpire(int expireSeconds){
        return new AccessKey(expireSeconds,"access");
    }
}
