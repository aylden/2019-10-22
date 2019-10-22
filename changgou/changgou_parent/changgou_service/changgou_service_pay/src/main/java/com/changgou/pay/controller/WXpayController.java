package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Ayden
 * @Date 2019/9/4 11:01
 * @Description
 * @Version
 **/
@RestController
@RequestMapping("/weixin")
public class WXpayController {
    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment environment;

    @RequestMapping("/native")
    public Result<Map> generateNative(@RequestParam Map<String, String> paramMap) {
        Map<String, String> resultMap = weixinPayService.generateNative(paramMap);
        return new Result(true, StatusCode.OK, "生成二维码成功", resultMap);
    }

    @RequestMapping("/statusQuery")
    public Result<Map<String, String>> payStatusQuery(String out_trade_no) {
        Map<String, String> resultMap = weixinPayService.payStatusQuery(out_trade_no);
        return new Result<Map<String, String>>(true, StatusCode.OK, "查询订单支付状态成功", resultMap);
    }

    @RequestMapping("/notify")
    public String notifyResult(HttpServletRequest request) {
        ServletInputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            is = request.getInputStream();
            bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }

            byte[] bytes = bos.toByteArray();
            String resultXML = new String(bytes);
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(resultXML);
            String xml = JSON.toJSONString(xmlToMap);

            //动态从attach中获取数据
            String attach = xmlToMap.get("attach");
            Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
            System.out.println(attachMap);

            //rabbitTemplate.convertAndSend(environment.getProperty("mq.pay.exchange.order"), environment.getProperty("mq.pay.routing.key"), xml);
            //发送消息到消息队列，处理微信返回来的支付状态消息
            rabbitTemplate.convertAndSend(attachMap.get("exchange"), attachMap.get("routingkey"), xml);

            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("return_code", "SUCCESS");
            resultMap.put("return_msg", "OK");
            System.out.println("返回成功，处理通知");
            return WXPayUtil.mapToXml(resultMap);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
