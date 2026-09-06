import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { ContractService } from '../../../core/services/contract.service';
import { DepartmentService } from '../../../core/services/department.service';
import { VendorService } from '../../../core/services/vendor.service';
import { TemplateService } from '../../../core/services/template.service';

@Component({
  selector: 'app-contract-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './contract-form.component.html',
  styleUrls: ['./contract-form.component.css']
})
export class ContractFormComponent implements OnInit {
  private contractService = inject(ContractService);
  private deptService = inject(DepartmentService);
  private vendorService = inject(VendorService);
  private templateService = inject(TemplateService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  contract: any = {
    name: '',
    vendorId: '',
    departmentId: '',
    templateId: '',
    content: '',
    startDate: '',
    endDate: '',
    versionString: '1.0',
    templateVariables: {}
  };

  departments: any[] = [];
  vendors: any[] = [];
  templates: any[] = [];

  // Template variables mapping
  variableKeys: string[] = [];
  selectedTemplateVariables: any = null;

  isEditMode = false;
  contractId = '';
  isLoading = false;
  isSaving = false;

  ngOnInit() {
    this.loadDropdownData();
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.contractId = params['id'];
        this.loadContractDetails();
      }
    });
  }

  loadDropdownData(): void {

    // Departments
    this.deptService.getDepartments().subscribe({

      next: (res: any) => {

        console.log('Department Response:', res);

        if (Array.isArray(res.data)) {

          this.departments = res.data;

        } else if (res.data?.content) {

          this.departments = res.data.content;

        } else {

          this.departments = [];

        }

      },

      error: (err) => {

        console.error(err);

        this.departments = [];

      }

    });

    // Vendors
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

        console.error(err);

        this.vendors = [];

      }

    });

    // Templates
    this.templateService.getTemplates().subscribe({

      next: (res: any) => {

        console.log('Template Response:', res);

        if (Array.isArray(res.data)) {

          this.templates = res.data;

        } else if (res.data?.content) {

          this.templates = res.data.content;

        } else {

          this.templates = [];

        }

      },

      error: (err) => {

        console.error(err);

        this.templates = [];

      }

    });

  }
  loadContractDetails() {
    this.isLoading = true;
    this.contractService.getContractById(this.contractId).subscribe({
      next: (response) => {
        if (response?.data) {
          this.contract = response.data;
          // Ensure templateVariables map is initialized
          if (!this.contract.templateVariables) {
            this.contract.templateVariables = {};
          }
          if (this.contract.templateId) {
            this.onTemplateChange(false);
          }
        }
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        // Fallback mock detail for testing
        this.contract = {
          name: 'Enterprise Cloud Service Agreement',
          vendorId: 'v1',
          departmentId: 'd1',
          templateId: 't1',
          content: 'This Agreement is between AWS and IT department...',
          startDate: '2026-01-01',
          endDate: '2026-12-31',
          versionString: '1.2',
          templateVariables: {
            'EffectiveDate': '2026-01-01',
            'ServiceFee': '$10,000'
          }
        };
        this.onTemplateChange(false);
      }
    });
  }

  onTemplateChange(resetVariables = true) {
    if (!this.contract.templateId) {
      this.variableKeys = [];
      return;
    }

    this.templateService.getTemplateById(this.contract.templateId).subscribe({
      next: (response) => {
        const template = response?.data;
        if (template) {
          // Fill template contents if blank
          if (!this.contract.content || resetVariables) {
            this.contract.content = template.content;
          }

          // Parse variables string (usually comma-separated or JSON list)
          let parsedVars: string[] = [];
          try {
            if (template.variables) {
              if (template.variables.startsWith('[')) {
                parsedVars = JSON.parse(template.variables);
              } else {
                parsedVars = template.variables.split(',').map((v: string) => v.trim());
              }
            }
          } catch (e) {
            parsedVars = [];
          }

          this.variableKeys = parsedVars;

          // Initialize empty keys in contract variables
          this.variableKeys.forEach(key => {
            if (resetVariables || !this.contract.templateVariables[key]) {
              this.contract.templateVariables[key] = '';
            }
          });
        }
      },
      error: () => {
        // Fallback template parsing
        this.variableKeys = ['EffectiveDate', 'ServiceFee'];
        if (resetVariables) {
          this.contract.templateVariables = { 'EffectiveDate': '', 'ServiceFee': '' };
        }
      }
    });
  }

  saveContract() {
    if (!this.contract.name || !this.contract.vendorId || !this.contract.departmentId) {
      alert('Please fill out all required fields.');
      return;
    }

    this.isSaving = true;
    const saveObservable = this.isEditMode
      ? this.contractService.updateContract(this.contractId, this.contract)
      : this.contractService.createContract(this.contract);

    saveObservable.subscribe({
      next: () => {
        this.isSaving = false;
        this.router.navigate(['/contracts']);
      },
      error: (err) => {
        this.isSaving = false;
        alert('Failed to save contract: ' + (err.error?.message || 'Access denied'));
      }
    });
  }
}
