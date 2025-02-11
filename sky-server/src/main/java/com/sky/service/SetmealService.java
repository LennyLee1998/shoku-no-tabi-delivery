package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
  void create(SetmealDTO setmealDTO);

  PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

  void deleteBatch(List<Long> ids);

  void update(SetmealDTO setmealDTO);

  SetmealVO getById(Long id);

  void updateStatus(Integer status, Long id);
}
