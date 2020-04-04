package com.limit.user.controller;

import com.limit.common.result.CodeMsg;
import com.limit.common.result.Result;
import com.limit.user.service.SeckillUserService;
import com.limit.user.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    SeckillUserService seckillUserService;

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    /**
     * 进入登录页面
     * @return 登录页面
     */
    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    /**
     * 执行登录验证
     * @param response 相应数据
     * @param loginVo 登录信息
     * @return 是否登陆成功
     */
    @RequestMapping("/do_login")
    @ResponseBody
    public Result<Boolean> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
        //登录
        CodeMsg cm = seckillUserService.login(response, loginVo);
        if (cm.getCode() == 0) {
            return Result.success(true);
        } else {
            return Result.error(cm);
        }
    }
}
