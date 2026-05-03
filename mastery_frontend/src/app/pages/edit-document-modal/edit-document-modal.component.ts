import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FormArray, FormBuilder, FormGroup,  ReactiveFormsModule } from '@angular/forms';
import { DocumentService } from '../../services/document.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { CommonModule } from '@angular/common';
import { MatIconModule } from "@angular/material/icon";

@Component({
  selector: 'app-edit-document-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule
],
  templateUrl: './edit-document-modal.component.html',
  styleUrls: ['./edit-document-modal.component.css']
})
export class EditDocumentModalComponent {

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private documentService: DocumentService,
    private dialogRef: MatDialogRef<EditDocumentModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {

    this.form = this.fb.group({
      documentType: [data.documentType],

      supplierName: [data.supplierName || data.supplier],
      documentNumber: [data.documentNumber],

      issueDate: [data.issueDate],
      dueDate: [data.dueDate],

      currency: [data.currency],

      subtotal: [data.subtotal],
      tax: [data.tax],
      total: [data.total],

      lineItems: this.fb.array(this.initLineItems(data.lineItems))
    });
  }

  initLineItems(items: any[]): FormGroup[] {
    return (items || []).map(item =>
      this.fb.group({
        description: [item.description],
        quantity: [item.quantity],
        unitPrice: [item.unitPrice],
        lineTotal: [item.lineTotal],
        taxRate: [item.taxRate]
      })
    );
  }

  get lineItems(): FormArray {
    return this.form.get('lineItems') as FormArray;
  }

  addItem() {
    this.lineItems.push(
      this.fb.group({
        description: [''],
        quantity: [0],
        unitPrice: [0],
        lineTotal: [0],
        taxRate: [0]
      })
    );
  }

  removeItem(index: number) {
    this.lineItems.removeAt(index);
  }

  save() {
    const v = this.form.value;

    const payload = {
      documentType: v.documentType,
      supplierName: v.supplierName,
      documentNumber: v.documentNumber,
      issueDate: v.issueDate,
      dueDate: v.dueDate,
      currency: v.currency,
      subtotal: v.subtotal,
      tax: v.tax,
      total: v.total,
      lineItems: v.lineItems
    };

    this.documentService.update(this.data.id, payload).subscribe(() => {
      this.dialogRef.close(true);
    });
  }

  close() {
    this.dialogRef.close();
  }
}