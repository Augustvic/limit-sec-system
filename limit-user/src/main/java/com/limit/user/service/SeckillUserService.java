package com.limit.user.service;

import com.limit.common.exception.GlobalException;
import com.limit.common.result.CodeMsg;
import com.limit.common.utils.MD5Util;
import com.limit.common.utils.UUIDUtil;
import com.limit.redis.service.RedisService;
import com.limit.redis.user.SeckillUserKey;
import com.limit.user.dao.SeckillUserDao;
import com.limit.user.entity.SeckillUser;
import com.limit.user.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillUserService {

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    SeckillUserDao seckillUserDao;

    @Autowired
    RedisService redisService;

    /**
     * 根据 id 获取用户信息
     * @param id
     * @return SeckillUser
     */
    public SeckillUser getById(long id) {
        // 取缓存
        SeckillUser user = redisService.hmcget(SeckillUserKey.getById, "" + id, SeckillUser.class);
        if (user != null) {
            return user;
        }
        // 取数据库
        user = seckillUserDao.getById(id);
        if (user != null) {
            redisService.hmcset(SeckillUserKey.getById, "" + id, user);
        }
        return user;
    }

    /**
     * 修改密码
     * @param token 用户 token
     * @param id 用户 id
     * @param formPass 生成密码
     * @return 修改成功返回 true
     */
    public boolean updatePassword(String token, long id, String formPass) {
        // 取user对象
        SeckillUser user = getById(id);
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        SeckillUser toBeUpdate = new SeckillUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        // 修改缓存
        user.setPassword(toBeUpdate.getPassword());
        redisService.hmcset(SeckillUserKey.token, token, user);
        redisService.hmcset(SeckillUserKey.getById, "" + id, user);
        // 更新数据库
        seckillUserDao.update(toBeUpdate);
        return true;
    }

    /**
     * 登录验证
     * @param response 返回
     * @param loginVo 登录信息
     * @return 是否成功
     */
    public CodeMsg login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            return CodeMsg.SERVER_ERROR;
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        SeckillUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            return CodeMsg.MOBILE_NOT_EXIST;
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if (!dbPass.equals(calcPass)) {
            return CodeMsg.PASSWORD_ERROR;
        }
        //登陆成功生成cookie
        String token = UUIDUtil.uuid();
        redisService.hmcset(SeckillUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        cookie.setPath("/");
        addCookie(response, token, user);
        return CodeMsg.SUCCESS;
    }

    /**
     * 根据 token 获取 SeckillUser
     * @param response 相应数据
     * @param token token
     * @return 获取到的用户信息
     */
    public SeckillUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        SeckillUser user = redisService.hmcget(SeckillUserKey.token, token, SeckillUser.class);
        //延长有效期
        if (user != null){
            addCookie(response, token, user);
        }
        return user;
    }

    /**
     * 延长 cookie 有效期，重新设置过期时间
     * @param response 相应数据
     * @param token token
     * @param user 用户
     */
    private void addCookie(HttpServletResponse response, String token, SeckillUser user) {
        redisService.hmcset(SeckillUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
