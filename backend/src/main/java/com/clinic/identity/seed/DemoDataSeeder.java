package com.clinic.identity.seed;

import com.clinic.catalog.domain.Sku;
import com.clinic.catalog.repo.SkuRepository;
import com.clinic.identity.domain.UserAccount;
import com.clinic.identity.repo.UserRepository;
import com.clinic.inventory.domain.InventoryStock;
import com.clinic.inventory.repo.InventoryStockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 第一次启动写入演示数据。已经有用户就什么都不做，避免把你造的病例数据冲掉。
 */
@Slf4j
@Component
@Order(0)
public class DemoDataSeeder implements ApplicationRunner {

    private final UserRepository users;
    private final SkuRepository skus;
    private final InventoryStockRepository stocks;
    private final PasswordEncoder encoder;

    public DemoDataSeeder(UserRepository users, SkuRepository skus, InventoryStockRepository stocks,
                          PasswordEncoder encoder) {
        this.users = users;
        this.skus = skus;
        this.stocks = stocks;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        users.save(UserAccount.create("demo", encoder.encode("demo123"), "演示医生", "ADMIN"));
        seed("TEA", "龙井试饮", 2800, 20);
        seed("MUG", "夜班马克杯", 3900, 8);
        seed("BOOK", "并发这件小事", 6800, 5);
        log.info("演示数据已写入。账号 demo / demo123");
    }

    private void seed(String code, String name, long priceCent, int qty) {
        var sku = skus.save(Sku.create(code, name, priceCent));
        stocks.save(InventoryStock.init(sku.getId(), qty));
    }
}
