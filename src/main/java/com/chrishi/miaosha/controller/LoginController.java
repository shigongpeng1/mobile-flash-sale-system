package com.chrishi.miaosha.controller;

import ch.qos.logback.core.pattern.util.RegularEscapeUtil;
import com.chrishi.miaosha.result.CodeMsg;
import com.chrishi.miaosha.result.Result;
import com.chrishi.miaosha.service.MiaoshaUserService;
import com.chrishi.miaosha.util.ValidatorUtil;
import com.chrishi.miaosha.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MiaoshaUserService miaoshaUserService;

    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo){
        log.info(loginVo.toString());
        String passInput = loginVo.getPassword();
        String mobile = loginVo.getMobile();
//        if(StringUtils.isEmpty(passInput)){
//            return Result.error(CodeMsg.PASSWORD_EMPTY);
//        }
//        if(StringUtils.isEmpty(mobile)){
//            return Result.error(CodeMsg.MOBILE_EMPTY);
//        }
//        if(!ValidatorUtil.isMobile(mobile)){
//            return Result.error(CodeMsg.MOBILE_ERROR);
//        }
//        CodeMsg cm = miaoshaUserService.login(loginVo);
//        if(cm.getCode() == 0){
//            return Result.success(true);
//        }else{
//            return Result.error(cm);
//        }
//        CodeMsg cm = user
        boolean bool = miaoshaUserService.login(response,loginVo);
        return Result.success(true);
    }
}
