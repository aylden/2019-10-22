package com.changgou.search.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.search.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/search")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;

    @RequestMapping("/import")
    public Result importSkuInfo() {
        skuInfoService.importSkuInfo();
        return new Result(true, StatusCode.OK, "导入成功");
    }

   /* @PostMapping
    public Result<Map> search(@RequestBody Map<String, String> searchMap) {
        Map map = skuInfoService.search(searchMap);
        return new Result<Map>(true, StatusCode.OK, "搜索成功", map);
    }*/

    @GetMapping
    public Map searchSkus(@RequestParam(required = false) Map<String, String> searchMap) {
        Map map = skuInfoService.search(searchMap);
        return map;
    }

}
