package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "套餐相关接口")
@RestController
@Slf4j
@RequestMapping("/admin/setmeal")
public class SetmealController {
  @Autowired
  private SetmealService setmealService;

  /**
   * 新增套餐
   * @param setmealDTO
   * @return
   */
  @PostMapping
  @ApiOperation("新增套餐")
  public Result create(@RequestBody SetmealDTO setmealDTO) {
    log.info("新增套餐: {}", setmealDTO);

    setmealService.create(setmealDTO);
    return Result.success();
  }

  /**
   * 按条件分页查询
   * @param setmealPageQueryDTO
   * @return
   */
  @ApiOperation("按条件分页查询")
  @GetMapping("/page")
  public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
    log.info("按条件分页查询: {}", setmealPageQueryDTO);
    PageResult pageResult = setmealService.page(setmealPageQueryDTO);
    return Result.success(pageResult);
  }

  /**
   * 批量删除套餐
   * @param ids
   * @return
   */
  @ApiOperation("批量删除套餐")
  @DeleteMapping
  public Result delete(@RequestParam List<Long> ids) {
    log.info("批量删除套餐: {}", ids);
    setmealService.deleteBatch(ids);
    return Result.success();
  }


  /**
   * 根据id查询套餐
   * @param id
   * @return
   */
  @ApiOperation("根据id查询套餐")
  @GetMapping("/{id}")
  public Result<SetmealVO> querySetmeal(@PathVariable Long id) {
    log.info("根据id查询套餐: {}", id);
    SetmealVO setmealVO = setmealService.getById(id);
    return Result.success(setmealVO);
  }

  /**
   * 修改套餐
   * @param setmealDTO
   * @return
   */
  @ApiOperation("修改套餐")
  @PutMapping
  public Result update(@RequestBody SetmealDTO setmealDTO) {
    log.info("修改套餐: {}", setmealDTO);
    setmealService.update(setmealDTO);
    return Result.success();
  }

  /**
   * 起售停售套餐
   * @param status
   * @return
   */
  @PostMapping("/status/{status}")
  @ApiOperation("起售停售套餐")
  public Result updateStatus(@PathVariable Integer status, Long id) {
    log.info("起售停售套餐: {}, {}", status, id);
    setmealService.updateStatus(status, id);
    return  Result.success();
  }
}
