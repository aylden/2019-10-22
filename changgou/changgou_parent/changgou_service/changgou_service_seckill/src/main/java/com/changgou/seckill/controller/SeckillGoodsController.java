package com.changgou.seckill.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.service.SeckillGoodsService;
import com.changgou.utils.DateUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/****
 * @Author:
 * @Description:
 * @Date 2019/6/14 0:18
 *****/

@RestController
@RequestMapping("/seckillGoods")
@CrossOrigin
public class SeckillGoodsController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /***
     * SeckillGoods分页条件搜索实现
     *
     * @param seckillGoods
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@RequestBody(required = false) SeckillGoods seckillGoods, @PathVariable(value = "page") int page, @PathVariable(value = "size") int size) {
        //调用SeckillGoodsService实现分页条件查询SeckillGoods
        PageInfo<SeckillGoods> pageInfo = seckillGoodsService.findPage(seckillGoods, page, size);
        return new Result(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /***
     * SeckillGoods分页搜索实现
     *
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@PathVariable(value = "page") int page, @PathVariable(value = "size") int size) {
        //调用SeckillGoodsService实现分页查询SeckillGoods
        PageInfo<SeckillGoods> pageInfo = seckillGoodsService.findPage(page, size);
        return new Result<PageInfo>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     *
     * @param seckillGoods
     * @return
     */
    @PostMapping(value = "/search")
    public Result<List<SeckillGoods>> findList(@RequestBody(required = false) SeckillGoods seckillGoods) {
        //调用SeckillGoodsService实现条件查询SeckillGoods
        List<SeckillGoods> list = seckillGoodsService.findList(seckillGoods);
        return new Result<List<SeckillGoods>>(true, StatusCode.OK, "查询成功", list);
    }

    /***
     * 根据ID删除品牌数据
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable(value = "id") Long id) {
        //调用SeckillGoodsService实现根据主键删除
        seckillGoodsService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /***
     * 修改SeckillGoods数据
     *
     * @param seckillGoods
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody SeckillGoods seckillGoods, @PathVariable(value = "id") Long id) {
        //设置主键值
        seckillGoods.setId(id);
        //调用SeckillGoodsService实现修改SeckillGoods
        seckillGoodsService.update(seckillGoods);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    /***
     * 新增SeckillGoods数据
     *
     * @param seckillGoods
     * @return
     */
    @PostMapping
    public Result add(@RequestBody SeckillGoods seckillGoods) {
        //调用SeckillGoodsService实现添加SeckillGoods
        seckillGoodsService.add(seckillGoods);
        return new Result(true, StatusCode.OK, "添加成功");
    }

    /***
     * 根据ID查询SeckillGoods数据
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SeckillGoods> findById(@PathVariable(value = "id") Long id) {
        //调用SeckillGoodsService实现根据主键查询SeckillGoods
        SeckillGoods seckillGoods = seckillGoodsService.findById(id);
        return new Result<SeckillGoods>(true, StatusCode.OK, "查询成功", seckillGoods);
    }

    /***
     * 查询SeckillGoods全部数据
     *
     * @return
     */

    @GetMapping
    public Result<List<SeckillGoods>> findAll() {
        //调用SeckillGoodsService实现查询所有SeckillGoods
        List<SeckillGoods> list = seckillGoodsService.findAll();
        return new Result<List<SeckillGoods>>(true, StatusCode.OK, "查询成功", list);
    }

    /***
     * 查询当前时间的五个区间
     * @return
     */
    @GetMapping("/menus")
    public Result<List<Date>> dateMenus() {
        //查询当前时间段以及当前时间段的后四个时间段，总共五个时间段显示
        List<Date> dateMenus = DateUtil.getDateMenus();
        return new Result<List<Date>>(true, StatusCode.OK, "查询成功", dateMenus);
    }

    @RequestMapping("/list")
    public Result<List<SeckillGoods>> getSeckillGoodsList(String timearea) {
        //查询当前时间段以及当前时间段的后四个时间段，总共五个时间段显示
        List<SeckillGoods> dateMenus = seckillGoodsService.getSeckillGoodsList(timearea);
        return new Result<List<SeckillGoods>>(true, StatusCode.OK, "查询成功", dateMenus);
    }

    @RequestMapping("/one")
    public Result<SeckillGoods> getSeckillGoods(long id, String timearea) {
        //查询当前时间段以及当前时间段的后四个时间段，总共五个时间段显示
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoods(id, timearea);
        return new Result<SeckillGoods>(true, StatusCode.OK, "查询成功", seckillGoods);
    }
}
