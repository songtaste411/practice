package com.clinic.config;

import com.clinic.shared.switcher.CaseSwitch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时把训练场状态打到日志里。出问题先看这三行。
 */
@Slf4j
@Component
public class BootstrapLogger implements ApplicationRunner {

    private final CaseSwitch caseSwitch;
    private final boolean redisEnabled;
    private final String jwtSecret;

    public BootstrapLogger(CaseSwitch caseSwitch,
                           @Value("${clinic.redis.enabled:false}") boolean redisEnabled,
                           @Value("${clinic.jwt.secret}") String jwtSecret) {
        this.caseSwitch = caseSwitch;
        this.redisEnabled = redisEnabled;
        this.jwtSecret = jwtSecret;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("老兵诊所已启动。库存策略开关={}，Redis={}", caseSwitch.get(), redisEnabled);
        log.info("演示账号 demo / demo123 。网页看 README 的启动步骤");
        if (jwtSecret.contains("change-me")) {
            log.warn("JWT 密钥还是示例值，只可用于本机训练");
        }
    }
}
