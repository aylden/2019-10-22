package com.changgou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.SystemConstants;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @Author Ayden
 * @Date 2019/9/8 22:18
 * @Description
 * @Version
 **/
@Component
@RabbitListener(queues = "queue.seckillorder")
public class SeckillOrderListener {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired(required = false)
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;


    @RabbitHandler
    public void cunsumer(String message) {
        System.out.println(message);

        Map<String, String> mqMap = JSON.parseObject(message, Map.class);
        String return_code = mqMap.get("return_code");
        String result_code = mqMap.get("result_code");
        //通讯成功
        if ("SUCCESS".equalsIgnoreCase(return_code)) {
            //支付成功
            String attach = mqMap.get("attach");

            Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
            String username = attachMap.get("username");
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).get(username);

            if ("success".equalsIgnoreCase(result_code)) {

                SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).get(username);

                seckillOrderMapper.deleteByPrimaryKey(seckillOrder.getId());

                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).delete(username);
                seckillOrder.setStatus("1");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String time_end = mqMap.get("time_end");
                Date payTime = null;
                try {
                    payTime = sdf.parse(time_end);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                seckillOrder.setPayTime(payTime);
                seckillOrder.setTransactionId(mqMap.get("transaction_id"));
                seckillOrderMapper.insertSelective(seckillOrder);
                //删除排队信息
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_QUEUE_COUNT).delete(username);
                //删除状态信息
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).delete(username);
               //System.gc();

            } else {

                //关闭微信订单  判断微信关闭订单的状态(1,已支付:调用方法 更新数据到数据库中.2 调用成功:(关闭订单成功:执行删除订单的业务 ) 3.系统错误: 人工处理.   )

                //5.判断业务状态是否成功  如果 不成功 1.删除预订单 2.恢复库存 3.删除排队标识 4.删除状态信息
                SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).get(username);

                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY).delete(username);


                // 2.恢复库存  压入商品的超卖的问题的队列中
                redisTemplate.boundListOps(SystemConstants.SEC_KILL_GOODS_COUNT_LIST + seckillOrder.getSeckillId()).leftPush(seckillOrder.getSeckillId());


                //2.恢复库存  获取商品的数据 商品的库存+1

                SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + seckillStatus.getTime()).get(seckillOrder.getSeckillId());
                if (seckillGoods == null) {//说明你买的是最后一个商品 在redis中被删除掉了
                    seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
                }


                Long increment = redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOOD_COUNT_KEY).increment(seckillOrder.getSeckillId(), 1);

                seckillGoods.setStockCount(increment.intValue());

                //3 删除 防止重复排队的标识
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_QUEUE_KEY).delete(username);
                //4 删除 排队标识
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).delete(username);

            }
        }

    }

}
