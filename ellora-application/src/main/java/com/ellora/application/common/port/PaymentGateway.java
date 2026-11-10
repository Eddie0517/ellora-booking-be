package com.ellora.application.common.port;

public interface PaymentGateway {
    PaymentLink createSubscriptionLink(long orderCode, long amount, String description);
    PaymentLink createPaymentLink(long orderCode, long amount, String description, String returnUrl, String cancelUrl);
    PaymentStatus getSubscriptionPayment(long orderCode);

    record PaymentLink(String paymentLinkId, String checkoutUrl, String qrCode) {}
    record PaymentStatus(long orderCode, String paymentLinkId, long amount, long amountPaid, String status, String reference) {}
}
