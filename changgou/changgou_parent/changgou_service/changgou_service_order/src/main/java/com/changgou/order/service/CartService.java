package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

/**
 * @Author Ayden
 * @Date 2019/9/1 10:58
 * @Description
 * @Version
 **/
public interface CartService {

    void addCart(Long skuId, Integer num, String username);

    List<OrderItem> getCart(String username);
}
