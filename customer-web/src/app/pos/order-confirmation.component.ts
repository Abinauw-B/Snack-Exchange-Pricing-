import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-order-confirmation',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="modal-overlay">
      <div class="receipt-modal">
        <div style="text-align: center; margin-bottom: 20px;">
          <div style="font-size: 40px; margin-bottom: 8px;">✅</div>
          <h2 style="font-size: 22px; font-weight: 700; color: #10b981;">Order Completed</h2>
          <p style="font-size: 13px; color: var(--text-muted);">Invoice #{{ order.orderNumber }}</p>
        </div>

        <div style="background: rgba(15, 23, 42, 0.8); border: 1px dashed var(--border-color); border-radius: 12px; padding: 16px; margin-bottom: 20px;">
          <div *for="let item of order.items" style="display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px;">
            <div>
              <div style="font-weight: 600;">{{ item.productName }}</div>
              <div style="font-size: 12px; color: var(--text-muted);">
                {{ item.quantity }} x {{ item.cupSizeMl }}ml cup ({{ item.volumeDeductedMl }}ml total)
              </div>
            </div>
            <div style="font-weight: 600;">₹{{ item.totalPrice }}</div>
          </div>
          
          <div style="border-top: 1px dashed var(--border-color); padding-top: 10px; margin-top: 10px; display: flex; justify-content: space-between; font-weight: 700; font-size: 16px;">
            <span>Total Amount Paid</span>
            <span style="color: #10b981;">₹{{ order.totalAmount }}</span>
          </div>
        </div>

        <div style="font-size: 12px; color: var(--text-muted); text-align: center; margin-bottom: 24px;">
          Payment Method: <strong>{{ order.paymentMethod }}</strong> &bull; Status: <span style="color: #10b981;">{{ order.paymentStatus }}</span><br>
          Volume deducted atomically from active 20L container.
        </div>

        <button (click)="close.emit()" style="width: 100%; padding: 14px; background: #3b82f6; border: none; border-radius: 10px; color: white; font-weight: 600; font-size: 15px; cursor: pointer;">
          Done / Next POS Order
        </button>
      </div>
    </div>
  `
})
export class OrderConfirmationComponent {
  @Input() order: any;
  @Output() close = new EventEmitter<void>();
}
