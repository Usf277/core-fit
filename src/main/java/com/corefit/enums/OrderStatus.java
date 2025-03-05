package com.corefit.enums;

public enum OrderStatus {
    ORDER_RECEIVED, // new
    ORDER_CONFIRMED,  // current
    ORDER_UNDER_PREPARATION, // current
    ORDER_UNDER_DELIVERY, // current
    ORDER_DELIVERED, /// completed   // previous
    ORDER_CANCELED, /// completed     // previous
}
