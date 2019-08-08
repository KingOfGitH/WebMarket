package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author zhbr
 * @date 2019/7/13 20:39
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {

    PRICE_CANNOT_BE_NULL(400,"价格不能为空"),
    CATEGORY_NOT_FOUND(404,"商品分类未找到"),
    BRAND_NOT_FOUND(404,"品牌未找到"),
    BRAND_SAVE_ERROR(500,"新增失败"),
    INVALID_FILE_TYPE(400,"无效的文件类型"),
    SPEC_GROUP_NOT_FOUND(400,"规格商品组未查到"),
    SPEC_PARAM_NOT_FOUND(400,"商品规格参数未找到"),
    GOODS_NOT_FOUND(400,"商品不存在"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    GOODS_UPDATE_ERROR(500,"更新商品失败"),
    GOODS_DETAIL_NOT_FOUND(400,"商品详情不存在"),
    GOODS_SKU_NOT_FOUND(400,"商品SKU不存在"),
    GOODS_STOCK_NOT_FOUND(400,"商品库存不存在"),
    GOODS_ID_CANNOT_BE_NULL(400,"商品ID不能为空"),
    SPU_NOT_FOUND(400,"SPU未找到"),
    DATA_TYPE_ERROR(400,"参数有误"),
    CREATE_ORDER_ERROR(500,"创建订单失败"),
    ORDER_NOT_FOUND(404,"查询订单失败"),
    ORDER_DETAIL_NOT_FOUND(404,"查询订单详情失败"),
    ORDER_STATUS_NOT_FOUND(404,"查询订单状态失败"),
    ORDER_STATUS_ERROR(400,"订单状态错误"),
    INVALID_ORDER_PARAM(400,"无效的订单参数"),
    UPDATE_ORDER_STATUS_ERROR(500,"更新订单状态错误"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    WX_PAY_ORDER_FAIL(500,"微信下单失败"),
    INVALID_SIGN_ERROR(500,"无效标识"),
    ;

    private int code;
    private String msg;

}
