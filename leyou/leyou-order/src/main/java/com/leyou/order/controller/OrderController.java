package com.leyou.order.controller;

import com.leyou.order.dto.OrderDto;
import com.leyou.order.service.OrderService;
import com.leyou.order.pojo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 新增订单
     * @param orderDto
     * @return
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDto orderDto){
        //创建订单
        return ResponseEntity.ok(orderService.createOrder(orderDto));
    }

    /**
     * 查询订单
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id){
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    /**
     * 创建支付链接(根据订单号来生成支付url)
     * @param orderid
     * @return
     */
    @GetMapping("/url/{id}")
    public ResponseEntity<String> createPayUrl(@PathVariable("id") Long orderid){
        return ResponseEntity.ok(orderService.createPayUrl(orderid));
    }

    /**
     *查询支付状态
     * @param orderId
     * @return
     */
    @GetMapping("/state/{id}")
    public ResponseEntity<Integer> queryOrderState(@PathVariable("id") Long orderId){
        return ResponseEntity.ok(orderService.queryOrderState(orderId).getValue());
    }
}
