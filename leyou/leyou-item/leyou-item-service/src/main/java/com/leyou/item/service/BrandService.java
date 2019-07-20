package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LeyouException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.IBrandMapper;
import com.leyou.item.pojo.Brand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
@Transactional
@Slf4j
public class BrandService {

    @Autowired
    private IBrandMapper brandMapper;

    /*
    * 分页查询品牌
    * */
    public PageResult<Brand> queryBrandByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        PageHelper.startPage(page,rows);
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)){
            example.createCriteria().orLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }
        if (StringUtils.isNotBlank(sortBy)){
            String clause=sortBy+(desc?" DESC ":" ASC ");
            example.setOrderByClause(clause);
        }
        List<Brand> brands = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(brands)){
            throw new LeyouException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        PageInfo<Brand> brandPageInfo = new PageInfo<>(brands);
        return new PageResult<>(brandPageInfo.getTotal(),brands);
    }

    /*
    * 根据页面数据保存品牌
    * */
    public void saveBrand(Brand brand, List<Long> cids) {
        brand.setId(null);
        int flag = brandMapper.insert(brand);
        if (flag!=1){
            throw new LeyouException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        for (Long cid:cids){
            flag = brandMapper.insertCategoryBrand(cid, brand.getId());
            if(flag!=1){
                throw new LeyouException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    /*
    * 根据id查询品牌
    * */
    public Brand queryBrandById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand==null){
            throw new LeyouException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brands = brandMapper.queryBrandByCid(cid);
        if (CollectionUtils.isEmpty(brands)){
            throw new LeyouException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)){
            log.error("数据库查询品牌失败",new LeyouException(ExceptionEnum.BRAND_NOT_FOUND));
            throw new LeyouException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }
}
