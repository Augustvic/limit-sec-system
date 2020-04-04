package com.limit.seckill.controller;

import com.limit.common.result.Result;
import com.limit.redis.service.RedisService;
import com.limit.seckill.service.GoodsService;
import com.limit.seckill.vo.GoodsDetailVo;
import com.limit.seckill.vo.GoodsVo;
import com.limit.user.entity.SeckillUser;
import com.limit.user.service.SeckillUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
//import org.thymeleaf.spring5.context.SpringWebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    SeckillUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    @RequestMapping(value = "/to_list")
    public String toList(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user) {
        model.addAttribute("user", user);
        //查询商品列表
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList", goodsList);
        return "goods_list";
//        //手动渲染
//        WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
//        //SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
//        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
//        if (!StringUtils.isEmpty(html)) {
//            redisService.set(GoodsKey.getGoodsList, "", html);
//        }
//        return html;
    }

    @RequestMapping(value = "/to_detail/{goodsId}")
    public String toDetail(HttpServletRequest request, HttpServletResponse response, Model model, SeckillUser user,  @PathVariable("goodsId")long goodsId) {
//        //取（页面）缓存
//        String html = redisService.get(GoodsKey.getGoodsDetail, "", String.class);
//        if (!StringUtils.isEmpty(html)) {
//            return html;
//        }

        model.addAttribute("user", user);
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods", goods);

        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        int seckillStatus = 0;
        int remainSeconds = 0;

        if (now < startAt) {
            // 秒杀还没开始，倒计时
            seckillStatus = 0;
            remainSeconds = (int)(startAt - now) / 1000;
        }
        else if(now > endAt) {
            //秒杀已经结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {
            // 秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("seckillStatus", seckillStatus);
        model.addAttribute("remainSeconds", remainSeconds);

        return "goods_detail";

//        //手动渲染
//        WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
//        //SpringWebContext ctx = new SpringWebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
//        String html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
//        if (!StringUtils.isEmpty(html)) {
//            redisService.set(GoodsKey.getGoodsDetail, "", html);
//        }
//        return html;
    }

    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model,SeckillUser user,
                                        @PathVariable("goodsId")long goodsId) {
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int seckillStatus = 0;
        int remainSeconds = 0;
        if(now < startAt ) {//秒杀还没开始，倒计时
            seckillStatus = 0;
            remainSeconds = (int)((startAt - now )/1000);
        }else  if(now > endAt){//秒杀已经结束
            seckillStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setSeckillStatus(seckillStatus);
        return Result.success(vo);
    }
}
