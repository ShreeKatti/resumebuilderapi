package com.shreek.resumebuilderapi.controller;

import com.razorpay.RazorpayException;
import com.shreek.resumebuilderapi.document.Payment;
import com.shreek.resumebuilderapi.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.shreek.resumebuilderapi.utils.AppConstants.PREMIUM;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String,String > request,
                                          Authentication authentication  ) throws RazorpayException {
        //Step 1: Validate the request
        String planType = request.get("planType");
        if(!PREMIUM.equalsIgnoreCase(planType)){
            return ResponseEntity.badRequest().body(Map.of("message","Invalid Plan Type"));
        }
        //Step 2: Call the Service Method
        Payment payment = paymentService.createOrder(authentication.getPrincipal(),planType);

        //Step 3: Prepare the response object
        Map<String,Object> response = Map.of(
          "orderId",payment.getRazorpayOrderId(),
          "amount",payment.getAmount(),
          "currency",payment.getCurrency(),
          "receipt",payment.getReceipt()
        );

        //Step 4: Return the Response
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) throws RazorpayException {
        //Step 1: Validate the request
        String razorpayOrderId = request.get("razorpay_order-id");
        String razorpayPaymentId = request.get("razorpay_payment-id");
        String razorpaySignature = request.get("razorpay_signature");

        if(Objects.isNull(razorpayPaymentId)||
                Objects.isNull(razorpayOrderId)||
                Objects.isNull(razorpaySignature)){
            return ResponseEntity.badRequest().body(Map.of("message","Missing required payment parameters "));
        }

        //Step 2: Call the Service Method
        boolean isValid = paymentService.verifyPayment(razorpayPaymentId,razorpaySignature,razorpayOrderId);

        //Step 3: Return the response
        if (isValid){
            return ResponseEntity.ok(Map.of(
                    "message","Payment successfully verified",
                    "status","success"
            ));
        }else{
            return ResponseEntity.badRequest().body(Map.of("message ","Payment not found"));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(Authentication authentication){
        //Step 1: Call the Service
        List<Payment> payments = paymentService.getUserPayments(authentication.getPrincipal());

        //Step 2: return the response
        return ResponseEntity.ok(payments);

    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId){
        //Step 1: Call the Service Method
        Payment paymentDetails = paymentService.getPaymentDetails(orderId);

        //step 2: return the Response
        return ResponseEntity.ok(paymentDetails);
    }
}


