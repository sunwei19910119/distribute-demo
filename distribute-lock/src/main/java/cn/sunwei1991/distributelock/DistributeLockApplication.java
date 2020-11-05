package cn.sunwei1991.distributelock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Title DistributeLockApplication
 * @Description
 * @Author SunWei
 * @Create 2020/11/3 3:29 下午
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("cn.sunwei1991.distributelock.dao")
public class DistributeLockApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributeLockApplication.class, args);
    }
}
