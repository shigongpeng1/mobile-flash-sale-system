package com.chrishi.miaosha.rabbitmq;

import com.chrishi.miaosha.domain.MiaoshaOrder;
import com.chrishi.miaosha.domain.MiaoshaUser;
import com.chrishi.miaosha.redis.RedisService;
import com.chrishi.miaosha.service.GoodsService;
import com.chrishi.miaosha.service.MiaoshaService;
import com.chrishi.miaosha.service.OrderService;
import com.chrishi.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @RabbitListener(queues=MQConfig.QUEUE)
    public void receive(String message){
        log.info("receive message:{}",message);
    }

    @RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message){
        log.info("topic queue1 message:{}",message);
    }

    @RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message){
        log.info("topic queue2 message:{}",message);
    }

    @RabbitListener(queues=MQConfig.HEADERS_QUEUE)
    public void receiveHeader(byte[] message){
        log.info("header queue message:{}",new String(message));
    }

    @RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
    public void receiveMiaosha(String msg){
        log.info("receive message:"+msg);
        MiaoshaMessage mm = RedisService.stringToBean(msg,MiaoshaMessage.class);
        MiaoshaUser user = mm.getUser();
        long goodsId = mm.getGoodsId();

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock<0){
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order != null){
            return;
        }
        //减库存，下订单，写入秒杀订单
        miaoshaService.miaosha(user,goods);
    }
}
