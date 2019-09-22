package com.chrishi.miaosha.controller;

import com.chrishi.miaosha.domain.MiaoshaUser;
import com.chrishi.miaosha.domain.User;
import com.chrishi.miaosha.redis.GoodsKey;
import com.chrishi.miaosha.redis.RedisService;
import com.chrishi.miaosha.result.Result;
import com.chrishi.miaosha.service.GoodsService;
import com.chrishi.miaosha.service.MiaoshaUserService;
import com.chrishi.miaosha.vo.GoodsDetailVo;
import com.chrishi.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.context.webflux.SpringWebFluxContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@RequestMapping("/goods")
@Controller
public class GoodsController {

    @Autowired
    MiaoshaUserService userService;
    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toLogin(Model model,
                          HttpServletResponse response,HttpServletRequest request,
//                          @CookieValue(value = MiaoshaUserService.COOKIE_NAME_TOKEN,required = false)String cookieToken,
//                          @RequestParam(value=MiaoshaUserService.COOKIE_NAME_TOKEN,required = false)String paramToken,
                          MiaoshaUser user){
//        if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
//            return "login";
//        }
//        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
//        MiaoshaUser user = userService.getByToken(response,token);
        model.addAttribute("user",user);
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);
//        return "goods_list";
        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList,"",String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        WebContext webContext = new WebContext(request,response,
                request.getServletContext(),request.getLocale(),
                model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",webContext);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    @RequestMapping(value="/to_detail/{goodsId}",produces = "text/html")
    @ResponseBody
    public String toDetail(Model model, MiaoshaUser user,
                           @PathVariable("goodsId")long goodsId,
                           HttpServletRequest request,HttpServletResponse response){
        model.addAttribute("user",user);
        GoodsVo goodsVo = goodsService.getGoodsVoById(goodsId);
        model.addAttribute("goods",goodsVo);
        String html = redisService.get(GoodsKey.getGetGoodsDetail,""+goodsId,String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
        long startAt = goodsVo.getStartDate().getTime();
        long endAt = goodsVo.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSecond = 0;
        if(now <startAt){
            miaoshaStatus = 0;
            remainSecond = (int)(startAt - now)/1000;
        }else if(now > endAt){
            miaoshaStatus = 2;
            remainSecond = -1;
        }else{
            miaoshaStatus = 1;
            remainSecond = 0;
        }
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSecond);
//        return "goods_detail";
        WebContext webContext = new WebContext(request,response,
                request.getServletContext(),request.getLocale(),
                model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",webContext);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGetGoodsDetail,""+goodsId,html);
        }
        return html;
    }

    @RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletResponse response,HttpServletRequest request,Model model,MiaoshaUser user,
                                        @PathVariable("goodsId")long goodsId){
        GoodsVo goods = goodsService.getGoodsVoById(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSecond = 0;
        if(now <startAt){
            miaoshaStatus = 0;
            remainSecond = (int)(startAt - now)/1000;
        }else if(now > endAt){
            miaoshaStatus = 2;
            remainSecond = -1;
        }else{
            miaoshaStatus = 1;
            remainSecond = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSecond);
        vo.setMiaoshaStatus(miaoshaStatus);
        return Result.success(vo);
    }
}
