import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <h1>报表管理系统登录</h1>

      <div *ngIf="authService.isLoggedIn(); else loginForm">
        <p>当前已登录用户：{{ authService.getCurrentUser()?.username }}</p>
        <button (click)="goAfterLogin()">进入系统</button>
      </div>

      <ng-template #loginForm>
        <form (ngSubmit)="onSubmit()" class="login-form">
          <label>
            用户名：
            <input [(ngModel)]="username" name="username" required />
          </label>

          <label>
            密码：
            <input type="password" [(ngModel)]="password" name="password" required />
          </label>

          <button type="submit" [disabled]="loggingIn">登录</button>

          <div *ngIf="loginError" class="error">{{ loginError }}</div>
        </form>
      </ng-template>
    </div>
  `,
  styles: [`
    .login-container {
      max-width: 420px;
      margin: 100px auto;
      padding: var(--space-6);
      background: white;
      border-radius: var(--radius-xl);
      box-shadow: var(--shadow-lg);
      border: 1px solid var(--gray-200);
    }

    .login-container h1 {
      text-align: center;
      margin-bottom: var(--space-6);
      font-size: 24px;
      color: var(--gray-900);
    }

    .login-container p {
      text-align: center;
      color: var(--gray-600);
      font-size: 14px;
      margin-bottom: var(--space-4);
    }

    .login-form {
      display: flex;
      flex-direction: column;
      gap: var(--space-4);
    }

    .login-form label {
      display: flex;
      flex-direction: column;
      gap: var(--space-2);
      font-size: 14px;
      font-weight: 500;
      color: var(--gray-700);
    }

    .login-form input {
      width: 100%;
      padding: 12px 14px;
      border: 1px solid var(--gray-300);
      border-radius: var(--radius-md);
      font-size: 15px;
      transition: all 0.2s ease;
      background: white;
    }

    .login-form input:focus {
      outline: none;
      border-color: var(--primary-500);
      box-shadow: 0 0 0 3px var(--primary-100);
    }

    button {
      width: 100%;
      padding: 12px 20px;
      background: var(--primary-600);
      color: white;
      border: none;
      border-radius: var(--radius-md);
      cursor: pointer;
      font-size: 15px;
      font-weight: 500;
      transition: all 0.2s ease;
      margin-top: var(--space-2);
    }

    button:hover {
      background: var(--primary-700);
      transform: translateY(-1px);
      box-shadow: var(--shadow-md);
    }

    button:active {
      transform: translateY(0);
    }

    button[disabled] {
      background: var(--gray-300) !important;
      color: var(--gray-500) !important;
      cursor: not-allowed;
      transform: none !important;
      box-shadow: none !important;
    }

    .error {
      background: var(--danger-light);
      color: var(--danger);
      padding: 12px 16px;
      border-radius: var(--radius-md);
      font-size: 14px;
      border-left: 4px solid var(--danger);
      margin-top: var(--space-3);
    }
  `]
})
export class LoginComponent implements OnInit {
  username = '';
  password = '';
  loggingIn = false;
  loginError: string | null = null;
  private redirectUrl: string | null = null;

  constructor(
    public authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.redirectUrl = this.route.snapshot.queryParamMap.get('redirect');
  }

  onSubmit(): void {
    if (!this.username || !this.password) {
      return;
    }
    this.loggingIn = true;
    this.loginError = null;

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.loggingIn = false;
        this.goAfterLogin();
      },
      error: (err) => {
        this.loggingIn = false;
        this.loginError = '登录失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  goAfterLogin(): void {
    const user = this.authService.getCurrentUser();
    let defaultTarget = '/reports';
    const role = user?.role || '';
    if (role.includes('CHECKER')) {
      defaultTarget = '/checker';
    } else if (role.includes('MAKER')) {
      defaultTarget = '/maker';
    }
    const target = this.redirectUrl || defaultTarget;
    this.router.navigateByUrl(target);
  }
}
