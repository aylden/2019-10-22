package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuInfoMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuInfoService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public void importSkuInfo() {
        //调用feign查询sku列表信息
        Result<List<Sku>> skuList = skuFeign.findByStatus("1");
        List<Sku> skus = skuList.getData();
        //调用elasticsearch的API导入到ES中
        //BeanUtils.copyProperties(skus, SkuInfo.class);
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(skus), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfos) {
            Map<String, Object> skuMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(skuMap);
        }
        skuInfoMapper.saveAll(skuInfos);
    }

    @Override
    public Map search(Map<String, String> searchMap) {
        //1.获取到关键字
        String keywords = searchMap.get("keywords");
        //2.判断是否为空 如果 为空 给一个默认 值:华为
        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";
        }
        //3.创建 查询构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //4.设置 查询的条件

        // 4.1 商品分类的列表展示: 按照商品分类的名称来分组
        //terms  指定分组的一个别名
        //field 指定要分组的字段名
        // size 指定查询结果的数量 默认是10个
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategorygroup").field("categoryName").size(50));
        //4.2 按照品牌名称进行分组
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandgroup").field("brandName").size(50));
        //4.2 按照规格名称进行分组
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecgroup").field("spec.keyword").size(50));

        //高亮显示
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("name"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));

        //匹配查询  先分词 再查询  主条件查询
        //参数1 指定要搜索的字段
        //参数2 要搜索的值(先分词 再搜索)
        //nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));
        //多个字段中存在
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords, "name", "brandName", "categoryName"));

        //------------------------过滤查询-----------------------//
        BoolQueryBuilder builder = getBoolQueryBuilder(searchMap);
        //过滤查询
        nativeSearchQueryBuilder.withFilter(builder);

        //---------------------过滤查询-----------------------//

        String pageNum = searchMap.get("pageNum");
        if (pageNum == null) {
            pageNum = "1";
        }
        Integer pageSize = 30;
        nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.valueOf(pageNum) - 1, pageSize));

        for (String key : searchMap.keySet()) {
            if (key.startsWith("sort_")) {
                String[] split = key.split("_");
                if (split[1].equalsIgnoreCase("price")) {
                    //  sort_price.desc
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort("price").order(
                            searchMap.get(key).equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC));
                }
                if (split[1].equalsIgnoreCase("date")) {
                    //  sort_date.desc
                }
            }
        }

        //5.构建查询对象(封装了查询的语法)
        NativeSearchQuery build = nativeSearchQueryBuilder.build();
        //6.执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(build, SkuInfo.class, new SearchResultMapperImpl());

        // 获取聚合结果  获取商品分类的列表数据
        List<String> categoryList = getStrings(skuInfos, "skuCategorygroup");
        //获取品牌的列表数据
        List<String> brandList = getStrings(skuInfos, "skuBrandgroup");
        //获取品牌的列表数据
        Map<String, Set<String>> specMap = getStringSetMap(skuInfos, "skuSpecgroup");

        //7.获取结果  返回map
        Map<String, Object> resultMap = new HashMap<>();
        //商品分类的列表数据

        //当前的页的集合
        List<SkuInfo> content = skuInfos.getContent();
        //总页数
        int totalPages = skuInfos.getTotalPages();
        //总记录数
        long totalElements = skuInfos.getTotalElements();
        resultMap.put("rows", content);
        resultMap.put("total", totalElements);
        resultMap.put("totalPages", totalPages);
        resultMap.put("pageNum", pageNum);
        resultMap.put("pageSize", pageSize);


        resultMap.put("categoryList", categoryList);
        resultMap.put("brandList", brandList);
        resultMap.put("specMap", specMap);
        return resultMap;
    }

    private BoolQueryBuilder getBoolQueryBuilder(Map<String, String> searchMap) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        String category = searchMap.get("category");
        if (!StringUtils.isEmpty(category)) {
            builder.filter(QueryBuilders.termQuery("categoryName", category));
        }
        String brand = searchMap.get("brand");
        if (!StringUtils.isEmpty(brand)) {
            builder.filter(QueryBuilders.termQuery("brandName", brand));
        }

        for (String key : searchMap.keySet()) {
            if (key.startsWith("spec_")) {
                builder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", searchMap.get(key)));
            }
        }
        String price = searchMap.get("price");
        if (!StringUtils.isEmpty(price)) {
            String[] split = price.split("-");

            if (!split[1].equals("*")) {
                builder.filter(QueryBuilders.rangeQuery("price").from(split[0]).to(split[1]));
            } else {
                builder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
            }
        }
        return builder;
    }


    private Map<String, Set<String>> getStringSetMap(AggregatedPage<SkuInfo> skuInfos, String name) {
        StringTerms stringTerms = (StringTerms) skuInfos.getAggregation(name);
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Set<String> specValues = null;
        if (stringTerms != null) {
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                Map<String, String> map = JSON.parseObject(bucket.getKeyAsString(), Map.class);
                for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
                    String key = stringStringEntry.getKey();
                    String value = stringStringEntry.getValue();
                    specValues = specMap.get(key);
                    if (specValues == null) {
                        specValues = new HashSet<>();
                    }
                    specValues.add(value);
                    specMap.put(key, specValues);
                }
            }
        }
        return specMap;
    }

    private List<String> getStrings(AggregatedPage<SkuInfo> skuInfos, String name) {
        StringTerms group = (StringTerms) skuInfos.getAggregation(name);
        List<String> strings = new ArrayList<>();
        if (group != null) {
            for (StringTerms.Bucket bucket : group.getBuckets()) {
                String one = bucket.getKeyAsString();
                strings.add(one);
            }
        }
        return strings;
    }
}
