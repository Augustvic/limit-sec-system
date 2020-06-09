package com.limit.common.limiter.slidingwindow;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.util.LinkedList;

public class testJSON {

    @Test
    public void test() {
        LinkedList<Node> list = new LinkedList<>();
        list.add(new Node(9, 8, 7));
        String s = JSON.toJSONString(new Window(list, 2, 3, 4));
        System.out.println(s);
        Window window = JSON.parseObject(s, Window.class);
        System.out.println("success");
    }
}
