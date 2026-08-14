# 🍹 Comprehensive Technical Implementation Plan & Architecture Blueprint
## Juice POS, Bar Stock Exchange Dynamic Pricing Engine & Market Crash Event System

---

## 1. Executive Overview & System Objective

The **Juice Shop POS & Dynamic Bar Stock Pricing System** is a production-grade retail point-of-sale platform coupled with a commercial **Bar Stock Exchange dynamic pricing engine** (inspired by Noida Pub Exchange / Shawman DD software). 

The primary business goal is to dynamically adjust drink prices based on **real-time order scarcity, container liquid volume pressure, and time multipliers**, while introducing an automated **Market Crash Routine** that periodically drops drink prices to absolute floor limits to incentivize rapid concurrent ordering.

---

## 2. Monorepo Directory & Architecture Model

```
Juice Dynamic Price Project/
├── backend/                              # Spring Boot 3.3 / Java 21 REST API
│   ├── src/main/java/com/retailpos/
│   │   ├── domain/                       # JPA Entities & Spring Data Repositories
│   │   │   ├── Product.java
│   │   │   ├── JuiceBatch.java
│   │   │   ├── SalesOrder.java
│   │   │   ├── SalesOrderItem.java
│   │   │   ├── PriceHistory.java
│   │   │   └── SystemConfig.java
│   │   ├── inventory/                    # 20L Liquid Container Volume & Lock Logic
│   │   │   └── JuiceBatchService.java
│   │   ├── pos/                          # POS Order Processing & Checkout API
│   │   │   ├── POSController.java
│   │   │   └── POSService.java
│   │   └── pricing/                      # Pub Exchange Algorithm & Market Crash
│   │       ├── DemandCalculationService.java
│   │       ├── StockPressureService.java
│   │       ├── TimeFactorService.java
│   │       ├── PriceAdjustmentService.java
│   │       ├── MarketCrashService.java
│   │       ├── PricingSimulationService.java
│   │       └── PricingController.java
│   ├── src/main/resources/
│   │   ├── application.properties        # Server port 8088 & H2/PostgreSQL config
│   │   └── db/migration/                 # Flyway Schema & Initial Seed Data
│   └── pom.xml
├── customer-web/                         # Customer POS Checkout & Live Stock Ticker (Port 8000)
│   ├── src/index.html                    # Glassmorphism UI, Siren Audio, Flash Cards
│   └── package.json                      # http-server script
├── admin-panel/                          # Executive Command Center & Sandbox Simulator (Port 8001)
│   ├── src/index.html                    # Container Batches, Crash Trigger, Sandbox Simulator
│   └── package.json                      # http-server script
├── docs/                                 # Technical Specifications & Documentation
│   ├── DYNAMIC_PRICING.md
│   ├── PRICING_SIMULATION.md
│   └── IMPLEMENTATION_PLAN.md
├── package.json                          # Root monorepo npm orchestration
└── README.md                             # Global project setup & run instructions
```

---

## 3. Port Allocation & Execution Matrix

| Component | Technology | Default Port | Description |
| :--- | :--- | :---: | :--- |
| **Backend REST API** | Java 21 / Spring Boot 3.3 | `8088` | REST API endpoints for POS, Inventory, Dynamic Pricing, & Market Crash. |
| **Customer POS Terminal** | Static Single Page App | `8000` | POS checkout, Live Bar Stock Ticker, green/red flash card indicators, Siren audio. |
| **Admin Control Center** | Static Single Page App | `8001` | Executive dashboard, 20L batch registration, Market Crash controller, Sandbox. |

---

## 4. Database Schema & Domain Entities

### A. `products` Table
- `id` (BIGINT, Primary Key)
- `name` (VARCHAR): Display name (e.g., "Fresh Mango Juice").
- `flavour` (VARCHAR): Flavour enum/code (`MANGO`, `LEMON`, `MINT`, `ORANGE`, `STRAWBERRY`, `GRAPE`, `LYCHEE`).
- `description` (TEXT): Product description.
- `default_cup_size_ml` (INT): Default serving size ($250\text{ ml}$).
- `current_cup_price` (DECIMAL): Active cup price ($₹18.00 - ₹25.00$).
- `min_cup_price` (DECIMAL): Floor price limit ($₹18.00$).
- `max_cup_price` (DECIMAL): Ceiling price limit ($₹25.00$).
- `last_price_change_timestamp` (TIMESTAMP): Time of last price evaluation.

