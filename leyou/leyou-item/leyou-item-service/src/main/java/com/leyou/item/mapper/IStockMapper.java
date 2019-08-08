package com.leyou.item.mapper;

import com.leyou.common.mapper.MyBaseMapper;
import com.leyou.item.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface IStockMapper extends MyBaseMapper<Stock> {

    @Update("UPDATE tb_stock SET stock=stock- #{num} WHERE sku_id = #{id} AND stock>=#{num}")
    int deceraseStock(@Param("id")Long id,@Param("num")Integer num);
}
