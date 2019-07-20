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
    ;

    private int code;
    private String msg;
}
