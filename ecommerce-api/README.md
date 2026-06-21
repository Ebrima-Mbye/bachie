# 🛒 E-Commerce REST API

A full-featured e-commerce backend built with **Spring Boot 3**, **PostgreSQL**, and **JWT Authentication**.

> **Repository**: `java2_teamX_ecommerce_api`

---

## 📚 Tech Stack

| Layer         | Technology                            |
| ------------- | ------------------------------------- |
| Language      | Java 17                               |
| Framework     | Spring Boot 3.2.5                     |
| Build Tool    | Apache Maven                          |
| Database      | PostgreSQL                            |
| ORM           | Spring Data JPA / Hibernate           |
| Security      | Spring Security 6 + JWT (jjwt 0.12.3) |
| Documentation | Swagger UI (SpringDoc OpenAPI 2)      |
| Utilities     | Lombok                                |

---

## 🏗️ Architecture

The project follows a clean **Layered Architecture**:

```
Controller → Service → Repository → Database
```

```
src/main/java/com/ecommerce/
├── config/               # Security, OpenAPI, DataInitializer
├── controller/           # REST controllers (Auth, Product, Category, Cart, Order)
├── dto/                  # Request & Response DTOs
│   ├── auth/
│   ├── product/
│   ├── category/
│   ├── cart/
│   └── order/
├── exception/            # Custom exceptions + Global handler
├── model/                # JPA Entities
│   └── enums/            # Role, OrderStatus
├── repository/           # Spring Data JPA repositories
├── security/             # JWT provider, filter, UserDetailsService
└── service/              # Business logic
```

---

## ⚙️ Prerequisites

