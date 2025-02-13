package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

  @Autowired
  private OrderMapper orderMapper;
  @Autowired
  private UserMapper userMapper;

  /**
   * 统计指定时间区间内的营业额数据
   *
   * @param begin
   * @param end
   * @return
   */
  public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

    //当前集合用于存放begin到end范围内的每天的日期
    List<LocalDate> dateList = new ArrayList<>();

    dateList.add(begin);
    while (!begin.equals(end)) {
      //日期计算, 计算指定日期的后一天对应的日期
      begin = begin.plusDays(1);
      dateList.add(begin);
    }

    String dateStr = StringUtils.join(dateList, ",");


    List<Double> turnoverList = new ArrayList<>();
    //查询date日期对应的营业额数据,营业额是指状态为已完成的订单金额合计
    dateList.forEach(date -> {
      LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN); //当天起始时刻
      LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX); //当天最后时刻

      Map map = new HashMap<>();
      map.put("begin", beginTime);
      map.put("end", endTime);
      map.put("status", Orders.COMPLETED);
      Double turnover = orderMapper.sumByMap(map);
      turnover = turnover == null ? 0.0 : turnover;
      turnoverList.add(turnover);
    });

    String turnoverStr = StringUtils.join(turnoverList, ",");

    return TurnoverReportVO
        .builder()
        .dateList(dateStr)
        .turnoverList(turnoverStr)
        .build();
  }

  /**
   * 统计指定时间区间内的用户数据
   *
   * @param begin
   * @param end
   * @return
   */
  public UserReportVO getuserStatistics(LocalDate begin, LocalDate end) {
    //得到dateList
    List<LocalDate> localDateList = new ArrayList<>();
    localDateList.add(begin);
    while (!begin.equals(end)) {
      begin = begin.plusDays(1);
      localDateList.add(begin);
    }
    String localDateStr = StringUtils.join(localDateList, ",");

    List<Integer> newUserList = new ArrayList<>();
    List<Integer> totalUserList = new ArrayList<>();

    //遍历localDateList
    localDateList.forEach(localDate -> {
      //把localDate转成localdatetime
      LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
      LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

      //计算newUser
      Map map = new HashMap();
      map.put("end", endTime);

      //得到totalUser
      Integer totalUser = userMapper.countByMap(map);
      totalUserList.add(totalUser);

      map.put("begin", beginTime);
      Integer newUser = userMapper.countByMap(map);
      newUserList.add(newUser);

    });
    String newUserStr = StringUtils.join(newUserList, ",");
    String totalUserStr = StringUtils.join(totalUserList, ",");

    return UserReportVO.builder()
        .dateList(localDateStr)
        .newUserList(newUserStr)
        .totalUserList(totalUserStr)
        .build();
  }

  /**
   * 订单统计
   *
   * @param begin
   * @param end
   * @return
   */
  public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
    // 获取dateList
    List<LocalDate> dateList = new ArrayList<>();
    dateList.add(begin);
    while (!begin.equals(end)) {
      begin = begin.plusDays(1);
      dateList.add(begin);
    }
    String dateStr = StringUtils.join(dateList, ",");

    //每日订单数 & 有效订单数
    List<Integer> orderCountList = new ArrayList<>();
    List<Integer> validOrderCountList = new ArrayList<>();
    //遍历dateList
    dateList.forEach(date -> {
      LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
      LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

      //每日订单数
      Integer orderCount = getOrderCount(beginTime, endTime, null);
      orderCountList.add(orderCount);

      //每日有效订单数
      Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
      validOrderCountList.add(validOrderCount);
    });

    String orderCountStr = StringUtils.join(orderCountList, ",");
    String validOrderCountStr = StringUtils.join(validOrderCountList, ",");

    //订单总数
    Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

    //有效订单数
    Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

    //订单完成率
    Double rate = 0.0;
    if (totalOrderCount != 0) {
      rate = validOrderCount.doubleValue() / totalOrderCount;
    }

    return OrderReportVO
        .builder()
        .dateList(dateStr)
        .orderCountList(orderCountStr)
        .validOrderCountList(validOrderCountStr)
        .totalOrderCount(totalOrderCount)
        .validOrderCount(validOrderCount)
        .orderCompletionRate(rate)
        .build();
  }

  /**
   * 根据条件统计订单数量
   *
   * @param begin
   * @param end
   * @param status
   * @return
   */
  private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
    Map map = new HashMap<>();
    map.put("begin", begin);
    map.put("end", end);
    map.put("status", status);

    return orderMapper.countByMap(map);

  }
}
