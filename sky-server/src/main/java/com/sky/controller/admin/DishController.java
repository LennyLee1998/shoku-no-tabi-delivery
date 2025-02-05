package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Api(tags = "菜品相关接口")
@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
  @Autowired
  private DishService dishService;
  @Autowired
  private RedisTemplate redisTemplate;

  /**
   * 新增菜品
   *
   * @param dishDTO
   * @return
   */
  @ApiOperation("新增菜品")
  @PostMapping
  public Result save(@RequestBody DishDTO dishDTO) {
    log.info("新增菜品: {}", dishDTO);
    dishService.saveWithFlavor(dishDTO);

    //清理缓存数据
    String key = "dish_" + dishDTO.getCategoryId();
    cleanCache(key);

    return Result.success();
  }

  /**
   * 菜品分页查询
   *
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
   *
   * @param ids
   * @return
   */
  @DeleteMapping
  @ApiOperation("批量删除菜品")
  public Result delete(@RequestParam List<Long> ids) {
    //这里传入参数的处理是由springMVC完成的, 所以需要加上@RequestParam的注解, 如果query传入数据和参数类型, 变量名一致,则不需要注解
    log.info("删除菜品: {}", ids);
    dishService.deleteBatch(ids);

    //清理缓存数据 (由于可能属于某个分类也可能属于不同分类下的菜品, 在缓存的时候就会变得比较复杂,所以这里直接全部删除比较好)
    cleanCache("dish_*");

    return Result.success();
  }

  /**
   * 根据id查询菜品
   *
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
   *
   * @param dishDTO
   * @return
   */
  @PutMapping
  @ApiOperation("修改菜品")
  public Result updateDish(@RequestBody DishDTO dishDTO) {
    log.info("修改菜品: {}", dishDTO);
    dishService.updateDish(dishDTO);

    //清理缓存数据(如果修改的是普通的数据, 可以只删除一个
    // 如果修改的是分类id那么这里redis里面必须把之前的对应键值对删掉还有新改的也删掉
    //修改操作不是常规操作, 不需要把这里的业务逻辑写得过于复杂, 所以可以都删掉
    cleanCache("dish_*");

    return Result.success();
  }

  /**
   * 根据分类id查询菜品
   *
   * @param categoryId
   * @return
   */
  @ApiOperation("根据分类id查询菜品")
  @GetMapping("/list")
  public Result<List<Dish>> listByCategoryId(Long categoryId) {
    log.info("根据分类id查询菜品: {}", categoryId);
    List<Dish> dishList = dishService.getByCategoryId(categoryId);
    return Result.success(dishList);
  }

  /**
   * 启售禁售菜品
   * @param status
   * @param id
   * @return
   */
  @PostMapping("/status/{status}")
  @ApiOperation("启售禁售菜品")
  public Result<String> startOrStop(@PathVariable("status") Integer status, Long id){
    dishService.startOrStop(status,id);

    //这里如果要精确清理的话, 需要做sql查询, 还不如直接删掉
    cleanCache("dish_*");

    return Result.success();
  }

  /**
   * 清除缓存数据
   * @param pattern
   */
  private void cleanCache(String pattern) {
    Set keys = redisTemplate.keys(pattern);
    redisTemplate.delete(keys);
  }
}
