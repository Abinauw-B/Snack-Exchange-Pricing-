# Configurable Dynamic Pricing Engine Specification

## Algorithm Overview

The Dynamic Pricing Engine calculates real-time cup prices for juice flavours based on sales velocity, stock pressure, and time-of-day multipliers.

```
                  +--------------------------------+
                  |  Sales Velocity Score (S_v)    | -- (Weight w_v = 0.40) --+
                  +--------------------------------+                          |
                                                                              v
                  +--------------------------------+               +----------------------+
                  |   Stock Pressure Score (S_s)   | -- (Weight w_s = 0.40) -> | Demand Score (0-100) |
                  +--------------------------------+               +----------------------+
                                                                              ^
                  +--------------------------------+                          |
                  |     Time Factor Score (S_t)    | -- (Weight w_t = 0.20) --+
                  +--------------------------------+
```

---

## Mathematical Equations

### 1. Demand Score Equation ($0 - 100$)
$$\text{Demand Score} = (w_v \times S_v) + (w_s \times S_s) + (w_t \times S_t)$$
Where default weights:
- Velocity Weight $w_v = 0.40$
- Stock Pressure Weight $w_s = 0.40$
- Time Factor Weight $w_t = 0.20$

### 2. Stock Pressure ($S_s$)
$$S_s = 100\% - \text{Remaining Volume Percentage}$$
$$S_s = 100 - \left( \frac{\text{remaining\_volume\_ml}}{\text{initial\_volume\_ml}} \times 100 \right)$$

- **LOW**: $0\% - 40\%$
- **NORMAL**: $40\% - 70\%$
- **HIGH**: $70\% - 90\%$
- **VERY HIGH**: $90\% - 100\%$

### 3. Time Factor Multipliers ($S_t$)
- **Morning** (06:00 - 11:00): Multiplier 1.0 (Score = 50.0)
- **Afternoon** (11:00 - 16:00): Multiplier 1.1 (Score = 75.0)
- **Evening** (16:00 - 21:00): Multiplier 1.2 (Score = 100.0)
- **Night** (21:00 - 23:00): Multiplier 1.0 (Score = 50.0)

---

## Price Adjustment & Bounded Limits

1. **Step Movement**: Maximum $\pm ₹1$ step per evaluation window (5 minutes).
2. **Cooldown Rule**: Minimum 10 minutes (`PRICE_CHANGE_COOLDOWN`) between consecutive price changes.
3. **Hard Boundaries**:
   $$\text{MIN\_PRICE} = ₹18 \le \text{Current Price} \le \text{MAX\_PRICE} = ₹25$$

---

## Human-Readable Explanations

Every price evaluation generates an audit log entry in `price_history` with human-readable rationale:
- *High Demand*: `"Increased price for MANGO by ₹1 to ₹21.00 due to HIGH DEMAND (Score: 74.5, Stock Pressure: 82.5%)."`
- *Low Demand*: `"Decreased price for LEMON by ₹1 to ₹19.00 due to LOW DEMAND (Score: 32.0, Stock Pressure: 20.0%)."`
- *Cooldown Active*: `"Price maintained at ₹20.00 for MANGO. Price change on cooldown (4/10 mins elapsed)."`
