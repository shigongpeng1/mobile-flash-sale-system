package com.chrishi.miaosha.controller;

import com.chrishi.miaosha.access.AccessLimit;
import com.chrishi.miaosha.domain.MiaoshaOrder;
import com.chrishi.miaosha.domain.MiaoshaUser;
import com.chrishi.miaosha.domain.OrderInfo;
import com.chrishi.miaosha.rabbitmq.MQSender;
import com.chrishi.miaosha.rabbitmq.MiaoshaMessage;
import com.chrishi.miaosha.redis.AccessKey;
import com.chrishi.miaosha.redis.GoodsKey;
import com.chrishi.miaosha.redis.MiaoshaKey;
import com.chrishi.miaosha.redis.RedisService;
import com.chrishi.miaosha.result.CodeMsg;
import com.chrishi.miaosha.result.Result;
import com.chrishi.miaosha.service.GoodsService;
import com.chrishi.miaosha.service.MiaoshaService;
import com.chrishi.miaosha.service.MiaoshaUserService;
import com.chrishi.miaosha.service.OrderService;
import com.chrishi.miaosha.util.MD5Util;
import com.chrishi.miaosha.util.UUIdUtil;
import com.chrishi.miaosha.vo.GoodsVo;
import com.sun.org.apache.bcel.internal.classfile.Code;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/miaosha")
@Controller
public class MiaoshaController implements InitializingBean{

    @Autowired
    MiaoshaUserService userService;
    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;
    @Autowired
    MQSender mqSender;

    private Map<Long,Boolean> localOverMap = new HashMap<Long,Boolean>();

    /**
     * 系统初始化
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if(goodsList == null){
            return;
        }
        for(GoodsVo goods:goodsList){
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goods.getId(),goods.getStockCount());
            localOverMap.put(goods.getId(),false);
        }
    }

    @RequestMapping("/do_miaosha")
    public String toLogin(Model model, MiaoshaUser user,
                          @RequestParam("goodsId")long goodsId){
        if(user == null){
            return "login";
        }
        model.addAttribute("user",user);
        GoodsVo goodsVo = goodsService.getGoodsVoById(goodsId);
        int stock = goodsVo.getStockCount();
        if(stock <= 0){
            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return "miaosha_fail";
        }
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order != null ){
            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
            return "miaosha_fail";
        }
        OrderInfo orderInfo = miaoshaService.miaosha(user,goodsVo);
        model.addAttribute("orderInfo",orderInfo);
        model.addAttribute("goods",goodsVo);
        return "order_detail";
    }

    @RequestMapping(value="/{path}/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
                       @RequestParam("goodsId")long goodsId,
                       @PathVariable("path")String path){
        model.addAttribute("user",user);
        if(user == null){
            return Result.error(CodeMsg.SERVER_ERROR);
        }
        //验证path
        boolean bool = miaoshaService.checkPath(user,goodsId,path);
        if(!bool){
           return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        boolean over = localOverMap.get(goodsId);
        if(over){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);
        if(stock < 0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order != null){
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setGoodsId(goodsId);
        mm.setUser(user);
        mqSender.sendMiaoshaMessage(mm);
        return Result.success(0);//排队中
        /*
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <=0){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order != null){
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
        return Result.success(orderInfo);
        */
    }

    /**
     * orderId:成功
     * 1：秒杀失败
     * 0：排队中
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model,MiaoshaUser user,
                                         @RequestParam("goodsId")long goodsId){
        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(),goodsId);
        return Result.success(result);
    }

    @AccessLimit(seconds = 5,maxCount = 5,needLogin = true)
    @RequestMapping(value = "path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> path(MiaoshaUser user,HttpServletRequest request,
                                @RequestParam("goodsId")long goodsId,
                               @RequestParam(value = "verifyCode",defaultValue = "0",required = false)int verifyCode){
        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //查询访问次数
        String uri = request.getRequestURI();
        String key = uri+"_"+user.getId();
//        Integer count = redisService.get(AccessKey.access,key,Integer.class);
//        if(count == null){
//            redisService.set(AccessKey.access,key,1);
//        }else if(count<5){
//            redisService.incr(AccessKey.access,key);
//        }else{
//            return Result.error(CodeMsg.ACCESS_LIMIT_REACHED);
//        }

        boolean check = miaoshaService.checkVerifyCode(user,goodsId,verifyCode);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = miaoshaService.createMiaoshaPath(user,goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "verifyCode",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaverifyCode(HttpServletResponse response, MiaoshaUser user,
                                               @RequestParam("goodsId")long goodsId){
        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image = miaoshaService.createVerifyCode(user,goodsId);
        try{
            OutputStream out = response.getOutputStream();
            ImageIO.write(image,"JPEG",out);
            out.flush();
            out.close();
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }


}


























