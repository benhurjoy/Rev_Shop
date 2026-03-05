package com.revshop.service;

import com.revshop.dto.CheckoutDTO;
import com.revshop.dto.OrderDTO;
import com.revshop.entity.*;
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
import static org.mockito.ArgumentMatchers.any;
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
        Cart cart = Cart.builder()
                .cartItems(List.of(cartItem))
                .build();

        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(cartService.getOrCreateCart("buyer@test.com")).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

        Order result = orderService.placeOrder("buyer@test.com", checkoutDTO);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart("buyer@test.com");
    }

    @Test
    void placeOrder_EmptyCart_ShouldThrowException() {
        Cart emptyCart = Cart.builder().cartItems(new ArrayList<>()).build();
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(cartService.getOrCreateCart("buyer@test.com")).thenReturn(emptyCart);

        assertThrows(Exception.class,
                () -> orderService.placeOrder("buyer@test.com", checkoutDTO));
    }

    @Test
    void getOrderHistory_ValidBuyer_ShouldReturnList() {
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(orderRepository.findByBuyerOrderByOrderedAtDesc(mockBuyer))
                .thenReturn(List.of(mockOrder));

        List<OrderDTO> result = orderService.getOrderHistory("buyer@test.com");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void cancelOrder_PendingOrder_ShouldCancel() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));
        when(orderRepository.save(any())).thenReturn(mockOrder);

        assertDoesNotThrow(() -> orderService.cancelOrder(1L, "buyer@test.com"));

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void cancelOrder_DeliveredOrder_ShouldThrowException() {
        mockOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(userRepository.findByEmail("buyer@test.com")).thenReturn(Optional.of(mockBuyer));

        assertThrows(Exception.class,
                () -> orderService.cancelOrder(1L, "buyer@test.com"));
    }

    @Test
    void updateOrderStatus_ValidOrder_ShouldUpdateStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any())).thenReturn(mockOrder);

        assertDoesNotThrow(() ->
                orderService.updateOrderStatus(1L, Order.OrderStatus.SHIPPED));

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        when(orderRepository.findAllByOrderByOrderedAtDesc()).thenReturn(List.of(mockOrder));

        List<OrderDTO> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}