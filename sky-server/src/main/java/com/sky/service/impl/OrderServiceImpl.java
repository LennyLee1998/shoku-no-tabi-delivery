package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
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
  @Autowired
  private UserMapper userMapper;
  @Autowired
  private WeChatPayUtil weChatPayUtil;

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
    orders.setUserName(addressBook.getConsignee());
    orders.setConsignee(addressBook.getConsignee());
    orders.setAddress(addressBook.getDistrictName() + addressBook.getDetail());
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

  /**
   * 订单支付
   *
   * @param ordersPaymentDTO
   * @return
   */
  public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
    // 当前登录用户id
    Long userId = BaseContext.getCurrentId();
    User user = userMapper.getById(userId);

    //调用微信支付接口，生成预支付交易单 (相当于时序图里面的第5步 调用微信下单接口
//    JSONObject jsonObject = weChatPayUtil.pay(
//        ordersPaymentDTO.getOrderNumber(), //商户订单号
//        new BigDecimal(0.01), //支付金额，单位 元
//        "苍穹外卖订单", //商品描述
//        user.getOpenid() //微信用户的openid
//    );
    JSONObject jsonObject = new JSONObject();

    if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
      throw new OrderBusinessException("该订单已支付");
    }

    OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
    vo.setPackageStr(jsonObject.getString("package"));

    paySuccess(ordersPaymentDTO.getOrderNumber());

    return vo;
  }

  /**
   * 支付成功，修改订单状态
   *
   * @param outTradeNo
   */
  public void paySuccess(String outTradeNo) {

    // 根据订单号查询订单
    Orders ordersDB = orderMapper.getByNumber(outTradeNo);

    // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
    Orders orders = Orders.builder()
        .id(ordersDB.getId())
        .status(Orders.TO_BE_CONFIRMED)
        .payStatus(Orders.PAID)
        .checkoutTime(LocalDateTime.now())
        .build();

    orderMapper.update(orders);
  }


  /**
   * 用户端历史订单分页查询
   * @param pageNum
   * @param pageSize
   * @param status
   * @return
   */
  @Override
  public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
    // 设置分页
    PageHelper.startPage(pageNum, pageSize);

    OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
    ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
    ordersPageQueryDTO.setStatus(status);
    // 分页条件查询
    Page<OrderVO> page = orderMapper.list(ordersPageQueryDTO);

    //返回records为orderVO
    List<OrderVO> orderVOList = page.getResult();
    // 查询出订单明细，并封装入OrderVO进行响应
    if (!orderVOList.isEmpty()) {
      //根据id查询orderDetailList
      orderVOList.forEach(orderVO -> {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderVO.getId());

        orderVO.setOrderDetailList(orderDetails);

      });
    }
    return new PageResult(page.getTotal(), orderVOList);
  }

  /**
   * 查询订单详情
   *
   * @param id
   * @return
   */
  @Override
  public OrderVO getDetailByOrderId(Long id) {
    //获取orders
    OrderVO orderVO = orderMapper.getById(id);
    if (orderVO == null) {
      throw new OrderBusinessException("订单不存在");
    }
    //获取orderDetails
    List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
    orderVO.setOrderDetailList(orderDetailList);
    return orderVO;
  }

  /**
   * 用户取消订单
   * @param id
   */
  @Override
  public void userCancel(Long id) throws Exception {
    //根据id查询订单
    OrderVO orderVO = orderMapper.getById(id);

    //校验订单是否存在
    if (orderVO == null) {
      throw  new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
    }

    //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
    if (orderVO.getStatus() >  2){
      throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
    }

    Orders orders = new Orders();
    orders.setId(id);

    //订单状态处于2时, 应该在用户取消订单的时候退款,
    if (Orders.TO_BE_CONFIRMED.equals(orderVO.getStatus())) {
//      weChatPayUtil.refund(orderVO.getNumber(), //商户订单号
//          orderVO.getNumber(), //商户退款单号
//          new BigDecimal("0.01"),//退款金额，单位 元
//          // BigDecimal 可以避免使用浮点数（如 float 和 double）可能带来的精度问题
//          new BigDecimal("0.01"));//原订单金额)

      //设置支付状态为REFUND
      orders.setPayStatus(Orders.REFUND);
    }

    orders.setStatus(Orders.CANCELLED);
    orders.setCancelTime(LocalDateTime.now());
    orders.setCancelReason("用户取消");
    orderMapper.update(orders);
  }

  /**
   * 再来一单
   * @param id
   */
  @Override
  public void repetition(Long id) {

    //根据订单id查询订单详情
    List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

    Long userId = BaseContext.getCurrentId();

    //把里面的商品加入到购物车里面
    List<ShoppingCart> shoppingCarts = orderDetails.stream().map(orderDetail -> {
      ShoppingCart shoppingCart = new ShoppingCart();
      //复制属性, 忽略id,
      BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
      shoppingCart.setUserId(userId);
      shoppingCart.setCreateTime(LocalDateTime.now());
      return shoppingCart;
    }).toList();

    // 将购物车对象批量添加到数据库
    shoppingCartMapper.insertBatch(shoppingCarts);
  }


  //管理端

  /**
   * 历史订单查询
   *
   * @param ordersPageQueryDTO
   * @return
   */

  public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
    //使用pagehelper进行分页
    PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

    Page<OrderVO> page = orderMapper.list(ordersPageQueryDTO);

    //返回records为orderVO
    List<OrderVO> orderVOList = page.getResult();
    //根据id查询orderDetailList
    if (!CollectionUtils.isEmpty(orderVOList)) {
      orderVOList.forEach(orderVO -> {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderVO.getId());

        StringBuilder orderDishes = new StringBuilder();
        orderDetails.forEach(orderDetail -> {
          orderDishes.append(orderDetail.getName())
              .append("*")
              .append(orderDetail.getNumber())
              .append("; ");
        });
        orderVO.setOrderDishes(orderDishes.toString());
      });
    }

    return new PageResult(page.getTotal(), orderVOList);
  }

  /**
   * 各个状态的订单数量统计
   *
   * @return
   */
  @Override
  public OrderStatisticsVO statistics() {
    OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
    //获取3 4 2状态的数量
    orderStatisticsVO.setConfirmed(orderMapper.countByStatus(Orders.CONFIRMED));
    orderStatisticsVO.setDeliveryInProgress(orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS));
    orderStatisticsVO.setToBeConfirmed(orderMapper.countByStatus(Orders.TO_BE_CONFIRMED));
    return orderStatisticsVO;
  }

  /**
   * 接单
   *
   * @param ordersConfirmDTO
   */
  @Override
  public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
    Orders orders = Orders.builder().id(ordersConfirmDTO.getId()).status(Orders.CONFIRMED).build();
    orderMapper.update(orders);
  }

}
