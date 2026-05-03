import { Component, OnInit } from '@angular/core';
import { DocumentService } from '../../services/document.service';
import { Document } from '../../models/document.model';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { EditDocumentModalComponent } from '../edit-document-modal/edit-document-modal.component';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule
],
  templateUrl: './document.component.html',
  styleUrls: ['./document.component.css']
})
export class DocumentComponent implements OnInit {

  columns: string[] = [
    'filename',
    'supplier',
    'number',
    'status',
    'date',
    'actions'
  ];

  dataSource = new MatTableDataSource<Document>();

  selectedFile: File | null = null;
  isUploading = false;

  page = 0;
  size = 10;
  totalElements = 0;

  expandedElement: any | null = null;

  constructor(private documentService: DocumentService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  uploadFile() {
    if (!this.selectedFile) return;

    this.isUploading = true;

    this.documentService.upload(this.selectedFile).subscribe({
      next: () => {
        this.isUploading = false;
        this.selectedFile = null;
        this.loadDocuments();
      },
      error: (err) => {
        console.error(err);
        this.isUploading = false;
      }
    });
  }

  loadDocuments() {
    this.documentService.getAll(this.page, this.size).subscribe({
      next: (res) => {
        this.dataSource.data = res.content;
        this.totalElements = res.totalElements;
      },
      error: (err) => console.error(err)
    });
  }

  onPageChange(event: any) {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.loadDocuments();
  }

  preview(doc: Document) {
    window.open(
      `${environment.apiUrl}/documents/${doc.id}/file/preview`,
      '_blank'
    );
  }

  download(doc: Document) {
    window.open(
      `${environment.apiUrl}/documents/${doc.id}/file/download`,
      '_blank'
    );
  }

  // delete(doc: Document) {
  //   if (!confirm('Delete document?')) return;

  //   this.documentService.delete(doc.id!).subscribe(() => {
  //     this.loadDocuments();
  //   });
  // }

  toggleRow(row: any) {
    this.expandedElement = this.expandedElement === row ? null : row;
  }

  edit(doc: Document) {

    (document.activeElement as HTMLElement)?.blur();

    const dialogRef = this.dialog.open(EditDocumentModalComponent, {
      width: '900px',
      maxWidth: '95vw',
      panelClass: 'document-dialog',
      data: doc,

      autoFocus: false,
      restoreFocus: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadDocuments();
      }
    });
  }

}