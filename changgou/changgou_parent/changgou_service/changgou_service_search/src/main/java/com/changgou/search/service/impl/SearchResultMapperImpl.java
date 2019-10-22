package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.search.pojo.SkuInfo;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchResultMapperImpl implements SearchResultMapper {
    public static void main(String[] args) {
        Set<String> s = new HashSet<>();

    }

    @Override
    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
        List<T> content = new ArrayList<T>();
        content.contains("dds");
        if (response.getHits() == null || response.getHits().getTotalHits() <= 0) {
            return new AggregatedPageImpl<>(content);
        }
        for (SearchHit hit : response.getHits()) {
            String source = hit.getSourceAsString();
            SkuInfo skuInfo = JSON.parseObject(source, SkuInfo.class);
            HighlightField name = hit.getHighlightFields().get("name");
            if (name != null) {
                StringBuffer stringBuffer = new StringBuffer();
                for (Text text : name.getFragments()) {
                    String s = text.string();
                    stringBuffer.append(s);
                }
                skuInfo.setName(stringBuffer.toString());
            }
            content.add((T) skuInfo);
        }
        SearchHits hits = response.getHits();
        long totalHits = hits.getTotalHits();
        Aggregations aggregations = response.getAggregations();
        return new AggregatedPageImpl<T>(content, pageable, totalHits, aggregations, response.getScrollId());
    }
}
