import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ReportAuditEvent, ReportService } from '../../services/report.service';

@Component({
  selector: 'app-report-run-flow',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="flow-container" *ngIf="!loading && !error">
      <button class="back" (click)="goBack()">返回</button>
      <h1>报表审批流程（Run #{{ runId }}）</h1>

      <div *ngIf="events.length === 0">
        <p>暂无审计记录。</p>
      </div>

      <ul class="timeline" *ngIf="events.length > 0">
        <li *ngFor="let e of events">
          <div class="time">{{ e.eventTime | date:'yyyy-MM-dd HH:mm:ss' }}</div>
          <div class="content">
            <div class="type">{{ e.eventType }}</div>
            <div class="meta">
              <span *ngIf="e.actorUsername">用户：{{ e.actorUsername }}</span>
              <span *ngIf="e.actorRole">（角色：{{ e.actorRole }}）</span>
            </div>
            <div class="comment" *ngIf="e.comment">备注：{{ e.comment }}</div>
          </div>
        </li>
      </ul>
    </div>

    <div *ngIf="loading">加载审批流程中...</div>
    <div *ngIf="error" class="error">{{ error }}</div>
  `,
  styles: [`
    .flow-container {
      max-width: 800px;
      margin: 0 auto;
      padding: var(--space-5);
    }

    .flow-container h1 {
      font-size: 24px;
      color: var(--gray-900);
      margin-bottom: var(--space-6);
      display: flex;
      align-items: center;
      gap: var(--space-3);
    }

    .back {
      margin-bottom: var(--space-4);
      background: var(--gray-100) !important;
      color: var(--gray-700) !important;
      border: 1px solid var(--gray-300) !important;
      padding: 8px 16px !important;
      font-size: 14px !important;
    }

    .back:hover {
      background: var(--gray-200) !important;
    }

    /* Timeline Styles */
    .timeline {
      list-style: none;
      padding: 0;
      margin: 0;
      position: relative;
    }

    .timeline::before {
      content: '';
      position: absolute;
      left: 20px;
      top: 0;
      bottom: 0;
      width: 2px;
      background: linear-gradient(to bottom, var(--primary-500), var(--gray-300));
      border-radius: 1px;
    }

    .timeline li {
      margin: 0 0 var(--space-5) 0;
      padding-left: 56px;
      position: relative;
      min-height: 60px;
    }

    .timeline li::before {
      content: '';
      width: 16px;
      height: 16px;
      border-radius: 50%;
      background: var(--primary-500);
      border: 3px solid white;
      box-shadow: var(--shadow-sm);
      position: absolute;
      left: 12px;
      top: 4px;
      z-index: 1;
    }

    .timeline li:nth-child(1)::before {
      background: var(--primary-600);
      width: 20px;
      height: 20px;
      left: 10px;
      top: 2px;
      box-shadow: 0 0 0 4px var(--primary-100);
    }

    .timeline li:last-child::before {
      background: var(--success);
    }

    .time {
      font-size: 13px;
      color: var(--gray-500);
      margin-bottom: var(--space-1);
      font-weight: 500;
    }

    .content {
      background: white;
      padding: var(--space-4);
      border-radius: var(--radius-md);
      box-shadow: var(--shadow-sm);
      border: 1px solid var(--gray-200);
    }

    .type {
      font-weight: 600;
      font-size: 15px;
      color: var(--gray-800);
      margin-bottom: var(--space-2);
      display: flex;
      align-items: center;
      gap: var(--space-2);
    }

    .meta {
      font-size: 13px;
      color: var(--gray-600);
      display: flex;
      align-items: center;
      gap: var(--space-2);
      margin-bottom: var(--space-2);
    }

    .meta span {
      display: inline-flex;
      align-items: center;
    }

    .comment {
      margin-top: var(--space-3);
      padding: var(--space-3);
      background: var(--gray-50);
      border-radius: var(--radius-sm);
      font-size: 14px;
      color: var(--gray-700);
      border-left: 3px solid var(--primary-300);
    }

    /* Empty State */
    .flow-container > div > p {
      text-align: center;
      color: var(--gray-400);
      padding: var(--space-8);
      font-style: italic;
      background: var(--gray-50);
      border-radius: var(--radius-md);
      border: 2px dashed var(--gray-200);
    }

    /* Loading State */
    div:contains("加载") {
      text-align: center;
      color: var(--gray-500);
      padding: var(--space-8);
    }

    /* Error State */
    .error {
      background: var(--danger-light);
      color: var(--danger);
      padding: 16px 20px;
      border-radius: var(--radius-md);
      border-left: 4px solid var(--danger);
      font-size: 14px;
    }

    /* Responsive */
    @media (max-width: 768px) {
      .flow-container {
        padding: var(--space-4);
      }

      .timeline::before {
        left: 14px;
      }

      .timeline li {
        padding-left: 44px;
      }

      .timeline li::before {
        width: 12px;
        height: 12px;
        left: 8px;
      }

      .timeline li:nth-child(1)::before {
        width: 16px;
        height: 16px;
        left: 6px;
      }
    }
  `]
})
export class ReportRunFlowComponent implements OnInit {
  runId!: number;
  events: ReportAuditEvent[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.error = '缺少 runId 参数';
      return;
    }
    this.runId = +idParam;
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading = true;
    this.error = null;
    this.reportService.getAuditTrail(this.runId).subscribe({
      next: (events) => {
        this.events = events;
        this.loading = false;
      },
      error: (err) => {
        this.error = '加载审批流程失败: ' + (err.error?.message || err.message || '');
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/reports']);
  }
}
