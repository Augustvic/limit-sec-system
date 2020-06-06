package com.limit.user.access;

import com.alibaba.fastjson.JSON;
import com.limit.common.concurrent.bloomfilter.BloomFilter;
import com.limit.common.concurrent.bloomfilter.BloomFilterConfig;
import com.limit.common.concurrent.bloomfilter.BloomFilterFactory;
import com.limit.common.result.CodeMsg;
import com.limit.common.result.Result;
import com.limit.redis.service.RedisService;
import com.limit.redis.key.user.AccessKey;
import com.limit.user.entity.SeckillUser;
import com.limit.user.service.SeckillUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    JedisPool jedisPool;

    @Autowired
    SeckillUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    BloomFilterFactory bloomFilterFactory;

    private BloomFilter bloomFilter;

    private static final String WHERE = "UserAccessLimit";

    @PostConstruct
    public void init() {
        this.bloomFilter = bloomFilterFactory.getBloomFilter(new BloomFilterConfig("AccessInterceptor", jedisPool));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            // 获取登录用户信息
            SeckillUser user = getUser(request, response);
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }

            // 参考 AccessLimit 注释的定义
            // seconds 时间内如果访问次数超过 maxCount 次，将会被加入黑名单
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
            if (bloomFilter.isExist(WHERE, String.valueOf(user.getId()))) {
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
                bloomFilter.put(WHERE, String.valueOf(user.getId()));
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

    private SeckillUser getUser(HttpServletRequest request, HttpServletResponse response) {
        String paramToken = request.getParameter(SeckillUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, SeckillUserService.COOKIE_NAME_TOKEN);
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
