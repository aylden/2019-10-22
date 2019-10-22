package com.changgou;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @Author Ayden
 * @Date 2019/9/4 10:57
 * @Description
 * @Version
 **/
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableEurekaClient
public class WeixinPayApplication {

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(WeixinPayApplication.class, args);
    }

    @Bean
    public Queue createQueue() {
        return new Queue(environment.getProperty("mq.pay.queue.order"));
    }

    @Bean
    public DirectExchange createExchange() {
        return new DirectExchange(environment.getProperty("mq.pay.exchange.order"));
    }

    @Bean
    public Binding basicBinding() {
        return BindingBuilder.bind(createQueue()).to(createExchange()).with(environment.getProperty("mq.pay.routing.key"));
    }

    //创建秒杀队列
    @Bean(name = "seckillQueue")
    public Queue createSeckillQueue() {
        return new Queue(environment.getProperty("mq.pay.queue.seckillorder"));
    }

    //创建秒杀交换机

    @Bean(name = "seckillExchanage")
    public DirectExchange basicSeckillExchanage() {
        return new DirectExchange(environment.getProperty("mq.pay.exchange.seckillorder"));
    }

    //绑定秒杀

    @Bean(name = "SeckillBinding")
    public Binding basicSeckillBinding() {
        return BindingBuilder.bind(createSeckillQueue()).to(basicSeckillExchanage()).with(environment.getProperty("mq.pay.routing.seckillkey"));
    }
}
