package com.clinic.ordering.domain;

public enum OrderStatus {
    /** 库存已预占，等支付。 */
    RESERVED,
    /** 支付已确认。 */
    PAID,
    /** 留给以后的取消病例。当前主链路不会写入这个状态。 */
    CANCELLED
}
