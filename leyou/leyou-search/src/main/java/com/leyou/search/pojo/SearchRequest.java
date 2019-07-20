package com.leyou.search.pojo;

public class SearchRequest {
    private String key;

    private Integer page;

    private static final Integer DEFAULT_SIZE=20;
    private static final Integer DEFAULT_PAGE=1;

    public Integer getSize() {
        return DEFAULT_SIZE;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getPage() {
        if (page==null){
            return DEFAULT_PAGE;
        }
        return Math.max(page,DEFAULT_PAGE);
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
