export interface Url {
  id: number;
  originalUrl: string;
  shortCode: string;
  customAlias: string | null;
  createdAt: string;
}

export interface ShortenRequest {
  originalUrl: string;
  customAlias?: string;
}

export interface EditUrlRequest {
  newOriginalUrl: string;
}
