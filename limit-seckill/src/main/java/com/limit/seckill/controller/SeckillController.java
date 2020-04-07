package com.limit.seckill.controller;

import com.limit.redis.service.RedisService;
import com.limit.seckill.exchange.DefaultFuture;
import com.limit.seckill.exchange.message.Request;
import com.limit.seckill.rocketmq.Sender;
import com.limit.user.access.AccessLimit;
import com.limit.user.entity.SeckillUser;
import com.limit.seckill.rocketmq.SeckillMessage;
import com.limit.common.result.CodeMsg;
import com.limit.common.result.Result;
import com.limit.seckill.service.impl.SeckillServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    SeckillServiceImpl seckillService;

    @Autowired
    Sender sender;

    /**
     * GET/POST 区别
     * GET幂等，从服务端获取数据
     * POST向服务端提交数据
     */
    @RequestMapping(value = "/{path}/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Long> doSeckill(Model model, SeckillUser user,
                                @RequestParam("goodsId") long goodsId,
                                @PathVariable("path") String path) {
        model.addAttribute("user", user);
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        // 验证 path
        boolean check = seckillService.checkPath(user, goodsId, path);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        CodeMsg preReduceResultMsg = CodeMsg.FAIL;
        // 预减库存
        try {
            preReduceResultMsg = seckillService.preReduceInventory(user, goodsId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (preReduceResultMsg != CodeMsg.SUCCESS) {
            return Result.error(preReduceResultMsg);
        }

        Request request = new Request(user, goodsId);
        DefaultFuture future = new DefaultFuture(request.getId(), request, -1L);
        DefaultFuture.FUTURES.put(request.getId(), future);

        try {
            sender.sendSeckillRequest(request);
        } catch (Exception e) {
            future.cancel();
            e.printStackTrace();
            return Result.error(CodeMsg.FAIL);
        }
        return Result.success(request.getId()); // 排队中
    }

    /**
     * orderId: 成功
     * -1：秒杀失败
     * 0：排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> seckillResult(Model model, SeckillUser user,
                                @RequestParam("requestId") long requestId) {
        model.addAttribute("user", user);
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        long result = DefaultFuture.get(requestId);
        return Result.success(result);
    }

    // access拦截器拦截消息
    @AccessLimit(seconds = 5, maxCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath(HttpServletRequest request,
                                         SeckillUser user,
                                         @RequestParam("goodsId") long goodsId,
                                         @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode) {
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        // 验证码验证
        boolean check = seckillService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check)
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        String path = seckillService.createSeckillPath(user, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillVerifyCode(HttpServletResponse response, SeckillUser user,
                                               @RequestParam("goodsId") long goodsId) {
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);
        try {
            BufferedImage image = seckillService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

}
