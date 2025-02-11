package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类, 用于创建AliOssUtil对象
 */
@Configuration
@Slf4j
public class OssConfiguration {

  @Bean
  @ConditionalOnMissingBean
  //  这里的 aliOssProperties 参数会由 Spring 容器自动注入，Spring 提供了一种更优雅的依赖注入方式：构造器注入。
  // @Bean 方法的参数自动注入 Spring 会自动寻找容器中类型匹配的 Bean 来注入到方法参数中
  //这是 Spring 的一种隐式依赖注入机制，不需要显式使用 @Autowired  Spring 的依赖注入优先级：
  //构造器注入（最推荐）
  //setter 注入
  //字段注入（@Autowired）（最不推荐）
  public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
    log.info("开始创建阿里云文件上传工具类对象: {}", aliOssProperties );
    return new AliOssUtil(aliOssProperties.getEndpoint(),
        aliOssProperties.getAccessKeyId(),
        aliOssProperties.getAccessKeySecret(),
        aliOssProperties.getBucketName());
  }
}
