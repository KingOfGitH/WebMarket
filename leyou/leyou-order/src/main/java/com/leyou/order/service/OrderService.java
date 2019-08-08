package com.leyou.order.service;

import com.leyou.order.client.AddressClient;
import com.leyou.order.dto.OrderDto;
import com.leyou.order.dto.PayStateEnum;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.auth.entity.UserInfo;
import com.leyou.common.dto.CartDto;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LeyouException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderStatusEnum;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDto orderDto) {
        //1新增订单
        Order order = new Order();
        //1.1新增订单编号,基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDto.getPaymentType());

        //1.2 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);

        //1.3收获人信息
        AddressDTO addr = AddressClient.findById(orderDto.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());
 
        //1.4金额
        // 把cartDTO为一个map, key是sku的id， 值是num
        Map<Long, Integer> numMap = orderDto.getCarts().stream().collect(Collectors.toMap(CartDto::getSkuId, CartDto::getNum));
        //获取所有的sku的id
        Set<Long> ids = numMap.keySet();
        //根据id来查询sku
        List<Sku> skus = goodsClient.querySkusByIds(new ArrayList<>(ids));

        //准备一个orderDetail集合
        List<OrderDetail> details = new ArrayList<>();
        long totoPay = 0L;

        for (Sku sku : skus) {
            totoPay += sku.getPrice() * numMap.get(sku.getId());
            //封装orderDetail
            OrderDetail detail = new OrderDetail();
            detail.setImage(StringUtils.substringBefore(sku.getImages(), ","));
            detail.setNum(numMap.get(sku.getId()));
            detail.setOrderId(orderId);
            detail.setOwnSpec(sku.getOwnSpec());
            detail.setPrice(sku.getPrice());
            detail.setSkuId(sku.getId());
            detail.setTitle(sku.getTitle());
            details.add(detail);
        }
        order.setTotalPay(totoPay);
        //实付金额， 总金额 + 邮费 - 优惠金额
        order.setActualPay(totoPay + order.getPostFee() - 0);

        //1.5 order写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1){
            log.error("[创建订单]创建订单失败,orderId:{}",orderId);
            throw new LeyouException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //2新增订单详情
        count = orderDetailMapper.insertList(details);
        if (count != details.size()){
            log.error("[创建订单]创建订单失败,orderId:{}",orderId);
            throw new LeyouException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //3新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        count = orderStatusMapper.insertSelective(orderStatus);
        if (count != 1){
            log.error("[创建订单]创建订单失败,orderId:{}",orderId);
            throw new LeyouException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //4减库存
        List<CartDto> carts = orderDto.getCarts();
        goodsClient.decreaseStock(carts);
        return orderId;
    }

    public Order queryOrderById(Long id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null){
            //不存在
            throw new LeyouException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        //查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = orderDetailMapper.select(detail);
       if (CollectionUtils.isEmpty(details)){
           throw new LeyouException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
       }
       order.setOrderDetails(details);
       //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
       if (orderStatus == null){
           //不存在
           throw new LeyouException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
       }
       order.setOrderStatus(orderStatus);
        return order;
    }

    public String createPayUrl(Long orderid) {
        //查询订单
        Order order = queryOrderById(orderid);
        //判断订单状态
        Integer status = order.getOrderStatus().getStatus();
        if (status != com.leyou.order.enums.OrderStatusEnum.UN_PAY.value()){
            //订单状态异常 不为1
            throw new LeyouException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        //支付金额
        Long actualPay = /*order.getActualPay()*/ 1L;
        //商品描述
        OrderDetail detail = order.getOrderDetails().get(0);
        String desc = detail.getTitle();
        return payHelper.createOrder(orderid,actualPay,desc);

    }

    public PayStateEnum queryOrderState(Long orderId) {
        //查询订单状态
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        //判断是否支付
        if (status != com.leyou.order.enums.OrderStatusEnum.UN_PAY.value()){
            // 如果已经支付，实际上就就已经支付好了
            return PayStateEnum.SUCCESS;
        }

        //如果未支付，不一定是未支付，必须去微信查询支付状态
        return payHelper.queryPayStateEnum(orderId);
    }

    public void handleNotify(Map<String, String> result) {
        //1 数据校验
        payHelper.isSuccess(result);

        //2 校验签名
        payHelper.isValidSign(result);

        //3 校验金额
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if (StringUtils.isEmpty(totalFeeStr)){
            throw new LeyouException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        //3.1 获取结果中的金额
        Long totalFee = Long.valueOf(totalFeeStr);
        //3.2 获取订单中的金额
        Long orderid = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderid);
        if (totalFee != /*order.getActualPay() */ 1L) {
            //金额不符
            throw new LeyouException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        //4 修改订单状态
        OrderStatus status = new OrderStatus();
        status.setStatus(com.leyou.order.enums.OrderStatusEnum.PAYED.value());
        status.setOrderId(orderid);
        status.setPaymentTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(status);
        if (count != 1){
            throw new LeyouException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
        log.info("[订单回调]，订单支付成功！ ，订单编号:{}",orderid);
    }
}
