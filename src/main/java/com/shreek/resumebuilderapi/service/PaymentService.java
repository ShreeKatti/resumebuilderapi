package com.shreek.resumebuilderapi.service;


import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.shreek.resumebuilderapi.document.Payment;
import com.shreek.resumebuilderapi.document.User;
import com.shreek.resumebuilderapi.dto.AuthResponse;
import com.shreek.resumebuilderapi.repository.PaymentRepository;
import com.shreek.resumebuilderapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.util.List;
import java.util.UUID;

import static com.shreek.resumebuilderapi.utils.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    public Payment createOrder(Object principal, String planType) throws RazorpayException {
        //Initial Step
        AuthResponse authResponse = authService.getProfile(principal);


        //Step 1: Initialize the razorpay client
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId , razorpaySecret);

        //step 2: prepare the Json to pass the razorpay
        int amount =99900;
        String currency ="INR";
        String receipt = PREMIUM+"_"+ UUID.randomUUID().toString().substring(0,8);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount",amount);
        orderRequest.put("currency",currency);
        orderRequest.put("receipt",receipt);

        //Step 3: Call the razorpay Api to create order
        Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        //step 4: Save the order Details into database
        Payment newPayment = Payment.builder()
                .userId(authResponse.getId())
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(amount)
                .currency(currency)
                .planType(planType)
                .status("created")
                .receipt(receipt)
                .build();

        //step 5:return the result
        return paymentRepository.save(newPayment);
    }

    public boolean verifyPayment(String razorpayPaymentId, String razorpaySignature, String razorpayOrderId) throws RazorpayException {
        try{
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);
            attributes.put("razorpay_order_id", razorpayOrderId);

            boolean isValidSignature = Utils.verifyPaymentSignature(attributes,razorpaySecret);

            if(isValidSignature){
                //update the Payment status
                Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow(() -> new RuntimeException("Payment not found"));
                payment.setRazorpayPaymentId(razorpayPaymentId);
                payment.setRazorpaySignature(razorpaySignature);
                payment.setStatus("Paid");
                paymentRepository.save(payment);

                //upgrade the user subscription
                upgradeUserSubscription(payment.getUserId(),payment.getPlanType());
                return true;

            }
            return  false;
        } catch (Exception e) {
            log.error("Error verifying the payment",e);
            return false;

        }
    }

    private void upgradeUserSubscription(String userId, String planType) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existingUser.setSubscriptionPlan(planType);
        userRepository.save(existingUser);
        log.info("User {} has been upgraded to {} plan", userId,planType);
    }

    public List<Payment> getUserPayments(Object principal) {
        //Step 1: Get the Current profile
        AuthResponse authResponse = authService.getProfile(principal);

        //step 2: Call the repo finder method
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(authResponse.getId());
    }

    public Payment getPaymentDetails(String orderId) {
        //Step 1: Call the repo finder method
        return paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
}
