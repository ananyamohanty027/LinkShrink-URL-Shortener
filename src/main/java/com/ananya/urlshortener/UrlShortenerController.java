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

@RestController
// ❌ REMOVED CLASS-LEVEL REQUEST MAPPING
// @RequestMapping("/api/v1")  <-- This was causing the 404!
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

    // --- 1. SHORTEN ENDPOINT (Kept at /api/v1/shorten) ---
    @PostMapping("/api/v1/shorten") 
    public ResponseEntity<?> createShortUrl(@RequestBody ShortenRequest request) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded.");
        }
        return ResponseEntity.ok(urlService.shortenUrl(request.getOriginalUrl()));
    }

    // --- 2. REDIRECT ENDPOINT (Moved to ROOT /) ---
    // Now it listens at https://your-site.com/{shortCode}
    @GetMapping("/{shortCode}") 
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        // Ignore favicon requests (browsers ask for this automatically)
        if ("favicon.ico".equals(shortCode)) {
            return ResponseEntity.notFound().build();
        }

        String originalUrl = urlService.getOriginalUrl(shortCode);
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
    
    // --- 3. ANALYTICS (Optional) ---
    @GetMapping("/api/v1/analytics/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {
        return ResponseEntity.ok("Analytics for " + shortCode);
    }
}
