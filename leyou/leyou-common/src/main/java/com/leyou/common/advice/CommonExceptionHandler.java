package com.leyou.common.advice;

import com.leyou.common.exception.LeyouException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author zhbr
 * @date 2019/7/13 17:04
 */
@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(LeyouException.class)
    public ResponseEntity<ExceptionResult> handlerException(LeyouException e){
        return ResponseEntity.status(e.getExceptionEnum().getCode())
                .body(new ExceptionResult(e.getExceptionEnum()));
    }
}
