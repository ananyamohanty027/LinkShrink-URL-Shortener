package com.ananya.urlshortener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class UrlShortenerController {

    @Autowired
    private UrlShortenerService urlService;

    // Create Short URL
    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> createShortUrl(@RequestBody ShortenRequest request) {
        return ResponseEntity.ok(urlService.shortenUrl(request.getOriginalUrl()));
    }

    // Redirect (The high-performance endpoint)
    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);
        
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
