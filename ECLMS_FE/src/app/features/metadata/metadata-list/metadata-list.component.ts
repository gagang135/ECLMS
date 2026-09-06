import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DepartmentService } from '../../../core/services/department.service';
import { VendorService } from '../../../core/services/vendor.service';

@Component({
  selector: 'app-metadata-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './metadata-list.component.html',
  styleUrls: ['./metadata-list.component.css']
})
export class MetadataListComponent implements OnInit {
  private deptService = inject(DepartmentService);
  private vendorService = inject(VendorService);

  activeTab: 'vendors' | 'departments' = 'vendors';
  isLoading = false;

  // Vendors list & pagination
  vendors: any[] = [];
  vendorSearch = '';
  vendorPage = 0;
  vendorTotalPages = 0;

  // Departments list (non-paginated on backend list)
  departments: any[] = [];

  // Modals properties
  showVendorModal = false;
  showDeptModal = false;
  isEditing = false;

  currentVendor: any = {
    id: '',
    name: '',
    email: '',
    phone: '',
    address: '',
    taxId: ''
  };

  currentDept: any = {
    id: '',
    name: '',
    code: '',
    description: ''
  };

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    if (this.activeTab === 'vendors') {
      this.loadVendors();
    } else {
      this.loadDepartments();
    }
  }

  switchTab(tab: 'vendors' | 'departments') {
    this.activeTab = tab;
    this.loadData();
  }

  // Vendors CRUD
  loadVendors() {
    this.isLoading = true;
    this.vendorService.getVendors(this.vendorPage, 10, this.vendorSearch).subscribe({
      next: (res) => {
        this.vendors = res.data?.content || [];
        this.vendorTotalPages = res.data?.totalPages || 0;
        this.isLoading = false;
      },
      error: () => {
        this.vendors = [];
        this.isLoading = false;
      }
    });
  }

  openAddVendor() {
    this.isEditing = false;
    this.currentVendor = { id: '', name: '', email: '', phone: '', address: '', taxId: '' };
    this.showVendorModal = true;
  }

  openEditVendor(vendor: any) {
    this.isEditing = true;
    this.currentVendor = { ...vendor };
    this.showVendorModal = true;
  }

  saveVendor() {
    if (!this.currentVendor.name) {
      alert('Vendor Name is required');
      return;
    }

    const obs = this.isEditing 
      ? this.vendorService.updateVendor(this.currentVendor.id, this.currentVendor)
      : this.vendorService.createVendor(this.currentVendor);

    obs.subscribe({
      next: () => {
        this.showVendorModal = false;
        this.loadVendors();
        alert(`Vendor ${this.isEditing ? 'updated' : 'created'} successfully!`);
      },
      error: (err) => {
        alert('Failed to save vendor: ' + (err.error?.message || 'Access Denied'));
      }
    });
  }

  deleteVendor(id: string) {
    if (confirm('Are you sure you want to delete this vendor? This might affect existing contracts.')) {
      this.vendorService.deleteVendor(id).subscribe({
        next: () => {
          this.loadVendors();
          alert('Vendor deleted successfully.');
        },
        error: (err) => {
          alert('Failed to delete vendor: ' + (err.error?.message || 'Access Denied'));
        }
      });
    }
  }

  // Departments CRUD
  loadDepartments() {
    this.isLoading = true;
    this.deptService.getDepartments().subscribe({
      next: (res) => {
        // Backend getDepartments returns a page or list. Let's inspect the controller.
        // It returns a List of DepartmentDto. Let's bind it.
        this.departments = res.data || [];
        this.isLoading = false;
      },
      error: () => {
        this.departments = [];
        this.isLoading = false;
      }
    });
  }

  openAddDept() {
    this.isEditing = false;
    this.currentDept = { id: '', name: '', code: '', description: '' };
    this.showDeptModal = true;
  }

  openEditDept(dept: any) {
    this.isEditing = true;
    this.currentDept = { ...dept };
    this.showDeptModal = true;
  }

  saveDept() {
    if (!this.currentDept.name || !this.currentDept.code) {
      alert('Department Name and Code are required');
      return;
    }

    const obs = this.isEditing
      ? this.deptService.updateDepartment(this.currentDept.id, this.currentDept)
      : this.deptService.createDepartment(this.currentDept);

    obs.subscribe({
      next: () => {
        this.showDeptModal = false;
        this.loadDepartments();
        alert(`Department ${this.isEditing ? 'updated' : 'created'} successfully!`);
      },
      error: (err) => {
        alert('Failed to save department: ' + (err.error?.message || 'Access Denied'));
      }
    });
  }

  deleteDept(id: string) {
    if (confirm('Are you sure you want to delete this department? This might affect existing contracts.')) {
      this.deptService.deleteDepartment(id).subscribe({
        next: () => {
          this.loadDepartments();
          alert('Department deleted successfully.');
        },
        error: (err) => {
          alert('Failed to delete department: ' + (err.error?.message || 'Access Denied'));
        }
      });
    }
  }

  prevVendorPage() {
    if (this.vendorPage > 0) {
      this.vendorPage--;
      this.loadVendors();
    }
  }

  nextVendorPage() {
    if (this.vendorPage < this.vendorTotalPages - 1) {
      this.vendorPage++;
      this.loadVendors();
    }
  }
}
