import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  Template,
  TemplateService
} from '../../../core/services/template.service';

@Component({
  selector: 'app-template-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './template-list.component.html',
  styleUrls: ['./template-list.component.css']
})
export class TemplateListComponent implements OnInit {

  private readonly templateService =
    inject(TemplateService);

  templates: Template[] = [];
  filteredTemplates: Template[] = [];

  isLoading = false;
  isSaving = false;

  showAddModal = false;
  isEditMode = false;

  searchText = '';

  selectedTemplateId = '';

  templateForm: Template = {

    name: '',

    description: '',

    content: '',

    variables: '',

    versionString: '1.0'

  };

  ngOnInit(): void {

    this.loadTemplates();

  }

  //------------------------------------
  // Load
  //------------------------------------

  loadTemplates(): void {

    this.isLoading = true;

    this.templateService.getTemplates().subscribe({

      next: (response) => {

        console.log("Template Response:", response);

        if (response.success && Array.isArray(response.data)) {

          this.templates = response.data.filter((t: any) => t != null);

          this.filteredTemplates = [...this.templates];

        } else {

          this.templates = [];
          this.filteredTemplates = [];

        }

        this.isLoading = false;

      },

      error: (error) => {

        console.error(error);

        this.templates = [];
        this.filteredTemplates = [];
        this.isLoading = false;

      }

    });

  }

  //------------------------------------
  // Search
  //------------------------------------

  searchTemplates(): void {

    const keyword = this.searchText.trim().toLowerCase();

    if (!keyword) {

      this.filteredTemplates = [...this.templates];

      return;

    }

    this.filteredTemplates = this.templates.filter(template => {

      if (!template) {
        return false;
      }

      const name = (template.name || '').toLowerCase();
      const description = (template.description || '').toLowerCase();

      return name.includes(keyword) || description.includes(keyword);

    });

  }

  //------------------------------------
  // Create
  //------------------------------------

  openAddModal(): void {

    this.isEditMode = false;

    this.selectedTemplateId = '';

    this.templateForm = {

      name: '',

      description: '',

      content: '',

      variables: '',

      versionString: '1.0'

    };

    this.showAddModal = true;

  }

  //------------------------------------
  // Edit
  //------------------------------------

  editTemplate(template: Template): void {

    if (!template.id) {
      return;
    }

    this.isEditMode = true;

    this.selectedTemplateId =
      template.id;

    this.templateForm = {

      id: template.id,

      name: template.name,

      description: template.description,

      content: template.content,

      variables: template.variables,

      versionString: template.versionString,

      status: template.status

    };

    this.showAddModal = true;

  }

  //------------------------------------
  // Close
  //------------------------------------

  closeModal(): void {

    this.showAddModal = false;

  }

  //------------------------------------
  // Validation
  //------------------------------------

  private validateForm(): boolean {

    if (!this.templateForm.name.trim()) {

      alert('Template Name is required');

      return false;

    }

    if (!this.templateForm.description.trim()) {

      alert('Description is required');

      return false;

    }

    if (!this.templateForm.content.trim()) {

      alert('Template Content is required');

      return false;

    }

    return true;

  }
  //------------------------------------
  // Save
  //------------------------------------

  saveTemplate(): void {

    if (!this.validateForm()) {
      return;
    }

    if (this.isSaving) {
      return;
    }

    this.isSaving = true;

    if (this.isEditMode) {

      this.updateTemplate();

    } else {

      this.createTemplate();

    }

  }

  //------------------------------------
  // Create
  //------------------------------------

  private createTemplate(): void {

    this.templateService
      .createTemplate(this.templateForm)
      .subscribe({

        next: (response) => {

          console.log('Template Created', response);

          this.isSaving = false;

          this.showAddModal = false;

          this.loadTemplates();

          alert('Template created successfully.');

        },

        error: (error) => {

          console.error(error);

          this.isSaving = false;

          alert(
            error?.error?.message ||
            'Failed to create template.'
          );

        }

      });

  }

  //------------------------------------
  // Update
  //------------------------------------

  private updateTemplate(): void {

    if (!this.selectedTemplateId) {

      this.isSaving = false;

      return;

    }

    this.templateService
      .updateTemplate(
        this.selectedTemplateId,
        this.templateForm
      )
      .subscribe({

        next: (response) => {

          console.log('Template Updated', response);

          this.isSaving = false;

          this.showAddModal = false;

          this.loadTemplates();

          alert('Template updated successfully.');

        },

        error: (error) => {

          console.error(error);

          this.isSaving = false;

          alert(
            error?.error?.message ||
            'Failed to update template.'
          );

        }

      });

  }

  //------------------------------------
  // Delete
  //------------------------------------

  deleteTemplate(template: Template): void {

    if (!template.id) {
      return;
    }

    const confirmed = confirm(
      `Delete template "${template.name}" ?`
    );

    if (!confirmed) {
      return;
    }

    this.templateService
      .deleteTemplate(template.id)
      .subscribe({

        next: () => {

          alert('Template deleted successfully.');

          this.loadTemplates();

        },

        error: (error) => {

          console.error(error);

          alert(
            error?.error?.message ||
            'Failed to delete template.'
          );

        }

      });

  }

  //------------------------------------
  // Publish
  //------------------------------------

  publishTemplate(template: Template): void {

    if (!template.id) {
      return;
    }

    this.templateService
      .publishTemplate(template.id)
      .subscribe({

        next: () => {

          alert('Template published successfully.');

          this.loadTemplates();

        },

        error: (error) => {

          console.error(error);

          alert(
            error?.error?.message ||
            'Failed to publish template.'
          );

        }

      });

  }

  //------------------------------------
  // Archive
  //------------------------------------

  archiveTemplate(template: Template): void {

    if (!template.id) {
      return;
    }

    this.templateService
      .archiveTemplate(template.id)
      .subscribe({

        next: () => {

          alert('Template archived successfully.');

          this.loadTemplates();

        },

        error: (error) => {

          console.error(error);

          alert(
            error?.error?.message ||
            'Failed to archive template.'
          );

        }

      });

  }

  //------------------------------------
  // Refresh
  //------------------------------------

  refresh(): void {

    this.searchText = '';

    this.filteredTemplates = [...this.templates];

  }

}