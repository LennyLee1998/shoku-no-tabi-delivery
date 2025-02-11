package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class SetmealServiceImpl implements SetmealService {
  @Autowired
  private SetmealMapper setmealMapper;

  @Autowired
  private SetmealDishMapper setmealDishMapper;

  @Autowired
  private DishMapper dishMapper;

  /**
   * 新增套餐和添加对应的中间表数据
   *
   * @param setmealDTO
   */
  @Transactional
  public void create(SetmealDTO setmealDTO) {
    //添加setmeal
    Setmeal setmeal = new Setmeal();
    BeanUtils.copyProperties(setmealDTO, setmeal);

    setmealMapper.insert(setmeal);

    //用于后面的中间表
    Long id = setmeal.getId();

    //添加setmeal_dish
    List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
    //设置setmealId
    setmealDishList.forEach(sd -> sd.setSetmealId(id));
    setmealDishMapper.insert(setmealDishList);

  }

  /**
   * 按条件分页查询
   *
   * @param pageQueryDTO
   * @return
   */
  @Override
  public PageResult page(SetmealPageQueryDTO pageQueryDTO) {
    PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
    Page<SetmealVO> page = setmealMapper.list(pageQueryDTO);
    PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
    return pageResult;
  }

  /**
   * 批量删除套餐
   *
   * @param ids
   */
  @Transactional
  public void deleteBatch(List<Long> ids) {
    //套餐在启售的时候不能删除
    for (Long id : ids) {
      Setmeal setmeal = setmealMapper.getById(id);
      if (Objects.equals(setmeal.getStatus(), StatusConstant.ENABLE)) {
        throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
      }
    }
//    MessageConstant
    // 删除setmeal里面套餐
    setmealMapper.deleteBatch(ids);

    // 删除setmeal_id对应的setmeal_dish中的row
    setmealDishMapper.deleteBySetmealIds(ids);
  }

  /**
   * 根据id查询套餐
   *
   * @param id
   * @return
   */
  @Override
  public SetmealVO getById(Long id) {
    Setmeal setmeal = setmealMapper.getById(id);
    SetmealVO setmealVO = new SetmealVO();
    BeanUtils.copyProperties(setmeal, setmealVO);

    //根据id获取sdes
    List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
    setmealVO.setSetmealDishes(setmealDishes);
    return setmealVO;
  }


  /**
   * 更新setmeal和setmeal_dish
   *
   * @param setmealDTO
   */
  @Transactional
  public void update(SetmealDTO setmealDTO) {
    //更新setmeal
    Setmeal setmeal = new Setmeal();
    BeanUtils.copyProperties(setmealDTO, setmeal);
    setmealMapper.update(setmeal);
    //套餐id
    Long setmealId = setmealDTO.getId();

    //删除setmeal_dish
    setmealDishMapper.deleteBySetmealIds(Collections.singletonList(setmealId));

    //新增setmeal_dish
    List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

    //判断, 如果setmealDishes不为null, 且有元素,则新增
    if (setmealDishes != null && !setmealDishes.isEmpty()) {

      for (SetmealDish setmealDish : setmealDishes) {
        setmealDish.setSetmealId(setmealId);
      }

      setmealDishMapper.insert(setmealDishes);

    }
  }


  /**
   * 起售停售套餐
   *
   * @param status
   */
  @Override
  public void updateStatus(Integer status, Long id) {
    //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
    if (status == StatusConstant.ENABLE) {
      //根据setmeal id获取里面的菜品list
      List<Dish> dishList = dishMapper.getBySetmealId(id);
      for (Dish dish : dishList) {
        if (dish.getStatus() == StatusConstant.DISABLE) {
          throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
        }
      }
    }
//    Setmeal setmeal = new Setmeal();
//    setmeal.setStatus(status);
//    setmeal.setId(id);
    Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
    setmealMapper.update(setmeal);

  }
}
