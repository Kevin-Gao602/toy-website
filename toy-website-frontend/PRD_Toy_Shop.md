# PRD: Toy Web Shop  
**Tech Stack:** Spring Boot (Backend) + Vue 3 (Frontend)

---

## 1. Overview

This project is a minimal toy e-commerce web application.

Users can:
- Browse a product catalog
- View product details
- Add/remove items from a cart
- Proceed through checkout (address + shipping + review)
- Place an order without payment integration

Orders are created with status **AWAITING_PAYMENT**.

---

## 2. Goals and Non-Goals

### 2.1 Goals

- End-to-end shopping flow from browsing to order placement (without payment)
- Clear separation of concerns:
  - Frontend UI (Vue)
  - Backend API (Spring Boot)
  - Database
- Basic authentication and authorization
- Basic admin capability to manage products (optional for MVP)
- Demonstrate clean architecture and API design

### 2.2 Non-Goals

- No real payment integration
- No advanced inventory reservation logic
- No third-party integrations (email/payment)

---

## 3. Success Criteria

A successful delivery means:

- A user can register/login
- A user can browse products and view product details
- A user can add items to cart and manage quantities
- A user can checkout and place an order
- Order is created with status `AWAITING_PAYMENT`
- Stock is validated and decremented at order creation
- Products are created via admin UI or seeded database
- Basic automated tests exist
- Project includes a clean README

---

## 4. Personas

### 4.1 Guest Visitor
- Browses product catalog
- Views product details

### 4.2 Customer
- Registers / logs in
- Manages cart
- Checks out
- Views past orders

### 4.3 Admin (Stretch)
- Creates / updates products
- Views orders

---

## 5. User Journeys

### 5.1 Browse → Add to Cart → Checkout → Place Order

1. Visitor opens Home / Catalog page
2. Filters or searches products
3. Opens a Product Detail page
4. Adds product to cart (quantity = 1)
5. Goes to Cart page and adjusts quantities
6. Proceeds to Checkout
7. Enters Shipping Address
8. Chooses Shipping Method
9. Reviews order summary
10. Places order
11. Sees Order Confirmation page with:
    - Order number
    - Status: `AWAITING_PAYMENT`
    - Summary of items and totals
    - Message: “Payment not supported in this toy app”

### 5.2 Customer Views Order History

1. Customer navigates to “My Orders”
2. Sees list of orders
3. Opens an order detail page

---

## 6. Scope

### 6.1 MVP Features

- Product catalog
- Authentication
- Cart
- Checkout (pre-payment)
- Orders

### 6.2 Stretch Features (Nice-to-have)

- Admin product management UI
- Product image upload or image URL
- Inventory low-stock warnings
- Guest cart merged with user cart on login
- Cancel order (if awaiting payment)
- Mock email notifications

---

## 7. Functional Requirements

---

### 7.1 Product Catalog

#### Requirements
- Product list page with pagination
- Search by keyword (name or description)
- Filter by category
- Product detail page

#### Product List Displays
- Name
- Price
- Thumbnail image
- “Add to cart” button

#### Behavior
- If stock = 0:
  - Show “Out of stock”
  - Disable add-to-cart

#### Acceptance Criteria
- Catalog loads within reasonable time (< 1s locally)
- Search returns matching products
- Category filter works

---

### 7.2 Product Detail

#### Requirements
- Show:
  - Name
  - Description
  - Price
  - Stock status
  - Image
- Quantity selector:
  - Min: 1
  - Max: available stock

#### Acceptance Criteria
- If product stock is 0, add-to-cart is disabled
- Backend rejects quantity exceeding stock

---

### 7.3 Authentication

#### Requirements
- Register: email, password, name
- Login: email, password
- JWT-based authentication
- Roles:
  - CUSTOMER
  - ADMIN (can be seeded)

#### Acceptance Criteria
- Unauthenticated users can browse products
- Only authenticated users can:
  - Place orders
  - View “My Orders”
- Admin-only endpoints are protected

---

### 7.4 Cart

#### Requirements
- Cart tied to a user (server-side preferred)
- Cart operations:
  - Add item (productId, quantity)
  - Update item quantity
  - Remove item
  - View cart

#### Cart Displays
- Items
- Subtotal
- Shipping (estimated or later)
- Total (at checkout)

#### Rules
- Adding same product increments quantity (or replaces; choose one and document)
- Quantity >= 1
- Quantity <= available stock

---

### 7.5 Checkout (Pre-payment)

#### Steps
1. Shipping address form
2. Shipping method selection
3. Review order

#### Shipping Methods
- Standard: $5
- Express: $15
- Flat fees, hardcoded

#### Totals Calculation
- Subtotal = sum(item.price * quantity)
- Total = subtotal + shipping fee

#### Validation
- Address fields are non-empty
- Cart must not be empty
- Quantities must not exceed stock

---

### 7.6 Order Creation

#### On “Place Order”
- Validate cart again:
  - Product exists
  - Stock sufficient
- Create Order and OrderItems
  - Snapshot product name and price
- Decrement stock
- Clear cart
- Return orderId / orderNumber

#### Order Status
- AWAITING_PAYMENT (default)
- CANCELLED (optional)
- FULFILLED (stretch)

#### Acceptance Criteria
- Order appears in “My Orders”
- Cart is cleared
- Stock is decreased accordingly

---

### 7.7 Orders: History & Details

#### Requirements
- My Orders list:
  - orderNumber
  - createdAt
  - status
  - total
- Order detail page:
  - items
  - totals
  - shipping address summary
  - status

#### Access Control
- Customer can only view own orders
- Admin can view all orders

---

### 7.8 Admin Product Management (Stretch)

#### Requirements
- CRUD operations on products
- Fields:
  - name
  - description
  - price
  - category
  - stock
  - imageUrl

#### Acceptance Criteria
- CRUD works
- Changes appear in catalog immediately

---

## 8. Non-Functional Requirements

### 8.1 Security
- Passwords stored using BCrypt
- JWT for protected routes

### 8.2 Reliability & Consistency
- Order placement must be transactional:
  - Either order + stock update succeed
  - Or nothing is persisted

### 8.3 Observability
- Backend logs:
  - Order placed
  - Stock changes
  - Auth events (no sensitive data)

### 8.4 Performance
- Avoid returning massive payloads
- Use pagination for product list

---

## 9. Frontend Requirements (Vue 3)

### 9.1 Pages

1. Home / Catalog
2. Product Detail
3. Cart
4. Checkout
5. Order Confirmation
6. My Orders
7. Order Detail
8. Login / Register
9. Admin Product Management (Stretch)

---

### 9.2 State Management

Use **Pinia** for:
- Auth token
- Current user
- Cart state

Persist token in `localStorage`.

---

### 9.3 UX Behavior

- Show loading states for API calls
- Show friendly backend error messages
- Disable checkout button if cart is empty
