package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LeyouException;
import com.leyou.item.mapper.ICategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author zhbr
 * @date 2019/7/13 11:45
 */
@Service
public class CategoryService {

    @Autowired
    private ICategoryMapper categoryMapper;


    public List<Category> queryCategoryListByPid(Long pid) {
        Category category=new Category();
        category.setParentId(pid);
        List<Category> list = categoryMapper.select(category);
        if (CollectionUtils.isEmpty(list)){
            throw new LeyouException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    public List<Category> queryCategoryByIds(List<Long> ids){
        List<Category> list=categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)){
            throw new LeyouException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }
}
