package com.technical_test_backend.technical_test_backend.model;

import java.util.Map;

public class ProductDetail {
    private final Number productId;
    private final Map<String, Object> productData;
    private final boolean timeout;

    public ProductDetail(Number productId, Map<String, Object> productData, boolean timeout) {
        this.productId = productId;
        this.productData = productData;
        this.timeout = timeout;
    }

    public Number getProductId() {
        return productId;
    }

    public Map<String, Object> getProductData() {
        return productData;
    }

    public boolean isTimeout() {
        return timeout;
    }
}
