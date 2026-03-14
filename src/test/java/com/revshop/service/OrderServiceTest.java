package com.revshop.service;

import com.revshop.dto.CheckoutDTO;
import com.revshop.dto.OrderDTO;
import com.revshop.entity.*;
import com.revshop.exception.BadRequestException;
import com.revshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CartService cartService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    private User mockBuyer;
    private Product mockProduct;
    private Order mockOrder;
    private CheckoutDTO checkoutDTO;

    @BeforeEach
    void setUp() {

        mockBuyer = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("buyer@test.com")
                .role(User.Role.BUYER)
                .enabled(true)
                .build();

        mockProduct = Product.builder()
                .id(1L)
                .name("Test Phone")
                .price(new BigDecimal("15000"))
                .mrp(new BigDecimal("18000"))
                .discountPercent(17)
                .stockQuantity(10)
                .active(true)
                .seller(mockBuyer)
                .build();

        mockOrder = Order.builder()
                .id(1L)
                .buyer(mockBuyer)
                .totalAmount(new BigDecimal("15000"))
                .status(Order.OrderStatus.PENDING)
                .shippingAddress("123 Main Street")
                .city("Hyderabad")
                .state("Telangana")
                .pincode("500001")
                .orderItems(new ArrayList<>())
                .build();

        checkoutDTO = new CheckoutDTO();
        checkoutDTO.setShippingAddress("123 Main Street");
        checkoutDTO.setCity("Hyderabad");
        checkoutDTO.setState("Telangana");
        checkoutDTO.setPincode("500001");
        checkoutDTO.setPaymentMethod(Payment.PaymentMethod.COD);
    }

    @Test
    void placeOrder_WithItemsInCart_ShouldCreateOrder() {

        CartItem cartItem = CartItem.builder()
                .product(mockProduct)
                .quantity(1)
                .build();

        when(userRepository.findByEmail("buyer@test.com"))
                .thenReturn(Optional.of(mockBuyer));

        when(cartService.getCartItems("buyer@test.com"))
                .thenReturn(List.of(cartItem));

        when(cartService.calculateTotal("buyer@test.com"))
                .thenReturn(new BigDecimal("15000"));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(mockOrder);

        doNothing().when(productService)
                .reduceStock(anyLong(), anyInt());

        doNothing().when(notificationService)
                .sendOrderNotification(anyString(), anyString(), any());

        Order result = orderService.placeOrder("buyer@test.com", checkoutDTO);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart("buyer@test.com");
        verify(productService).reduceStock(anyLong(), anyInt());
    }

    @Test
    void placeOrder_EmptyCart_ShouldThrowException() {

        when(userRepository.findByEmail("buyer@test.com"))
                .thenReturn(Optional.of(mockBuyer));

        when(cartService.getCartItems("buyer@test.com"))
                .thenReturn(new ArrayList<>());

        assertThrows(BadRequestException.class,
                () -> orderService.placeOrder("buyer@test.com", checkoutDTO));
    }

    @Test
    void getOrderHistory_ValidBuyer_ShouldReturnList() {

        when(userRepository.findByEmail("buyer@test.com"))
                .thenReturn(Optional.of(mockBuyer));

        when(orderRepository.findByBuyerWithItems(mockBuyer))
                .thenReturn(List.of(mockOrder));

        when(paymentRepository.findByOrder(any()))
                .thenReturn(Optional.empty());

        List<OrderDTO> result = orderService.getOrderHistory("buyer@test.com");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void cancelOrder_PendingOrder_ShouldCancel() {

        mockOrder.setOrderItems(List.of(
                OrderItem.builder()
                        .product(mockProduct)
                        .quantity(1)
                        .build()
        ));

        when(orderRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(mockOrder));

        when(orderRepository.save(any()))
                .thenReturn(mockOrder);

        when(productRepository.save(any(Product.class)))
                .thenReturn(mockProduct);

        doNothing().when(notificationService)
                .sendOrderNotification(anyString(), anyString(), any());

        doNothing().when(notificationService)
                .sendNotification(anyString(), anyString(), anyString(), any());

        assertDoesNotThrow(() ->
                orderService.cancelOrder(1L, "buyer@test.com"));

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void cancelOrder_DeliveredOrder_ShouldThrowException() {

        mockOrder.setStatus(Order.OrderStatus.DELIVERED);

        when(orderRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(mockOrder));

        assertThrows(BadRequestException.class,
                () -> orderService.cancelOrder(1L, "buyer@test.com"));
    }

    @Test
    void updateOrderStatus_ValidOrder_ShouldUpdateStatus() {

        when(orderRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(mockOrder));

        when(orderRepository.save(any()))
                .thenReturn(mockOrder);

        doNothing().when(notificationService)
                .sendOrderNotification(anyString(), anyString(), any());

        assertDoesNotThrow(() ->
                orderService.updateOrderStatus(1L, Order.OrderStatus.SHIPPED));

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {

        when(orderRepository.findAllWithDetails())
                .thenReturn(List.of(mockOrder));

        when(paymentRepository.findByOrder(any()))
                .thenReturn(Optional.empty());

        List<OrderDTO> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}