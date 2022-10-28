package cn.iocoder.yudao.module.market.api.price.dto;

import cn.iocoder.yudao.module.market.enums.common.PromotionLevelEnum;
import cn.iocoder.yudao.module.market.enums.common.PromotionTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 价格计算 Response DTO
 *
 * 整体设计，参考 taobao 的技术文档：
 * 1. <a href="https://developer.alibaba.com/docs/doc.htm?treeId=1&articleId=1029&docType=1">订单管理</a>
 * 2. <a href="https://open.taobao.com/docV3.htm?docId=108471&docType=1">常用订单金额说明</a>
 *
 * 举个例子：<a href="https://img.alicdn.com/top/i1/LB1mALAi4HI8KJjy1zbXXaxdpXa">订单图</a>
 * 输入：
 * 1. 订单实付： trade.payment = 198.00；订单邮费：5 元；
 * 2. 商品级优惠 圣诞价: 省 29.00 元 和 圣诞价:省 150.00 元； 订单级优惠，圣诞 2:省 5.00 元；
 * 分摊：
 * 1. 商品 1：原价 108 元，优惠 29 元，子订单实付 79 元，分摊主订单优惠 1.99 元；
 * 2. 商品 2：原价 269 元，优惠 150 元，子订单实付 119 元，分摊主订单优惠 3.01 元；
 *
 * @author 芋道源码
 */
@Data
public class PriceCalculateRespDTO {

    /**
     * 订单
     */
    private Order order;

    /**
     * 营销活动数组
     *
     * 只对应 {@link Order#items} 商品匹配的活动
     */
    private List<Promotion> promotions;

    /**
     * 订单
     */
    @Data
    public static class Order {

        /**
         * 商品原价（总），单位：分
         *
         * 基于 {@link OrderItem#getOriginalPrice()} 求和
         * 对应 taobao 的 trade.total_fee 字段
         */
        private Integer originalPrice;
        /**
         * 商品优惠（总），单位：分
         *
         * 订单级优惠：对主订单的优惠，常见如：订单满 200 元减 10 元；订单满 80 包邮。
         *
         * 对应 taobao 的 order.discount_fee 字段
         */
        private Integer discountPrice;
        /**
         * 优惠劵减免金额（总），单位：分
         *
         * 对应 taobao 的 trade.coupon_fee 字段
         */
        private Integer couponPrice;
        /**
         * 积分减免金额（总），单位：分
         *
         * 对应 taobao 的 trade.point_fee 字段
         */
        private Integer pointPrice;
        /**
         * 运费金额，单位：分
         */
        private Integer deliveryPrice;
        /**
         * 最终购买金额（总），单位：分
         *
         * = {@link OrderItem#getPayPrice()} 求和
         * - {@link #couponPrice}
         * - {@link #pointPrice}
         * + {@link #deliveryPrice}
         */
        private Integer payPrice;
        /**
         * 商品 SKU 数组
         */
        private List<OrderItem> items;

        // ========== 营销基本信息 ==========
        /**
         * 优惠劵编号
         */
        private Long couponId;

    }

    /**
     * 订单商品 SKU
     */
    @Data
    public static class OrderItem extends PriceCalculateReqDTO.Item {

        /**
         * 购买数量
         */
        private Integer count;

        /**
         * 商品原价（总），单位：分
         *
         * = {@link #originalUnitPrice} * {@link #getCount()}
         */
        private Integer originalPrice;
        /**
         * 商品原价（单），单位：分
         *
         * 对应 ProductSkuDO 的 price 字段
         * 对应 taobao 的 order.price 字段
         */
        private Integer originalUnitPrice;
        /**
         * 商品优惠（总），单位：分
         *
         * 商品级优惠：对单个商品的，常见如：商品原价的 8 折；商品原价的减 50 元
         *
         * 对应 taobao 的 order.discount_fee 字段
         */
        private Integer discountPrice;
        /**
         * 子订单实付金额，不算主订单分摊金额，单位：分
         *
         * = {@link #originalPrice}
         * - {@link #discountPrice}
         *
         * 对应 taobao 的 order.payment 字段
         */
        private Integer payPrice;

        /**
         * 子订单分摊金额（总），单位：分
         * 需要分摊 {@link Order#discountPrice}、{@link Order#couponPrice}
         *
         * 对应 taobao 的 order.part_mjz_discount 字段
         */
        private Integer orderPartPrice;
        /**
         * 分摊后子订单实付金额（总），单位：分
         *
         * 对应 taobao 的 divide_order_fee 字段
         */
        private Integer orderDividePrice;

    }

    /**
     * 营销活动
     */
    @Data
    public static class Promotion {

        /**
         * 营销编号
         *
         * 例如说：营销活动的编号、优惠劵的编号
         */
        private Long id;
        /**
         * 营销名字
         */
        private String name;
        /**
         * 营销类型
         *
         * 枚举 {@link PromotionTypeEnum}
         */
        private Integer type;
        /**
         * 营销级别
         *
         * 枚举 {@link PromotionLevelEnum}
         */
        private Integer level;
        /**
         * 计算时的原价（总），单位：分
         */
        private Integer beforePrice;
        /**
         * 计算时的优惠（总），单位：分
         */
        private Integer afterPrice;
        /**
         * 匹配的商品 SKU 数组
         */
        private List<PromotionItem> items;

        // ========== 匹配情况 ==========

        /**
         * 是否满足优惠条件
         */
        private Boolean meet;
        /**
         * 满足条件的提示
         *
         * 如果 {@link #meet} = true 满足，则提示“圣诞价:省 150.00 元”
         * 如果 {@link #meet} = false 不满足，则提示“购满 85 元，可减 40 元”
         */
        private String meetTip;

    }

    /**
     * 营销匹配的商品 SKU
     */
    @Data
    public static class PromotionItem {

        /**
         * 商品 SKU 编号
         */
        private Long skuId;
        /**
         * 计算时的原价（总），单位：分
         */
        private Integer beforePrice;
        /**
         * 计算时的优惠（总），单位：分
         */
        private Integer afterPrice;

    }

}
