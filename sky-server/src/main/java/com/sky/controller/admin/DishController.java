package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "菜品相关接口")
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
  @Autowired
  private DishService dishService;

  /**
   * 新增菜品
   * @param dishDTO
   * @return
   */
  @ApiOperation("新增菜品")
  @PostMapping
  public Result save(@RequestBody DishDTO dishDTO) {
    log.info("新增菜品: {}", dishDTO);
    dishService.saveWithFlavor(dishDTO);
    return Result.success();
  }

  /**
   * 菜品分页查询
   * @param dishPageQueryDTO
   * @return
   */
  @GetMapping("/page")
  @ApiOperation("菜品分页查询")
  public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
    log.info("菜品分页查询: {}", dishPageQueryDTO);
    PageResult pageResult = dishService.page(dishPageQueryDTO);
    return Result.success(pageResult);
  }

  /**
   * 删除菜品
   * @param ids
   * @return
   */
  @DeleteMapping
  @ApiOperation("批量删除菜品")
  public Result delete(@RequestParam List<Long> ids) {
    //这里传入参数的处理是由springMVC完成的, 所以需要加上@RequestParam的注解, 如果query传入数据和参数类型, 变量名一致,则不需要注解
    log.info("删除菜品: {}", ids);
    dishService.deleteBatch(ids);
    return Result.success();
  }

  /**
   * 根据id查询菜品
   * @param id
   * @return
   */
  @GetMapping("/{id}")
  @ApiOperation("根据id查询菜品")
  public Result<DishVO> queryDish(@PathVariable Long id) {
    log.info("根据id查询菜品: {}", id);
    DishVO dishVO = dishService.queryDish(id);
    return Result.success(dishVO);
  }

  /**
   * 修改菜品
   * @param dishDTO
   * @return
   */
  @PutMapping
  @ApiOperation("修改菜品")
  public Result updateDish(@RequestBody DishDTO dishDTO) {
    log.info("修改菜品: {}", dishDTO);
    dishService.updateDish(dishDTO);
    return Result.success();
  }

  /**
   * 启用、禁用菜品
   * @param status
   * @param id
   * @return
   */
  @PostMapping("/status/{status}")
  @ApiOperation("启用禁用菜品")
  public Result<String> startOrStop(@PathVariable("status") Integer status, Long id){
    dishService.startOrStop(status,id);
    return Result.success();
  }
}

