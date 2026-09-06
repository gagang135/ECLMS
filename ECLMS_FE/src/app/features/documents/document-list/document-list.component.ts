import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DocumentService } from '../../../core/services/document.service';
import { ContractService } from '../../../core/services/contract.service';

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './document-list.component.html',
  styleUrls: ['./document-list.component.css']
})
export class DocumentListComponent implements OnInit {
  private documentService = inject(DocumentService);
  private contractService = inject(ContractService);

  contracts: any[] = [];
  selectedContractId = '';
  selectedContractName = '';
  documents: any[] = [];
  
  // Upload properties
  selectedFile: File | null = null;
  isUploading = false;
  isLoadingDocs = false;
  isLoadingContracts = false;

  ngOnInit() {
    this.loadContracts();
  }

  loadContracts() {
    this.isLoadingContracts = true;
    this.contractService.getContracts(0, 100, '').subscribe({
      next: (res) => {
        this.contracts = res.data?.content || [];
        this.selectGeneralDocuments();
        this.isLoadingContracts = false;
      },
      error: () => {
        this.selectGeneralDocuments();
        this.isLoadingContracts = false;
      }
    });
  }

  selectContract(contract: any) {
    this.selectedContractId = contract.id;
    this.selectedContractName = contract.name;
    this.loadDocuments();
  }

  selectGeneralDocuments() {
    this.selectedContractId = 'general';
    this.selectedContractName = 'General / Shared Documents';
    this.loadDocuments();
  }

  loadDocuments() {
    if (!this.selectedContractId) return;
    this.isLoadingDocs = true;
    this.documentService.getDocumentsByContract(this.selectedContractId).subscribe({
      next: (res) => {
        this.documents = res.data || [];
        this.isLoadingDocs = false;
      },
      error: () => {
        this.documents = [];
        this.isLoadingDocs = false;
      }
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  uploadFile() {
    if (!this.selectedFile) {
      alert('Please choose a file to upload first.');
      return;
    }

    this.isUploading = true;
    this.documentService.uploadDocument(this.selectedFile, this.selectedContractId).subscribe({
      next: (res) => {
        this.isUploading = false;
        this.selectedFile = null;
        alert('Document uploaded successfully!');
        this.loadDocuments();
      },
      error: (err) => {
        this.isUploading = false;
        alert('Failed to upload document: ' + (err.error?.message || 'Size exceeded or invalid format'));
      }
    });
  }

  downloadFile(doc: any) {
    this.documentService.downloadDocument(doc.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = doc.fileName;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
      },
      error: (err) => {
        alert('Failed to download document.');
      }
    });
  }

  previewFile(doc: any) {
    this.documentService.getPreviewUrl(doc.id).subscribe({
      next: (res) => {
        if (res?.data) {
          window.open(res.data, '_blank');
        } else {
          alert('Preview URL not generated.');
        }
      },
      error: () => {
        alert('Failed to load preview URL.');
      }
    });
  }

  deleteFile(docId: string) {
    if (confirm('Are you sure you want to permanently delete this document?')) {
      this.documentService.deleteDocument(docId).subscribe({
        next: () => {
          alert('Document deleted successfully.');
          this.loadDocuments();
        },
        error: (err) => {
          alert('Failed to delete document: ' + (err.error?.message || 'Access Denied'));
        }
      });
    }
  }

  formatBytes(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = 2;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
  }
}
