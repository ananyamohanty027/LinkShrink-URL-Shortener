package com.ananya.urlshortener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class UrlShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}

// ------------------- 1. THE MODEL (MongoDB Entity) -------------------
@Document(collection = "urls")
@Data
@NoArgsConstructor
@AllArgsConstructor
class UrlMapping {
    @Id
    private String id;
    private String originalUrl;
    private String shortCode;
    private LocalDateTime createdAt;
    private long clickCount;
}

// ------------------- 2. THE DTO (Data Transfer Object) -------------------
@Data
class ShortenRequest {
    private String originalUrl;
}

@Data
@AllArgsConstructor
class ShortenResponse {
    private String shortUrl;
    private String originalUrl;
    private long expiresSeconds;
}

// ------------------- 3. THE REPOSITORY (Database Access) -------------------
interface UrlRepository extends MongoRepository<UrlMapping, String> {
    Optional<UrlMapping> findByShortCode(String shortCode);
}

// ------------------- 4. THE SERVICE (Business Logic & System Design) -------------------
@Service
class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate; // Redis for Caching

    private static final String BASE_URL = "http://localhost:8080/";

    // --- Core Logic: Shortening ---
    public ShortenResponse shortenUrl(String originalUrl) {
        // 1. Generate a generic unique ID (In prod, use a distributed ID generator like Snowflake)
        String shortCode = generateBase62Code();

        // 2. Save to Database (MongoDB)
        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortCode(shortCode);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setClickCount(0);

        urlRepository.save(mapping);

        // 3. Cache the result in Redis immediately (Write-Through)
        // Key: "url:xyz123", Value: "https://google.com", TTL: 10 minutes
        redisTemplate.opsForValue().set("url:" + shortCode, originalUrl, 10, TimeUnit.MINUTES);

        return new ShortenResponse(BASE_URL + shortCode, originalUrl, 600);
    }

    // --- Core Logic: Retreival (The "Heavy" Backend Part) ---
    public String getOriginalUrl(String shortCode) {
        // Step 1: Check Redis Cache (Fastest)
        String cachedUrl = redisTemplate.opsForValue().get("url:" + shortCode);
        if (cachedUrl != null) {
            System.out.println("🔥 Cache HIT for code: " + shortCode);
            return cachedUrl;
        }

        // Step 2: If Cache Miss, Check MongoDB (Slower)
        System.out.println("🐢 Cache MISS. Querying DB for code: " + shortCode);
        UrlMapping mapping = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        // Step 3: Populate Redis for next time (Lazy Loading)
        redisTemplate.opsForValue().set("url:" + shortCode, mapping.getOriginalUrl(), 10, TimeUnit.MINUTES);

        // Step 4: Increment Analytics (Async ideally, but sync here for simplicity)
        mapping.setClickCount(mapping.getClickCount() + 1);
        urlRepository.save(mapping);

        return mapping.getOriginalUrl();
    }

    // Simple Base62 Logic (In prod, this needs to be collision-proof)
    private String generateBase62Code() {
        String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<7; i++) {
            int index = (int)(Math.random() * allowed.length());
            sb.append(allowed.charAt(index));
        }
        return sb.toString();
    }
}

// ------------------- 5. THE CONTROLLER (API Endpoints) -------------------
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*") // Allow React Frontend to access
class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> createShortUrl(@RequestBody ShortenRequest request) {
        return ResponseEntity.ok(urlService.shortenUrl(request.getOriginalUrl()));
    }
}

// ------------------- 6. THE REDIRECT CONTROLLER -------------------
@RestController
class RedirectController {

    @Autowired
    private UrlService urlService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.getOriginalUrl(shortCode);

        // Return 302 Found (Temporary Redirect)
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", originalUrl)
                .build();
    }
}