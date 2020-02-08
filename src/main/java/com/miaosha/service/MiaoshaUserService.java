package com.miaosha.service;

import com.miaosha.dao.MiaoshaUserDao;
import com.miaosha.entity.MiaoshaUser;
import com.miaosha.exception.GlobalException;
import com.miaosha.redis.MiaoshaUserKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.util.MD5Util;
import com.miaosha.util.UUIDUtil;
import com.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;

    /**
     * 根据 id 获取用户信息
     * @param id
     * @return MiaoshaUser
     */
    public MiaoshaUser getById(long id) {
        // 取缓存
        MiaoshaUser user = redisService.hget(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
        if (user != null) {
            return user;
        }
        // 取数据库
        user = miaoshaUserDao.getById(id);
        if (user != null) {
            redisService.hset(MiaoshaUserKey.getById, "" + id, user);
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
        MiaoshaUser user = getById(id);
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        MiaoshaUser toBeUpdate = new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        // 修改缓存
        user.setPassword(toBeUpdate.getPassword());
        redisService.hset(MiaoshaUserKey.token, token, user);
        redisService.hset(MiaoshaUserKey.getById, "" + id, user);
        // 更新数据库
        miaoshaUserDao.update(toBeUpdate);
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
        MiaoshaUser user = getById(Long.parseLong(mobile));
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
        redisService.hset(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        addCookie(response, token, user);
        return CodeMsg.SUCCESS;
    }

    /**
     * 根据 token 获取 MiaoshaUser
     * @param response 相应数据
     * @param token token
     * @return 获取到的用户信息
     */
    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        MiaoshaUser user = redisService.hget(MiaoshaUserKey.token, token, MiaoshaUser.class);
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
    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisService.hset(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
