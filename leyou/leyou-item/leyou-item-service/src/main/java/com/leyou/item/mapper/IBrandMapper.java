package com.leyou.item.mapper;

import com.leyou.common.mapper.MyBaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IBrandMapper extends MyBaseMapper<Brand> {
    @Insert("insert into tb_category_brand (category_id,brand_id) values (#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid")Long cid,@Param("bid")Long bid);

    @Select("SELECT b.* FROM tb_category_brand cb INNER JOIN tb_brand b ON b.`id`=cb.`brand_id` WHERE cb.`category_id`=#{cid}")
    List<Brand> queryBrandByCid(@Param("cid") Long cid);
}
