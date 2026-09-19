import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router } from '@angular/router';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { IdleService } from './core/services/idle.service';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent],
  template: `
    @if (showNavbar()) {
      <app-navbar></app-navbar>
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
    } @else {
      <router-outlet></router-outlet>
    }
  `,
  styles: [`
    .main-content {
      margin-left: 220px;
      margin-top: 56px;
      min-height: calc(100vh - 56px);
    }
    @media (max-width: 768px) {
      .main-content {
        margin-left: 0;
      }
    }
  `]
})
export class AppComponent implements OnInit, OnDestroy {
  constructor(
    private router: Router,
    private idleService: IdleService,
    private auth: AuthService
  ) {}

  ngOnInit() {
    if (this.auth.isAuthenticated()) {
      this.idleService.start();
    }
  }

  ngOnDestroy() {
    this.idleService.stop();
  }

  showNavbar(): boolean {
    return !this.router.url.includes('/login');
  }
}
