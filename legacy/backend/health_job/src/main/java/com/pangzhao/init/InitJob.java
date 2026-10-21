package com.pangzhao.init;

import com.pangzhao.util.QiniuUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 *
 */
@Component
public class InitJob {

    @Autowired
    private RedisTemplate redisTemplate;

    //初始化容器时 执行该方法 即每隔一分钟 清理一下远程的服务器
//    @Scheduled(cron = "0/30 * * * * ?")
    public void cleanImg() {
        System.out.println("定时任务开始了");
        //比较两个集合的缓存 得到无效的图片
        Set<String> imgSet = redisTemplate.opsForSet().difference("UPLOAD_IMG", "ADD_IMG");

        for (String img : imgSet) {
            //删除云端的图片
            QiniuUtils.deleteFileFromQiniu(img);
            System.out.println("删除了无效的图片");
            //删除缓存的图片
            redisTemplate.boundSetOps("UPLOAD_IMG").remove(img);
        }
    }
}
