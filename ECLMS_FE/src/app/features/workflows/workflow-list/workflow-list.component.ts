import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WorkflowService } from '../../../core/services/workflow.service';
import { ContractService } from '../../../core/services/contract.service';
import { TemplateService } from '../../../core/services/template.service';

@Component({
  selector: 'app-workflow-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './workflow-list.component.html',
  styleUrls: ['./workflow-list.component.css']
})
export class WorkflowListComponent implements OnInit {
  private workflowService = inject(WorkflowService);
  private contractService = inject(ContractService);
  private templateService = inject(TemplateService);

  workflows: any[] = [];
  instances: any[] = [];
  contracts: any[] = [];
  history: any[] = [];
  templates: any[] = [];

  // Dropdown selections
  selectedTemplateId = '';
  selectedContract = '';
  filteredContracts: any[] = [];

  // Form selections for approvals
  actionInstanceId = '';
  actionType = 'APPROVED';
  actionComments = '';

  isLoading = false;
  showActionModal = false;
  showHistoryModal = false;
  activeHistoryInstanceName = '';

  ngOnInit() {

    this.loadTemplates();

    this.loadWorkflows();

    this.loadContracts();

    this.loadWorkflowInstances();

  }

  loadWorkflows(): void {

    this.isLoading = true;

    this.workflowService.getWorkflows().subscribe({

      next: (res: any) => {

        console.log('Workflow Response:', res);

        if (Array.isArray(res.data)) {

          this.workflows = res.data;

        } else if (res.data?.content) {

          this.workflows = res.data.content;

        } else {

          this.workflows = [];

        }

        this.isLoading = false;

      },

      error: (err) => {

        console.error('Workflow Error:', err);

        this.workflows = [];

        this.isLoading = false;

      }

    });

  }

  loadContracts(): void {
    this.contractService.getContracts().subscribe({
      next: (res: any) => {
        console.log('Contract Response:', res);
        if (Array.isArray(res.data)) {
          this.contracts = res.data;
        } else if (res.data?.content) {
          this.contracts = res.data.content;
        } else {
          this.contracts = [];
        }
        this.onTemplateChange();
      },
      error: (err) => {
        console.error('Contract Error:', err);
        this.contracts = [];
        this.onTemplateChange();
      }
    });
  }

  onTemplateChange() {
    this.selectedContract = '';
    if (this.selectedTemplateId) {
      this.filteredContracts = this.contracts.filter(c => c.templateId === this.selectedTemplateId);
    } else {
      this.filteredContracts = [];
    }
  }

  startWorkflow() {
    if (!this.selectedTemplateId || !this.selectedContract) {
      alert('Please select both a contract template and a contract.');
      return;
    }

    const selectedTemplate = this.templates.find(t => t.id === this.selectedTemplateId);
    if (!selectedTemplate) {
      alert('Selected template not found.');
      return;
    }

    // Match workflow by name
    const templateNameBase = selectedTemplate.name.replace(' Agreement', '').trim();
    const matchingWorkflow = this.workflows.find(w =>
      w.name.toLowerCase().includes(templateNameBase.toLowerCase())
    );

    if (!matchingWorkflow) {
      alert(`No active workflow definition found matching template name "${selectedTemplate.name}".`);
      return;
    }

    this.workflowService.startWorkflow(matchingWorkflow.id, this.selectedContract).subscribe({
      next: (res) => {
        alert('Workflow started successfully!');
        this.selectedTemplateId = '';
        this.selectedContract = '';
        this.filteredContracts = [];
        this.loadWorkflowInstances();
      },
      error: (err) => {
        alert('Failed to start workflow: ' + (err.error?.message || 'Access Denied'));
      }
    });
  }

  openActionModal(id: string) {
    this.actionInstanceId = id;
    this.actionType = 'APPROVED';
    this.actionComments = '';
    this.showActionModal = true;
  }

  submitAction() {
    if (!this.actionComments) {
      alert('Please enter approval/rejection comments.');
      return;
    }

    this.workflowService.recordAction(this.actionInstanceId, this.actionType, this.actionComments).subscribe({
      next: (res) => {
        this.showActionModal = false;
        alert('Workflow action recorded!');
        this.loadWorkflowInstances();
      },
      error: (err) => {
        this.showActionModal = false;
        const errMsg = err.error?.errors?.[0] || err.error?.message || 'Access Denied';
        alert('Failed to record action: ' + errMsg);
      }
    });
  }

  loadWorkflowInstances(): void {
    this.workflowService.getWorkflowInstances().subscribe({
      next: (res: any) => {
        this.instances = res.data || [];
      },
      error: (err) => {
        console.error('Error loading workflow instances:', err);
        this.instances = [];
      }
    });
  }

  openHistoryModal(instance: any) {
    this.activeHistoryInstanceName = instance.contractName;
    this.showHistoryModal = true;
    this.isLoading = true;

    this.workflowService.getApprovalHistory(instance.id).subscribe({
      next: (res) => {
        this.history = res.data || [];
        this.isLoading = false;
      },
      error: () => {
        this.history = [];
        this.isLoading = false;
      }
    });
  }

  loadTemplates(): void {

    this.templateService.getTemplates().subscribe({

      next: (res: any) => {

        console.log("Templates:", res);

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
}
