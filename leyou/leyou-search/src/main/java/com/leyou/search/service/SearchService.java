package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LeyouException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SearchService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    public Goods buildGoods(Spu spu) {
        // 创建goods对象
        Goods goods = new Goods();
        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        // 查询分类名称
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 查询spu下的所有sku
        List<Sku> skus = goodsClient.querySkusBySpuId(spu.getId());
        List<Long> prices = new ArrayList<>();
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        // 遍历skus，获取价格集合
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", sku.getId());
            skuMap.put("title", sku.getTitle());
            skuMap.put("price", sku.getPrice());
            skuMap.put("image", StringUtils.isNotBlank(sku.getImages()) ? StringUtils.substringBefore(sku.getImages(),",") : "");
            skuMapList.add(skuMap);
        });
        // 查询出所有的搜索规格参数
        List<SpecParam> params = specificationClient.queryParams(null, spu.getCid3(), null, true);
        // 查询spuDetail。获取规格参数值
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spu.getId());
        // 获取通用的规格参数
        Map<String, String> genericSpecMap = JsonUtils.parseMap(spuDetail.getGenericSpec(), String.class, String.class);
        // 获取特殊的规格参数
        Map<String, List<String>> specialSpecMap = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<String, List<String>>>() {});
        // 定义map接收{规格参数名，规格参数值}
        Map<String, Object> paramMap = new HashMap<>();
        for (SpecParam param : params) {
            String  id = param.getId().toString();
            if (id==null){
                throw new LeyouException(ExceptionEnum.GOODS_ID_CANNOT_BE_NULL);
            }
            // 判断是否通用规格参数
            if (param.getGeneric()) {
                // 获取通用规格参数值
                String value = genericSpecMap.get(id);
                // 判断是否是数值类型
                if (param.getNumeric()) {
                    // 如果是数值的话，判断该数值落在那个区间
                    value = chooseSegment(value, param);
                }
                // 把参数名和值放入结果集中
                paramMap.put(param.getName(), value);
            } else {
                paramMap.put(param.getName(), specialSpecMap.get(id));
            }
        }
        // 设置参数
        goods.setId(spu.getId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        goods.setAll(spu.getTitle() + brand.getName() + StringUtils.join(categories, " "));
        goods.setPrice(prices);
        if(CollectionUtils.isEmpty(skuMapList)){
            throw new LeyouException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        goods.setSkus(JsonUtils.serialize(skuMapList));
        goods.setSpecs(paramMap);
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest request) {
        String key = request.getKey();
        if (StringUtils.isBlank(key)) {
            return null;
        }
        int page = request.getPage()-1;
        Integer size = request.getSize();

        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        nativeSearchQueryBuilder.withPageable(PageRequest.of(page,size));
//        QueryBuilder all=QueryBuilders.matchQuery("all",key);
        QueryBuilder all = buildBasicQuery(request);
        nativeSearchQueryBuilder.withQuery(all);

        //聚合
        String categoryAggName="category_agg";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //聚合
        String brandAggName="brand_agg";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        AggregatedPage<Goods> search = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);

        long totalElements = search.getTotalElements();
        int totalPages = search.getTotalPages();
        List<Goods> goodsList = search.getContent();

        Aggregations aggregations = search.getAggregations();
        List<Category> categories=parseCategoryAgg(aggregations.get(categoryAggName));
        List<Brand> brands=parseBrandAgg(aggregations.get(brandAggName));

        List<Map<String ,Object>> specs=null;
        if (categories!=null&&categories.size()==1){
            specs=buildSpecificationAgg(categories.get(0).getId(),all);
        }
        return new SearchResult(totalElements,totalPages,goodsList,categories,brands,specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()));
        Map<String, String> filter = request.getFilter();
        System.out.println(filter.toString());
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            if (!"cid3".equals(key)&&!"brandId".equals(key)){
                key="specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return boolQueryBuilder;
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long id, QueryBuilder all) {
        List<Map<String ,Object>> specs=new ArrayList<>();
        List<SpecParam> specParams = specificationClient.queryParams(null, id, null, true);
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(all);
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        AggregatedPage<Goods> goods = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        Aggregations aggregations = goods.getAggregations();
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            StringTerms aggregation = aggregations.get(name);
            List<String> collect = aggregation.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            Map<String ,Object> map=new HashMap<>();
            map.put("k",name);
            map.put("options",collect);
            specs.add(map);
        }
        return specs;
    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            return brandClient.queryBrandByIds(ids);
        }catch (Exception e){
            log.error("[查询品牌失败]",e);
            return null;
        }

    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            return categoryClient.queryCategoryByIds(ids);
        }catch (Exception e){
            log.error("[查询分类失败]",e);
            return null;
        }
    }

    public void createUpdateIndex(Long spuId) {
        Spu spu = goodsClient.queryById(spuId);
        Goods goods = buildGoods(spu);
        goodsRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}