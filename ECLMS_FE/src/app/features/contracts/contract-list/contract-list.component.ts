import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ContractService } from '../../../core/services/contract.service';
import { DepartmentService } from '../../../core/services/department.service';
import { VendorService } from '../../../core/services/vendor.service';

@Component({
  selector: 'app-contract-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './contract-list.component.html',
  styleUrls: ['./contract-list.component.css']
})
export class ContractListComponent implements OnInit {
  private contractService = inject(ContractService);
  private deptService = inject(DepartmentService);
  private vendorService = inject(VendorService);
  private route = inject(ActivatedRoute);

  contracts: any[] = [];
  departments: any[] = [];
  vendors: any[] = [];

  // Filter params
  searchQuery = '';
  selectedStatus = '';
  selectedDept = '';
  selectedVendor = '';

  // Pagination params
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;

  isLoading = false;

  // Renewal modal controls
  showRenewModal = false;
  renewalContractId = '';
  renewalDate = '';

  ngOnInit() {
    this.loadFilterOptions();

    // Check if redirect has query parameters (e.g. from Dashboard click)
    this.route.queryParams.subscribe(params => {
      if (params['action'] === 'new') {
        // Direct handling if route changes, but listing is standard.
      }
    });

    this.loadContracts();
  }

  loadFilterOptions(): void {

    this.deptService.getDepartments().subscribe({

      next: (res: any) => {

        console.log('Departments Response:', res);

        if (Array.isArray(res.data)) {

          this.departments = res.data;

        } else if (res.data?.content) {

          this.departments = res.data.content;

        } else {

          this.departments = [];

        }

      },

      error: (err) => {

        console.error('Department Error', err);

        this.departments = [];

      }

    });

    this.vendorService.getVendors().subscribe({

      next: (res: any) => {

        console.log('Vendor Response:', res);

        if (Array.isArray(res.data)) {

          this.vendors = res.data;

        } else if (res.data?.content) {

          this.vendors = res.data.content;

        } else {

          this.vendors = [];

        }

      },

      error: (err) => {

        console.error('Vendor Error', err);

        this.vendors = [];

      }

    });

  }

  loadContracts() {
    this.isLoading = true;
    this.contractService.getContracts(
      this.page,
      this.size,
      this.searchQuery,
      this.selectedStatus,
      this.selectedDept,
      this.selectedVendor
    ).subscribe({
      next: (response) => {
        if (response?.data) {
          this.contracts = response.data.content || [];
          this.totalElements = response.data.totalElements || 0;
          this.totalPages = response.data.totalPages || 0;
        } else {
          this.contracts = [];
          this.totalElements = 0;
          this.totalPages = 0;
        }
        this.isLoading = false;
      },
      error: () => {
        this.contracts = [];
        this.totalElements = 0;
        this.totalPages = 0;
        this.isLoading = false;
      }
    });
  }

  onFilterChange() {
    this.page = 0;
    this.loadContracts();
  }

  clearFilters() {
    this.searchQuery = '';
    this.selectedStatus = '';
    this.selectedDept = '';
    this.selectedVendor = '';
    this.page = 0;
    this.loadContracts();
  }

  deleteContract(id: string) {
    if (confirm('Are you sure you want to delete this contract?')) {
      this.contractService.deleteContract(id).subscribe({
        next: () => {
          this.loadContracts();
        },
        error: (err) => {
          alert('Failed to delete contract: ' + (err.error?.message || 'Unauthorized'));
        }
      });
    }
  }

  openRenewModal(id: string) {
    this.renewalContractId = id;
    this.renewalDate = '';
    this.showRenewModal = true;
  }

  submitRenewal() {
    if (!this.renewalDate) {
      alert('Please select a renewal end date.');
      return;
    }

    this.contractService.renewContract(this.renewalContractId, this.renewalDate).subscribe({
      next: () => {
        this.showRenewModal = false;
        this.loadContracts();
      },
      error: (err) => {
        alert('Failed to renew contract: ' + (err.error?.message || 'Access Denied'));
      }
    });
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadContracts();
    }
  }

  nextPage() {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadContracts();
    }
  }
}
