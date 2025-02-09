package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
  @Autowired
  private ShoppingCartMapper shoppingCartMapper;
  @Autowired
  private DishMapper dishMapper;
  @Autowired
  private SetmealMapper setmealMapper;

  /**
   * 添加购物车
   *
   * @param shoppingCartDTO
   */
  public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
    //判断当前加入购物车给中的商品是否已经存在了

    ShoppingCart shoppingCart = new ShoppingCart();
    BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
    shoppingCart.setUserId(BaseContext.getCurrentId());

    //这里用list大概率是为了在后面的查询里面能使用用一个接口
    List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

    //如果已经存在了, 只需要将数量加1
    if (list != null && !list.isEmpty()) {
      ShoppingCart cart = list.get(0);
      cart.setNumber(cart.getNumber() + 1); //update shopping cart set number
      shoppingCartMapper.updateById(cart);
    } else {
      //如果不存在, 就需要插入一条购物车数据
      //如果dishId不为空
      Long dishId = shoppingCart.getDishId();
      Long setmealId = shoppingCart.getSetmealId();
      //本次传进来是菜品
      if (dishId != null) {
        Dish dish = dishMapper.queryById(dishId);
        shoppingCart.setName(dish.getName());
        shoppingCart.setAmount(dish.getPrice());
        shoppingCart.setImage(dish.getImage());

      } else {
        //本次传进来是套餐
        Setmeal setmeal = setmealMapper.getById(setmealId);
        shoppingCart.setName(setmeal.getName());
        shoppingCart.setAmount(setmeal.getPrice());
        shoppingCart.setImage(setmeal.getImage());
      }

      shoppingCart.setNumber(1);
      shoppingCart.setCreateTime(LocalDateTime.now());
      List<ShoppingCart> shoppingCarts = List.of(shoppingCart);
      shoppingCartMapper.insertBatch(shoppingCarts);
    }


  }

  /**
   * 查看购物车
   * @return
   */
  @Override
  public List<ShoppingCart> list() {
    Long uerId = BaseContext.getCurrentId();
    ShoppingCart cart = ShoppingCart.builder().userId(uerId).build();
    List<ShoppingCart> list = shoppingCartMapper.list(cart);
    return list;
  }

  @Override
  public void cleanShoppingCart() {
    Long uerId = BaseContext.getCurrentId();
    shoppingCartMapper.deleteByUserId(uerId);
  }
}
