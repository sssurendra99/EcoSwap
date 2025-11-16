# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

EcoSwap is a sustainable e-commerce marketplace built with Spring Boot 3.3.5, Java 21, and Thymeleaf templates. The platform supports three user roles (CUSTOMER, SELLER, ADMIN) with role-based dashboards and authentication flows.

## Build and Run Commands

### Running the Application
```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
.\mvnw spring-boot:run
```

The application runs on port 9000 by default (http://localhost:9000).

### Building the Project
```bash
./mvnw clean install
```

### Running Tests
```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=ClassName

# Run a specific test method
./mvnw test -Dtest=ClassName#methodName
```

### Database Setup
The application uses MySQL via Docker Compose:
```bash
docker-compose up -d
```

**Important**: The compose.yaml defines database name as `mydatabase`, but application.properties connects to `ecoswap`. Ensure database exists or update configuration for consistency.

## Architecture

### Multi-Role Authentication System
The application implements role-based access control with three distinct user types:
- **CUSTOMER**: Shopping, cart, orders, profile management
- **SELLER**: Product management, order fulfillment, sales dashboard
- **ADMIN**: User management, platform administration

**Authentication Flow**: `RoleBasedAuthSuccessHandler` redirects users post-login:
- ADMIN → `/admin/dashboard`
- SELLER → `/seller/dashboard`
- CUSTOMER → `/customer/dashboard`

Spring Security configuration in `SecurityConfig.java` enforces role-based URL patterns.

### Domain Model Relationships
Key entities and their relationships:

**User** (Central entity):
- Has one `CustomerProfile` or `SellerProfile` based on role
- One User (as seller) → Many Products
- One User (as customer) → Many Orders
- One User → One Cart

**Product**:
- ManyToOne relationship with `Category`
- ManyToOne relationship with `User` (seller)
- Tracks sustainability metrics: `ecoScore`, `co2Saved`, `plasticSaved`
- Status management: ACTIVE/INACTIVE/ARCHIVED

**Order**:
- ManyToOne with `User` (customer)
- OneToMany with `OrderItem` (cascade ALL, orphan removal)
- Status flow: PENDING → CONFIRMED → SHIPPED → DELIVERED (see `OrderStatus` enum)
- Includes helper methods: `calculateTotal()`, `addOrderItem()`, `removeOrderItem()`

**Cart**:
- OneToOne with `User`
- OneToMany with `CartItem` (cascade ALL, orphan removal)

### Service Layer Pattern
Services contain business logic and orchestrate repository operations:
- `UserService`: User management, registration
- `ProductService`: Product CRUD, inventory management
- `CategoryService`: Category management with parent-child relationships
- `OrderService`: Order processing, status transitions
- `CartService`: Cart operations, item management

### Template Layout System
Thymeleaf templates use Layout Dialect for consistent page structure:

**Layout Files** (`/templates/layout/`):
- `public_layout.html`: For unauthenticated pages (/, /shop, /product/*)
- `dashboard_layout.html`: For authenticated role-specific dashboards

**Template Directories**:
- `/templates/public/`: Public-facing pages (shop, product details)
- `/templates/dashboard/`: Role-specific dashboard pages
- `/templates/admin/`: Admin-only pages
- `/templates/cart/`: Shopping cart and checkout
- `/templates/profile/`: User profile management

### Repository Layer
All repositories extend `JpaRepository`. Key repositories:
- Custom query methods follow Spring Data naming conventions
- `ProductRepository`: Includes queries for filtering by category, seller, status
- `OrderRepository`: Queries by customer, seller (for order management), status
- `CategoryRepository`: Parent-child category hierarchy support

## Technology Stack Notes

### Lombok Usage
Models extensively use Lombok annotations:
- `@Getter`, `@Setter`: Generate getters/setters
- `@NoArgsConstructor`: Required by JPA/Hibernate
- `@AllArgsConstructor`: For custom constructors
- Ensure Lombok annotation processor is configured in IDE

### Jakarta Persistence (JPA)
Uses Jakarta EE namespace (`jakarta.persistence.*`), not legacy `javax.persistence.*`. Spring Boot 3+ requires Jakarta APIs.

### Security
- BCrypt password encoding
- CSRF disabled (csrf disabled in SecurityConfig)
- Custom `UserDetailsService` implementation: `CustomUserDetailsService`
- Session-based authentication with form login

### Database
- MySQL 8 dialect
- Hibernate DDL auto-update enabled (consider using migrations for production)
- Show SQL enabled for debugging

## Key Implementation Patterns

### Entity Lifecycle Callbacks
Models use `@PreUpdate` to automatically set `updatedAt` timestamps before updates.

### Bidirectional Relationships
When working with bidirectional JPA relationships (e.g., Order-OrderItem), always use helper methods to maintain both sides:
```java
order.addOrderItem(item);  // Sets both order.orderItems and item.order
```

### Product SKU Generation
SKUs must be unique. Consider implementing auto-generation if not already present.

### Order Number Generation
Order numbers must be unique (`orderNumber` field). Implement generation strategy if missing.

## Common Pitfalls

1. **Database Name Mismatch**: compose.yaml creates `mydatabase`, but app connects to `ecoswap`
2. **Role Prefix**: Spring Security adds `ROLE_` prefix. Use `ROLE_ADMIN` in authorities checks, but `Role.ADMIN` enum
3. **Cascade Operations**: Order and Cart have cascade ALL - be careful with entity state transitions
4. **Image Paths**: Product images stored as string paths - ensure file upload handling is implemented
5. **BigDecimal for Money**: Always use `BigDecimal` for currency calculations, never `double`
6. **Invalid HTML Nesting**: NEVER nest interactive elements (buttons, links) inside `<a>` tags. This causes browsers to auto-correct the DOM structure, breaking layouts. If a card needs to be clickable, make the entire card a link OR use JavaScript with divs

## Development Workflow

When adding new features:
1. Start with model/entity changes and database schema updates
2. Create/update repository interfaces with custom query methods
3. Implement business logic in service layer
4. Add controller endpoints with proper role-based security
5. Create/update Thymeleaf templates using appropriate layout
6. Test role-based access control for new endpoints

## Frontend Development Guidelines

### CSS and JavaScript Separation
**IMPORTANT**: Always keep styles and JavaScript in separate files. Never use inline `<style>` tags or `<script>` tags within HTML templates.

**CSS Files**:
- Location: `/src/main/resources/static/css/`
- Create a dedicated CSS file for each page or component (e.g., `about.css`, `faq.css`, `product-card.css`)
- Link CSS files in the `<head>` section using Thymeleaf:
  ```html
  <link th:href="@{/css/filename.css}" rel="stylesheet"/>
  ```

**JavaScript Files**:
- Location: `/src/main/resources/static/js/`
- Create dedicated JS files for page-specific functionality (e.g., `faq.js`, `cart.js`, `checkout.js`)
- Always use the `defer` attribute to ensure scripts execute after DOM is loaded
- Link JS files in the `<head>` section using Thymeleaf:
  ```html
  <script th:src="@{/js/filename.js}" defer></script>
  ```

**Benefits of Separation**:
- Improved maintainability and code organization
- Better browser caching and performance
- Easier debugging and testing
- Cleaner HTML templates
- Reusability across multiple pages

**Example Template Structure**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/public_layout}">
<head>
    <meta charset="UTF-8">
    <title>Page Title - EcoSwap</title>
    <link th:href="@{/css/page-specific.css}" rel="stylesheet"/>
    <script th:src="@{/js/page-specific.js}" defer></script>
</head>
<body>
    <main layout:fragment="content">
        <!-- Page content here -->
    </main>
</body>
</html>
```

**Exceptions**:
- Small inline styles for dynamic values (e.g., `style="color: [[${dynamicColor}]]"`) are acceptable when needed
- Avoid using `onclick` attributes; prefer event listeners in external JS files
