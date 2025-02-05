package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

  /**
   * 动态条件查询
   *
   * @param shoppingCart
   * @return
   */
  List<ShoppingCart> list(ShoppingCart shoppingCart);


  /**
   * 根据id修改商品数量
   *
   * @param cart
   */
  @Update("update shopping_cart set number = #{number} where id = #{id}")
  void updateById(ShoppingCart cart);

  /**
   *
   * @param shoppingCart
   */
  @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, amount, create_time) values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{amount}, #{createTime})")
  void insert(ShoppingCart shoppingCart);

  /**
   * 根据用户id删除购物车
   * @param uerId
   */
  @Delete("delete from shopping_cart where user_id = #{userId}")
  void deleteByUserId(Long uerId);
}
