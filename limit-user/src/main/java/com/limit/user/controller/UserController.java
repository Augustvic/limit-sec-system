package com.limit.user.controller;

import com.limit.common.concurrent.bloomfilter.BloomFilter;
import com.limit.common.result.Result;
import com.limit.user.entity.SeckillUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.DecimalFormat;

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/info")
    @ResponseBody
    public Result<SeckillUser> info(Model model, SeckillUser user) {
        return Result.success(user);
    }


    @RequestMapping("/test")
    public String test() {
//        // Code
//        int capacity = 1000;
//        String where = "test";
//        for (int i = 0; i < capacity; i++) {
//            bloomFilter.put(where, String.valueOf(i));
//        }
//        int sum = 0;
//        for (int i = capacity + 2000; i < capacity + 3000; i++) {
//            if (bloomFilter.isExist(where, String.valueOf(i))) {
//                sum ++;
//            }
//        }
//        //0.03
//        DecimalFormat df=new DecimalFormat("0.00");//设置保留位数
//        System.out.println("错判率为:" + df.format((float)sum/10000));
        return "login";
    }
}
