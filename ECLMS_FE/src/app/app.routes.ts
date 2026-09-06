import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { LayoutComponent } from './shared/layout/layout.component';
import { LoginComponent } from './features/login/login.component';
import { RegisterComponent } from './features/register/register.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ContractListComponent } from './features/contracts/contract-list/contract-list.component';
import { ContractFormComponent } from './features/contracts/contract-form/contract-form.component';
import { WorkflowListComponent } from './features/workflows/workflow-list/workflow-list.component';
import { DocumentListComponent } from './features/documents/document-list/document-list.component';
import { TemplateListComponent } from './features/templates/template-list/template-list.component';
import { MetadataListComponent } from './features/metadata/metadata-list/metadata-list.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'contracts', component: ContractListComponent },
      { path: 'contracts/new', component: ContractFormComponent },
      { path: 'contracts/edit/:id', component: ContractFormComponent },
      { path: 'workflows', component: WorkflowListComponent },
      { path: 'documents', component: DocumentListComponent },
      { path: 'templates', component: TemplateListComponent },
      { path: 'metadata', component: MetadataListComponent }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
