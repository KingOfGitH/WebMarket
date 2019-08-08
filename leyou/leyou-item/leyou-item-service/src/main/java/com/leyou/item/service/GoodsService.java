package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDto;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LeyouException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.ISkuMapper;
import com.leyou.item.mapper.ISpuDetailMapper;
import com.leyou.item.mapper.ISpuMapper;
import com.leyou.item.mapper.IStockMapper;
import com.leyou.item.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class GoodsService {

    @Autowired
    private ISpuMapper spuMapper;

    @Autowired
    private ISpuDetailMapper spuDetailMapper;

    @Autowired
    private ISkuMapper skuMapper;

    @Autowired
    private IStockMapper stockMapper;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AmqpTemplate amqpTemplate;



    public PageResult<Spu> querySpuByPage( Integer page, Integer rows,Boolean saleable, String key) {
        PageHelper.startPage(page,rows);
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        if (saleable!=null){
            criteria.andEqualTo("saleable",saleable);
        }
        example.setOrderByClause("last_update_time DESC");
        List<Spu> spus = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)){
            throw new LeyouException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        loadCategoryAndBrandName(spus);
        PageInfo<Spu> spuPageInfo = new PageInfo<>();
        return new PageResult<>(spuPageInfo.getTotal(),spus);
    }

    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            List<String> names = categoryService.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(),
                    spu.getCid3())).stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            String name = brandService.queryBrandById(spu.getBrandId()).getName();
            spu.setBname(name);
        }
    }

    public void saveGoods(Spu spu) {
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);

        int insert = spuMapper.insert(spu);
        if (insert!=1){
            throw new LeyouException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());

        insert=spuDetailMapper.insert(spuDetail);
        if (insert!=1){
            throw new LeyouException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        saveSkuAndStock(spu);
        sendMessage(spu.getId(),"insert");
    }

    public void saveSkuAndStock(Spu spu) {
        int insert;
        List<Stock> stocks=new ArrayList<>();

        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setId(null);
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());


            insert = skuMapper.insert(sku);
            if (insert!=1){
                throw new LeyouException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            Stock stock=new Stock();
            stock.setStock(sku.getStock());
            stock.setSkuId(sku.getId());

            stocks.add(stock);
        }

        //批量新增
        insert=stockMapper.insertList(stocks);
        if (insert!=1){
            throw new LeyouException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null) {
            throw new LeyouException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> querySkusBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);

        if (CollectionUtils.isEmpty(skus)){
            throw new LeyouException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        selectStockBySkuList(skus);

        return skus;
    }

    public void selectStockBySkuList(List<Sku> skus) {
        skus.forEach(s -> {
            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
            if (stock==null){
                throw new LeyouException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
            }
            s.setStock(stock.getStock());
        });
    }

    public void updateGoods(Spu spu) {
        if (spu.getId()==null){
            throw new LeyouException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
        }
        Sku sku=new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> skus = skuMapper.select(sku);
        // 如果以前存在，则删除
        if(!CollectionUtils.isEmpty(skus)) {
            List<Long> ids = skus.stream().map(s -> s.getId()).collect(Collectors.toList());
            // 删除以前库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId", ids);
            stockMapper.deleteByExample(example);
            // 删除以前的sku
            Sku record = new Sku();
            record.setSpuId(spu.getId());
            skuMapper.delete(record);

        }
        // 新增sku和库存
        saveSkuAndStock(spu);

        // 更新spu
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        int flag = spuMapper.updateByPrimaryKeySelective(spu);
        if (flag!=1){
            throw new LeyouException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        // 更新spu详情
        flag = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (flag!=1){
            throw new LeyouException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        sendMessage(spu.getId(),"update");
    }

    public Spu querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu==null){
            throw new LeyouException(ExceptionEnum.SPU_NOT_FOUND);
        }
        spu.setSpuDetail(querySpuDetailBySpuId(id));
        spu.setSkus(querySkusBySpuId(id));
        return spu;
    }

    private void sendMessage(Long id, String type){
        // 发送消息
        try {
            this.amqpTemplate.convertAndSend("item." + type, id);
        } catch (Exception e) {
            log.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }

    public Sku querySkuById(Long id) {
        return this.skuMapper.selectByPrimaryKey(id);
    }

    public List<Sku> querySkusByIds(List<Long> ids) {
        List<Sku> skus = this.skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)){
            throw new LeyouException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        selectStockBySkuList(skus);
        return skus;
    }

    public void decreaseStock(List<CartDto> cartDTOList) {
        for (CartDto cartDTO : cartDTOList) {
            int i = stockMapper.deceraseStock(cartDTO.getSkuId(), cartDTO.getNum());
            if (i!=1){
                throw new LeyouException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
