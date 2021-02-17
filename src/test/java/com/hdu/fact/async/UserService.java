package com.hdu.fact.async;

import com.hdu.fact.async.annotation.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final static Logger logger = LoggerFactory.getLogger(TeacherService.class);

    @Async
    public User addUser(User user) {

        logger.info("正在添加用户{}", user.getName());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Async
    public User getInt(int num) {

        logger.info("正在添加用户{}", num);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new User();
    }

    @Async
    public int getInteger(int num){
        logger.info("正在添加用户{}", num);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return num;
    }

    @Async
    public void getVoid(int num){
        logger.info("正在添加用户{}", num);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return;
    }

}
