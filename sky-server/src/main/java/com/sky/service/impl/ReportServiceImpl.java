package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
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
}
