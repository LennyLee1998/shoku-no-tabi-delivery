package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

  @Autowired
  private OrderMapper orderMapper;
  @Autowired
  private OrderDetailMapper orderDetailMapper;
  @Autowired
  private AddressBookMapper addressBookMapper;
  @Autowired
  private ShoppingCartMapper shoppingCartMapper;

  /**
   * 用户下单
   *
   * @param ordersSubmitDTO
   * @return
   */
  @Transactional
  public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

    //处理各种业务异常(地址簿为空, 购物车数据为空)
    AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
    if (addressBook == null) {
      //抛出业务异常, 创建专门的异常类（如 AddressBookBusinessException）确实可以提高代码的可读性和可维护性。
      throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
    }

    //查询当前用户的购物车数据
    Long userId = BaseContext.getCurrentId();
    ShoppingCart cart = ShoppingCart.builder().userId(userId).build();
    List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(cart);
    if (shoppingCartList == null || shoppingCartList.isEmpty()) {
      //抛出业务异常
      throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
    }


    //向订单表插入1条数据
    Orders orders = new Orders();
    BeanUtils.copyProperties(ordersSubmitDTO, orders);
    orders.setOrderTime(LocalDateTime.now());
    orders.setPayStatus(Orders.UN_PAID);
    orders.setStatus(Orders.PENDING_PAYMENT);
    orders.setNumber(String.valueOf(System.currentTimeMillis()));
    orders.setPhone(addressBook.getPhone());
    orders.setUserId(userId);
    orders.setDeliveryStatus(1);

    orderMapper.insert(orders);
    //向订单明细表插入n条数据
    List<OrderDetail> orderDetailList = new ArrayList<>();
    shoppingCartList.forEach(shoppingCart -> {
      OrderDetail orderDetail = new OrderDetail();
      BeanUtils.copyProperties(shoppingCart, orderDetail);
      orderDetail.setOrderId(orders.getId());
      orderDetailList.add(orderDetail);
    });
    //批量插入效率更高
    orderDetailMapper.insertBatch(orderDetailList);

    //清空当前用户的购物车数据
    shoppingCartMapper.deleteByUserId(userId);

    //封装VO返回结果
    OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
        .id(orders.getId())
        .orderAmount(orders.getAmount())
        .orderNumber(orders.getNumber())
        .orderTime(orders.getOrderTime())
        .build();

    return orderSubmitVO;
  }
}
