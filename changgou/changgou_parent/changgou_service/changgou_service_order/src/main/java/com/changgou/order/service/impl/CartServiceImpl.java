package com.changgou.order.service.impl;

import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Ayden
 * @Date 2019/9/1 10:59
 * @Description
 * @Version
 **/
@Service
public class CartServiceImpl implements CartService {
    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired(required = false)
    private SpuFeign spuFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @param: [skuId商品的id, num 购买商品的数量, username 用户名]
     * @return: void
     * @date: 2019/9/1 11:04
     * @description
     * @version
     */
    @Override
    public void addCart(Long skuId, Integer num, String username) {

        if (num <= 0) {
            redisTemplate.boundHashOps("Cart_" + username).delete(skuId);
            return;
        }
        Result<Sku> skuResult = skuFeign.findById(skuId);
        Sku sku = skuResult.getData();
        if (sku != null) {
            Spu spu = spuFeign.findById(sku.getSpuId()).getData();

            OrderItem orderItem = new OrderItem();

            orderItem.setCategoryId1(spu.getCategory1Id());
            orderItem.setCategoryId2(spu.getCategory2Id());
            orderItem.setCategoryId3(spu.getCategory3Id());
            orderItem.setSpuId(spu.getId());
            orderItem.setSkuId(skuId);
            orderItem.setName(sku.getName());//商品的名称  sku的名称
            orderItem.setPrice(sku.getPrice());//sku的单价
            orderItem.setNum(num);//购买的数量
            orderItem.setMoney(orderItem.getNum() * orderItem.getPrice());//单价* 数量
            orderItem.setPayMoney(orderItem.getNum() * orderItem.getPrice());//单价* 数量
            orderItem.setImage(sku.getImage());//商品的图片dizhi
            //redis 的存储类型：string hash zset
            //hash： key  field  value
            redisTemplate.boundHashOps("Cart_" + username).put(skuId, orderItem);
        }
    }

    @Override
    public List<OrderItem> getCart(String username) {
        List<OrderItem> values = redisTemplate.boundHashOps("Cart_" + username).values();
        return values;
    }
}
