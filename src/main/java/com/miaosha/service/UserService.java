package com.miaosha.service;

import com.miaosha.dao.UserDao;
import com.miaosha.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by August on 2019/6/7 22:49
 **/

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public User getById(Integer id) {
        return userDao.getById(id);
    }

    @Transactional
    public boolean tx() {
        User u1 = new User();
        u1.setId(2);
        u1.setName("22222");
        userDao.insert(u1);

        User u2 = new User();
        u2.setId(1);
        u2.setName("111111");
        userDao.insert(u2);

        return true;
    }
}
