package com.changgou.pay.service;

import java.util.Map;

/**
 * @Author Ayden
 * @Date 2019/9/4 11:10
 * @Description
 * @Version
 **/
public interface WeixinPayService {
    Map<String, String> generateNative(Map<String,String> paramMap);

    Map<String, String> payStatusQuery(String out_trade_n);

}
