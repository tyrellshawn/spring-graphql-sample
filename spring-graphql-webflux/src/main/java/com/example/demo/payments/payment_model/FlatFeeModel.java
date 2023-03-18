package com.example.demo.payments.payment_model;

import lombok.Data;

@Data
public class FlatFeeModel extends PaymentModel {
    private double flatFee;

    public FlatFeeModel(double flatFee) {
        this.flatFee = flatFee;
    }

    public double getFlatFee() {
        return flatFee;
    }

    public void setFlatFee(double flatFee) {
        this.flatFee = flatFee;
    }
}
