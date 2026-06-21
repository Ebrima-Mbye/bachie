package com.ecommerce.service;

import com.ecommerce.dto.order.OrderRequest;
import com.ecommerce.dto.order.OrderResponse;
import com.ecommerce.dto.order.OrderStatusRequest;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.model.*;
import com.ecommerce.model.enums.OrderStatus;
import com.ecommerce.model.enums.Role;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public Page<OrderResponse> getMyOrders(String email, int page, int size) {
        User user = getUserByEmail(email);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    public OrderResponse getOrderById(String email, Long orderId) {
        User user = getUserByEmail(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("You don't have permission to view this order.");
        }

        return toResponse(order);
    }

    @Transactional
    public OrderResponse placeOrder(String email, OrderRequest request) {
        User user = getUserByEmail(email);
        Cart cart = cartService.getCartByEmail(email);

        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty. Add products before placing an order.");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .orderItems(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity());
            }

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            orderItems.add(orderItem);

            // Decrease stock
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        savedOrder.setOrderItems(orderItems);
        savedOrder.setTotalAmount(totalAmount);
        Order finalOrder = orderRepository.save(savedOrder);

        // Clear the cart after successful order
        cartService.clearCart(email);

        return toResponse(finalOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a " + order.getStatus().name().toLowerCase() + " order.");
        }

        order.setStatus(request.getStatus());
        return toResponse(orderRepository.save(order));
    }

    public Page<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return OrderResponse.OrderItemResponse.builder()
                            .orderItemId(item.getId())
                            .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                            .productName(item.getProduct() != null ? item.getProduct().getName() : "Product removed")
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .subtotal(subtotal)
                            .build();
                })
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
