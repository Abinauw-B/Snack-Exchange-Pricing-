import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-juice-batches',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div>
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
        <div>
          <h1 style="font-size: 26px; font-weight: 700; margin-bottom: 4px;">20L Juice Container Batches</h1>
          <p style="color: var(--text-muted);">Manage 20,000 ml container batches, status, and volume tracking.</p>
        </div>
        <button class="btn-primary" (click)="showNewBatchModal = true">+ Register New 20L Batch</button>
      </div>

      <div class="section-card">
        <table class="table-custom">
          <thead>
            <tr>
              <th>Batch Code</th>
              <th>Product ID</th>
              <th>Initial Vol (ML)</th>
              <th>Remaining Vol (ML)</th>
              <th>Cup Size</th>
              <th>Est. Remaining Cups</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let b of batches">
              <td style="font-weight: 600; font-family: monospace;">{{ b.batchCode }}</td>
              <td>Product #{{ b.productId }}</td>
              <td>{{ b.initialVolumeMl }} ml (20L)</td>
              <td style="font-weight: 700; color: #3b82f6;">{{ b.remainingVolumeMl }} ml</td>
              <td>{{ b.cupSizeMl }} ml</td>
              <td style="font-weight: 700; color: #10b981;">
                {{ Math.floor(b.remainingVolumeMl / b.cupSizeMl) }} cups
              </td>
              <td>
                <span class="status-tag" [ngClass]="b.status === 'ACTIVE' ? 'tag-active' : 'tag-depleted'">
                  {{ b.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- New Batch Modal -->
      <div *ngIf="showNewBatchModal" style="position: fixed; top:0; left:0; width:100vw; height:100vh; background: rgba(0,0,0,0.7); display: flex; align-items: center; justify-content: center; z-index: 1000;">
        <div style="background: #1e293b; border: 1px solid var(--border-color); border-radius: 16px; width: 400px; padding: 24px;">
          <h3 style="font-size: 18px; font-weight: 700; margin-bottom: 16px;">Register New 20L Container Batch</h3>
          
          <div style="margin-bottom: 16px;">
            <label style="display: block; font-size: 13px; color: var(--text-muted); margin-bottom: 6px;">Select Flavour Product</label>
            <select [(ngModel)]="newBatchProductId" style="width: 100%; padding: 10px; background: #0f172a; color: white; border: 1px solid var(--border-color); border-radius: 8px;">
              <option [value]="1">Mango Juice (Product #1)</option>
              <option [value]="2">Lemon Juice (Product #2)</option>
              <option [value]="3">Mint Cooler (Product #3)</option>
              <option [value]="4">Orange Sunrise (Product #4)</option>
              <option [value]="5">Strawberry Delight (Product #5)</option>
              <option [value]="6">Royal Grape (Product #6)</option>
              <option [value]="7">Lychee Mist (Product #7)</option>
            </select>
          </div>

          <div style="margin-bottom: 20px;">
            <label style="display: block; font-size: 13px; color: var(--text-muted); margin-bottom: 6px;">Container Capacity (ML)</label>
            <input type="number" [(ngModel)]="newBatchCapacityMl" style="width: 100%; padding: 10px; background: #0f172a; color: white; border: 1px solid var(--border-color); border-radius: 8px;">
          </div>

          <div style="display: flex; gap: 12px; justify-content: flex-end;">
            <button (click)="showNewBatchModal = false" style="padding: 10px 16px; background: transparent; border: 1px solid var(--border-color); color: white; border-radius: 8px;">Cancel</button>
            <button (click)="submitNewBatch()" class="btn-primary">Register Batch</button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class JuiceBatchesComponent implements OnInit {
  Math = Math;
  batches: any[] = [];
  showNewBatchModal: boolean = false;
  newBatchProductId: number = 1;
  newBatchCapacityMl: number = 20000;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchBatches();
  }

  fetchBatches() {
    this.http.get<any[]>('http://localhost:8088/api/batches').subscribe({
      next: (data) => {
        this.batches = data;
      },
      error: () => {
        this.batches = [
          { id: 1, productId: 1, batchCode: 'BATCH-MNG-001', containerCapacityMl: 20000, initialVolumeMl: 20000, remainingVolumeMl: 17500, cupSizeMl: 250, status: 'ACTIVE' },
          { id: 2, productId: 2, batchCode: 'BATCH-LMN-001', containerCapacityMl: 20000, initialVolumeMl: 20000, remainingVolumeMl: 16000, cupSizeMl: 250, status: 'ACTIVE' },
          { id: 3, productId: 3, batchCode: 'BATCH-MNT-001', containerCapacityMl: 20000, initialVolumeMl: 20000, remainingVolumeMl: 19000, cupSizeMl: 250, status: 'ACTIVE' }
        ];
      }
    });
  }

  submitNewBatch() {
    const payload = {
      productId: this.newBatchProductId,
      containerCapacityMl: this.newBatchCapacityMl
    };

    this.http.post('http://localhost:8088/api/batches', payload).subscribe({
      next: () => {
        this.showNewBatchModal = false;
        this.fetchBatches();
      },
      error: () => {
        this.batches.unshift({
          id: Date.now(),
          productId: this.newBatchProductId,
          batchCode: 'BATCH-NEW-' + Math.floor(Math.random() * 1000),
          containerCapacityMl: this.newBatchCapacityMl,
          initialVolumeMl: this.newBatchCapacityMl,
          remainingVolumeMl: this.newBatchCapacityMl,
          cupSizeMl: 250,
          status: 'ACTIVE'
        });
        this.showNewBatchModal = false;
      }
    });
  }
}
