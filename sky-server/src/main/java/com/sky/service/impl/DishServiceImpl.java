package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
@Slf4j
public class DishServiceImpl implements DishService {
  @Autowired
  private DishMapper dishMapper;
  @Autowired
  private DishFlavorMapper dishFlavorMapper;
  @Autowired
  private SetmealDishMapper setmealDishMapper;

  /**
   * 新增菜品和对应的口味
   *
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
    if (flavors != null && !flavors.isEmpty()) {
      flavors.forEach(flavor -> flavor.setDishId(dishId));
      dishFlavorMapper.insertBatch(flavors);
    }

  }

  /**
   * 菜品分页查询
   *
   * @param dishPageQueryDTO
   * @return
   */
  @Override
  public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
    //分页查询
    PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
    Page<DishVO> dishPage = dishMapper.list(dishPageQueryDTO);

//    List<DishVO> dishVOList = new ArrayList<>();

    //flavors字段处理
    dishPage.getResult().forEach(dishVO -> {
      //将数据库返回的dish => dishVO
//      DishVO dishVO = new DishVO();
//      BeanUtils.copyProperties(dish, dishVO);

      //将dish的categoryId字段转成categoryName
//      Category category = categoryMapper.queryById(dishVO.getCategoryId());
//      dishVO.setCategoryName(category.getName());

      //查询flavor, 找到当前菜品对应的flavor
      List<DishFlavor> flavors = dishFlavorMapper.listByDishId(dishVO.getId());
      dishVO.setFlavors(flavors);

//      dishVOList.add(dishVO);
    });

    PageResult pageResult = new PageResult(dishPage.getTotal(), dishPage);


    return pageResult;
  }

  /**
   * 根据ids删除菜品
   *
   * @param ids
   */
  //dish删除, 关联的flavor也要被删除
  @Transactional
  public void deleteBatch(List<Long> ids) {
    // 判断菜品是否起售(status时候为1) - 如果其中一个菜品的status为1, 则抛出异常, 所有的都不能删除
    for (Long id : ids) {
      Dish dish = dishMapper.queryById(id);
      //抛出异常还需要break吗 或者return
      if (Objects.equals(dish.getStatus(), StatusConstant.ENABLE)) {
        //处于启售中, 不能删除 - 这个提示信息由全局的异常处理器捕获到,然后给到前端进行展示
        throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
      }
    }

    //是否被套餐关联 - 中间表setmeal-dish (setmeal_id & dish_id)
    List<Long> setmealIds = setmealDishMapper.getByDishId(ids);
    if (setmealIds != null && !setmealIds.isEmpty()) {
      //当前菜品被套餐关联, 不能删除
      throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
    }

    //在mapper里面批量删除
    //删除dish
    dishMapper.deleteByIds(ids);
    //删除dish关联的flavor
    dishFlavorMapper.deleteByDishIds(ids);


    //在mapper里面逐个删除
//    for (Long id : ids) {
    //删除dish
//      dishMapper.deleteById(id);
    //删除dish关联的flavor - 不需要去查询时候有, 直接尝试去删除即可
//      dishFlavorMapper.deleteByDishId(id);
//    }


  }

  /**
   * 根据id查询菜品和对应的口味数据
   *
   * @param id
   * @return
   */
  @Override
  public DishVO queryDish(Long id) {
    //获取dishVO数据 和上面的queryById是一样的
//    DishVO dishVO = dishMapper.getById(id);

    //使用已有接口获取dish
    Dish dish = dishMapper.queryById(id);
    //获取当前dish的flavors
    List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);

    DishVO dishVO = new DishVO();
    BeanUtils.copyProperties(dish, dishVO);

    dishVO.setFlavors(flavors);

    return dishVO;
  }

  /**
   * 修改菜品
   *
   * @param dishDTO
   */
  @Transactional
  public void updateDish(DishDTO dishDTO) {
    //更新dish
    Dish dish = new Dish();
    BeanUtils.copyProperties(dishDTO, dish);

    dishMapper.update(dish);

    //更新dish_flavor (?? 怎么更新比较好
    List<DishFlavor> dishFlavorList = dishDTO.getFlavors();

    //将之前的dishId对应的flavor删掉
    dishFlavorMapper.deleteByDishId(dishDTO.getId());

    //加入新的flavor
    if(dishFlavorList != null && !dishFlavorList.isEmpty()) {
      dishFlavorList.forEach(flavor -> flavor.setDishId(dishDTO.getId()));
      dishFlavorMapper.insertBatch(dishFlavorList);
    }


  }

  @Override
  public void startOrStop(Integer status, Long id) {
    Dish dish = Dish.builder().id(id).status(status).build();
    dishMapper.update(dish);
  }
}
