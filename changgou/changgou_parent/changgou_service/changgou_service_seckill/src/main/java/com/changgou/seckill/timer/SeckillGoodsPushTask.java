package com.changgou.seckill.timer;

import com.changgou.entity.SystemConstants;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Author Ayden
 * @Date 2019/9/5 22:59
 * @Description
 * @Version
 **/
@Component
public class SeckillGoodsPushTask {

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //反复被执行的方法 隔5秒钟执行一次
    @Scheduled(cron = "0/5 * * * * ?")
    public void loadGoodsPushRedis() {
        List<Date> dateAreas = DateUtil.getDateMenus();
        for (Date currentHour : dateAreas) {

            String exName = DateUtil.data2str(currentHour, DateUtil.PATTERN_YYYYMMDDHH);
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status", "1");
            criteria.andGreaterThan("stockCount", 0);
            criteria.andGreaterThanOrEqualTo("startTime", currentHour);
            criteria.andLessThan("endTime", DateUtil.addDateHour(currentHour, 2));
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + exName).keys();

            //排除掉redis中已有的商品
            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id", keys);
            }
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);

            //設置有效期
            for (SeckillGoods seckillGood : seckillGoods) {
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOODS_PREFIX + exName).put(seckillGood.getId(), seckillGood);
                redisTemplate.expireAt(SystemConstants.SEC_KILL_GOODS_PREFIX + exName, DateUtil.addDateHour(currentHour, 2));
                for (Integer i = 0; i < seckillGood.getStockCount(); i++) {
                    redisTemplate.boundListOps(SystemConstants.SEC_KILL_GOODS_COUNT_LIST + seckillGood.getId()).leftPush(seckillGood.getId());
                }
                redisTemplate.boundHashOps(SystemConstants.SEC_KILL_GOOD_COUNT_KEY).increment(seckillGood.getId(), seckillGood.getStockCount());
            }
        }
    }

    public static void main(String[] args) {
        /*List<Date> dateAreas = DateUtil.getDateMenus();
        for (Date dateArea : dateAreas) {
            System.out.println(dateArea);
        }*/
        String s =  "abc";
        System.out.println(s.hashCode());
        s=s+"d";
        System.out.println(s.hashCode());
        Set<String> set = new HashSet<>();
        set.add("dd");
        set.add("ss");
        set.add("aa");
        set.add("ew");
        System.out.println(set);
    }
}
