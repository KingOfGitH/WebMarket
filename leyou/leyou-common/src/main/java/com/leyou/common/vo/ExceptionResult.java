package com.leyou.common.vo;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Data;

/**
 * @author zhbr
 * @date 2019/7/13 21:23
 */
@Data
public class ExceptionResult {
    private int status;
    private String message;
    private Long timestamp;

    public ExceptionResult(ExceptionEnum exceptionEnum) {
        this.status=exceptionEnum.getCode();
        this.message=exceptionEnum.getMsg();
        this.timestamp=System.currentTimeMillis();
    }
}
