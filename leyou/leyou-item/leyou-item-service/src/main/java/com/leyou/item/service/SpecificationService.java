package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LeyouException;
import com.leyou.item.mapper.ISpecGroupMapper;
import com.leyou.item.mapper.ISpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationService {
    @Autowired
    private ISpecGroupMapper specGroupMapper;

    @Autowired
    private ISpecParamMapper specParamMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup specGroup=new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> specGroups = specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(specGroups)){
            throw new LeyouException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return specGroups;
    }

//    public List<SpecParam> queryParamByGid(Long gid) {
//        SpecParam specParam=new SpecParam();
//        specParam.setGroupId(gid);
//        List<SpecParam> specParams = specParamMapper.select(specParam);
//        if (CollectionUtils.isEmpty(specParams)){
//            throw new LeyouException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
//        }
//        return specParams;
//    }

    public void updateParam(SpecParam specParam) {
        specParamMapper.updateByPrimaryKey(specParam);
    }

    public void deleteParamById(Long id) {
        SpecParam specParam = specParamMapper.selectByPrimaryKey(id);
        specParamMapper.delete(specParam);
    }

    public List<SpecParam> queryParamByList(Long gid, Long cid, Boolean searching, Boolean generic) {
        SpecParam specParam=new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        specParam.setGeneric(generic);
        List<SpecParam> specParams = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(specParams)){
            throw new LeyouException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return specParams;
    }

    public List<SpecGroup> queryGroupAndParamsByCid(Long cid) {
        List<SpecGroup> specGroups = queryGroupByCid(cid);
        List<SpecParam> specParams = queryParamByList(null, cid, null, null);
        Map<Long,List<SpecParam>> map=new HashMap<>();
        for (SpecParam specParam : specParams) {
            if (!map.containsKey(specParam.getGroupId())){
                map.put(specParam.getGroupId(),new ArrayList<>());
            }
            map.get(specParam.getGroupId()).add(specParam);
        }
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }
}
