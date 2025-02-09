package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
  OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

  /**
   * 订单支付
   * @param ordersPaymentDTO
   * @return
   */
  OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

  /**
   * 支付成功，修改订单状态
   * @param outTradeNo
   */
  void paySuccess(String outTradeNo);

  //用户端
  /**
   * 用户端订单分页查询
   * @param page
   * @param pageSize
   * @param status
   * @return
   */
  PageResult pageQuery4User(int page, int pageSize, Integer status);

  /**
   * 查询订单详情
   * @param id
   * @return
   */
  OrderVO getDetailByOrderId(Long id);

  /**
   * 用户取消订单
   * @param id
   * @throws Exception
   */
  void userCancel(Long id) throws Exception;
  
}
