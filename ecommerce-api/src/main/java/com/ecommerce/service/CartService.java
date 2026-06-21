package com.ecommerce.service;

import com.ecommerce.dto.cart.CartItemRequest;
import com.ecommerce.dto.cart.CartResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartResponse getCart(String email) {
        Cart cart = getCartByEmail(email);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String email, CartItemRequest request) {
        Cart cart = getCartByEmail(email);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return toResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional
    public CartResponse updateItem(String email, Long cartItemId, CartItemRequest request) {
        Cart cart = getCartByEmail(email);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart.");
        }

        if (item.getProduct().getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + item.getProduct().getStockQuantity());
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return toResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional
    public CartResponse removeItem(String email, Long cartItemId) {
        Cart cart = getCartByEmail(email);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart.");
        }

        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);

        return toResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    @Transactional
    public void clearCart(String email) {
        Cart cart = getCartByEmail(email);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    public Cart getCartByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartResponse.CartItemResponse> items = cart.getCartItems().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return CartResponse.CartItemResponse.builder()
                            .cartItemId(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .imageUrl(item.getProduct().getImageUrl())
                            .unitPrice(item.getProduct().getPrice())
                            .quantity(item.getQuantity())
                            .subtotal(subtotal)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal totalPrice = items.stream()
                .map(CartResponse.CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalPrice(totalPrice)
                .totalItems(items.size())
                .build();
    }
}
