package com.ananya.urlshortener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;

// --- ENTITY ---
@Document(collection = "urls")
@Data
@NoArgsConstructor
@AllArgsConstructor
class UrlMapping {
    @Id
    private String id; // This will store the ShortCode directly for faster lookups
    private String originalUrl;
    private String shortCode;
    private LocalDateTime createdAt;
    private long clickCount;
}

// --- DTOs ---
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

// --- REPOSITORY ---
interface UrlRepository extends MongoRepository<UrlMapping, String> {
    // Standard MongoRepository methods are enough now since ID = ShortCode
}