### B. `juice_batches` Table (20L Liquid Tracking)
- `id` (BIGINT, Primary Key)
- `product_id` (BIGINT, Foreign Key)
- `batch_code` (VARCHAR): Unique code (e.g., `BATCH-MNG-001`).
- `container_capacity_ml` (INT): $20,000\text{ ml}$ ($20\text{ Litres}$).
- `initial_volume_ml` (INT): Initial filled volume ($20,000\text{ ml}$).
- `remaining_volume_ml` (INT): Current liquid remaining in container.
- `cup_size_ml` (INT): $250\text{ ml}$.
- `status` (VARCHAR): `ACTIVE` or `DEPLETED`.

### C. `sales_orders` & `sales_order_items` Tables
- `sales_orders`: `id`, `order_number` (`ORD-...`), `total_amount`, `payment_method` (`CASH`, `UPI`, `CARD`), `payment_status` (`COMPLETED`), `created_at`.
- `sales_order_items`: `id`, `sales_order_id`, `product_id`, `product_name`, `cup_size_ml`, `unit_price`, `quantity`, `total_price`, `volume_deducted_ml`.

### D. `price_history` Table (Audit Logs)
- `id` (BIGINT, Primary Key)
- `product_id` (BIGINT)
- `old_price` (DECIMAL)
- `new_price` (DECIMAL)
- `demand_score` (DOUBLE): Calculated score ($0.0 - 100.0$).
- `stock_pressure_pct` (DOUBLE): Container depletion percentage ($0.0\% - 100.0\%$).
- `time_factor_multiplier` (DOUBLE): Time factor ($1.0 - 1.2$).
- `explanation` (TEXT): Detailed human-readable log explanation.
- `created_at` (TIMESTAMP)

---

## 5. Dynamic Pricing Engine (Pub Exchange Bar Stock Algorithm)

### A. Demand Score Equation ($0.0 - 100.0$)
$$\text{Demand Score} = (w_v \times S_v) + (w_s \times S_s) + (w_t \times S_t)$$
- **Velocity Weight ($w_v = 0.40$)**: Calculated from order frequency in the last 15-minute window.
- **Stock Pressure Weight ($w_s = 0.40$)**:
  $$S_s = 100\% - \left( \frac{\text{remaining\_volume\_ml}}{\text{initial\_volume\_ml}} \times 100 \right)$$
- **Time Factor Weight ($w_t = 0.20$)**:
  - Morning (06:00 - 11:00): Multiplier 1.0 (Score = 50.0)
  - Afternoon (11:00 - 16:00): Multiplier 1.1 (Score = 75.0)
  - Evening (16:00 - 21:00): Multiplier 1.2 (Score = 100.0)
  - Night (21:00 - 23:00): Multiplier 1.0 (Score = 50.0)

### B. Stock Market Field Cross-Pricing Feedback Loop
When a purchase order is submitted at the POS terminal:
1. **Purchased Product(s)**:
   - Demand spikes due to order scarcity.
   - Price increases by $+₹1.00$ (or $+₹2.00$ for bulk quantity $\ge 2$) up to `max_cup_price` ($₹25.00$).
   - Displays green indicator badge ($\mathbf{+₹1\ \mathbf{\Delta}\ SURGE}$) and flashes green (`flash-surge`).
2. **Unpurchased Products**:
   - Demand shifts away.
   - Prices discount by $-₹1.00$ down to `min_cup_price` ($₹18.00$) to incentivize buyers to purchase unbought stock.
   - Displays red indicator badge ($\mathbf{-₹1\ \mathbf{\nabla}\ DISCOUNT}$) and flashes red (`flash-discount`).

### C. Bounded Safety Limits
$$\text{Floor Price } (₹18.00) \le \text{Current Cup Price} \le \text{Ceiling Price } (₹25.00)$$

---

## 6. Noida Pub Exchange "Market Crash" Event Script

```
+-------------------------------------------------------------------------+
|                  Admin Trigger / Scheduled Timer Script                 |
+-------------------------------------------------------------------------+
                                     |
                                     v [POST /api/pricing/market-crash/trigger]
+-------------------------------------------------------------------------+
|                          Market Crash Execution                         |
|  1. Web Audio API Digital Siren Gong Sound Plays on POS Terminals       |
|  2. Emergency Red Screen Takeover Banner & 03:00 Countdown Timer Active |
|  3. Batch Override: All Product Prices Set to Absolute Floor (₹18.00)   |
|  4. Dynamic Pricing Engine Paused During Crash Period                  |
+-------------------------------------------------------------------------+
                                     |
                                     v (Timer Expires / STOP Command)
+-------------------------------------------------------------------------+
|                   Normal Stock Market Trading Resumes                   |
+-------------------------------------------------------------------------+
```

