# Juice Shop POS, Inventory & Dynamic Pricing System

A production-ready retail management system for fresh juice shops featuring a **20L liquid volume model**, **concurrency-safe checkout**, and a **configurable dynamic pricing engine**.

---

## 🏗️ System Architecture

```
                      +-----------------------------+
                      |    Customer / POS Web       |
                      |     (Angular / SPA @ 8000)  |
                      +--------------+--------------+
                                     |
                                     v [REST / JSON]
+-----------------------------+      |      +-----------------------------+
|        Admin Panel          |------+----->|     Spring Boot Backend     |
|     (Angular / SPA @ 8001)  |<----------->|        (Java 21 @ 8088)     |
+-----------------------------+             +--------------+--------------+
                                           |
                                           v
                            +-----------------------------+
                            |   PostgreSQL / H2 Database  |
                            |     (Flyway Migrations)     |
                            +-----------------------------+
```

---

## 🌟 Key Features

1. **20L Container & Millilitre Volume Tracking**:
   - Source of truth stored as `remaining_volume_ml` ($20,000\text{ ml}$ per container).
   - Standard $250\text{ ml}$ cup serving size ($80\text{ cups per batch}$).
   - `estimated_remaining_cups = floor(remaining_volume_ml / 250)`.

2. **Concurrency Safety & Row Locking**:
   - Uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` during checkout to guarantee atomic volume deductions under concurrent POS cashier requests.

3. **Configurable Dynamic Pricing Engine**:
   - **Demand Score Equation ($0 - 100$)**:
     $$\text{Demand Score} = (w_v \times S_v) + (w_s \times S_s) + (w_t \times S_t)$$
     *(Default weights: Velocity $w_v = 0.40$, Stock Pressure $w_s = 0.40$, Time Factor $w_t = 0.20$)*.
   - **Step Adjustments**: Controlled $\pm ₹1$ step per evaluation window.
   - **Cooldown Window**: Minimum 10 minutes between price changes.
   - **Bounded Limits**: Strict $[₹18, ₹25]$ floor and ceiling limits.
   - **Human-Readable Audit Log**: Generates human explanations for every price change logged to `price_history`.

4. **Dynamic Pricing Sandbox Simulator**:
   - In-memory testing sandbox for store managers to simulate customer purchase scenarios over time without mutating production data.

---

## 🚀 Getting Started

### Prerequisites
- **Java 21+** & **Maven 3.8+**
- **Node.js 18+**

### 1. Running the Backend Server (Port 8088)
```bash
cd backend
mvn spring-boot:run
# Backend APIs: http://localhost:8088
# H2 Console: http://localhost:8088/h2-console
```

### 2. Running Customer / POS Web Terminal (Port 8000)
```bash
cd customer-web
npx http-server src -p 8000 --cors
# Open: http://localhost:8000
```

### 3. Running Admin Control Center (Port 8001)
```bash
cd admin-panel
npx http-server src -p 8001 --cors
# Open: http://localhost:8001
```

---

## 🧪 Running Automated Tests

Run the full JUnit 5 test suite covering all 18 requirements:
```bash
cd backend
mvn test
```

---

## 📑 Project Structure

```
├── backend/            # Spring Boot 3.3 Java 21 REST API server & Flyway migrations
├── customer-web/       # Customer POS single-page checkout application (Port 8000)
├── admin-panel/        # Admin Control Center & Pricing Simulator SPA (Port 8001)
├── docs/               # Technical specifications (JUICE_INVENTORY, DYNAMIC_PRICING, PRICING_SIMULATION)
└── README.md
```
