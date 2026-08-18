# Juice Bar Stock Exchange — POS, Inventory & Dynamic Pricing System

A production-ready retail management system for fresh juice bars featuring a **20L liquid volume batch model**, **PostgreSQL 18.3 persistence**, **Flyway schema migrations**, **Pessimistic Row Locking**, and a **60-Second Automated Enterprise Dynamic Pricing Engine**.

---

## 🏗️ System Architecture

```
                                  +-----------------------------+
                                  |    Customer / POS Web       |
                                  |    (Port 8000 / HTML5 SPA)  |
                                  +--------------+--------------+
                                                 |
                                                 v [REST & STOMP / WebSocket]
+-----------------------------+                  |                  +-----------------------------+
|        Admin Panel          |------------------+----------------->|     Spring Boot Backend     |
|    (Port 8001 / HTML5 SPA)  |<----------------------------------->|    (Java 24.0.2 / Port 8088) |
+-----------------------------+                                     +--------------+--------------+
                                                                                   |
                                                                                   v [Flyway Managed]
                                                                    +-----------------------------+
                                                                    |     PostgreSQL 18.3 DB      |
                                                                    |        (retailposdb)        |
                                                                    +-----------------------------+
```

---

## 🌟 Key Features

1. **20L Batch Container & Volume Tracking**:
   - Liquid inventory tracked in `remaining_volume_ml` ($20,000\text{ ml}$ per batch).
   - Standard $250\text{ ml}$ cup serving size ($80\text{ cups per batch}$).
   - `estimated_remaining_cups = floor(remaining_volume_ml / 250)`.

2. **Concurrency Safety & Pessimistic Row Locking**:
   - Uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` during checkout to guarantee atomic volume deductions under concurrent POS cashier requests.

3. **Enterprise Dynamic Pricing Engine**:
   - **Demand Score Equation ($0 - 100$)**:
     $$\text{Demand Score} = (w_v \times S_v) + (w_s \times S_s) + (w_t \times S_t)$$
     *(Default weights: Velocity $w_v = 0.40$, Stock Pressure $w_s = 0.40$, Time Factor $w_t = 0.20$)*.
   - **Step Adjustments**: Controlled $\pm ₹1$ step per evaluation window.
   - **Cooldown Window**: Minimum 10 minutes between price changes.
   - **Bounded Limits**: Strict $[₹18, ₹25]$ floor and ceiling limits.

4. **Market Crash Routine**:
   - Triggers panic floor pricing ($₹18.00$) for all juice varieties.
   - Broadcasts real-time alert via STOMP WebSocket on `/topic/market-crash`.

5. **Real-Time WebSocket STOMP Engine**:
   - Live price updates broadcast on `/topic/prices` and `/topic/led-display`.
   - Dynamic UI updates without manual browser refresh.

---

## 🚀 Getting Started

### System Requirements
- **Java 24 / 21+** & **Maven 3.9+**
- **Node.js 18+**
- **PostgreSQL 18.3** service (`postgresql-x64-18`) on `localhost:5432`

---

## 💻 Startup Commands (PowerShell)

### 1. Verify PostgreSQL Database Service
```powershell
Get-Service -Name postgresql-x64-18
```

### 2. Start Spring Boot Backend Server (Port 8088)
```powershell
cd "D:\Juice Dynamic Price Project\backend"
.\mvnw.cmd spring-boot:run
```
- **Backend URL:** `http://localhost:8088`
- **Health Check:** `http://localhost:8088/api/health`

### 3. Start Both Frontend Applications Simultaneously
From the root directory:
```powershell
cd "D:\Juice Dynamic Price Project"
npm run dev
```
- **Customer POS Web:** `http://localhost:8000`
- **Admin Control Center:** `http://localhost:8001`

---

## 🧪 Running Automated Tests

Run the full Maven JUnit test suite:
```powershell
cd "D:\Juice Dynamic Price Project\backend"
.\mvnw.cmd clean test
```

Build production JAR package:
```powershell
cd "D:\Juice Dynamic Price Project\backend"
.\mvnw.cmd clean package
```

---

## 📑 Project Structure

```
├── backend/            # Spring Boot 3.3 Java 24 REST API server & Flyway migrations
├── customer-web/       # Customer POS application (Port 8000)
├── admin-panel/        # Admin Control Center & Pricing Simulator (Port 8001)
├── docs/               # API_DOCUMENTATION, DYNAMIC_PRICING, JUICE_INVENTORY
├── DEMO_CHECKLIST.md   # Pre-demo verification checklist
├── TROUBLESHOOTING.md  # Port cleanup & error resolution guide
└── README.md
```
