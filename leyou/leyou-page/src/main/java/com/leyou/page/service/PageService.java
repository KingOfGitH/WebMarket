package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private TemplateEngine templateEngine;

    private static final Logger logger = LoggerFactory.getLogger(PageService.class);

    public Map<String, Object> loadModel(Long spuId) {
        Map<String ,Object> model=new HashMap<>();
        Spu spu = goodsClient.queryById(spuId);
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(),
                spu.getCid2(), spu.getCid3()));
        List<SpecGroup> specs = specificationClient.queryGroupAndParamsByCid(spu.getCid3());




        model.put("title", spu.getTitle());
        model.put("subTitle", spu.getSubTitle());
        model.put("skus", spu.getSkus());
        model.put("detail", spu.getSpuDetail());
        model.put("categories", categories);
        model.put("brand", brand);
        model.put("specs", specs);
        return model;
    }

    public Map<String, Object> loadModelMap(Long id) {
        try {
            // 模型数据
            Map<String, Object> modelMap = new HashMap<>();

            // 查询spu
            Spu spu = this.goodsClient.queryById(id);
            // 查询spuDetail
            SpuDetail detail = this.goodsClient.querySpuDetailById(id);
            // 查询sku
            List<Sku> skus = spu.getSkus();
            // 装填模型数据
            modelMap.put("spu", spu);
            modelMap.put("spuDetail", detail);
            modelMap.put("skus", skus);

            // 准备商品分类
            List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(),
                    spu.getCid2(), spu.getCid3()));
            if (categories != null) {
                modelMap.put("categories", categories);
            }

            // 准备品牌数据
            Brand brand = brandClient.queryBrandById(spu.getBrandId());

            modelMap.put("brand", brand);

            // 查询规格组及组内参数
            List<SpecGroup> groups = specificationClient.queryGroupAndParamsByCid(spu.getCid3());
            modelMap.put("groups", groups);

            // 查询商品分类下的特有规格参数
            List<SpecParam> params =
                    this.specificationClient.queryParams(null, spu.getCid3(), null, null);

            // 处理成id:name格式的键值对
            Map<Long,String> paramMap = new HashMap<>();
            for (SpecParam param : params) {
                paramMap.put(param.getId(), param.getName());//4 机身颜色，5，内存
            }
            modelMap.put("paramMap", paramMap);
            return modelMap;

        } catch (Exception e) {
            logger.error("加载商品数据出错,spuId:{}", id, e);
        }
        return null;
    }

    public void createHtml(Long spuId) {
        Context context = new Context();
        context.setVariables(loadModel(spuId));

        File dest=new File("D:\\app\\GitHub\\WebMarket\\leyou\\leyou-page\\src\\main\\resources\\static\\",spuId+".html");

        if (dest.exists()){
            dest.delete();
        }
        try (PrintWriter writer=new PrintWriter(dest,"UTF-8")){
            templateEngine.process("item",context,writer);
        }catch (Exception e){
            log.error("[静态页服务] 生成静态页异常！",e);
        }
    }

    public void deleteHtml(Long spuId) {
        File dest=new File("D:\\app\\GitHub\\WebMarket\\leyou\\leyou-page\\src\\main\\resources\\static\\",spuId+".html");
        if (dest.exists()){
            dest.delete();
        }
    }
}
