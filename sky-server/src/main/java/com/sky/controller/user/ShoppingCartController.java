package com.sky.controller.user;

import com.sky.dto.DishDTO;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "购物车相关接口")
@RestController
@Slf4j
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {

  @Autowired
  private ShoppingCartService shoppingCartService;
  /**
   * 添加购物车
   * @param shoppingCartDTO
   * @return
   */
  @ApiOperation("添加购物车")
  @PostMapping("/add")
  public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
    log.info("添加购物车, {}", shoppingCartDTO);
    shoppingCartService.addShoppingCart(shoppingCartDTO);
    return Result.success();
  }

  /**
   * 查看购物车
   * @return
   */
  @ApiOperation("查看购物车")
  @GetMapping("/list")
  public Result<List<ShoppingCart>> list() {
    log.info("查看购物车");
    List<ShoppingCart> list = shoppingCartService.list();
    return Result.success(list);
  }

  /**
   * 清空购物车
   * @return
   */
  @ApiOperation("清空购物车")
  @DeleteMapping("/clean")
  public Result clean() {
    log.info("清空购物车");
    shoppingCartService.cleanShoppingCart();
    return Result.success();
  }

  /**
   * 删除菜品
   * @param shoppingCartDTO
   * @return
   */
  @PostMapping("/sub")
  @ApiOperation("删除菜品")
  public Result sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
    log.info("删除菜品, {}", shoppingCartDTO);
    shoppingCartService.sub(shoppingCartDTO);
    return Result.success();
  }
}
