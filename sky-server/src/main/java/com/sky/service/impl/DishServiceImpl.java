package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
public class DishServiceImpl implements DishService {
  @Autowired
  private DishMapper dishMapper;
  @Autowired
  private DishFlavorMapper dishFlavorMapper;

  /**
   * 新增菜品和对应的口味
   * @param dishDTO
   */
  //要想下面生效需要开启注解方式的事务管理, 在启动类里面@EnableTransactionManagement
  @Transactional
  public void saveWithFlavor(DishDTO dishDTO) {
    //向菜品表插入1条数据
    Dish dish = new Dish();

      //属性命名要保持一致
    BeanUtils.copyProperties(dishDTO, dish);
    dishMapper.insert(dish);

    //获取insert语句生成的主键值
    Long dishId = dish.getId();

    //向口味表插入n条数据
    List<DishFlavor> flavors = dishDTO.getFlavors();
    // 非必须, 需要判断
    if(flavors != null && !flavors.isEmpty()) {
      flavors.forEach(flavor -> flavor.setDishId(dishId));
      dishFlavorMapper.insertBatch(flavors);
    }

  }
}
