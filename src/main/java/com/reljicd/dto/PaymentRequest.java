package com.reljicd.dto;  // bạn có thể dùng package khác nếu muốn

public class PaymentRequest {
    private Double amount;
    private String currency;

    public PaymentRequest() {}

    public PaymentRequest(Double amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    // Getter và Setter dùng để lấy và gán giá trị
    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
