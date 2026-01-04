package com.ananya.urlshortener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class UrlShortenerService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${app.baseUrl:http://localhost:8080/}")
    private String baseUrl;

    // --- 1. WRITE PATH (Write-Around: DB Only) ---
    public ShortenResponse shortenUrl(String originalUrl) {
        String shortCode = generateBase62Hash(originalUrl);

        // Check if exists to avoid duplicates (Idempotency)
        if (urlRepository.existsById(shortCode)) {
             return new ShortenResponse(baseUrl + shortCode, originalUrl, 600);
        }

        UrlMapping mapping = new UrlMapping();
        mapping.setId(shortCode); // Use shortCode as the DB ID
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortCode(shortCode);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setClickCount(0);

        // Write-Around: Save ONLY to DB. Do not write to cache yet.
        urlRepository.save(mapping);

        return new ShortenResponse(baseUrl + shortCode, originalUrl, 600);
    }

    // --- 2. READ PATH (Cache-Aside + TTL) ---
    public String getOriginalUrl(String shortCode) {
        // A. Check Redis
        String cachedUrl = redisTemplate.opsForValue().get("url:" + shortCode);
        if (cachedUrl != null) {
            incrementClickCount(shortCode); // Async analytics
            return cachedUrl;
        }

        // B. Database Fallback
        UrlMapping mapping = urlRepository.findById(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        // C. Write to Redis (TTL 24 Hours)
        redisTemplate.opsForValue().set(
            "url:" + shortCode, 
            mapping.getOriginalUrl(), 
            24, TimeUnit.HOURS
        );

        incrementClickCount(shortCode);
        return mapping.getOriginalUrl();
    }

    // --- 3. ANALYTICS (Async) ---
    @Async
    public void incrementClickCount(String shortCode) {
        // Fast in-memory counter
        redisTemplate.opsForValue().increment("analytics:" + shortCode);
    }

    // --- 4. UTILS (SHA-256 + Base62) ---
    private String generateBase62Hash(String originalUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(originalUrl.getBytes(StandardCharsets.UTF_8));
            
            // Simple Base62 encoding of the hash
            String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            // Use first 7 bytes of hash for entropy
            for (int i = 0; i < 7; i++) {
                int index = Math.abs(hash[i]) % allowed.length();
                sb.append(allowed.charAt(index));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }
}
