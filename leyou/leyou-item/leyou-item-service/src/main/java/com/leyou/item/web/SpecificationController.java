package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryGroupByCid(cid));
    }

    /**
     *
     * @param gid 组id
     * @param cid 分类id
     * @param searching 是否搜索
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamByList(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "generic", required = false) Boolean generic,
            @RequestParam(value = "searching",required = false)Boolean searching){
        return ResponseEntity.ok(specificationService.queryParamByList(gid,cid,searching,generic));
    }

    @DeleteMapping("param/{id}")
    public ResponseEntity<Void> deleteParamById(@PathVariable("id") Long id){
        specificationService.deleteParamById(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("param")
    public ResponseEntity<Void> updateParam(SpecParam specParam){
        specificationService.updateParam(specParam);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryGroupAndParamsByCid( @RequestParam(value = "cid") Long cid){
        return ResponseEntity.ok(specificationService.queryGroupAndParamsByCid(cid));
    }
}
