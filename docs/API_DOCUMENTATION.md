# Juice Dynamic Pricing System — API & WebSocket Documentation

**Version:** 1.0.0  
**Backend Port:** 8088  
**Base URL:** `http://localhost:8088/api`  
**WebSocket Endpoint:** `ws://localhost:8088/ws`  
**Database:** PostgreSQL 18.3 (`retailposdb`)

---

## 1. System Health API

### `GET /api/health`
- **Purpose:** Health check endpoint for system uptime and monitoring.
- **Request Parameters:** None
- **Response (200 OK):**
  ```json
  {
    "status": "UP",
    "service": "dynamic-pricing-backend",
    "timestamp": "2026-08-18T11:47:17.576597800"
  }
  ```

---

## 2. Products API

### `GET /api/products` or `GET /api/pos/products`
- **Purpose:** Retrieves all available juice products with current dynamic prices, floor/ceiling bounds, and timestamps.
- **Response (200 OK):**
  ```json
  [
    {
      "id": 1,
      "name": "Fresh Mango Juice",
      "flavour": "MANGO",
      "description": "Sweet fresh Alphonso mango pulp juice",
      "defaultCupSizeMl": 250,
      "defaultCupPrice": 20.00,
      "currentCupPrice": 21.00,
      "minCupPrice": 18.00,
      "maxCupPrice": 25.00,
      "lastPriceChangeTimestamp": "2026-08-18T11:52:19.664492"
    }
  ]
  ```

### `GET /api/products/{id}`
- **Purpose:** Retrieves details for a single juice product by its ID.
- **Response (200 OK):** Product object
- **Error (404 Not Found):** `Product not found with id: {id}`

---

## 3. Inventory & Batches API

### `GET /api/batches`
- **Purpose:** Retrieves all active 20L juice batches with remaining volume, capacity, and remaining cup estimates.
- **Response (200 OK):**
  ```json
  [
    {
      "id": 1,
      "productId": 1,
      "batchCode": "BATCH-MNG-001",
      "containerCapacityMl": 20000,
      "initialVolumeMl": 20000,
      "remainingVolumeMl": 9250,
      "cupSizeMl": 250,
      "status": "ACTIVE",
      "estimatedRemainingCups": 37
    }
  ]
  ```

---

## 4. POS Checkout API

### `POST /api/pos/checkout` or `POST /api/pos/orders`
- **Purpose:** Processes customer purchases, creates sales orders, updates inventory batch volume, and recalculates dynamic price demand.
- **Request Body:**
  ```json
  {
    "items": [
      {
        "productId": 1,
        "quantity": 1,
        "cupSizeMl": 250
      }
    ],
    "paymentMethod": "CASH"
  }
  ```
- **Response (200 OK):**
  ```json
  {
    "orderNumber": "ORD-1787034139546-A7FA",
    "totalAmount": 20.00,
    "paymentMethod": "CASH",
    "paymentStatus": "COMPLETED",
    "timestamp": "2026-08-18T11:52:19.6938624",
    "items": [
      {
        "productName": "Fresh Mango Juice",
        "quantity": 1,
        "cupSizeMl": 250,
        "unitPrice": 20.00,
        "totalPrice": 20.00,
        "volumeDeductedMl": 250
      }
    ]
  }
  ```

---

## 5. Orders API

### `GET /api/orders` or `GET /api/pos/orders`
- **Purpose:** Retrieves all completed sales orders.
- **Response (200 OK):** Array of order objects.

---

## 6. Dynamic Pricing Engine API

### `GET /api/pricing/evaluate`
- **Purpose:** Triggers immediate evaluation of the dynamic pricing engine based on order velocity, stock pressure, and peak hour multiplier.
- **Response (200 OK):**
  ```json
  [
    {
      "productId": 1,
      "flavour": "MANGO",
      "oldPrice": 20.00,
      "newPrice": 21.00,
      "priceChanged": true,
      "demandScore": 76.5,
      "stockPressurePct": 53.75,
      "explanation": "📈 BAR STOCK SURGE: Buying volume surge (+1 cups). Price increased from ₹20.00 to ₹21.00 for MANGO.",
      "statusReason": "PRICE_UPDATED"
    }
  ]
  ```

### `GET /api/pricing/history`
- **Purpose:** Retrieves historic price evaluation logs for all products.
- **Response (200 OK):** Array of price history objects.

### `POST /api/pricing/market-crash/trigger?durationMinutes=3`
- **Purpose:** Triggers manual Market Crash mode, locking all prices to absolute floor limit (₹18.00).

### `POST /api/pricing/market-crash/stop`
- **Purpose:** Restores standard dynamic pricing engine.

### `GET /api/pricing/market-crash/status`
- **Purpose:** Returns active status and remaining duration of market crash event.

---

## 7. Reports & Summary API

### `GET /api/reports/summary` or `GET /api/reports/dashboard`
- **Purpose:** Retrieves dashboard key metrics for the Admin Panel.
- **Response (200 OK):**
  ```json
  {
    "totalOrders": 41,
    "activeBatches": 7,
    "totalRevenue": 3840.00,
    "cupsSold": 192,
    "liquidVolumeLitres": 118.5
  }
  ```

---

## 8. System Notifications API

### `GET /api/notifications`
- **Purpose:** Retrieves system notifications ordered by creation timestamp descending.

---

## 9. WebSocket STOMP Specification

- **Endpoint:** `http://localhost:8088/ws` (SockJS or native STOMP)
- **Subscribed Topics:**
  - `/topic/prices`: Broadcasts live price updates every 60s or upon evaluation trigger.
  - `/topic/market-crash`: Broadcasts market crash status changes.
  - `/topic/led-display`: Broadcasts real-time prices formatted for LED ticker display.
