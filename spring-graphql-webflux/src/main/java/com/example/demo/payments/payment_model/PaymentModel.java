package com.example.demo.payments.payment_model;

import lombok.Data;

@Data
public abstract class PaymentModel {
    private Integer modelId;
    private String modelName;
    private String modelDescription;
    private String modelType;
}
