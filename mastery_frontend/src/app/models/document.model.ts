import { DocumentStatus, DocumentType } from './enums';
import { LineItem } from './line-item.model';
import { ValidationIssue } from './validation-issue.model';

export interface Document {
  id?: number;

  originalFilename?: string;
  storedFilename?: string;
  mimeType?: string;

  documentType?: DocumentType;
  supplier?: string;
  documentNumber?: string;

  issueDate?: string;
  dueDate?: string;

  currency?: string;

  subtotal?: number;
  tax?: number;
  total?: number;

  status?: DocumentStatus;

  createdDate?: string;
  updatedAt?: string;

  items?: LineItem[];
  validationIssues?: ValidationIssue[];
}