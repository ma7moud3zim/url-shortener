package com.azim.url_shortener.url_shortener.service;

import com.azim.url_shortener.url_shortener.entity.Url;
import com.azim.url_shortener.url_shortener.entity.User;
import com.azim.url_shortener.url_shortener.repository.UrlBlocklistRepository;
import com.azim.url_shortener.url_shortener.repository.UrlRepository;
import com.azim.url_shortener.url_shortener.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final UrlBlocklistRepository urlBlocklistRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    @Value("${application.safebrowsing.key}")
    private String safeBrowsingApiKey;

    // get currently logged in user
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // generate short code from URL using hashing
    private String generateShortCode(String originalUrl) {
        int hash = Math.abs(originalUrl.hashCode());
        return Integer.toString(hash, 36); // base36 encoding
    }

    // check if URL is in local blocklist
    private boolean isBlockedLocally(String url) {
        return urlBlocklistRepository.existsByUrl(url);
    }


    // check URL against Google Safe Browsing API
    private boolean isMalicious(String url) {
        String apiUrl = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + safeBrowsingApiKey;

        String requestBody = """
                {
                  "client": { "clientId": "url-shortener", "clientVersion": "1.0" },
                  "threatInfo": {
                    "threatTypes": ["MALWARE", "SOCIAL_ENGINEERING"],
                    "platformTypes": ["ANY_PLATFORM"],
                    "threatEntryTypes": ["URL"],
                    "threatEntries": [{ "url": "%s" }]
                  }
                }
                """.formatted(url);

        try {
            String response = restTemplate.postForObject(apiUrl, requestBody, String.class);
            // if response contains "matches" the URL is malicious
            return response != null && response.contains("matches");
        } catch (Exception e) {
            // if API call fails, allow the URL
            return false;
        }
    }

    // shorten a URL
    public Url shortenUrl(String originalUrl, String customAlias) {
        // check blocklist
        if (isBlockedLocally(originalUrl)) {
            throw new RuntimeException("This URL is blocked");
        }

        // check Google Safe Browsing
        if (isMalicious(originalUrl)) {
            throw new RuntimeException("This URL is flagged as malicious");
        }

        // determine short code
        String shortCode;
        if (customAlias != null && !customAlias.isBlank()) {
            // use custom alias if provided
            if (urlRepository.existsByCustomAlias(customAlias)) {
                throw new RuntimeException("Custom alias already taken");
            }
            shortCode = customAlias;
        } else {
            // generate from hash
            shortCode = generateShortCode(originalUrl);
            // handle collision
            while (urlRepository.existsByShortCode(shortCode)) {
                shortCode = generateShortCode(originalUrl + System.currentTimeMillis());
            }
        }

        Url url = Url.builder()
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .customAlias(customAlias)
                .user(getCurrentUser())
                .build();

        return urlRepository.save(url);
    }

    // resolve short code to original URL (cached in Redis)
    @Cacheable(value = "urls", key = "#shortCode")
    public String resolveUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .or(() -> urlRepository.findByCustomAlias(shortCode))
                .orElseThrow(() -> new RuntimeException("URL not found"));

        return url.getOriginalUrl();
    }

    // get all URLs for current user
    public List<Url> getUserUrls() {
        return urlRepository.findByUser(getCurrentUser());
    }

    // edit destination URL
    @CacheEvict(value = "urls", key = "#shortCode")
    public Url editUrl(String shortCode, String newOriginalUrl) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        // make sure current user owns this URL
        if (!url.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new RuntimeException("Unauthorized");
        }

        url.setOriginalUrl(newOriginalUrl);
        return urlRepository.save(url);
    }

    // delete URL
    @CacheEvict(value = "urls", key = "#shortCode")
    public void deleteUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        // make sure current user owns this URL
        if (!url.getUser().getEmail().equals(getCurrentUser().getEmail())) {
            throw new RuntimeException("Unauthorized");
        }

        urlRepository.delete(url);
    }

    // generate QR code as base64 string
    public String generateQrCode(String shortCode) {
        try {
            String shortUrl = "http://localhost:8080/" + shortCode;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(shortUrl, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code");
        }
    }
}