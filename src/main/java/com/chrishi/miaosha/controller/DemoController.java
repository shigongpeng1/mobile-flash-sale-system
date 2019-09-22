package com.chrishi.miaosha.controller;

import com.chrishi.miaosha.result.CodeMsg;
import com.chrishi.miaosha.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class DemoController {

    @RequestMapping("/")
    @ResponseBody
    String home(){
        return "hello World!";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String>  hello(){
        return Result.success("hello chrishi");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String>  helloError(){
//        return new Result(500100,"faild","helle,Chrishi");
        return Result.error(CodeMsg.SERVER_ERROR);
    }
}
