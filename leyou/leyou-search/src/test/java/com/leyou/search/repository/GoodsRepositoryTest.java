package com.leyou.search.repository;

import com.leyou.LeyouSearchApplication;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouSearchApplication.class)
public class GoodsRepositoryTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void createIndex(){
        // 创建索引库，以及映射
        //只运行一次
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData(){

        Integer page = 1;
        Integer rows = 100;

        do {
            // 分批查询spu
            PageResult<Spu> pageResult = goodsClient.querySpuByPage(page, rows,true,null);
            // 遍历spu集合转化为List<Goods>
            List<Spu> items = pageResult.getItems();

            if (CollectionUtils.isEmpty(items)) break;

            List<Goods> collect = items.stream().map(searchService::buildGoods).collect(Collectors.toList());

            goodsRepository.saveAll(collect);

            // 获取当前页的数据条数，如果是最后一页，没有100条
            rows = pageResult.getItems().size();
            // 每次循环页码加1
            page++;
        } while (rows == 100);
    }
}