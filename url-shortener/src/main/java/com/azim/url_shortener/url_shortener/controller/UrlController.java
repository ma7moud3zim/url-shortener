package com.azim.url_shortener.url_shortener.controller;

import com.azim.url_shortener.url_shortener.dto.EditUrlRequest;
import com.azim.url_shortener.url_shortener.dto.ShortenRequest;
import com.azim.url_shortener.url_shortener.entity.Url;
import com.azim.url_shortener.url_shortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    // shorten a URL
    @PostMapping("/api/urls/shorten")
    public ResponseEntity<Url> shorten(@Valid @RequestBody ShortenRequest request) {
        Url url = urlService.shortenUrl(request.getOriginalUrl(), request.getCustomAlias());
        return ResponseEntity.ok(url);
    }

    // browse all URLs of current user
    @GetMapping("/api/urls")
    public ResponseEntity<List<Url>> getUserUrls() {
        return ResponseEntity.ok(urlService.getUserUrls());
    }

    // edit destination URL
    @PutMapping("/api/urls/{shortCode}")
    public ResponseEntity<Url> editUrl(@PathVariable String shortCode,
                                       @Valid @RequestBody EditUrlRequest request) {
        Url url = urlService.editUrl(shortCode, request.getNewOriginalUrl());
        return ResponseEntity.ok(url);
    }

    // delete a URL
    @DeleteMapping("/api/urls/{shortCode}")
    public ResponseEntity<String> deleteUrl(@PathVariable String shortCode) {
        urlService.deleteUrl(shortCode);
        return ResponseEntity.ok("URL deleted successfully");
    }

    // get QR code for a short URL
    @GetMapping("/api/urls/{shortCode}/qrcode")
    public ResponseEntity<String> getQrCode(@PathVariable String shortCode) {
        String base64Qr = urlService.generateQrCode(shortCode);
        return ResponseEntity.ok(base64Qr);
    }

    // public redirect endpoint — this is what users actually click
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.resolveUrl(shortCode);
        return ResponseEntity.status(302)
                .header("Location", originalUrl)
                .build();
    }
}