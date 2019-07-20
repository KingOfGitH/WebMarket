package com.leyou.item.web;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 分页查询所有
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "key",required = false)String  key,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy",required = false)String  sortBy,
            @RequestParam(value = "desc",defaultValue = "false")Boolean desc
    ){
        return ResponseEntity.ok(brandService.queryBrandByPage(key,page,rows,sortBy,desc));
    }

    /**
     * 新增品牌
     * @param brand 表单数据
     * @param cids 三级分类id
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand,@RequestParam(value = "cids") List<Long> cids){
        brandService.saveBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据cid查询品牌
     * @param cid 分类id
     * @return
     */
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(brandService.queryBrandByCid(cid));
    }

    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id") Long id){
        return ResponseEntity.ok(brandService.queryBrandById(id));
    }

    /**
     * 根据id集合查询
     * @param ids
     * @return
     */
    @GetMapping("brands")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam(value = "ids") List<Long> ids){
        return ResponseEntity.ok(brandService.queryBrandByIds(ids));
    }
}
