package com.chrishi.miaosha.service;

import com.chrishi.miaosha.dao.UserDao;
import com.chrishi.miaosha.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;
    public User getById(int id){
        return userDao.getById(id);
    }
}
