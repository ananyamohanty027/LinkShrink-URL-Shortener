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

    // Default to root path so links look like http://localhost:8080/AbCd12
    @Value("${app.baseUrl:http://localhost:8080/}")
    private String baseUrl;

    // --- 1. WRITE PATH (Write-Around: DB Only) ---
    public ShortenResponse shortenUrl(String originalUrl) {
        String shortCode = generateBase62Hash(originalUrl);

        // Idempotency check: If URL already exists, return the existing one
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
        // A. Check Redis (Fastest)
        String cachedUrl = redisTemplate.opsForValue().get("url:" + shortCode);
        if (cachedUrl != null) {
            incrementClickCount(shortCode); // Async analytics update
            return cachedUrl;
        }

        // B. Database Fallback (Slower)
        UrlMapping mapping = urlRepository.findById(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        // C. Write to Redis (TTL 24 Hours)
        // This ensures subsequent requests hit the cache
        redisTemplate.opsForValue().set(
            "url:" + shortCode, 
            mapping.getOriginalUrl(), 
            24, TimeUnit.HOURS
        );

        incrementClickCount(shortCode);
        return mapping.getOriginalUrl();
    }

    // --- 3. ANALYTICS (Write-Behind Pattern) ---
    @Async // Runs in a separate thread to not block the user
    public void incrementClickCount(String shortCode) {
        try {
            // 1. Instant Increment in Redis (Atomic)
            String analyticsKey = "analytics:" + shortCode;
            redisTemplate.opsForValue().increment(analyticsKey);

            // 2. Persist to MongoDB (Eventual Consistency)
            // We update the DB count so data isn't lost if Redis restarts
            urlRepository.findById(shortCode).ifPresent(mapping -> {
                mapping.setClickCount(mapping.getClickCount() + 1);
                urlRepository.save(mapping);
            });
        } catch (Exception e) {
            System.err.println("Failed to update analytics for " + shortCode + ": " + e.getMessage());
        }
    }

    // --- 4. GET ANALYTICS DATA ---
    public long getClickCount(String shortCode) {
        String analyticsKey = "analytics:" + shortCode;
        
        // Try reading from Redis first
        String count = redisTemplate.opsForValue().get(analyticsKey);
        if (count != null) {
            return Long.parseLong(count);
        }
        
        // Fallback to DB if Redis is empty (e.g., after restart)
        return urlRepository.findById(shortCode)
                .map(UrlMapping::getClickCount)
                .orElse(0L);
    }

    // --- 5. UTILS (SHA-256 + Base62) ---
    private String generateBase62Hash(String originalUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(originalUrl.getBytes(StandardCharsets.UTF_8));
            
            String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            
            // Use first 7 bytes of hash to map to Base62 chars
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
