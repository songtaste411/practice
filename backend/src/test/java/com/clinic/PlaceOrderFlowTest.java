package com.clinic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 健康路径的体检：同一幂等键不能扣两次库存；库存不够要拒绝且不把库存扣成负数。
 */
@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("local")
class PlaceOrderFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Test
    void sameKeyDoesNotReserveTwice() throws Exception {
        var token = login();
        var body = """
                {"buyerName":"测试","lines":[{"skuCode":"MUG","quantity":1}]}
                """;
        var first = postOrder(token, "test-key-mug", body);
        var second = postOrder(token, "test-key-mug", body);
        assertEquals(first.get("data").get("orderNo").asText(), second.get("data").get("orderNo").asText());
        assertFalse(first.get("data").get("replayed").asBoolean());
        assertTrue(second.get("data").get("replayed").asBoolean());

        var stock = read(mvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        var mug = findSku(stock.get("data"), "MUG");
        assertEquals(7, mug.get("availableQty").asInt());
        assertEquals(1, mug.get("reservedQty").asInt());
    }

    @Test
    void notEnoughStockLeavesNumbersUntouched() throws Exception {
        var token = login();
        var before = available(token, "BOOK");
        var body = """
                {"buyerName":"测试","lines":[{"skuCode":"BOOK","quantity":999}]}
                """;
        var result = read(mvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "test-key-book-too-many")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertEquals(40901, result.get("code").asInt());
        assertEquals(before, available(token, "BOOK"));
    }

    private int available(String token, String sku) throws Exception {
        var stock = read(mvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
        return findSku(stock.get("data"), sku).get("availableQty").asInt();
    }

    private JsonNode postOrder(String token, String key, String body) throws Exception {
        return read(mvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private String login() throws Exception {
        var raw = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"demo123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return read(raw).get("data").get("token").asText();
    }

    private JsonNode read(String raw) throws Exception {
        return json.readTree(raw);
    }

    private JsonNode findSku(JsonNode array, String code) {
        for (var node : array) {
            if (code.equals(node.get("skuCode").asText())) {
                return node;
            }
        }
        throw new AssertionError("找不到商品 " + code);
    }
}
