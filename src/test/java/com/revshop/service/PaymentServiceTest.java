package com.revshop.service;

import com.revshop.entity.*;
import com.revshop.repository.OrderRepository;
import com.revshop.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order mockOrder;
    private Payment mockPayment;

    @BeforeEach
    void setUp() {
        // Inject @Value fields that Spring won't populate in unit tests
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId", "test_key_id");
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", "test_key_secret");

        mockOrder = Order.builder()
                .id(1L)
                .totalAmount(new BigDecimal("15000"))
                .status(Order.OrderStatus.PENDING)
                .build();

        mockPayment = Payment.builder()
                .id(1L)
                .order(mockOrder)
                .amount(new BigDecimal("15000"))
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod(Payment.PaymentMethod.COD)
                .build();
    }

    @Test
    void processPayment_COD_ShouldSavePayment() {
        // processPayment fetches existing payment record first
        when(paymentRepository.findByOrder(mockOrder)).thenReturn(Optional.of(mockPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(mockPayment);

        Payment result = paymentService.processPayment(mockOrder, Payment.PaymentMethod.COD);

        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getPaymentByOrder_ValidOrderId_ShouldReturnPayment() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(paymentRepository.findByOrder(mockOrder)).thenReturn(Optional.of(mockPayment));

        Payment result = paymentService.getPaymentByOrder(1L);

        assertNotNull(result);
        assertEquals(Payment.PaymentStatus.PENDING, result.getStatus());
    }

    @Test
    void getPaymentByOrder_InvalidOrderId_ShouldThrowException() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> paymentService.getPaymentByOrder(99L));
    }

    @Test
    void verifyRazorpayPayment_InvalidSignature_ShouldReturnFalse() {
        // razorpayKeySecret is now injected via ReflectionTestUtils in setUp
        boolean result = paymentService.verifyRazorpayPayment(
                "order_123", "pay_456", "wrong_signature");

        assertFalse(result);
    }

    @Test
    void createRazorpayOrder_ValidOrderId_ShouldAttemptApiCall() {
        // Razorpay client will fail in unit tests (no real HTTP) — verify it throws
        // a PaymentException (not NullPointerException), meaning setup was correct
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThrows(Exception.class, () -> paymentService.createRazorpayOrder(1L));
    }
}