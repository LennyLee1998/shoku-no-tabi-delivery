package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DishService {
  void saveWithFlavor(DishDTO dishDTO);

  PageResult page(DishPageQueryDTO dishPageQueryDTO);

  void deleteBatch(List<Long> ids);

  DishVO queryDish(Long id);

  void updateDish(DishDTO dishDTO);


  void startOrStop(Integer status, Long id);

  List<Dish> getByCategoryId(Long categoryId);

}
