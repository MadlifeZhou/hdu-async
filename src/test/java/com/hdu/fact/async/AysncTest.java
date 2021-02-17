package com.hdu.fact.async;

import com.hdu.fact.async.core.AsyncCallable;
import com.hdu.fact.async.core.AsyncFutureCallback;
import com.hdu.fact.async.template.AsyncTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/config/spring.xml"})
public class AysncTest {

    private final static Logger logger = LoggerFactory.getLogger(AysncTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Test
    public void testAsyncAnnotation() {
        long s = System.currentTimeMillis();
        User user1 = userService.addUser(new User(34, "李一"));
        User user2 = userService.addUser(new User(35, "李二"));
        long end = System.currentTimeMillis();
        logger.info("异步任务已执行");
        logger.info("异步任务总共耗时：{}", end - s);
        logger.info("执行结果  任务1：{}  任务2：{}", user1.getName(), user2.getName());
    }

    @Test
    public void testVoid() {
        long s = System.currentTimeMillis();
        userService.getVoid(1);
        userService.getVoid(2);
        long end = System.currentTimeMillis();
        logger.info("异步任务总共耗时：{}", end - s);
    }


    @Test
    public void testAsyncTemplate() {

        AsyncTemplate.submit(new AsyncCallable<User>() {

            @Override
            public User doAsync() {
                return teacherService.addTeacher(new User(12, "李三"));
            }
        }, new AsyncFutureCallback<User>() {
            @Override
            public void onSuccess(User user) {
                logger.info("添加用户成功：{}", user.getName());
            }

            @Override
            public void onFailure(Throwable t) {
                logger.info("添加用户失败：{}", t);
            }
        });
        userService.getInteger(1);
        logger.info("调用结束");
    }

}
