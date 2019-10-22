package com.changgou.search.service;

import java.util.Map;

public interface SkuInfoService {
    void importSkuInfo();

    Map search(Map<String, String> searchMap);
}
