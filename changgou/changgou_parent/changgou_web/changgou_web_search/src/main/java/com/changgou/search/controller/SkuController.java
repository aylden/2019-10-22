package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.utils.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/search")
public class SkuController {
    @Autowired
    private SkuFeign skuFeign;

    @GetMapping("/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model) {
        Map resultMap = skuFeign.searchSkus(searchMap);
        model.addAttribute("result", resultMap);
        //将参数返回到前端，进行数据回显
        model.addAttribute("searchMap", searchMap);
        String url = getUrl(searchMap);
        model.addAttribute("url", url);
        Page<SkuInfo> page = new Page<SkuInfo>(Long.valueOf(resultMap.get("total").toString()), Integer.valueOf(resultMap.get("pageNum").toString()), Integer.valueOf(resultMap.get("pageSize").toString()));
        model.addAttribute("page", page);
        return "search";
    }

    private String getUrl(Map<String, String> searchMap) {
        String url = "/search/list";
        if (searchMap != null && searchMap.size() > 0) {
            url += "?";
            for (Map.Entry<String, String> stringEntry : searchMap.entrySet()) {
                String key = stringEntry.getKey();
                if (key.startsWith("sort_") || key.equals("pageNum")) {
                    continue;
                }
                String value = stringEntry.getValue();
                url += "&" + key + "=" + value;
                //url += key + "=" + value + "&";
            }
        }
        //url = url.substring(0, url.lastIndexOf("&"));
        return url;
    }

}
