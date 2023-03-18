package com.example.demo.payments.payment_model;

import lombok.Data;

@Data
public class PayPerUseModel extends PaymentModel {
    private double pricePerUse;

    public PayPerUseModel(double pricePerUse) {
        this.pricePerUse = pricePerUse;
    }

    public double getPricePerUse() {
        return pricePerUse;
    }

    public void setPricePerUse(double pricePerUse) {
        this.pricePerUse = pricePerUse;
    }
}
