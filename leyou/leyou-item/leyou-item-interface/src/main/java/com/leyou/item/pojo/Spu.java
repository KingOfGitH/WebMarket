package com.leyou.item.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Table(name = "tb_spu")
@Data
public class Spu {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private String title;
    private String subTitle;
    private Long cid1;
    private Long cid2;
    private Long cid3;
    private Long brandId;
    private Boolean saleable;
    @JsonIgnore //返回页面时忽略该字段
    private Boolean valid;//是否有效，逻辑删除用
    private Date createTime;
    @JsonIgnore
    private Date lastUpdateTime;

    @Transient
    private String cname;
    @Transient
    private String bname;

    @Transient
    private List<Sku> skus;

    @Transient
    private SpuDetail spuDetail;
}
