# 🔗 LinkShrink - Enterprise-Grade URL Shortener

LinkShrink is a high-performance, scalable URL shortening service designed to handle high-concurrency read traffic with low latency. It utilizes a **Redis-First Caching Strategy** to ensure sub-100ms response times for frequently accessed links, backed by **MongoDB** for persistent storage and **Docker** for containerization.

### 🚀 **Live Demo:** [Live Link](https://link-shrink-url-shortener.vercel.app/)
---

## 🚀 Key Features
- ⚡ **High Performance:** Uses Redis caching to serve hot URLs instantly, reducing DB load by ~80%.
- 🛡️ **Collision-Free ID Generation:** SHA-256 Hashing + Base62 encoding for deterministic 7-character short codes.
- 🚦 **Rate Limiting:** Implements Token Bucket algorithm (Bucket4j) to prevent abuse and ensure API stability.
- 📊 **Real-Time Analytics:** Tracks click counts using Redis atomic counters with async write-behind persistence to MongoDB.
- 📦 **Containerized:** Fully Dockerized stack (Spring Boot, MongoDB, Redis).
- 🎨 **Modern UI:** Responsive React + Tailwind CSS frontend with QR Code generation.

---

## 🛠️ Tech Stack

| Component | Technology | Purpose |
|----------|------------|----------|
| Backend | Java 17, Spring Boot 3 | REST APIs, business logic |
| Database | MongoDB | NoSQL storage for URL mapping |
| Caching | Redis | High-speed in-memory retrieval & atomic counters |
| Security | Bucket4j | Rate limiting (Token Bucket Algorithm) |
| Frontend | React.js, Tailwind CSS | UI for shortening and viewing URLs |
| DevOps | Docker, Docker Compose, GitHub Actions | Containerization and CI/CD Pipeline |
| Testing | k6 | Load testing and performance benchmarking |

---

## 🏗️ System Architecture

The system uses a **Read-Through / Write-Behind Cache Pattern** optimized for speed and data consistency.

### 1. Read Request (Redirect)
- **Check Redis:** If key exists, return URL immediately (**~40ms latency**).
- **Cache Miss:** Fetch from MongoDB → Write to Redis (24h TTL) → Redirect.
- **Analytics:** Increment Redis counter instantly; asynchronously persist count to MongoDB.

### 2. Write Request (Shorten)
- Generate SHA-256 Hash -> Base62 Encode.
- Save to MongoDB.
- **Cold Start Optimization:** Does not pre-load cache to save memory (cache is populated on first access).

---

## 🚀 Performance & Scalability

The application was stress-tested using **k6** to simulate concurrent user traffic on a production deployment (Render Free Tier).

**Benchmark Results:**
- **Concurrent Users:** 50
- **Error Rate:** **0.00%** (High Availability)
- **Median Latency:** **40.6ms** (High Performance)
- **Throughput:** ~300 requests/minute handled without degradation.

### 📸 Load Test Proof

<img width="1191" height="667" alt="Screenshot 2026-01-04 212658" src="https://github.com/user-attachments/assets/cf1dd6bf-f630-4fa6-979a-20ed77560411" />

---

## 📸 Application Screenshots
<img width="1619" height="944" alt="Screenshot 2025-12-01 210554" src="https://github.com/user-attachments/assets/8a5bd93b-a439-451d-a66d-e35e1d2f7352" />
<img width="1887" height="936" alt="Screenshot 2026-01-04 124742" src="https://github.com/user-attachments/assets/ec7471d5-272e-4134-9ef1-cf5fff7a1a8d" />

---

## 🏃‍♂️ How to Run Locally

### **Prerequisites**
- Docker Desktop (Recommended)
- Java 17+ (if running backend manually)
- Node.js (if running frontend manually)

## **Option 1: Run with Docker (Fastest)**

### 1. Clone the repository
```bash
git clone [https://github.com/ananyamohanty027/LinkShrink-URL-Shortener.git](https://github.com/ananyamohanty027/LinkShrink-URL-Shortener.git)
cd LinkShrink-URL-Shortener

```

### 2. Start Redis & MongoDB

```bash
docker-compose up -d

```

### 3. Run Backend

```bash
./mvnw spring-boot:run

```

### 4. Run Frontend

```bash
cd frontend
npm install
npm run dev

```

### 5. Access the app

* Frontend → **http://localhost:5173**
* Backend API → **http://localhost:8080**

---

## 🔌 API Endpoints

### **1. Shorten a URL**

**POST** `/api/v1/shorten`

**Body:**

```json
{
  "originalUrl": "[https://www.google.com](https://www.google.com)"
}

```

### **2. Redirect**

**GET** `/{shortCode}`
Redirects user to the original URL (HTTP 302).

### **3. Get Analytics**

**GET** `/api/v1/analytics/{shortCode}`
Returns click counts from Redis/DB.

---

## 🔮 Future Improvements

* Kafka-based async click tracking (for massive scale)
* JWT-based user authentication
* Custom alias selection

---

## 👤 Author

**Ananya Mohanty**

🔗 **LinkedIn:** [https://www.linkedin.com/in/ananya008](https://www.linkedin.com/in/ananya008)

💻 **GitHub:** [https://github.com/ananyamohanty027](https://github.com/ananyamohanty027)
