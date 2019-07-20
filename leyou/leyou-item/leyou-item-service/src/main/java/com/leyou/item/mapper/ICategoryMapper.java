package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;


//IdListMapper<Category,Long>
//根据id集合查询，参数为类名和主键类型
public interface ICategoryMapper extends Mapper<Category>, IdListMapper<Category,Long> {
}
