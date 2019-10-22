package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.OrderItemService;
import com.changgou.order.service.OrderService;
import com.changgou.user.feign.UserFeign;
import com.changgou.user.pojo.User;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Author Ayden
 * @Date 2019/9/4 17:22
 * @Description
 * @Version
 **/
@Component
@RabbitListener(queues = "${mq.pay.queue.order}")
public class OrderUpdateListener {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserFeign userFeign;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private OrderItemService orderItemService;



    @RabbitHandler
    public void OrderUpdateHandler(String msg) {

        Map<String, String> resultMap = JSON.parseObject(msg, Map.class);
        String out_trade_no = resultMap.get("out_trade_no");
        if (null != resultMap) {
            if (resultMap.get("return_code").equalsIgnoreCase("SUCCESS")) {

                String transaction_id = resultMap.get("transaction_id");
                String time_end = resultMap.get("time_end");
                System.out.println("监听到成功消息");
                orderService.updateOrderStatus(out_trade_no, transaction_id, time_end);
                System.out.println("更新订单状态");
            } else {
                System.out.println("监听到支付失败");
                //支付失败，删除订单，恢复库存
                System.out.println("恢复库存");
                orderService.delete(out_trade_no);
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(out_trade_no);
                List<OrderItem> orderItems = orderItemService.findList(orderItem);
                for (OrderItem item : orderItems) {
                    String orderItemId = item.getId();
                    //恢复数量
                    skuFeign.increaseSku(orderItem);
                    System.out.println("恢复数量");
                    orderItemService.delete(orderItemId);
                    //恢复积分
                    Order order = orderService.findById(out_trade_no);
                    User user = userFeign.findById(order.getUsername()).getData();
                    System.out.println("恢复积分");
                    user.setPoints(user.getPoints() - 10);
                    userFeign.update(user, order.getUsername());
                }
            }
        }
    }
}
