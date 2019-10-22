package com.changgou.entity;

/**
 * 描述
 *
 * @author www.itheima.com
 * @version 1.0
 * @package entity *
 * @since 1.0
 */
public class SystemConstants {
    /**
     * 秒杀商品存储到前缀的KEY
     */
    public static final String SEC_KILL_GOODS_PREFIX = "SeckillGoods_";


    /**
     * 存储域订单的hash的大key
     */
    public static final String SEC_KILL_ORDER_KEY = "SeckillOrder";

    /**
     * 用户排队的队列的KEY
     */
    public static final String SEC_KILL_USER_QUEUE_KEY = "SeckillOrderQueue";


    /**
     * 用户排队标识的key (用于存储 谁 买了什么商品 以及抢单的状态)
     */

    public static final String SEC_KILL_USER_STATUS_KEY = "UserQueueStatus";
    /**
     * @param:
     * @return:
     * @date: 2019/9/8 9:29
     * @description 排队状态统计
     * @version
     */
    public static final String SEC_KILL_USER_QUEUE_COUNT = "UserQueueCount";
    /**
     * @param: null
     * @return:
     * @date: 2019/9/8 9:44
     * @description 单个商品按照数量生成队列，每次有一个用户抢单成功，就从队列中取出一个商品生成订单，保证商品不会超卖
     * @version
     */
    public static final String SEC_KILL_GOODS_COUNT_LIST = "SeckillGoodsCount_";
    public static final String SEC_KILL_GOOD_COUNT_KEY = "SeckillGoodsCount";
}
