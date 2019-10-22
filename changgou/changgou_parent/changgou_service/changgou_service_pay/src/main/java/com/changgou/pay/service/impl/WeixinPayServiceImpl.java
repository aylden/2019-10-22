package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.changgou.utils.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Ayden
 * @Date 2019/9/4 11:11
 * @Description
 * @Version
 **/
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.partner}")
    private String partner;

    @Value("${weixin.partnerkey}")
    private String partnerkey;

    @Value("${weixin.notifyurl}")
    private String notifyurl;

    @Value("${mq.pay.exchange.seckillorder}")
    private String exchange;

    @Value("${mq.pay.queue.seckillorder}")
    private String queue;

    @Value("${mq.pay.routing.seckillkey}")
    private String seckillkey;


    @Override
    public Map<String, String> generateNative(Map<String, String> parameMap) {

        //1.创建参数对象，组合参数
        Map<String, String> paramMap = new HashMap<String, String>();
        //2.设置参数
        try {
            paramMap.put("appid", appid);
            paramMap.put("mch_id", partner);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("body", "全民购物平台");
            paramMap.put("out_trade_no", parameMap.get("out_trade_no"));
            paramMap.put("total_fee", parameMap.get("total_fee"));
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", notifyurl);
            paramMap.put("trade_type", "NATIVE");

            Map<String, String> attach = new HashMap<>();
            attach.put("username", parameMap.get("username"));
            attach.put("queue", queue);
            attach.put("routingkey", seckillkey);
            attach.put("exchange", exchange);
            parameMap.put("attach", JSON.toJSONString(attach));

            System.out.println("参数信息：" + parameMap);

            //3.转换成xml字符串（同时自动设置签名）
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            //4.创建httpclient对象
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //5.设置http协议
            httpClient.setHttps(true);
            //6.设置请求体
            httpClient.setXmlParam(xmlParam);
            //7.发送请求
            httpClient.post();
            //8.获取微信支付系统返回的响应结果(xml格式)
            String content = httpClient.getContent();
            //9.转换成map，返回
            Map<String, String> allMap = WXPayUtil.xmlToMap(content);
            Map<String, String> resultMap = new HashMap<>();

            resultMap.put("out_trade_no", parameMap.get("out_trade_no"));
            resultMap.put("total_fee", parameMap.get("total_fee"));
            resultMap.put("code_url", allMap.get("code_url"));
            resultMap.put("err_code_des", allMap.get("err_code_des"));
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, String> payStatusQuery(String out_trade_no) {

        //1.创建参数对象，组合参数
        Map<String, String> paramMap = new HashMap<String, String>();
        //2.设置参数
        try {
            paramMap.put("appid", appid);
            paramMap.put("mch_id", partner);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("out_trade_no", out_trade_no);
            //3.转换成xml字符串（同时自动设置签名）
            String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            //4.创建httpclient对象
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            //5.设置http协议
            httpClient.setHttps(true);
            //6.设置请求体
            httpClient.setXmlParam(xmlParam);
            //7.发送请求
            httpClient.post();
            //8.获取微信支付系统返回的响应结果(xml格式)
            String content = httpClient.getContent();
            //9.转换成map，返回
            Map resultMap = WXPayUtil.xmlToMap(content);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