### Key Components:
- **Audio Gong**: Web Audio API oscillator synthesis generating a digital siren gong.
- **REST Endpoints**:
  - `POST /api/pricing/market-crash/trigger?durationMinutes=3`: Initiates Market Crash.
  - `GET /api/pricing/market-crash/status`: Returns active status, remaining seconds, end timestamp.
  - `POST /api/pricing/market-crash/stop`: Manually stops crash and resumes normal dynamic pricing.

---

## 7. Concurrency & Data Consistency

To prevent race conditions when multiple POS terminals concurrently order cups from the same active 20L container batch:
- **Pessimistic Locking**: `JuiceBatchRepository` uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` during volume deductions:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT b FROM JuiceBatch b WHERE b.productId = :productId AND b.status = 'ACTIVE'")
Optional<JuiceBatch> findActiveBatchForProductWithLock(@Param("productId") Long productId);
```
- **Atomicity**: Volume deduction ($250\text{ ml} \times \text{quantity}$) is executed within a `@Transactional` block. If remaining volume falls below $250\text{ ml}$, the batch status is automatically updated to `DEPLETED`.

---

## 8. REST API Specifications

### POS Endpoints
- `GET /api/pos/products`: Returns list of all products with live prices, floor/ceiling bounds, and stock status.
- `POST /api/pos/checkout`:
  - **Request Body**:
    ```json
    {
      "items": [
        { "productId": 1, "quantity": 2, "cupSizeMl": 250 }
      ],
      "paymentMethod": "CASH"
    }
    ```
  - **Response Body**:
    ```json
    {
      "orderNumber": "ORD-1723554100-A8F2",
      "totalAmount": 42.00,
      "paymentMethod": "CASH",
      "paymentStatus": "COMPLETED",
      "timestamp": "2026-08-13T12:00:00",
      "items": [
        {
          "productName": "Fresh Mango Juice",
          "quantity": 2,
          "cupSizeMl": 250,
          "unitPrice": 21.00,
          "totalPrice": 42.00,
          "volumeDeductedMl": 500
        }
      ]
    }
    ```

### Pricing & Market Crash Endpoints
- `GET /api/pricing/evaluate`: Triggers pricing evaluation for all products.
- `GET /api/pricing/history`: Returns price change audit history log.
- `POST /api/pricing/simulate`: Runs multi-step pricing sandbox simulation.
- `GET /api/pricing/market-crash/status`: Returns current Market Crash status.
- `POST /api/pricing/market-crash/trigger`: Triggers Market Crash routine.
- `POST /api/pricing/market-crash/stop`: Stops Market Crash routine.

### Batch Management Endpoints
- `GET /api/batches`: Returns all 20L container batches.
- `POST /api/batches`: Registers a new 20L container batch.

---

## 9. Developer & System Setup Guide

### Environment Prerequisites
- Java 21 JDK (Amazon Corretto or Eclipse Temurin)
- Maven 3.9+
- Node.js 18+ & npm

### Starting the Applications

1. **Install Root Dependencies**:
   ```bash
   npm install
   ```

2. **Start Customer POS Terminal (Port 8000)**:
   ```bash
   npm run dev:customer
   ```

3. **Start Admin Control Center (Port 8001)**:
   ```bash
   npm run dev:admin
   ```

4. **Start Backend Spring Boot API (Port 8088)**:
   ```bash
   npm run dev:backend
   ```
   *Alternatively*: `cd backend && mvn spring-boot:run`

---

## 10. Summary Checklist for AI & Engineers

- [x] **Monorepo setup**: Root `package.json` orchestrates static frontends and Spring Boot backend.
- [x] **Database Schema**: H2 in-memory DB / PostgreSQL with Flyway migrations.
- [x] **20L Volume Deductions**: Atomic volume subtraction with `PESSIMISTIC_WRITE` locks.
- [x] **Bar Stock Pricing Engine**: Demand scoring based on velocity ($0.40$), stock pressure ($0.40$), and time factor ($0.20$).
- [x] **Stock Market Field Cross-Pricing**: Purchased items surge ($+₹1$), unpurchased items discount ($-₹1$).
- [x] **Market Crash Routine**: Digital siren gong, screen takeover header, countdown timer, and floor price override ($₹18.00$).
- [x] **Pricing Sandbox Simulator**: Allows step-by-step timeline simulation with optional Market Crash event injection.
