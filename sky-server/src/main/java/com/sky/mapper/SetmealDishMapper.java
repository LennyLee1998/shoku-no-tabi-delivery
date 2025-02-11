package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

  /**
   * 根据菜品id查询对应的套餐id
   * @param dishIds
   * @return
   */
  //@Select("select setmeal_id from setmeal_dish where dish_id in (1,2,3,4)")
  List<Long> getByDishId(List<Long> dishIds);

  /**
   * 根据新增套餐->新增setmeal_dish
   * @param setmealDishList
   */
  void insert(List<SetmealDish> setmealDishList);

  /**
   * 删除setmeal_ids对应的setmeal_dish中的row
   * @param ids
   */
  void deleteBySetmealIds(List<Long> ids);

  /**
   * 根据setmealId获取sdes
   * @param setmealId
   * @return
   */
  @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
  List<SetmealDish> getBySetmealId(Long setmealId);
}