- **Java 17+** — [Download JDK](https://adoptium.net/)
- **Maven 3.8+** — [Download Maven](https://maven.apache.org/download.cgi)
- **PostgreSQL 14+** — [Download](https://www.postgresql.org/download/)
- **pgAdmin 4** (optional, for DB GUI) — [Download](https://www.pgadmin.org/)
- **Postman** (for API testing) — [Download](https://www.postman.com/)

---

## 🚀 Setup & Run

### 1. Create the PostgreSQL Database

Open **pgAdmin** or `psql` and run:

```sql
CREATE DATABASE ecommerce_db;
```

Then run the schema script at:

```
src/main/resources/db/schema.sql
```

### 2. Configure Database Credentials

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: postgres # your PostgreSQL username
    password: your_password_here # your PostgreSQL password
```

### 3. Build & Run

```bash
# Navigate to project root
cd ecommerce-api

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The API will start at: **http://localhost:8080**

### 4. Swagger UI

Open your browser and go to:

```
http://localhost:8080/swagger-ui.html
```

---

## 🔐 Authentication

The API uses **JWT Bearer Token** authentication.

**Flow:**

1. `POST /api/auth/register` — Create account → get token
2. `POST /api/auth/login` — Login → get token
3. Add the token to every protected request as a header:
   ```
   Authorization: Bearer <your_token_here>
   ```

**Seeded Admin Account** (available immediately after first run):

```
Email:    admin@ecommerce.com
Password: admin123
Role:     ADMIN
```

---

## 📡 API Endpoints

### 🔑 Authentication

| Method | Endpoint             | Access | Description             |
| ------ | -------------------- | ------ | ----------------------- |
| POST   | `/api/auth/register` | Public | Register a new user     |
| POST   | `/api/auth/login`    | Public | Login and get JWT token |

### 📦 Products

| Method | Endpoint                              | Access | Description                   |
| ------ | ------------------------------------- | ------ | ----------------------------- |
| GET    | `/api/products`                       | Public | List all products (paginated) |
| GET    | `/api/products/{id}`                  | Public | Get product by ID             |
| GET    | `/api/products/category/{categoryId}` | Public | Products by category          |
| GET    | `/api/products/search?q=keyword`      | Public | Search products               |
| POST   | `/api/products`                       | Admin  | Create product                |
| PUT    | `/api/products/{id}`                  | Admin  | Update product                |
| DELETE | `/api/products/{id}`                  | Admin  | Delete product                |

### 🗂️ Categories

| Method | Endpoint               | Access | Description         |
| ------ | ---------------------- | ------ | ------------------- |
| GET    | `/api/categories`      | Public | List all categories |
| GET    | `/api/categories/{id}` | Public | Get category by ID  |
| POST   | `/api/categories`      | Admin  | Create category     |
| PUT    | `/api/categories/{id}` | Admin  | Update category     |
| DELETE | `/api/categories/{id}` | Admin  | Delete category     |

### 🛒 Cart

| Method | Endpoint               | Access | Description          |
| ------ | ---------------------- | ------ | -------------------- |
| GET    | `/api/cart`            | User   | View cart            |
| POST   | `/api/cart/items`      | User   | Add item to cart     |
| PUT    | `/api/cart/items/{id}` | User   | Update item quantity |
| DELETE | `/api/cart/items/{id}` | User   | Remove item          |
| DELETE | `/api/cart`            | User   | Clear entire cart    |

### 📋 Orders

| Method | Endpoint                  | Access | Description            |
| ------ | ------------------------- | ------ | ---------------------- |
| GET    | `/api/orders`             | User   | My orders (paginated)  |
| GET    | `/api/orders/{id}`        | User   | Order details          |
| POST   | `/api/orders`             | User   | Place order from cart  |
| PUT    | `/api/orders/{id}/status` | Admin  | Update order status    |
| GET    | `/api/orders/all`         | Admin  | All orders (paginated) |

---

## 📬 Postman — Sample Requests

### Register

```json
POST /api/auth/register
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane@example.com",
  "password": "password123"
}
```

### Login

```json
POST /api/auth/login
{
  "email": "admin@ecommerce.com",
  "password": "admin123"
}
```

### Create Category (Admin)

```json
POST /api/categories
Authorization: Bearer <token>
{
  "name": "Electronics",
  "description": "Phones, Laptops, and Gadgets"
}
```

### Create Product (Admin)

```json
POST /api/products
Authorization: Bearer <token>
{
  "name": "iPhone 15 Pro",
  "description": "Apple flagship smartphone",
  "price": 999.99,
  "stockQuantity": 50,
  "imageUrl": "https://example.com/image.jpg",
  "categoryId": 1
}
```

### Add to Cart

```json
POST /api/cart/items
Authorization: Bearer <token>
{
  "productId": 1,
  "quantity": 2
}
```

### Place Order

```json
POST /api/orders
Authorization: Bearer <token>
{
  "shippingAddress": "123 Main Street, New York, NY 10001"
}
```

### Update Order Status (Admin)

```json
PUT /api/orders/1/status
Authorization: Bearer <token>
{
  "status": "SHIPPED"
}
```

---

## 🗄️ Entity Relationship Diagram

```
users ──── carts ──── cart_items ──── products
  │                                      │
  └─── orders ──── order_items ──────────┘
                          │
                      categories ──── products
```

**Order Status Flow:**

```
PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
                                       └──→ CANCELLED
```

---

## 🔑 OOP Principles Applied

| Principle         | Where Applied                                                                                                                                   |
| ----------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| **Encapsulation** | All entities use private fields with Lombok getters/setters; DTOs separate internal model from API contract                                     |
| **Inheritance**   | Exception hierarchy (`ResourceNotFoundException`, `BadRequestException` extend `RuntimeException`)                                              |
| **Abstraction**   | `JpaRepository` interfaces abstract all DB operations; `Service` layer abstracts business logic from controllers                                |
| **Polymorphism**  | Spring's `UserDetailsService` interface implemented by `UserDetailsServiceImpl`; `OncePerRequestFilter` overridden by `JwtAuthenticationFilter` |

---

## ✅ Running Tests

```bash
mvn test
```

---

## 📁 Deliverables Checklist

- [x] Spring Boot project setup pushed to GitHub
- [x] PostgreSQL schema (`src/main/resources/db/schema.sql`)
- [x] JWT Authentication (register + login)
- [x] CRUD — Products, Categories, Cart, Orders
- [x] Role-based access control (USER / ADMIN)
- [x] Validation & error handling
- [x] Swagger UI documentation
- [x] Postman sample requests (this README)
- [x] Unit/integration tests
- [x] Data seeding (admin user + sample data)
