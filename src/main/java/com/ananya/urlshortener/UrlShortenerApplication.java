package com.ananya.urlshortener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

// ------------------- 2. THE DTOs (Data Transfer Objects) -------------------
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

// ------------------- 4. THE SERVICE (Business Logic) -------------------
@Service
class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private StringRedisTemplate redisTemplate; // Redis for Caching

    // UPDATED: Dynamically inject URL from properties/env, default to localhost
    @Value("${app.baseUrl:http://localhost:8080/}")
    private String baseUrl;

    // --- Core Logic: Shortening ---
    public ShortenResponse shortenUrl(String originalUrl) {
        // 1. Generate a generic unique ID
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

        // Use the dynamic baseUrl here
        return new ShortenResponse(baseUrl + shortCode, originalUrl, 600);
    }

    // --- Core Logic: Retrieval (The "Heavy" Backend Part) ---
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

        // Step 4: Increment Analytics
        mapping.setClickCount(mapping.getClickCount() + 1);
        urlRepository.save(mapping);

        return mapping.getOriginalUrl();
    }

    // Simple Base62 Logic
    private String generateBase62Code() {
        String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            int index = (int) (Math.random() * allowed.length());
            sb.append(allowed.charAt(index));
        }
        return sb.toString();
    }
}

// ------------------- 5. THE CONTROLLER (API Endpoints) -------------------
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*") // Allows Frontend (React) to access this API
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
@CrossOrigin(origins = "*")
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
