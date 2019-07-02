package com.miaosha.redis;

/**
 * Created by August on 2019/6/14 16:02
 **/
public class UserKey extends BasePrefix{

    public UserKey(String prefix) {
        super(prefix);
    }

    public static UserKey getById = new UserKey("id");
    public static UserKey getByName = new UserKey("name");
}
