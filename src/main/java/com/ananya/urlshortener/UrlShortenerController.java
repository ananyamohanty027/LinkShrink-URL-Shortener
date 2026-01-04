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
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlService;

    // Bucket4j: Allows 10 requests per minute per IP (Simple Global Limit for now)
    private final Bucket bucket;

    public UrlShortenerController() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> createShortUrl(@RequestBody ShortenRequest request) {
        // 1. Check Rate Limit
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Rate limit exceeded. Try again later.");
        }
        
        // 2. Proceed if allowed
        return ResponseEntity.ok(urlService.shortenUrl(request.getOriginalUrl()));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
    
    // NEW: Simple Analytics Endpoint
    @GetMapping("/analytics/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {
        // In a real app, you'd fetch this from the Service
        return ResponseEntity.ok("Analytics feature enabled for: " + shortCode);
    }
}
