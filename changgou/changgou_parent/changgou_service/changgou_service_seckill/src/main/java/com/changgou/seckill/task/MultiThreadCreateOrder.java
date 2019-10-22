package com.changgou.seckill.task;

import com.changgou.entity.SystemConstants;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author Ayden
 * @Date 2019/9/6 10:39
 * @Description
 * @Version
 **/
@Component
public class MultiThreadCreateOrder {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    @Async
    public void createOder() {

        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps(SystemConstants.SEC_KILL_USER_QUEUE_KEY).rightPop();
        if (null != seckillStatus) {

            String username = seckillStatus.getUsername();
            String timearea = seckillStatus.getTime();
            Long id = seckillStatus.getGoodsId();

            //根据商品id查询商品数据
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + timearea).get(id);


            // 高并发会导致超卖问题，用redis中的队列来解决
           /* //判断是否有库存
            if (seckillGoods.getStockCount() <= 0 || null == seckillGoods) {
                throw new RuntimeException("已告罄");
            }*/
            Object o = redisTemplate.boundListOps(SystemConstants.SEC_KILL_GOODS_COUNT_LIST + id).rightPop();
            if (o == null) {
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_QUEUE_COUNT).delete(username);
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).delete(username);
                throw new RuntimeException("队列中无商品，卖完了");
            } else {
                //如果有库存，创建预订单，减库存
                //seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
                seckillGoods.setStockCount(redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOOD_COUNT_KEY).increment(id, -1).intValue());
                if (null == seckillGoods || seckillGoods.getStockCount() <= 0) {
                    // 删除数据库的库存
                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                    // 删除redis中的数据
                    redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + timearea).delete(id);
                    throw new RuntimeException("已告罄");
                } else {
                    // redis中的数量减一
                    redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + timearea).put(id, seckillGoods);
                }

                //成功创建订单，生成订单
                SeckillOrder seckillOrder = new SeckillOrder();
                seckillOrder.setId(idWorker.nextId());
                seckillOrder.setSeckillId(id);
                seckillOrder.setCreateTime(new Date());
                seckillOrder.setMoney(seckillGoods.getCostPrice());
                seckillOrder.setUserId(username);
                seckillOrder.setStatus("0");

                seckillStatus.setStatus(2);
                seckillStatus.setMoney(Float.valueOf(seckillGoods.getCostPrice()));
                seckillStatus.setOrderId(seckillOrder.getId());

                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_ORDER_KEY + timearea).put(username, seckillOrder);
                //进入排队中
                redisTemplate.boundListOps(SystemConstants.SEC_KILL_USER_QUEUE_KEY).leftPush(seckillStatus);
                //进入排队标识
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_USER_STATUS_KEY).put(username, seckillStatus);
            }
        }
    }

}
