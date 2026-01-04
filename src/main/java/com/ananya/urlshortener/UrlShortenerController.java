package com.ananya.urlshortener;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlService;

    private final Bucket bucket;

    public UrlShortenerController() {
        // Rate Limit: 10 requests per minute
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    // --- 1. SHORTEN ENDPOINT ---
    @PostMapping("/api/v1/shorten") 
    public ResponseEntity<?> createShortUrl(@RequestBody ShortenRequest request) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded.");
        }
        return ResponseEntity.ok(urlService.shortenUrl(request.getOriginalUrl()));
    }

    // --- 2. REDIRECT ENDPOINT ---
    @GetMapping("/{shortCode}") 
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        // Ignore favicon requests
        if ("favicon.ico".equals(shortCode)) {
            return ResponseEntity.notFound().build();
        }

        String originalUrl = urlService.getOriginalUrl(shortCode);
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
    
    // --- 3. ANALYTICS (UPDATED) ---
    @GetMapping("/api/v1/analytics/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {
        // 1. Get the real click count from Service (Redis + DB)
        long clicks = urlService.getClickCount(shortCode);
        
        // 2. Return as clean JSON
        Map<String, Object> response = new HashMap<>();
        response.put("shortCode", shortCode);
        response.put("clicks", clicks);
        
        return ResponseEntity.ok(response);
    }
}
