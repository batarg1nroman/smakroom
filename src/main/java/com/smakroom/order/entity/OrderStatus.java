package com.smakroom.order.entity;

public enum OrderStatus {
    PENDING("Ожидает подтверждения"),
    CONFIRMED("Подтверждён"),
    IN_PROGRESS("Готовится"),
    READY("Готов к выдаче"),
    COMPLETED("Выдан"),
    CANCELLED("Отменён");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
