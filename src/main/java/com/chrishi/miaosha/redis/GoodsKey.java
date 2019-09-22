package com.chrishi.miaosha.redis;

public class GoodsKey extends BasePrefix{
    public GoodsKey(String prefix) {
        super(prefix);
    }

    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static GoodsKey getGoodsList = new GoodsKey(60,"gl");

    public static GoodsKey getGetGoodsDetail = new GoodsKey(60,"gd");

    public static GoodsKey getMiaoshaGoodsStock = new GoodsKey(0,"gs");
}
