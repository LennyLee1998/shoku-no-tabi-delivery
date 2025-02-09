package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {

  /**
   * 插入订单数据
   * @param orders
   */
  void insert(Orders orders);

  /**
   * 根据订单号查询订单
   * @param orderNumber
   */
  @Select("select * from orders where number = #{orderNumber}")
  Orders getByNumber(String orderNumber);

  /**
   * 修改订单信息
   * @param orders
   */
  void update(Orders orders);

  /**
   * 分页查询orders
   * @return
   */

  Page<OrderVO> list(OrdersPageQueryDTO ordersPageQueryDTO);

  /**
   * 根据id获取订单
   *
   * @param id
   * @return
   */
  @Select("select * from orders where id = #{id}")
  OrderVO getById(Long id);


  /**
   * 根据userId和status获取数量
   * @param status
   * @return
   */
  @Select("select count(*) from orders where  status = #{status}")
  Integer countByStatus(Integer status);

}
