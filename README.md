<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=6,11,20&height=200&section=header&text=RevShop&fontSize=80&fontColor=FF6B35&fontAlignY=38&desc=Production-Grade%20Multi-Role%20E-Commerce%20Platform&descAlignY=58&descSize=18&animation=fadeIn" width="100%"/>

<br/>

[![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security_6-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL_8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://mysql.com)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf_3.1-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://thymeleaf.org)
[![Bootstrap](https://img.shields.io/badge/Bootstrap_5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)](https://getbootstrap.com)
[![Razorpay](https://img.shields.io/badge/Razorpay-02042B?style=for-the-badge&logo=razorpay&logoColor=3395FF)](https://razorpay.com)
[![Maven](https://img.shields.io/badge/Maven_3.9-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org)

<br/>

[![Tests Passing](https://img.shields.io/badge/Tests-40%2B_Passing-2ea44f?style=flat-square&logo=junit5&logoColor=white)]()
[![JPA Entities](https://img.shields.io/badge/JPA_Entities-12-4479A1?style=flat-square)]()
[![DB Tables](https://img.shields.io/badge/DB_Tables-13-4479A1?style=flat-square)]()
[![User Roles](https://img.shields.io/badge/User_Roles-3-FF6B35?style=flat-square)]()
[![Templates](https://img.shields.io/badge/Thymeleaf_Templates-45%2B-005F0F?style=flat-square)]()
[![Version](https://img.shields.io/badge/Release-v1.0.0-FF6B35?style=flat-square)]()
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)]()

<br/>

> **RevShop** is a full-stack, production-ready multi-role e-commerce platform built as a collaborative 5-person team project over 8 days. It covers the **complete commerce lifecycle**: OTP-verified registration → product discovery → cart management → Razorpay/COD payments → order tracking → review submission — with separate, fully-featured dashboards for Buyers, Sellers, and Admins.

<br/>

**[📖 Diagrams & Architecture](https://revshop-erd.vercel.app/#arch)** · **[🐛 Report a Bug](../../issues)** · **[💡 Request a Feature](../../issues)**

</div>

---

## 📑 Table of Contents

<details>
<summary>Expand full table of contents</summary>

1. [Project Overview](#-project-overview)
2. [Team & Module Ownership](#-team--module-ownership)
3. [System Architecture](#-system-architecture)
4. [Entity Relationship Diagram](#-entity-relationship-diagram)
5. [User Flow Diagrams](#-user-flow-diagrams)
6. [Features Deep Dive](#-features-deep-dive)
7. [Tech Stack](#-tech-stack)
8. [Project Structure](#-project-structure)
9. [Getting Started](#-getting-started)
10. [Environment Variables](#-environment-variables)
11. [API / Route Reference](#-api--route-reference)
12. [Database Schema Notes](#-database-schema-notes)
13. [Testing](#-testing)
14. [Git Workflow](#-git-workflow)
15. [UI Design System](#-ui-design-system)
16. [Troubleshooting](#-troubleshooting)

</details>

---

## 🌟 Project Overview

RevShop is built around a **single core principle**: every part of the purchase lifecycle is handled safely, atomically, and without data leakage between layers.

### The Complete Commerce Lifecycle

```
 REGISTER ──► VERIFY OTP ──► LOGIN ──► BROWSE ──► CART ──► CHECKOUT ──► PAY ──► TRACK ──► REVIEW
    │               │            │        │          │          │          │        │          │
 BCrypt          Gmail        JWT      search     stock      address    HMAC    status     1 per
 encoded         SMTP        cookie    filter     check      + method   verify  updates    product
```

### What makes this production-ready?

| Concern | Problem | Solution |
|---------|---------|----------|
| **Lazy Loading** | `LazyInitializationException` when Thymeleaf renders `@ManyToOne(LAZY)` fields | `@Transactional` at **class-level** on all 11 services keeps the Hibernate session open through the full render cycle |
| **Null Safety** | `th:each` on a `null` list → HTTP 500 | Every list-returning service method wraps in try/catch and returns `List.of()` — Thymeleaf handles empty lists gracefully |
| **Entity Leakage** | Exposing Hibernate proxies to templates causes serialization errors | Every service has a `toDTO()` method; entities **never** reach controllers or templates |
| **Payment Security** | Fake payment callbacks | Server-side HMAC-SHA256 verification on every Razorpay callback before marking any order as paid |
| **Race Conditions** | Two users buy the last item simultaneously | Stock validated at cart-add time **and again** at order placement inside a transaction |
| **Secret Management** | Credentials committed to git | `dotenv-java` reads `.env` at startup — `application.properties` only contains `${VAR}` placeholders |
| **Observability** | Silent failures in production | Log4j2 structured logging with rolling file appender on every service method entry/exit and exception |

---

## 👥 Team & Module Ownership

<table>
<thead>
<tr>
<th>Member</th>
<th>Role</th>
<th>Branch</th>
<th>Primary Files</th>
</tr>
</thead>
<tbody>
<tr>
<td><strong>🔴 Benhur</strong><br/><em>Team Lead</em></td>
<td>Auth · JWT · Admin · Security · Config</td>
<td><code>feature/benhur-auth-admin</code></td>
<td>

`SecurityConfig.java` · `JwtUtil.java` · `JwtAuthenticationFilter.java` · `UserDetailsServiceImpl.java` · `AuthController.java` · `AuthService.java` · `AdminController.java` · `AdminService.java` · `AppConfig.java` · `DataInitializer.java` · `WebConfig.java` · `AsyncConfig.java` · `application.properties` · `templates/auth/*` · `templates/admin/dashboard.html` · `templates/admin/users.html`

</td>
</tr>
<tr>
<td><strong>🟢 Chandini</strong></td>
<td>Products · Categories · Seller UI</td>
<td><code>feature/chandini-seller-product</code></td>
<td>

`ProductService.java` · `CategoryService.java` · `ProductRepository.java` · `CategoryRepository.java` · `ProductDTO.java` · `CategoryDTO.java` · `templates/home.html` · `templates/seller/dashboard.html` · `templates/seller/products.html` · `templates/seller/add-product.html` · `templates/seller/edit-product.html` · `templates/admin/products.html` · `templates/admin/categories.html`

</td>
</tr>
<tr>
<td><strong>🔵 Sai</strong></td>
<td>Cart · Wishlist · Buyer UI</td>
<td><code>feature/sai-buyer-cart</code></td>
<td>

`CartService.java` · `WishlistService.java` · `BuyerController.java` · `CartRepository.java` · `CartItemRepository.java` · `WishlistRepository.java` · `templates/buyer/cart.html` · `templates/buyer/wishlist.html` · `templates/buyer/product-detail.html`

</td>
</tr>
<tr>
<td><strong>🟣 Naveen</strong></td>
<td>Orders · Payments · Checkout</td>
<td><code>feature/naveen-order-payment</code></td>
<td>

`OrderService.java` · `PaymentService.java` · `PaymentController.java` · `OrderRepository.java` · `PaymentRepository.java` · `CheckoutDTO.java` · `OrderDTO.java` · `OrderItemDTO.java` · `templates/buyer/checkout.html` · `templates/buyer/razorpay-payment.html` · `templates/buyer/orders.html` · `templates/buyer/order-detail.html`

</td>
</tr>
<tr>
<td><strong>🟡 Veerababu</strong></td>
<td>Reviews · Notifications · Inventory</td>
<td><code>feature/veera-inventory-review-notification</code></td>
<td>

`ReviewService.java` · `NotificationService.java` · `SellerController.java` · `ReviewRepository.java` · `NotificationRepository.java` · `templates/buyer/notifications.html` · `templates/seller/notifications.html` · `templates/seller/low-stock.html` · `templates/admin/orders.html`

</td>
</tr>
</tbody>
</table>

---

## 🏗️ System Architecture

### Full Layered Architecture

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  CLIENT LAYER                                                                ║
║                                                                              ║
║  ┌─────────────────────────────────────────────────────────────────────┐    ║
║  │  Browser                                                            │    ║
║  │  Thymeleaf 3.1 (server-rendered HTML)  Bootstrap 5.3 (layout/CSS)  │    ║
║  │  Font Awesome 6 (icons)                Razorpay.js (payment popup)  │    ║
║  │  Playfair Display + DM Sans (fonts)                                 │    ║
║  └─────────────────────────────┬───────────────────────────────────────┘    ║
╚═════════════════════════════════╪════════════════════════════════════════════╝
                                  │  HTTP GET / POST  (Thymeleaf form submits)
╔═════════════════════════════════╪════════════════════════════════════════════╗
║  SECURITY LAYER                 │                                            ║
║                                 ▼                                            ║
║  ┌──────────────────────────────────────────────────────────────────────┐   ║
║  │  Spring Security 6 — Filter Chain                                    │   ║
║  │                                                                      │   ║
║  │  JwtAuthenticationFilter (OncePerRequestFilter)                      │   ║
║  │    └─► reads "jwt" cookie ──► JwtUtil.validateToken()                │   ║
║  │         └─► valid ──► UserDetailsServiceImpl.loadUserByUsername()    │   ║
║  │              └─► sets SecurityContextHolder                          │   ║
║  │                                                                      │   ║
║  │  SecurityConfig (URL access rules)                                   │   ║
║  │    Public  :  /  /auth/**  /product/**  /uploads/**                  │   ║
║  │    BUYER   :  /buyer/**                                              │   ║
║  │    SELLER  :  /seller/**                                             │   ║
║  │    ADMIN   :  /admin/**                                              │   ║
║  └──────────────────────────────────────────────────────────────────────┘   ║
╚══════════════════════════════════╪═══════════════════════════════════════════╝
                                   │  authenticated request
╔══════════════════════════════════╪═══════════════════════════════════════════╗
║  CONTROLLER LAYER                ▼                                           ║
║                                                                              ║
║  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐            ║
║  │    Auth    │  │   Admin    │  │   Seller   │  │   Buyer    │            ║
║  │ Controller │  │ Controller │  │ Controller │  │ Controller │            ║
║  │ /auth/**   │  │ /admin/**  │  │ /seller/** │  │ /buyer/**  │            ║
║  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘           ║
║        │               │               │               │                   ║
║  ┌─────┴───────────────────────────────┴──────┐         │                  ║
║  │             HomeController                 │  ┌──────┴──────────┐       ║
║  │             /  /product/{id}               │  │PaymentController│       ║
║  └─────────────────────────────────────────────┘  │/buyer/checkout  │       ║
║                                                   │/buyer/payment/* │       ║
║  Rule: controllers receive DTOs only.             └─────────────────┘       ║
║        Entities never reach the view layer.                                 ║
╚══════════════════════════════════╪═══════════════════════════════════════════╝
                                   │  DTOs in / DTOs out
╔══════════════════════════════════╪═══════════════════════════════════════════╗
║  SERVICE LAYER — All @Transactional at class level                          ║
║                                  ▼                                          ║
║  ┌──────────────────────────────────────────────────────────────────────┐  ║
║  │  AuthService      AdminService     ProductService   CategoryService   │  ║
║  │  CartService      WishlistService  OrderService     PaymentService    │  ║
║  │  ReviewService    NotificationService               EmailService      │  ║
║  │                                                                       │  ║
║  │  Every service enforces:                                              │  ║
║  │   • @Transactional  — Hibernate session stays open for full request   │  ║
║  │   • toDTO()         — null-safe entity → DTO mapping                  │  ║
║  │   • List.of()       — fallback return on any exception                │  ║
║  │   • Log4j2          — INFO on entry, ERROR on exception               │  ║
║  └──────────────────────────────────────────────────────────────────────┘  ║
╚══════════════════════════════════╪═══════════════════════════════════════════╝
                                   │  @Transactional session boundary
╔══════════════════════════════════╪═══════════════════════════════════════════╗
║  REPOSITORY LAYER — Spring Data JPA                                         ║
║                                  ▼                                          ║
║  ┌──────────────────────────────────────────────────────────────────────┐  ║
║  │  UserRepository          findByEmail · existsByEmail                  │  ║
║  │  ProductRepository        findByActiveTrue · findBySeller             │  ║
║  │                           findByStockQuantityEquals(0) · search       │  ║
║  │  CategoryRepository       existsByName                                │  ║
║  │  OrderRepository          findByBuyerOrderByOrderedAtDesc             │  ║
║  │                           @Query for revenue stats                    │  ║
║  │  CartRepository           findByUser (Optional)                       │  ║
║  │  CartItemRepository       findByCartAndProduct · deleteByCart         │  ║
║  │  WishlistRepository       findByUser (Optional)                       │  ║
║  │  ReviewRepository         existsByProductAndBuyer                     │  ║
║  │                           findAverageRatingByProduct (@Query)         │  ║
║  │  NotificationRepository   findByUserAndIsReadFalse                    │  ║
║  │                           countByUserAndIsReadFalse                   │  ║
║  │  PaymentRepository        findByRazorpayOrderId                       │  ║
║  │  OtpVerificationRepository findByEmail                                │  ║
║  │  OrderItemRepository      findByOrder                                 │  ║
║  └──────────────────────────────────────────────────────────────────────┘  ║
╚══════════════════════════════════╪═══════════════════════════════════════════╝
                                   │  JPA / Hibernate ORM queries
╔══════════════════════════════════╪═══════════════════════════════════════════╗
║  DATA LAYER                      ▼                                          ║
║  ┌──────────────────────────────────────────────────────────────────────┐  ║
║  │  MySQL 8.0  ·  UTF8MB4  ·  13 Tables  ·  Hibernate auto-DDL          │  ║
║  └──────────────────────────────────────────────────────────────────────┘  ║
╚══════════════════════════════════════════════════════════════════════════════╝

EXTERNAL SERVICES
  ┌────────────────────────────┐    ┌────────────────────────────┐
  │  Razorpay API              │    │  Gmail SMTP                │
  │  • Create order (paise)    │    │  smtp.gmail.com:587         │
  │  • HMAC-SHA256 callback    │    │  JavaMailSender @Async      │
  │    verify before SUCCESS   │    │  OTP + confirmation emails  │
  └────────────────────────────┘    └────────────────────────────┘
```

### Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| `@Transactional` at class-level on all services | Keeps the Hibernate persistence context open while Thymeleaf renders `@ManyToOne(LAZY)` fields. Eliminates `LazyInitializationException` without switching to eager fetching (which would cause N+1 queries). |
| DTOs everywhere — `toDTO()` in every service | Prevents Hibernate proxies and circular references from escaping the service layer. Templates receive plain Java objects, never JPA entities. |
| `List.of()` as fallback on exceptions | Every `List`-returning service method catches `Exception` and returns `List.of()`. Thymeleaf's `th:each` handles empty lists silently. A single NPE in a service no longer kills the entire page. |
| HMAC-SHA256 on every Razorpay callback | Razorpay signs `razorpayOrderId + "|" + razorpayPaymentId` with the key secret. The server recomputes this and compares before marking any payment as SUCCESS. Prevents fake callbacks. |
| `DataInitializer` `@PostConstruct` admin seed | Checks `existsByEmail("admin@revshop.com")` before creating. Safe to call on every restart. No migration scripts required for the admin account. |
| `dotenv-java` + `${VAR}` in properties | No credentials in source control. `.env` file loaded once at JVM startup via `static {}` block in `RevShopApplication`. |
| `@EnableAsync` + `EmailService` | OTP emails sent in a background thread pool. Login/register responses return immediately without waiting for SMTP. |

---

## 🗄️ Entity Relationship Diagram

### Complete Schema — 12 Entities → 13 Tables

```
  ┌──────────────────────────────────────────────────────────────────────────────┐
  │                           TABLE: users                                       │
  │  PK  id            BIGINT         AUTO_INCREMENT                             │
  │      firstName     VARCHAR        NOT NULL                                   │
  │      lastName      VARCHAR        NOT NULL                                   │
  │  UQ  email         VARCHAR        NOT NULL UNIQUE                            │
  │      password      VARCHAR        NOT NULL  (BCrypt $2a$ hash)               │
  │      phone         VARCHAR        NOT NULL                                   │
  │      role          ENUM           NOT NULL  {BUYER, SELLER, ADMIN}           │
  │      enabled       BOOLEAN        NOT NULL  DEFAULT false                    │
  │      blocked       BOOLEAN        NOT NULL  DEFAULT false                    │
  │      createdAt     TIMESTAMP      NOT NULL  (@PrePersist)                    │
  │      updatedAt     TIMESTAMP                (@PrePersist + @PreUpdate)       │
  └──────────┬──────────────┬──────────────┬────────────────┬────────────────────┘
             │              │              │                │
         1:1 │          1:1 │          1:N │            1:N │
             ▼              ▼              ▼                ▼
  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐  ┌───────────────────┐
  │ TABLE: carts │  │TABLE:wishlists│ │ TABLE: orders │  │TABLE:notifications│
  │──────────────│  │──────────────│  │───────────────│  │───────────────────│
  │ PK id BIGINT │  │ PK id BIGINT │  │ PK id  BIGINT │  │ PK id  BIGINT     │
  │ FK user_id UQ│  │ FK user_id UQ│  │ FK buyer_id   │  │ FK user_id        │
  │              │  │              │  │    totalAmount │  │    title VARCHAR  │
  │ cascade: ALL │  │  (1:1 UQ)   │  │    status ENUM │  │    message NN     │
  └──────┬───────┘  └──────┬───────┘  │    shippAddr  │  │    type ENUM      │
         │                 │          │    city       │  │    isRead BOOL    │
      1:N│              M:N│          │    state      │  │    createdAt TS   │
         ▼                 │          │    pincode    │  │                   │
  ┌──────────────┐         │  via     │    orderedAt  │  │ types:            │
  │TABLE:cart_   │         │  wishlist│    updatedAt  │  │  ORDER_PLACED     │
  │items         │         │  _product│               │  │  ORDER_STATUS_    │
  │──────────────│         │  s table │ ENUM values:  │  │    UPDATED        │
  │ PK id BIGINT │         │          │  PENDING      │  │  LOW_STOCK        │
  │ FK cart_id   │         │          │  PROCESSING   │  │  REVIEW_ADDED     │
  │ FK product_id│         │          │  SHIPPED      │  │  ACCOUNT_BLOCKED  │
  │    quantity  │         │          │  DELIVERED    │  │  GENERAL          │
  │    DEFAULT 1 │         │          │  CANCELLED    │  └───────────────────┘
  │              │         │          └───────┬───────┘
  │ cascade: ALL │         ▼                  │
  │ orphanRemoval│  ┌──────────────────┐      │ 1:N
  └──────────────┘  │TABLE:wishlist_   │      ▼
                    │products (M:N)    │  ┌──────────────────────┐
         ┌──────────│──────────────────│  │ TABLE: order_items   │
         │          │ FK wishlist_id   │  │──────────────────────│
         │          │ FK product_id    │  │ PK id       BIGINT   │
         │          └──────────────────┘  │ FK order_id  NN      │
         │                  │             │ FK product_id NN      │
         │                  │ N           │    quantity   INT  NN │
         ▼                  ▼             │    price    DECIMAL NN│ ← snapshot
  ┌─────────────────────────────────────────────────────────────┐
  │                   TABLE: products                           │
  │─────────────────────────────────────────────────────────────│
  │  PK  id              BIGINT       AUTO_INCREMENT            │
  │      name            VARCHAR      NOT NULL                  │
  │      description     TEXT(2000)                             │
  │      price           DECIMAL      NOT NULL                  │
  │      mrp             DECIMAL      NOT NULL                  │
  │      discountPercent INT          NOT NULL DEFAULT 0        │
  │      stockQuantity   INT          NOT NULL DEFAULT 0        │
  │      imageUrl        VARCHAR                                │
  │      active          BOOLEAN      NOT NULL DEFAULT true     │
  │  FK  category_id     → categories.id                        │
  │  FK  seller_id       → users.id   NOT NULL                  │
  │      createdAt       TIMESTAMP    (@PrePersist)              │
  │      updatedAt       TIMESTAMP    (@PreUpdate)               │
  └──────────────────────────────┬──────────────────────────────┘
                                  │ N                    N
                         ┌────────┴────────┐             │
                         │                 │             │
              ┌──────────┘                 └─────┐       │
              ▼ 1:N                              ▼ 1:N   │
  ┌────────────────────────┐      ┌──────────────────────┤
  │   TABLE: reviews       │      │  TABLE: categories   │
  │────────────────────────│      │──────────────────────│
  │ PK id       BIGINT     │      │ PK id    BIGINT       │
  │ FK product_id NN       │      │ UQ name  VARCHAR UQ   │
  │ FK buyer_id   NN       │      │    desc  VARCHAR       │
  │    rating     INT (1-5)│      └──────────────────────┘
  │    comment    TEXT     │
  │    createdAt  TS       │
  │ UQ (product_id,        │ ← database-level duplicate prevention
  │      buyer_id)         │
  └────────────────────────┘
                                          FK: order_id (1:1, UNIQUE)
                                                    │
                                                    ▼
  ┌────────────────────────────────────────────────────────────┐
  │                   TABLE: payments                          │
  │────────────────────────────────────────────────────────────│
  │  PK  id                BIGINT       AUTO_INCREMENT         │
  │  FK  order_id          → orders.id  NOT NULL UNIQUE        │
  │      amount            DECIMAL      NOT NULL               │
  │      status            ENUM         NOT NULL DEFAULT PENDING│
  │      paymentMethod     ENUM                                │
  │      razorpayOrderId   VARCHAR      (from Razorpay API)    │
  │      razorpayPaymentId VARCHAR      (from callback)        │
  │      razorpaySignature VARCHAR      (HMAC-SHA256 verified) │
  │      paidAt            TIMESTAMP    (set on SUCCESS)       │
  │      createdAt         TIMESTAMP    (@PrePersist)          │
  │                                                            │
  │  status values:  PENDING → SUCCESS | FAILED → REFUNDED    │
  │  method values:  COD | RAZORPAY                            │
  └────────────────────────────────────────────────────────────┘

  ┌────────────────────────────────────────────────────────────┐
  │               TABLE: otp_verifications                     │
  │────────────────────────────────────────────────────────────│
  │  PK  id          BIGINT     AUTO_INCREMENT                 │
  │      email       VARCHAR    NOT NULL  (matched by string,  │
  │      otp         VARCHAR    NOT NULL   no FK to users)     │
  │      expiresAt   TIMESTAMP  NOT NULL                       │
  │      used        BOOLEAN    NOT NULL  DEFAULT false        │
  │      createdAt   TIMESTAMP  (@PrePersist)                  │
  │                                                            │
  │  ⚠  Intentionally no FK: supports pre-registration OTP    │
  │     before user.enabled = true                             │
  └────────────────────────────────────────────────────────────┘
```

### Relationship Matrix

| From | To | Cardinality | JPA Annotation | Cascade | Notes |
|------|----|-------------|---------------|---------|-------|
| `users` | `carts` | 1:1 | `@OneToOne` | ALL | `unique=true` on `user_id`; auto-created on first access |
| `users` | `wishlists` | 1:1 | `@OneToOne` | ALL | `unique=true` on `user_id`; auto-created on first access |
| `users (SELLER)` | `products` | 1:N | `@ManyToOne` (from product) | — | `seller_id` FK; ownership enforced before edit/delete |
| `users (BUYER)` | `orders` | 1:N | `@ManyToOne` (from order) | — | `buyer_id` FK; sorted by `orderedAt DESC` |
| `users` | `reviews` | 1:N | `@ManyToOne` (from review) | — | `buyer_id` FK; part of UNIQUE(product,buyer) |
| `users` | `notifications` | 1:N | `@ManyToOne` (from notification) | — | `user_id` FK; supports 6 typed notification events |
| `categories` | `products` | 1:N | `@OneToMany` | ALL | `mappedBy="category"` |
| `orders` | `payments` | 1:1 | `@OneToOne` | ALL | `order_id` UNIQUE in payments |
| `orders` | `order_items` | 1:N | `@OneToMany` | ALL | `mappedBy="order"`; price snapshot captured at purchase |
| `carts` | `cart_items` | 1:N | `@OneToMany` | ALL + orphanRemoval | `mappedBy="cart"`; cleared after checkout |
| `wishlists` ↔ `products` | junction | M:N | `@ManyToMany` | — | `wishlist_products` table; `@JoinTable` defined on `Wishlist` |

### Status Transition Diagrams

```
Order Status:
  PENDING ──► PROCESSING ──► SHIPPED ──► DELIVERED
     │              │            │
     └──────────────┴────────────┴──► CANCELLED (stock restored per item)

  Rule: admin or seller updates status forward only.
        Buyer can cancel from PENDING, PROCESSING, or SHIPPED.
        Cannot cancel DELIVERED.

Payment Status:
  PENDING ──► SUCCESS     (Razorpay: HMAC verified; COD: direct)
  PENDING ──► FAILED      (HMAC mismatch or Razorpay error)
  FAILED  ──► REFUNDED    (manual admin action)
```

---

## 🔄 User Flow Diagrams

### Auth Flow

```
  POST /auth/register
        │
        ├─► email already exists? ──► AuthException("Email already registered")
        │
        ├─► passwords don't match? ──► validation error → re-render form
        │
        ├─► hash password (BCrypt strength 10)
        │
        ├─► save user (enabled=false, role=BUYER|SELLER)
        │
        └─► generate OTP (6-digit random, 10-min expiry)
              └─► save to otp_verifications
              └─► EmailService.sendOtpEmail() ──► @Async ──► Gmail SMTP

  POST /auth/verify-otp
        ├─► OTP expired?  ──► AuthException("OTP expired. Request a new one.")
        ├─► OTP wrong?    ──► AuthException("Invalid OTP.")
        └─► user.enabled = true ──► saved ──► redirect to /auth/login

  POST /auth/login
        ├─► user not found?  ──► AuthException
        ├─► user blocked?    ──► AuthException("Your account has been blocked.")
        ├─► not enabled?     ──► AuthException("Please verify your email first.")
        ├─► wrong password?  ──► AuthException  (BCryptPasswordEncoder.matches())
        └─► generate JWT (HMAC-SHA256, sub=email, role claim, exp=24hr)
              └─► set HttpOnly cookie "jwt" (maxAge=86400)
              └─► redirect based on role:
                    ADMIN  ──► /admin/dashboard
                    SELLER ──► /seller/dashboard
                    BUYER  ──► /buyer/home
```

### Buyer Purchase Flow

```
  /buyer/home  (GET, ROLE_BUYER)
       │  keyword param ──► productService.searchProducts(keyword)
       │  categoryId param ──► productService.filterByCategory(id)
       │  no params ──► productService.getAllActiveProducts()
       │  + cartCount, unreadCount loaded
       │
       ▼
  /buyer/product/{id}  (GET)
       │  product + reviews + avgRating + hasReviewed + isInWishlist
       │
       ▼
  POST /buyer/cart/add  { productId, quantity }
       ├─► CartService.addToCart(email, productId, qty)
       │     ├─► get or create Cart for user
       │     ├─► load Product, check active=true
       │     ├─► stockQuantity >= qty? (validation point #1)
       │     └─► save CartItem (or increment existing)
       │
       ▼
  GET /buyer/checkout
       ├─► redirect to /buyer/cart if cart is empty
       └─► render CheckoutDTO form + cart summary
       │
       ▼
  POST /buyer/checkout  { shippingAddress, city, state, pincode, paymentMethod }
       │
       └─► OrderService.placeOrder(email, checkoutDTO)
             ├─► reload cart items
             ├─► for each CartItem:
             │     ├─► re-validate stock (validation point #2 — race condition guard)
             │     ├─► product.stockQuantity -= item.quantity  (decremented)
             │     └─► create OrderItem (captures price, mrp, discountPercent snapshot)
             ├─► calculate totalAmount
             ├─► save Order (status=PENDING)
             ├─► cartService.clearCart(email)
             └─► notificationService.send(buyer, ORDER_PLACED)
             │
             ├─► paymentMethod == COD?
             │     └─► create Payment(method=COD, status=SUCCESS immediately)
             │         order.status = PROCESSING
             │         redirect /buyer/orders ✓
             │
             └─► paymentMethod == RAZORPAY?
                   └─► PaymentService.createRazorpayOrder(orderId)
                         └─► Razorpay API: POST /v1/orders { amount in paise }
                         └─► returns razorpayOrderId
                   └─► render buyer/razorpay-payment.html
                         └─► Razorpay.js popup ──► user pays
                         └─► POST /buyer/payment/callback
                               { razorpay_order_id, razorpay_payment_id, razorpay_signature }
                               ├─► HMAC-SHA256(orderId|paymentId, keySecret)
                               ├─► signature mismatch? ──► PaymentException ──► status=FAILED
                               └─► match ──► payment.status=SUCCESS
                                             order.status=PROCESSING
                                             redirect /buyer/orders ✓
```

### Order Cancellation Flow

```
  POST /buyer/orders/cancel/{orderId}
        │
        ├─► verify order.buyer.email == authenticated email (ownership check)
        ├─► order.status == DELIVERED? ──► exception("Cannot cancel delivered order")
        │
        └─► for each OrderItem:
              product.stockQuantity += item.quantity  ← stock restored
        └─► order.status = CANCELLED
        └─► payment.status = REFUNDED  (if was SUCCESS)
        └─► notificationService.send(buyer, ORDER_STATUS_UPDATED)
```

### Seller Inventory Flow

```
  POST /seller/products/add
        │
        ├─► image != null? ──► saveImage(multipart) ──► UUID_originalName in /uploads/
        └─► ProductService.addProduct(productDTO, sellerEmail)
              └─► load User by email (must be SELLER role)
              └─► load Category by id
              └─► save Product (active=true, stockQuantity from DTO)

  After any order is placed:
        └─► for each sold product:
              if product.stockQuantity < 5 after deduction:
                notificationService.send(seller, LOW_STOCK,
                  "Stock for '" + product.name + "' is now " + product.stockQuantity)
```

---

## ✨ Features Deep Dive

### 🔐 Authentication & Security

| Feature | Detail |
|---------|--------|
| Password hashing | BCryptPasswordEncoder, strength 10, irreversible |
| JWT format | HMAC-SHA256 signed; claims: `email`, `role`, `iat`, `exp` |
| Token storage | HttpOnly cookie named `jwt`; maxAge = 86400s (24hr); not accessible from JS |
| OTP generation | `String.format("%06d", new Random().nextInt(999999))` — 6 digits, zero-padded |
| OTP expiry | `LocalDateTime.now().plusMinutes(10)` stored in `otp_verifications.expiresAt` |
| OTP re-use prevention | `used` boolean flipped to `true` after one successful verification |
| Account blocking | `user.blocked = true` → `UserDetailsServiceImpl` checks this before loading → login rejected |
| Route protection | `@PreAuthorize("hasRole('X')")` on every controller class |
| CSRF | Disabled (stateless JWT; all mutating actions require authenticated session anyway) |
| Password reset | Separate OTP flow — OTP sent to email, verified, then `BCryptPasswordEncoder.encode(newPassword)` |

### 🛒 Cart & Wishlist

| Feature | Detail |
|---------|--------|
| Cart auto-creation | `CartService.getOrCreate(email)` — creates `Cart` if none found, then saves |
| Stock double-check | `CartService.addToCart` checks `product.stockQuantity >= qty`; checked again in `OrderService.placeOrder` |
| Wishlist move-to-cart | `WishlistService.moveToCart(email, productId)` → calls `CartService.addToCart` → removes from wishlist |
| Cart clear | `CartItemRepository.deleteByCart(cart)` + `cart.getCartItems().clear()` → atomic under `@Transactional` |
| Cart item count | `CartService.getCartItemCount(email)` → shown as badge in navbar on every buyer page |

### 💳 Payment Details

```
                 ┌──────────────────────────────────────────────┐
                 │           RAZORPAY INTEGRATION               │
                 └──────────────────────────────────────────────┘

Server creates order:
  RazorpayClient client = new RazorpayClient(keyId, keySecret);
  JSONObject options = new JSONObject();
  options.put("amount", order.getTotalAmount()
              .multiply(BigDecimal.valueOf(100))  ← convert to paise
              .intValue());
  options.put("currency", "INR");
  com.razorpay.Order razorpayOrder = client.orders.create(options);
  String razorpayOrderId = razorpayOrder.get("id");  → stored in Payment entity

Browser checkout popup:
  Razorpay({ key: razorpayKeyId, order_id: razorpayOrderId,
             amount, name, email }) → user pays

Server callback verification:
  String payload = razorpayOrderId + "|" + razorpayPaymentId;
  String expected = new HmacUtils("HmacSHA256", keySecret).hmacHex(payload);
  if (!expected.equals(razorpaySignature)) throw new PaymentException("Verification failed");
  // Only if equal:
  payment.setStatus(SUCCESS); payment.setRazorpayPaymentId(paymentId);
  order.setStatus(PROCESSING);
```

### 📦 Inventory Logic

```
  On order placement:
    for (CartItem item : cartItems) {
        Product p = item.getProduct();
        if (p.getStockQuantity() < item.getQuantity())
            throw ProductException("Insufficient stock for: " + p.getName());
        p.setStockQuantity(p.getStockQuantity() - item.getQuantity());
        // notify seller if stock now < 5
        if (p.getStockQuantity() < 5)
            notificationService.sendLowStockAlert(p.getSeller(), p);
    }

  On order cancellation:
    for (OrderItem item : order.getOrderItems()) {
        Product p = item.getProduct();
        p.setStockQuantity(p.getStockQuantity() + item.getQuantity()); // ← restored
    }
```

### 🔔 Notification System

| Event | Triggered By | Recipient | Type |
|-------|-------------|-----------|------|
| Order placed | `OrderService.placeOrder()` | Buyer | `ORDER_PLACED` |
| Order status change | `OrderService.updateOrderStatus()` | Buyer | `ORDER_STATUS_UPDATED` |
| User blocked | `AdminService.blockUser()` | Blocked user | `ACCOUNT_BLOCKED` |
| Low stock (< 5 units) | `OrderService` after stock decrement | Seller (product owner) | `LOW_STOCK` |
| New review on product | `ReviewService.addReview()` | Seller (product owner) | `REVIEW_ADDED` |
| Admin broadcasts | Manual via service | Any user | `GENERAL` |

Unread count shown as a badge in the navbar on every protected page via `notificationService.getUnreadCount(email)` injected into `Model`.

---

## 🛠️ Tech Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Language | Java | 17 LTS | Application language |
| Framework | Spring Boot | 3.2.x | Core framework, auto-configuration |
| Security | Spring Security + jjwt | 6.x + 0.11.x | Auth, JWT generation & validation |
| ORM | Spring Data JPA / Hibernate | 6.x | Database access, entity mapping |
| Database | MySQL | 8.0 | Production data store |
| Templating | Thymeleaf | 3.1.x | Server-side HTML rendering |
| CSS Framework | Bootstrap | 5.3.x | Responsive layout & components |
| Icons | Font Awesome | 6.x | UI iconography |
| Payments | Razorpay Java SDK | latest | Payment gateway |
| Email | Spring Mail (JavaMailSender) | — | Gmail SMTP OTP delivery |
| Config | dotenv-java (cdimascio) | 5.x | `.env` file secret loading |
| Logging | Log4j2 | 2.x | Structured logging, rolling files |
| File Upload | Spring MultipartFile | — | Product image uploads |
| Async | Spring `@EnableAsync` | — | Non-blocking email sending |
| Build | Apache Maven | 3.9+ | Dependency management, packaging |
| Testing | JUnit 5 + Mockito | — | Unit testing |
| Test DB | H2 (in-memory) | — | Isolated test environment |
| Fonts | Playfair Display + DM Sans | — | UI typography (Google Fonts) |

---

## 📁 Project Structure

```
Rev_Shop/
│
├── src/
│   ├── main/
│   │   ├── java/com/revshop/
│   │   │   │
│   │   │   ├── RevShopApplication.java
│   │   │   │     @SpringBootApplication @EnableAsync @EnableScheduling
│   │   │   │     static { AppConfig.loadDotenv(); }  ← loads .env before Spring starts
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java
│   │   │   │   │     @Bean BCryptPasswordEncoder (strength 10)
│   │   │   │   │     static loadDotenv() — reads .env via dotenv-java
│   │   │   │   ├── AsyncConfig.java
│   │   │   │   │     @EnableAsync, ThreadPoolTaskExecutor (core=2, max=5)
│   │   │   │   ├── DataInitializer.java
│   │   │   │   │     @PostConstruct — seeds admin@revshop.com if not exists
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   │     filter chain: JwtAuthFilter → UserDetailsService
│   │   │   │   │     URL matchers: public / BUYER / SELLER / ADMIN
│   │   │   │   └── WebConfig.java
│   │   │   │         addResourceHandlers: /uploads/** → file:/uploads/
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java       @RequestMapping("/admin") @PreAuthorize("hasRole('ADMIN')")
│   │   │   │   ├── AuthController.java        @RequestMapping("/auth")
│   │   │   │   ├── BuyerController.java       @RequestMapping("/buyer") @PreAuthorize("hasRole('BUYER')")
│   │   │   │   ├── HomeController.java        @GetMapping("/") @GetMapping("/product/{id}")
│   │   │   │   ├── PaymentController.java     @RequestMapping("/buyer") — checkout + callback
│   │   │   │   └── SellerController.java      @RequestMapping("/seller") @PreAuthorize("hasRole('SELLER')")
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── CartItemDTO.java
│   │   │   │   ├── CategoryDTO.java
│   │   │   │   ├── CheckoutDTO.java           shippingAddress + city + state + pincode + paymentMethod
│   │   │   │   ├── DashboardDTO.java          @Builder — all admin stat fields in one object
│   │   │   │   ├── LoginDTO.java              @Valid email + password
│   │   │   │   ├── NotificationDTO.java
│   │   │   │   ├── OrderDTO.java              includes List<OrderItemDTO>
│   │   │   │   ├── OrderItemDTO.java          price snapshot: price, mrp, discountPercent
│   │   │   │   ├── ProductDTO.java            categoryName + sellerName as derived fields
│   │   │   │   ├── RegisterDTO.java           confirmPassword + phone + role
│   │   │   │   ├── ReviewDTO.java
│   │   │   │   └── UserDTO.java               includes phone + blocked
│   │   │   │
│   │   │   ├── entity/                        ← 12 JPA entities → 13 MySQL tables
│   │   │   │   ├── Cart.java                  @OneToOne(user) @OneToMany(cartItems, cascade+orphan)
│   │   │   │   ├── CartItem.java              @ManyToOne(cart) @ManyToOne(product)
│   │   │   │   ├── Category.java              @OneToMany(products, cascade=ALL)
│   │   │   │   ├── Notification.java          @ManyToOne(user); NotificationType enum (6 values)
│   │   │   │   ├── Order.java                 @ManyToOne(buyer) @OneToMany(orderItems) @OneToOne(payment)
│   │   │   │   │                              OrderStatus enum: PENDING/PROCESSING/SHIPPED/DELIVERED/CANCELLED
│   │   │   │   ├── OrderItem.java             price snapshot fields: price, mrp, discountPercent
│   │   │   │   ├── OtpVerification.java       no FK to users — email string match; isExpired() helper
│   │   │   │   ├── Payment.java               @OneToOne(order, unique=true); PaymentStatus + PaymentMethod enums
│   │   │   │   ├── Product.java               @ManyToOne(category) @ManyToOne(seller/user); active flag
│   │   │   │   ├── Review.java                @Table(uniqueConstraints = UNIQUE(product_id, buyer_id))
│   │   │   │   ├── User.java                  Role enum: BUYER/SELLER/ADMIN; enabled + blocked booleans
│   │   │   │   └── Wishlist.java              @ManyToMany(products) via wishlist_products junction
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── AuthException.java         extends RuntimeException
│   │   │   │   ├── GlobalExceptionHandler.java @ControllerAdvice — catches all, logs, returns error page
│   │   │   │   ├── PaymentException.java
│   │   │   │   └── ProductException.java
│   │   │   │
│   │   │   ├── repository/                    ← 12 JpaRepository interfaces
│   │   │   │   ├── CartItemRepository.java    findByCartAndProduct, deleteByCart
│   │   │   │   ├── CartRepository.java        findByUser(User): Optional<Cart>
│   │   │   │   ├── CategoryRepository.java    existsByName(String): boolean
│   │   │   │   ├── NotificationRepository.java findByUserAndIsReadFalse, countByUserAndIsReadFalse
│   │   │   │   ├── OrderItemRepository.java   findByOrder
│   │   │   │   ├── OrderRepository.java       findByBuyerOrderByOrderedAtDesc; @Query SUM for revenue
│   │   │   │   ├── OtpVerificationRepository.java findByEmail; findByEmailAndUsedFalse
│   │   │   │   ├── PaymentRepository.java     findByRazorpayOrderId
│   │   │   │   ├── ProductRepository.java     findByActiveTrue; findBySeller; findByStockQuantityEquals(0)
│   │   │   │   │                              findByNameContainingIgnoreCaseAndActiveTrue
│   │   │   │   ├── ReviewRepository.java      existsByProductAndBuyer; @Query AVG(rating)
│   │   │   │   ├── UserRepository.java        findByEmail; existsByEmail
│   │   │   │   └── WishlistRepository.java    findByUser(User): Optional<Wishlist>
│   │   │   │
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java  OncePerRequestFilter; reads cookie "jwt"; sets SecurityContext
│   │   │   │   ├── JwtUtil.java                  generateToken(email,role); validateToken; extractClaims
│   │   │   │   └── UserDetailsServiceImpl.java   loadUserByUsername(email) → checks enabled + blocked
│   │   │   │
│   │   │   └── service/                       ← 11 services — ALL @Transactional at class level
│   │   │       ├── AdminService.java          getDashboardStats (counts + revenue); blockUser; unblockUser
│   │   │       ├── AuthService.java           register; sendOtp; verifyOtp; login; resetPassword
│   │   │       ├── CartService.java           getOrCreate; addToCart; removeFromCart; updateQty; clearCart; total
│   │   │       ├── CategoryService.java       getAllCategories; addCategory (dup check); deleteCategory
│   │   │       ├── EmailService.java          @Async sendOtpEmail; sendOrderConfirmation
│   │   │       ├── NotificationService.java   send; getNotifications; getUnreadCount; markAsRead; markAllAsRead
│   │   │       ├── OrderService.java          placeOrder (stock deduct + snapshot); cancelOrder (restore); updateStatus
│   │   │       ├── PaymentService.java        createRazorpayOrder (paise); confirmPayment (HMAC verify)
│   │   │       ├── ProductService.java        addProduct; updateProduct (ownership); deleteProduct; search; toggle
│   │   │       ├── ReviewService.java         addReview (dup check); getByProduct; avgRating; delete
│   │   │       └── WishlistService.java       getOrCreate; add; remove; moveToCart; isInWishlist
│   │   │
│   │   └── resources/
│   │       ├── application.properties         ← only ${ENV_VAR} placeholders, no real secrets
│   │       ├── log4j2.xml                     ← console + rolling file appender
│   │       └── templates/
│   │           ├── admin/
│   │           │   ├── categories.html        add + delete categories table
│   │           │   ├── dashboard.html         stats cards: users/products/orders/revenue
│   │           │   ├── orders.html            all orders + status update dropdown
│   │           │   ├── products.html          all products + force-delete
│   │           │   └── users.html             all users + block/unblock buttons
│   │           ├── auth/
│   │           │   ├── forgot-password.html   email input → sends OTP
│   │           │   ├── login.html             email + password + flash messages
│   │           │   ├── register.html          firstName, lastName, email, phone, password, confirm, role
│   │           │   ├── reset-password.html    email + OTP + newPassword
│   │           │   └── verify-otp.html        email + OTP + resend button
│   │           ├── buyer/
│   │           │   ├── cart.html              items table + quantities + total + checkout button
│   │           │   ├── checkout.html          address form + payment method radio + summary
│   │           │   ├── home.html              product grid + search bar + category filter
│   │           │   ├── notifications.html     list with type badges + mark-read buttons
│   │           │   ├── order-detail.html      order info + items table + payment info
│   │           │   ├── orders.html            order history table + status badges + cancel button
│   │           │   ├── product-detail.html    product info + reviews + add-to-cart + wishlist toggle
│   │           │   ├── razorpay-payment.html  Razorpay.js integration + auto-submit form
│   │           │   └── wishlist.html          wishlist items + move-to-cart + remove
│   │           ├── error/
│   │           │   ├── 404.html
│   │           │   └── 500.html
│   │           ├── seller/
│   │           │   ├── add-product.html       full product form + image upload
│   │           │   ├── dashboard.html         stats + recent notifications
│   │           │   ├── edit-product.html      pre-populated product form
│   │           │   ├── low-stock.html         filtered list: stockQuantity < 5
│   │           │   ├── notifications.html     seller notification center
│   │           │   └── products.html          product table + edit/delete/toggle actions
│   │           ├── home.html                  public homepage (no auth needed)
│   │           └── product-detail.html        public product detail (no auth needed)
│   │
│   └── test/
│       └── java/com/revshop/
│           ├── config/
│           │   └── DataInitializerTest.java
│           ├── security/
│           │   └── JwtUtilTest.java
│           └── service/
│               ├── AuthServiceTest.java
│               ├── CartServiceTest.java
│               ├── CategoryServiceTest.java
│               ├── OrderServiceTest.java
│               ├── PaymentServiceTest.java
│               ├── ProductServiceTest.java
│               ├── ReviewServiceTest.java
│               └── WishlistServiceTest.java
│
├── logs/                    ← Log4j2 rolling file output (git-ignored)
├── uploads/                 ← Product images stored at runtime (git-ignored)
│   └── products/
├── .env                     ← NOT COMMITTED — copy from .env.example
├── .env.example             ← Template showing all required variable names
├── .gitignore
├── pom.xml
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Min Version | Check |
|------|-------------|-------|
| Java JDK | 17 | `java -version` |
| Maven | 3.9 | `mvn -version` |
| MySQL Server | 8.0 | `mysql --version` |
| Git | any | `git --version` |

### Step 1 — Clone

```bash
git clone https://github.com/benhurjoy/Rev_Shop.git
cd Rev_Shop
```

### Step 2 — Database Setup

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create the database
CREATE DATABASE IF NOT EXISTS revshop_database
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- (Optional) dedicated user instead of root
CREATE USER 'revshop_user'@'localhost' IDENTIFIED BY 'StrongPassword!';
GRANT ALL PRIVILEGES ON revshop_database.* TO 'revshop_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### Step 3 — Environment File

```bash
cp .env.example .env
# Open .env and fill in every variable — see Environment Variables section below
```

### Step 4 — Create Required Directories

```bash
mkdir -p logs
mkdir -p uploads/products
```

### Step 5 — Build & Run

```bash
# First run — skip tests until DB is confirmed
mvn clean install -DskipTests
mvn spring-boot:run
```

Application starts at **`http://localhost:8081`**

> To change the port, set `server.port=XXXX` in `application.properties`.

### Step 6 — Default Admin Login

| Field | Value |
|-------|-------|
| URL | `http://localhost:8081/auth/login` |
| Email | `admin@revshop.com` |
| Password | `admin123` |

> Created automatically by `DataInitializer.java` on first startup.  
> Only created if the email does not already exist — safe to restart.

---

## 🔑 Environment Variables

Create `.env` in the root directory (same folder as `pom.xml`):

```env
# ─── Database ──────────────────────────────────────────────────────────────
# Only the password is in .env; host, port, dbname are in application.properties
DB_PASSWORD=your_mysql_password_here

# ─── JWT ───────────────────────────────────────────────────────────────────
# Must be at least 32 characters for HMAC-SHA256 to work securely
JWT_SECRET=replace-with-a-long-random-secret-string-min-32-chars

# ─── Gmail SMTP ────────────────────────────────────────────────────────────
# Use a Gmail App Password, NOT your real Gmail password
# How to get one: Google Account → Security → 2-Step Verification → App Passwords
MAIL_USERNAME=your.gmail@gmail.com
MAIL_PASSWORD=xxxx-xxxx-xxxx-xxxx

# ─── Razorpay ──────────────────────────────────────────────────────────────
# Get from: https://dashboard.razorpay.com/app/keys
# Use test keys (rzp_test_*) during development
RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### Full Variable Reference

| Variable | Required | Where Used | Notes |
|----------|----------|-----------|-------|
| `DB_PASSWORD` | ✅ | `application.properties` `spring.datasource.password` | MySQL password |
| `JWT_SECRET` | ✅ | `JwtUtil.java` | HMAC-SHA256 signing key; min 32 chars |
| `MAIL_USERNAME` | ✅ | `EmailService.java` | Gmail sender address |
| `MAIL_PASSWORD` | ✅ | `EmailService.java` | Gmail App Password (16 chars, no spaces) |
| `RAZORPAY_KEY_ID` | ✅ | `PaymentController.java` | Shown in Razorpay.js frontend |
| `RAZORPAY_KEY_SECRET` | ✅ | `PaymentService.java` | HMAC-SHA256 verification; never sent to client |

---

## 📡 API / Route Reference

### 🌐 Public — No Authentication Required

| Method | Path | Controller | Description |
|--------|------|-----------|-------------|
| `GET` | `/` | `HomeController` | Public homepage; all active products |
| `GET` | `/?keyword={q}` | `HomeController` | Search by product name (case-insensitive) |
| `GET` | `/?categoryId={id}` | `HomeController` | Filter by category |
| `GET` | `/product/{id}` | `HomeController` | Public product detail + reviews |
| `GET` | `/auth/login` | `AuthController` | Login page |
| `POST` | `/auth/login` | `AuthController` | Submit credentials → sets JWT cookie → redirects by role |
| `GET` | `/auth/register` | `AuthController` | Registration page |
| `POST` | `/auth/register` | `AuthController` | Creates user (enabled=false) → sends OTP email |
| `GET` | `/auth/verify-otp` | `AuthController` | OTP entry page |
| `POST` | `/auth/verify-otp` | `AuthController` | Verifies OTP → sets user.enabled=true |
| `POST` | `/auth/resend-otp` | `AuthController` | Resends OTP to email |
| `GET` | `/auth/logout` | `AuthController` | Clears JWT cookie → redirects to login |
| `GET` | `/auth/forgot-password` | `AuthController` | Forgot password page |
| `POST` | `/auth/forgot-password` | `AuthController` | Sends password-reset OTP |
| `GET` | `/auth/reset-password` | `AuthController` | Reset password form |
| `POST` | `/auth/reset-password` | `AuthController` | Verifies OTP → saves new BCrypt hash |

### 🛒 Buyer Routes (`ROLE_BUYER` required)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/buyer/home` | Buyer home — product grid with search and filter |
| `GET` | `/buyer/product/{id}` | Product detail (with wishlist status, review check) |
| `GET` | `/buyer/cart` | View cart — items, quantities, total |
| `POST` | `/buyer/cart/add` | Add to cart `{productId, quantity}` — validates stock |
| `POST` | `/buyer/cart/update/{cartItemId}` | Update item quantity |
| `POST` | `/buyer/cart/remove/{cartItemId}` | Remove item from cart |
| `GET` | `/buyer/wishlist` | View wishlist |
| `POST` | `/buyer/wishlist/add/{productId}` | Add product to wishlist |
| `POST` | `/buyer/wishlist/remove/{productId}` | Remove from wishlist |
| `POST` | `/buyer/wishlist/move-to-cart/{productId}` | Move item to cart + remove from wishlist |
| `GET` | `/buyer/checkout` | Checkout page — redirects to cart if empty |
| `POST` | `/buyer/checkout` | Place order — COD confirms; Razorpay renders payment page |
| `POST` | `/buyer/payment/callback` | Razorpay callback — HMAC verified → SUCCESS or FAILED |
| `GET` | `/buyer/orders` | Order history |
| `GET` | `/buyer/orders/{orderId}` | Order detail with line items and payment info |
| `POST` | `/buyer/orders/cancel/{orderId}` | Cancel order — restores stock per item |
| `POST` | `/buyer/review/add` | Submit review `{productId, rating, comment}` |
| `GET` | `/buyer/notifications` | Notification center |
| `POST` | `/buyer/notifications/read/{id}` | Mark single notification as read |
| `POST` | `/buyer/notifications/read-all` | Mark all notifications as read |

### 🏪 Seller Routes (`ROLE_SELLER` required)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/seller/dashboard` | Dashboard — my products, unread count |
| `GET` | `/seller/products` | All my product listings |
| `GET` | `/seller/products/add` | Add product form |
| `POST` | `/seller/products/add` | Submit new product with optional image upload |
| `GET` | `/seller/products/edit/{id}` | Edit product form |
| `POST` | `/seller/products/edit/{id}` | Submit edit — ownership verified |
| `POST` | `/seller/products/delete/{id}` | Delete product — ownership verified |
| `POST` | `/seller/products/toggle/{id}` | Toggle `active` flag — hides/shows to buyers |
| `GET` | `/seller/low-stock` | Products with `stockQuantity < 5` |
| `GET` | `/seller/notifications` | Seller notification center |
| `POST` | `/seller/notifications/read/{id}` | Mark read |
| `POST` | `/seller/notifications/read-all` | Mark all read |

### 🛡️ Admin Routes (`ROLE_ADMIN` required)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/admin/dashboard` | Stats: users, products, orders, revenue |
| `GET` | `/admin/users` | All users with role and status |
| `POST` | `/admin/users/block/{id}` | Set `user.blocked = true` |
| `POST` | `/admin/users/unblock/{id}` | Set `user.blocked = false` |
| `GET` | `/admin/products` | All active products |
| `POST` | `/admin/products/delete/{id}` | Force-delete any product |
| `GET` | `/admin/categories` | Category management |
| `POST` | `/admin/categories/add` | Add category — duplicate name rejected |
| `POST` | `/admin/categories/delete/{id}` | Delete category |
| `GET` | `/admin/orders` | All orders platform-wide with status dropdowns |
| `POST` | `/admin/orders/status/{id}` | Update order status `{status}` |
| `POST` | `/admin/reviews/delete/{id}` | Remove inappropriate review |

---

## 🗃️ Database Schema Notes

### Unique Constraints

```sql
-- Enforced at DB level (also validated in services)
ALTER TABLE users     ADD CONSTRAINT uq_users_email     UNIQUE (email);
ALTER TABLE carts     ADD CONSTRAINT uq_carts_user      UNIQUE (user_id);
ALTER TABLE wishlists ADD CONSTRAINT uq_wishlists_user  UNIQUE (user_id);
ALTER TABLE payments  ADD CONSTRAINT uq_payments_order  UNIQUE (order_id);
ALTER TABLE reviews   ADD CONSTRAINT uq_reviews_pb      UNIQUE (product_id, buyer_id);
ALTER TABLE categories ADD CONSTRAINT uq_cat_name       UNIQUE (name);
```

### Why `order_items` Stores Price Snapshots

```
Product.price may change after purchase (seller edits it).
order_items stores price, mrp, discountPercent AT the time of purchase.
This ensures order history always shows what the buyer actually paid —
not the current product price.
```

### Why `otp_verifications` Has No FK to `users`

```
OTPs are sent before the account is fully activated.
At send time the user row exists but enabled=false.
Matching by email string (not FK) keeps the logic simple
and avoids complications if the user row is deleted before verification.
```

---

## 🧪 Testing

```bash
# Run all tests (uses H2 in-memory — no MySQL required)
mvn test

# Run a specific test class
mvn test -Dtest=CartServiceTest

# Run with output printed to console
mvn test -Dsurefire.useFile=false

# Generate Jacoco coverage report
mvn test jacoco:report
# View: target/site/jacoco/index.html
```

### Test Coverage

| Test File | Owner | Key Scenarios |
|-----------|-------|--------------|
| `AuthServiceTest` | Benhur | Register OK, duplicate email, OTP expiry, invalid OTP, login OK, blocked user, wrong password, reset password |
| `JwtUtilTest` | Benhur | Token generated, email extracted, role extracted, expired token rejected, tampered token rejected |
| `DataInitializerTest` | Benhur | Admin created on first run, not duplicated on second run |
| `ProductServiceTest` | Chandini | Add product OK, edit with ownership check, edit by wrong seller rejected, delete OK, search by keyword, filter by category, toggle visibility |
| `CategoryServiceTest` | Chandini | Add OK, duplicate name rejected, list all, delete |
| `CartServiceTest` | Sai | Add to cart OK, out-of-stock rejected, update quantity, remove item, calculate total, clear cart |
| `WishlistServiceTest` | Sai | Add OK, duplicate silent, remove, move-to-cart (adds + removes), isInWishlist |
| `OrderServiceTest` | Naveen | Place order OK, stock deducted, empty cart rejected, stock insufficient rejected, cancel → stock restored, status update |
| `PaymentServiceTest` | Naveen | Razorpay order created (paise), valid HMAC accepted, mismatched HMAC rejected, COD direct confirm |
| `ReviewServiceTest` | Veerababu | Add review OK, duplicate blocked (UNIQUE constraint), average rating calculation, delete by admin |

### H2 Test Configuration

```properties
# src/test/resources/application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

---

## 🌿 Git Workflow

### Branch Strategy

```
main        ← tagged releases only (v1.0.0, v1.1.0 ...)
  └── develop  ← integration — all PRs target here
        ├── feature/benhur-auth-admin
        ├── feature/chandini-seller-product
        ├── feature/sai-buyer-cart
        ├── feature/naveen-order-payment
        └── feature/veera-inventory-review-notification
```

### Rules

- **Never push directly to `main` or `develop`**
- Every change goes through a Pull Request
- Pull Requests must target `develop`
- Benhur reviews and merges all PRs (resolves conflicts)
- Delete your feature branch after merge

### Commit Message Convention

```
Day{N}-{type}: short description of what changed

Types:
  feat     new feature
  fix      bug fix
  refactor code restructure (no behavior change)
  test     add or fix tests
  docs     README, comments, javadoc
  chore    build config, .gitignore, etc.

Examples:
  Day6-feat: add ProductService.searchProducts by keyword
  Day7-fix: CartService returns List.of() instead of null
  Day8-refactor: move HMAC logic from controller to PaymentService
```

### Daily Workflow

```bash
# Start of day — get latest from develop
git checkout feature/your-branch
git pull origin develop

# Work on your files only — stage selectively
git add src/main/java/com/revshop/service/YourService.java
git add src/main/resources/templates/your/page.html
git status   # verify only your files are staged

# Commit and push
git commit -m "Day8-fix: WishlistService null safety on empty wishlist"
git push origin feature/your-branch

# Open Pull Request on GitHub:
# Base: develop ← Compare: feature/your-branch
```

### PR Merge Order (avoids dependency conflicts)

```
1. feature/benhur-auth-admin              ← security foundation first
2. feature/chandini-seller-product        ← Product + Category entities
3. feature/veera-inventory-review-notification
4. feature/sai-buyer-cart                 ← Cart + Wishlist
5. feature/naveen-order-payment           ← depends on Cart and Product
```

### Final Release

```bash
# After all 5 PRs are merged to develop and tested
git checkout main
git merge develop --no-ff -m "Release v1.0.0"
git push origin main

git tag -a v1.0.0 -m "RevShop v1.0.0 — production release"
git push origin v1.0.0
```

---

## 🎨 UI Design System

| Token | Value | Used For |
|-------|-------|---------|
| Background | `#0A1628` | Page backgrounds |
| Card background | `#0F1E35` | Navbar, cards, modals |
| Border | `#1A3050` | Card borders, dividers |
| Primary accent | `#FF6B35` | CTA buttons, highlights, links |
| Primary hover | `#FF8C5A` | Hover states on primary elements |
| Text primary | `#E2E8F0` | Body text |
| Text muted | `#6B8AB3` | Secondary info, captions |
| Success | `#2EC98E` | Success badges, delivered status |
| Warning | `#FFD166` | Warning badges, low-stock |
| Danger | `#FC8181` | Error messages, cancel actions |
| Display font | Playfair Display 700/900 | `h1`–`h3` headings |
| Body font | DM Sans 400/500/600 | All body text |
| Mono font | JetBrains Mono | Code snippets, IDs |

---

## 🔧 Troubleshooting

<details>
<summary><strong>LazyInitializationException on any page</strong></summary>

**Symptom:** `org.hibernate.LazyInitializationException: could not initialize proxy — no Session`

**Cause:** A `@ManyToOne(fetch = FetchType.LAZY)` relationship is accessed after the Hibernate session has closed (outside a `@Transactional` boundary), typically while Thymeleaf is rendering.

**Fix:** Add `@Transactional` at class level to the service that loads the entity:
```java
@Service
@Transactional           // ← this line
public class ProductService { ... }
```
All 11 services in RevShop should already have this. If the error appears, check which service is loading the entity for that route.

</details>

<details>
<summary><strong>HTTP 500 on a buyer page with lists</strong></summary>

**Symptom:** 500 error on pages that use `th:each` (cart, wishlist, orders, notifications).

**Cause:** A service method returned `null` instead of an empty list.

**Fix:** Every list-returning method must wrap in try/catch and return `List.of()`:
```java
public List<CartItemDTO> getCartItems(String email) {
    try {
        Cart cart = cartRepository.findByUser(...).orElse(null);
        if (cart == null) return List.of();
        return cart.getCartItems().stream().map(this::toDTO).toList();
    } catch (Exception e) {
        log.error("getCartItems failed for {}", email, e);
        return List.of();                // ← Thymeleaf handles empty list safely
    }
}
```

</details>

<details>
<summary><strong>OTP emails not arriving</strong></summary>

**Cause:** Gmail App Password not configured correctly.

**Steps:**
1. Go to [myaccount.google.com](https://myaccount.google.com)
2. Security → 2-Step Verification → enable it
3. Security → App Passwords → generate for "Mail"
4. Copy the 16-character code (remove spaces) into `MAIL_PASSWORD` in `.env`
5. Ensure `MAIL_USERNAME` is the same Gmail address

</details>

<details>
<summary><strong>Razorpay: Payment always shows as FAILED</strong></summary>

**Cause:** `RAZORPAY_KEY_SECRET` is wrong or has extra whitespace in `.env`.

**Fix:**
1. Copy the secret directly from Razorpay Dashboard → Settings → API Keys
2. Ensure no leading/trailing spaces in the `.env` value
3. Make sure you're using **test** keys (`rzp_test_*`) for development — live keys need SSL

</details>

<details>
<summary><strong>Build fails: "cannot find symbol" on DTO or Repository class</strong></summary>

**Cause:** A DTO, Exception, or Repository file is missing from the source tree.

**Fix:** Verify all files listed in the Project Structure section above exist.  
Common missing files: `DashboardDTO.java`, `CheckoutDTO.java`, `OrderItemDTO.java`, `OtpVerificationRepository.java`.

</details>

<details>
<summary><strong>Images not loading (product images show as broken)</strong></summary>

**Cause:** The `/uploads/` directory doesn't exist or `WebConfig` resource handler isn't mapping it.

**Fix:**
1. Ensure `uploads/products/` directory exists in project root
2. Verify `WebConfig.java` has:
```java
registry.addResourceHandler("/uploads/**")
        .addResourceLocations("file:uploads/");
```
3. Check `application.properties` has `file.upload-dir=uploads`

</details>

---

<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=6,11,20&height=100&section=footer" width="100%"/>

**Built with ❤️ by Team RevShop**

🔴 **Benhur** · 🟢 **Chandini** · 🔵 **Sai** · 🟣 **Naveen** · 🟡 **Veerababu**

*Spring Boot 3.2 · MySQL 8.0 · Razorpay · March 2026 · Release v1.0.0*

[![GitHub](https://img.shields.io/badge/GitHub-Rev__Shop-FF6B35?style=flat-square&logo=github&logoColor=white)](https://github.com/benhurjoy/Rev_Shop)

</div>
