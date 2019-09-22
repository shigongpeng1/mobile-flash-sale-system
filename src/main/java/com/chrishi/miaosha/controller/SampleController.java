package com.chrishi.miaosha.controller;

import com.chrishi.miaosha.domain.User;
import com.chrishi.miaosha.rabbitmq.MQReceiver;
import com.chrishi.miaosha.rabbitmq.MQSender;
import com.chrishi.miaosha.redis.RedisService;
import com.chrishi.miaosha.redis.UserKey;
import com.chrishi.miaosha.result.Result;
import com.chrishi.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MQSender mqSender;
    @Autowired
    private MQReceiver mqReceiver;


    @RequestMapping("/thymeleaf")
    public  String thymeleaf(Model model){
        model.addAttribute("name","S.");
        return "hello";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public String getDb(){
        User user = userService.getById(1);
        return user.getName();
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<Long> redisGet(){
        Long v1 = redisService.get(UserKey.getById,"key1",Long.class);
        return Result.success(v1);
    }

    @RequestMapping("redis/set")
    @ResponseBody
    public Result<User> redisSet(){
        User user = new User();
        user.setId(1);
        user.setName("hello dear");
        boolean bool = redisService.set(UserKey.getById,"k3",user);
        User user1= redisService.get(UserKey.getById,"k3",User.class);
        return Result.success(user1);
    }


    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mq(){
        mqSender.send("hello,the one i don't know");
        return Result.success("hi");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topic(){
        mqSender.sendTopic("hello,the one i don't know");
        return Result.success("hi");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> fanout(){
        mqSender.sendFanout("hello,the one i don't know");
        return Result.success("hi");
    }

    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> header(){
        mqSender.sendHeader("hello,the one i don't know");
        return Result.success("hi");
    }
}
