import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { UrlService } from '../../core/services/url.service';
import { AuthService } from '../../core/services/auth.service';
import { Url, ShortenRequest } from '../../core/models/url.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  urls: Url[] = [];
  newUrl: ShortenRequest = { originalUrl: '', customAlias: '' };

  isLoading = false;
  isShortening = false;
  errorMessage = '';
  copiedCode: string | null = null;

  // QR modal state
  qrModalUrl: string | null = null;
  qrCodeBase64: string | null = null;
  isLoadingQr = false;

  // Edit modal state
  editingUrl: Url | null = null;
  editValue = '';
  isSavingEdit = false;
  editError = '';

  constructor(
    private urlService: UrlService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadUrls();
  }

  loadUrls(): void {
    this.isLoading = true;
    this.urlService.getUserUrls().subscribe({
      next: (urls) => {
        this.urls = urls;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  onShorten(): void {
    this.isShortening = true;
    this.errorMessage = '';

    this.urlService.shorten(this.newUrl).subscribe({
      next: (url) => {
        this.urls.unshift(url);
        this.newUrl = { originalUrl: '', customAlias: '' };
        this.isShortening = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.error || 'Failed to shorten URL';
        this.isShortening = false;
      },
    });
  }

  deleteUrl(shortCode: string): void {
    this.urlService.deleteUrl(shortCode).subscribe({
      next: () => {
        this.urls = this.urls.filter((u) => u.shortCode !== shortCode);
      },
    });
  }

  copyToClipboard(shortCode: string): void {
    const shortUrl = `http://localhost:8080/${shortCode}`;
    navigator.clipboard.writeText(shortUrl).then(() => {
      this.copiedCode = shortCode;
      setTimeout(() => (this.copiedCode = null), 2000);
    });
  }

  // ---- QR code ----
  openQrModal(shortCode: string): void {
    this.qrModalUrl = shortCode;
    this.isLoadingQr = true;
    this.qrCodeBase64 = null;

    this.urlService.getQrCode(shortCode).subscribe({
      next: (base64) => {
        this.qrCodeBase64 = base64;
        this.isLoadingQr = false;
      },
      error: () => {
        this.isLoadingQr = false;
      },
    });
  }

  closeQrModal(): void {
    this.qrModalUrl = null;
    this.qrCodeBase64 = null;
  }

  // ---- Edit ----
  openEditModal(url: Url): void {
    this.editingUrl = url;
    this.editValue = url.originalUrl;
    this.editError = '';
  }

  closeEditModal(): void {
    this.editingUrl = null;
    this.editValue = '';
  }

  saveEdit(): void {
    if (!this.editingUrl) return;

    this.isSavingEdit = true;
    this.editError = '';

    this.urlService
      .editUrl(this.editingUrl.shortCode, { newOriginalUrl: this.editValue })
      .subscribe({
        next: (updatedUrl) => {
          const index = this.urls.findIndex((u) => u.shortCode === updatedUrl.shortCode);
          if (index !== -1) {
            this.urls[index] = updatedUrl;
          }
          this.isSavingEdit = false;
          this.closeEditModal();
        },
        error: (err) => {
          this.editError = err.error?.error || 'Failed to update URL';
          this.isSavingEdit = false;
        },
      });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
