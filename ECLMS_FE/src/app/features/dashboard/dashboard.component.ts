import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../core/services/dashboard.service';
import { ReportService } from '../../core/services/report.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private reportService = inject(ReportService);

  showReportDropdown = false;

  stats: any = {
    totalContracts: 0,
    totalVendors: 0,
    totalDepartments: 0,
    contractsExpiringSoon: 0,
    contractsByStatus: {}
  };

  statusKeys: string[] = [];
  isLoading = true;

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.isLoading = true;
    this.dashboardService.getStats().subscribe({
      next: (response) => {
        if (response?.data) {
          this.stats = response.data;
          this.statusKeys = Object.keys(this.stats.contractsByStatus || {});
        } else {
          this.clearStats();
        }
        this.isLoading = false;
      },
      error: () => {
        this.clearStats();
        this.isLoading = false;
      }
    });
  }

  clearStats() {
    this.stats = {
      totalContracts: 0,
      totalVendors: 0,
      totalDepartments: 0,
      contractsExpiringSoon: 0,
      contractsByStatus: {}
    };
    this.statusKeys = [];
  }

  getStatusPercentage(count: number): number {
    if (!this.stats.totalContracts) return 0;
    return Math.round((count / this.stats.totalContracts) * 100);
  }

  toggleReportDropdown() {
    this.showReportDropdown = !this.showReportDropdown;
  }

  downloadReport(format: 'excel' | 'pdf' | 'csv') {
    let downloadObs;
    let filename = `contracts_report_${new Date().toISOString().slice(0, 10)}`;
    let contentType = '';

    if (format === 'excel') {
      downloadObs = this.reportService.downloadExcel();
      filename += '.xlsx';
      contentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
    } else if (format === 'pdf') {
      downloadObs = this.reportService.downloadPdf();
      filename += '.pdf';
      contentType = 'application/pdf';
    } else {
      downloadObs = this.reportService.downloadCsv();
      filename += '.csv';
      contentType = 'text/csv';
    }

    downloadObs.subscribe({
      next: (blob) => {
        const file = new Blob([blob], { type: contentType });
        const fileURL = URL.createObjectURL(file);
        const a = document.createElement('a');
        a.href = fileURL;
        a.download = filename;
        a.click();
        URL.revokeObjectURL(fileURL);
        this.showReportDropdown = false;
      },
      error: (err) => {
        console.error('Failed to download report', err);
        alert('Failed to download report. Ensure the backend server is running and you have necessary permissions.');
      }
    });
  }
}
