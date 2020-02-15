package com.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.miaosha.entity.MiaoshaUser;
import com.miaosha.redis.AccessKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.util.concurrent.BloomFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    BloomFilter bloomFilter;

    private static final String where = "userAccessLimit";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            // 获取登录用户信息
            MiaoshaUser user = getUser(request, response);
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }

            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    render(response, CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_" + user.getId();
            }

            // 在黑名单里
            if (bloomFilter.isExist(where, String.valueOf(user.getId()))) {
                render(response, CodeMsg.ACCESS_LIMIT_BLACKLIST);
                return false;
            }

            AccessKey ak = AccessKey.withExpire(seconds);
            Integer count = redisService.get(ak, key, Integer.class);
            if (count == null) {
                redisService.set(ak, key, 1);
            }
            else if (count < maxCount) {
                redisService.incr(ak, key);
            }
            else {
                render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                bloomFilter.put(where, String.valueOf(user.getId()));
                return false;
            }
        }
        return true;
    }

    private void render(HttpServletResponse response, CodeMsg cm) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, MiaoshaUserService.COOKIE_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return userService.getByToken(response, token);
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[]  cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0)
            return null;
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookieName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
