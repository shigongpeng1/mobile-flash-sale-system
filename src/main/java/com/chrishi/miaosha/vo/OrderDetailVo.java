package com.chrishi.miaosha.vo;

import com.chrishi.miaosha.domain.OrderInfo;

public class OrderDetailVo {

    private GoodsVo goods;

    private OrderInfo orderInf;

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }

    public OrderInfo getOrderInf() {
        return orderInf;
    }

    public void setOrderInf(OrderInfo orderInf) {
        this.orderInf = orderInf;
    }
}
