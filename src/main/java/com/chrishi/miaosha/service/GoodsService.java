package com.chrishi.miaosha.service;

import com.chrishi.miaosha.dao.GoodsDao;
import com.chrishi.miaosha.domain.Goods;
import com.chrishi.miaosha.domain.MiaoshaGoods;
import com.chrishi.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoods();
    }

    public GoodsVo getGoodsVoById(long goodsId) {
        return goodsDao.getGoodsVoById(goodsId);
    }

    public boolean reduceStock(GoodsVo goodsVo) {
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goodsVo.getId());
        g.setStockCount(goodsVo.getStockCount()-1);
        int ret = goodsDao.reduceStock(g);
        return ret>0;
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }
}
