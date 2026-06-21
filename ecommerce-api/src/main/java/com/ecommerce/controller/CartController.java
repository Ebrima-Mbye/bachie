package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.cart.CartItemRequest;
import com.ecommerce.dto.cart.CartResponse;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart", description = "Shopping cart management (requires authentication)")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get the current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(userDetails.getUsername())));
    }

    @PostMapping("/items")
    @Operation(summary = "Add a product to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addItem(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", response));
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update quantity of a cart item")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.updateItem(userDetails.getUsername(), cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", response));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove an item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        CartResponse response = cartService.removeItem(userDetails.getUsername(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", response));
    }

    @DeleteMapping
    @Operation(summary = "Clear all items from cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
