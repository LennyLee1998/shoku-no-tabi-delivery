package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@Api(tags = "店铺相关接口")
@RestController("userShopController")
@RequestMapping("/user/shop")
@Slf4j
public class ShopController {
  public static final String KEY = "SHOP STATUS";

  @Autowired
  private RedisTemplate redisTemplate;



  /**
   * 查询营业状态
   * @return
   */
  @ApiOperation("查询营业状态")
  @GetMapping("/status")
  public Result<Integer> getStatus() {
    ValueOperations valueOperations = redisTemplate.opsForValue();
    Integer status = (Integer) valueOperations.get(KEY);
    log.info("查询营业状态: {}", status == 1? "营业中":"打烊中");

    return Result.success(status);
  }
}
