package com.changgou;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.changgou.utils.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author Ayden
 * @Date 2019/9/5 22:45
 * @Description
 * @Version
 **/
@SpringBootApplication
@EnableEurekaClient
//@EnableFeignClients
@MapperScan(basePackages = "com.changgou.seckill.dao")
@EnableScheduling
@EnableAsync    //开启多线程支持
public class SeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }

    @Bean
    public IdWorker createIdwork() {
        return new IdWorker(1, 1);
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new FastJsonRedisSerializer<>(String.class));
        //template.setHashValueSerializer(new FastJsonRedisSerializer<>(String.class));
        return template;
    }


}
