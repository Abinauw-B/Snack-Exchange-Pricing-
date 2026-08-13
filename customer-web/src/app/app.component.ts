import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PosComponent } from './pos/pos.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, PosComponent],
  template: `<app-pos></app-pos>`
})
export class AppComponent {}
