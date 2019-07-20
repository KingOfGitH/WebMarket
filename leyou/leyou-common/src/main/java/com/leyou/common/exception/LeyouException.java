package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author zhbr
 * @date 2019/7/13 20:48
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LeyouException extends RuntimeException{
    private ExceptionEnum exceptionEnum;
}
