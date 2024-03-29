package com.changgou.seckill.feign;

import com.changgou.entity.Result;
import com.changgou.seckill.pojo.SeckillGoods;
import com.github.pagehelper.PageInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:
 * @Description:
 * @Date 2019/6/18 13:58
 *****/
@FeignClient(name = "seckill")
@RequestMapping("/seckillGoods")
public interface SeckillGoodsFeign {

    /***
     * SeckillGoods分页条件搜索实现
     * @param seckillGoods
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    Result<PageInfo> findPage(@RequestBody(required = false) SeckillGoods seckillGoods, @PathVariable(value = "page") int page, @PathVariable(value = "size") int size);

    /***
     * SeckillGoods分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    Result<PageInfo> findPage(@PathVariable(value = "page") int page, @PathVariable(value = "size") int size);

    /***
     * 多条件搜索品牌数据
     * @param seckillGoods
     * @return
     */
    @PostMapping(value = "/search")
    Result<List<SeckillGoods>> findList(@RequestBody(required = false) SeckillGoods seckillGoods);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    Result delete(@PathVariable(value = "id") Long id);

    /***
     * 修改SeckillGoods数据
     * @param seckillGoods
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    Result update(@RequestBody SeckillGoods seckillGoods, @PathVariable(value = "id") Long id);

    /***
     * 新增SeckillGoods数据
     * @param seckillGoods
     * @return
     */
    @PostMapping
    Result add(@RequestBody SeckillGoods seckillGoods);

    /***
     * 根据ID查询SeckillGoods数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<SeckillGoods> findById(@PathVariable(value = "id") Long id);

    /***
     * 查询SeckillGoods全部数据
     * @return
     */
    @GetMapping
    Result<List<SeckillGoods>> findAll();
}