package com.chrishi.miaosha.controller;

import com.chrishi.miaosha.domain.MiaoshaUser;
import com.chrishi.miaosha.domain.OrderInfo;
import com.chrishi.miaosha.redis.GoodsKey;
import com.chrishi.miaosha.redis.RedisService;
import com.chrishi.miaosha.result.CodeMsg;
import com.chrishi.miaosha.result.Result;
import com.chrishi.miaosha.service.GoodsService;
import com.chrishi.miaosha.service.MiaoshaUserService;
import com.chrishi.miaosha.service.OrderService;
import com.chrishi.miaosha.vo.GoodsDetailVo;
import com.chrishi.miaosha.vo.GoodsVo;
import com.chrishi.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/order")
@Controller
public class OrderController {

    @Autowired
    MiaoshaUserService userService;
    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    ApplicationContext applicationContext;

    @RequestMapping(value = "/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, MiaoshaUser user,
                     @RequestParam("orderId")long orderId){
        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order = orderService.getOrderById(orderId);
        if(order ==null){
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setGoods(goods);
        vo.setOrderInf(order);
        return Result.success(vo);
    }

}
