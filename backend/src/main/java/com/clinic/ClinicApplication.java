package com.clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 老兵诊所入口。
 * 一个进程里的模块化单体：身份、商品、库存、订单、支付、病例。
 * 先不要拆微服务。病是从边界里长出来的，边界先用包划清。
 * 登录用自己的用户表和 JWT，关掉 Spring Security 自带的内存账号，避免启动时冒出一个随机密码。
 */
@EnableScheduling
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ClinicApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClinicApplication.class, args);
    }
}
