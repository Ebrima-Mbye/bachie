package com.ecommerce.config;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.model.Cart;
import com.ecommerce.model.enums.Role;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the database with initial data on first run.
 * Admin user: admin@ecommerce.com / admin123
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedCategories();
        seedProducts();
    }

    private void seedAdminUser() {
        if (!userRepository.existsByEmail("admin@ecommerce.com")) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@ecommerce.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .orders(new ArrayList<>())
                    .build();
            User saved = userRepository.save(admin);

            Cart cart = Cart.builder()
                    .user(saved)
                    .cartItems(new ArrayList<>())
                    .build();
            cartRepository.save(cart);

            log.info("Admin user seeded: admin@ecommerce.com / admin123");
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                    Category.builder().name("Electronics").description("Phones, Laptops, and Gadgets")
                            .products(new ArrayList<>()).build(),
                    Category.builder().name("Clothing").description("Men and Women Apparel").products(new ArrayList<>())
                            .build(),
                    Category.builder().name("Books").description("Fiction, Non-Fiction, and Educational Books")
                            .products(new ArrayList<>()).build(),
                    Category.builder().name("Home & Kitchen").description("Furniture, Appliances, and Cookware")
                            .products(new ArrayList<>()).build(),
                    Category.builder().name("Sports").description("Sports Equipment and Accessories")
                            .products(new ArrayList<>()).build());
            categoryRepository.saveAll(categories);
            log.info("Categories seeded: {}", categories.size());
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            Category electronics = categoryRepository.findByNameIgnoreCase("Electronics").orElse(null);
            Category clothing = categoryRepository.findByNameIgnoreCase("Clothing").orElse(null);
            Category books = categoryRepository.findByNameIgnoreCase("Books").orElse(null);

            List<Product> products = new ArrayList<>();

            if (electronics != null) {
                products.add(Product.builder().name("iPhone 15 Pro")
                        .description("Latest Apple flagship smartphone with A17 Pro chip")
                        .price(new BigDecimal("999.99")).stockQuantity(50).imageUrl("https://example.com/iphone15.jpg")
                        .category(electronics).build());
                products.add(Product.builder().name("Samsung Galaxy S24")
                        .description("Samsung flagship with AI features").price(new BigDecimal("849.99"))
                        .stockQuantity(40).imageUrl("https://example.com/galaxys24.jpg").category(electronics).build());
                products.add(Product.builder().name("MacBook Air M3")
                        .description("Ultra-thin laptop with Apple M3 chip").price(new BigDecimal("1299.99"))
                        .stockQuantity(30).imageUrl("https://example.com/macbook.jpg").category(electronics).build());
            }
            if (clothing != null) {
                products.add(Product.builder().name("Classic White T-Shirt").description("100% cotton premium t-shirt")
                        .price(new BigDecimal("19.99")).stockQuantity(200).imageUrl("https://example.com/tshirt.jpg")
                        .category(clothing).build());
                products.add(Product.builder().name("Slim Fit Jeans").description("Comfortable stretch denim jeans")
                        .price(new BigDecimal("49.99")).stockQuantity(150).imageUrl("https://example.com/jeans.jpg")
                        .category(clothing).build());
            }
            if (books != null) {
                products.add(Product.builder().name("Clean Code")
                        .description("A Handbook of Agile Software Craftsmanship by Robert C. Martin")
                        .price(new BigDecimal("34.99")).stockQuantity(100).imageUrl("https://example.com/cleancode.jpg")
                        .category(books).build());
                products.add(Product.builder().name("Spring Boot in Action")
                        .description("A practical guide to Spring Boot framework").price(new BigDecimal("44.99"))
                        .stockQuantity(80).imageUrl("https://example.com/springboot.jpg").category(books).build());
            }

            productRepository.saveAll(products);
            log.info("Products seeded: {}", products.size());
        }
    }
}
