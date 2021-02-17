package com.hdu.fact.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {

    private final static Logger logger = LoggerFactory.getLogger(TeacherService.class);

    public User addTeacher(User user) {
        logger.info("正在添加教师{}", user.getName());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return user;
    }

}
