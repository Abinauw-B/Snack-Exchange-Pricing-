import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pricing-simulator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div>
      <h1 style="font-size: 26px; font-weight: 700; margin-bottom: 4px;">Dynamic Pricing Sandbox Simulator</h1>
      <p style="color: var(--text-muted); margin-bottom: 24px;">Simulate customer purchasing scenarios over time without modifying live production database tables.</p>

      <div style="display: grid; grid-template-columns: 340px 1fr; gap: 24px;">
        <!-- Control Form -->
        <div class="section-card">
          <h2 style="font-size: 18px; font-weight: 600; margin-bottom: 16px;">Simulation Parameters</h2>
          
          <div style="margin-bottom: 14px;">
            <label style="display: block; font-size: 12px; color: var(--text-muted); margin-bottom: 4px;">Juice Flavour</label>
            <input type="text" [(ngModel)]="req.flavourName" style="width: 100%; padding: 8px; background: #0f172a; color: white; border: 1px solid var(--border-color); border-radius: 6px;">
          </div>

          <div style="margin-bottom: 14px;">
            <label style="display: block; font-size: 12px; color: var(--text-muted); margin-bottom: 4px;">Initial Volume (ML)</label>
            <input type="number" [(ngModel)]="req.initialVolumeMl" style="width: 100%; padding: 8px; background: #0f172a; color: white; border: 1px solid var(--border-color); border-radius: 6px;">
            <span style="font-size: 11px; color: var(--text-muted);">20,000 ML = 20 Litres (80 cups)</span>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 14px;">
            <div>
              <label style="display: block; font-size: 12px; color: var(--text-muted); margin-bottom: 4px;">Start Price (₹)</label>
              <input type="number" [(ngModel)]="req.initialPrice" style="width: 100%; padding: 8px; background: #0f172a; color: white; border: 1px solid var(--border-color); border-radius: 6px;">
            </div>
            <div>
              <label style="display: block; font-size: 12px; color: var(--text-muted); margin-bottom: 4px;">Cups / Step</label>
              <input type="number" [(ngModel)]="req.cupsPerInterval" style="width: 100%; padding: 8px; background: #0f172a; color: white; border: 1px solid var(--border-color); border-radius: 6px;">
            </div>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 16px;">
            <div>
              <label style="display: block; font-size: 12px; color: var(--text-muted); margin-bottom: 4px;">Min Limit (₹)</label>
              <input type="number" [(ngModel)]="req.minPrice" style="width: 100%; padding: 8px; background: #0f172a; color: white; border: 1px solid var(--border-color); border-radius: 6px;">
            </div>
            <div>
              <label style="display: block; font-size: 12px; color: var(--text-muted); margin-bottom: 4px;">Max Limit (₹)</label>
              <input type="number" [(ngModel)]="req.maxPrice" style="width: 100%; padding: 8px; background: #0f172a; color: white; border: 1px solid var(--border-color); border-radius: 6px;">
            </div>
          </div>

          <button class="btn-primary" style="width: 100%; padding: 12px;" (click)="runSimulation()">▶️ Run Sandbox Simulation</button>
        </div>

        <!-- Simulation Results & Timeline -->
        <div>
          <div *ngIf="simulationResult" class="metric-grid">
            <div class="metric-card">
              <div class="metric-label">Simulated Flavour</div>
              <div class="metric-val" style="font-size: 20px;">{{ simulationResult.flavourName }}</div>
            </div>
            <div class="metric-card">
              <div class="metric-label">Initial vs Final Price</div>
              <div class="metric-val" style="font-size: 20px; color: #10b981;">₹{{ simulationResult.initialPrice }} &rarr; ₹{{ simulationResult.finalPrice }}</div>
            </div>
            <div class="metric-card">
              <div class="metric-label">Total Cups Sold</div>
              <div class="metric-val" style="font-size: 20px; color: #3b82f6;">{{ simulationResult.totalCupsSold }} cups</div>
            </div>
          </div>

          <div *ngIf="simulationResult" class="section-card">
            <h2 style="font-size: 18px; font-weight: 600; margin-bottom: 16px;">Step-by-Step Price Trajectory Timeline</h2>
            <table class="table-custom">
              <thead>
                <tr>
                  <th>Step</th>
                  <th>Time</th>
                  <th>Rem. Vol (ML)</th>
                  <th>Cups Sold</th>
                  <th>Demand Score</th>
                  <th>Movement</th>
                  <th>Current Price</th>
                  <th>Explanation Log</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let step of simulationResult.steps">
                  <td style="font-weight: 600;">#{{ step.stepIndex }}</td>
                  <td style="font-size: 13px; color: var(--text-muted);">{{ step.timeStr }}</td>
                  <td style="font-size: 13px;">{{ step.remainingVolumeMl }} ml</td>
                  <td style="font-weight: 600;">+{{ step.cupsSoldThisStep }}</td>
                  <td style="font-weight: 700;">{{ Math.round(step.demandScore) }} / 100</td>
                  <td>
                    <span class="status-tag" [ngClass]="getMovementClass(step.priceMovement)">
                      {{ step.priceMovement }}
                    </span>
                  </td>
                  <td style="font-weight: 700; font-size: 16px; color: #10b981;">₹{{ step.price }}</td>
                  <td style="font-size: 12px; color: var(--text-muted); max-width: 280px;">{{ step.explanation }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  `
})
export class PricingSimulatorComponent {
  Math = Math;
  req: any = {
    flavourName: 'Fresh Mango Juice',
    initialVolumeMl: 20000,
    initialPrice: 20,
    minPrice: 18,
    maxPrice: 25,
    totalSimulatedPurchases: 40,
    cupsPerInterval: 4,
    intervalMinutes: 5,
    startTimeStr: '12:00',
    weightVelocity: 0.40,
    weightStockPressure: 0.40,
    weightTimeFactor: 0.20
  };

  simulationResult: any = null;

  constructor(private http: HttpClient) {
    this.runSimulation();
  }

  runSimulation() {
    this.http.post('http://localhost:8088/api/pricing/simulate', this.req).subscribe({
      next: (res) => {
        this.simulationResult = res;
      },
      error: () => {
        // Fallback local simulator demo if backend is initializing
        const steps = [];
        let vol = this.req.initialVolumeMl;
        let price = this.req.initialPrice;
        let totalSold = 0;

        for (let i = 1; i <= 10; i++) {
          const sold = 4;
          vol -= sold * 250;
          totalSold += sold;
          const score = 50 + (i * 3);
          let movement = 'UNCHANGED';

          if (i % 2 === 0 && score >= 65 && price < this.req.maxPrice) {
            price += 1;
            movement = '+₹1';
          }

          steps.push({
            stepIndex: i,
            timeStr: `12:${(i * 5).toString().padStart(2, '0')}`,
            remainingVolumeMl: vol,
            estimatedRemainingCups: Math.floor(vol / 250),
            cupsSoldThisStep: sold,
            cumulativeCupsSold: totalSold,
            demandScore: score,
            price: price,
            priceMovement: movement,
            explanation: `Step ${i}: Demand score ${score} evaluated. Price at ₹${price}.`
          });
        }

        this.simulationResult = {
          flavourName: this.req.flavourName,
          initialVolumeMl: this.req.initialVolumeMl,
          finalVolumeMl: vol,
          initialPrice: this.req.initialPrice,
          finalPrice: price,
          totalCupsSold: totalSold,
          steps: steps
        };
      }
    });
  }

  getMovementClass(m: string): string {
    if (m === '+₹1') return 'tag-depleted';
    if (m === '-₹1') return 'tag-active';
    return '';
  }
}
