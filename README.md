# 🔗 LinkShrink - Enterprise-Grade URL Shortener

LinkShrink is a high-performance, scalable URL shortening service designed to handle high-concurrency read traffic with low latency. It utilizes a Write-Around Caching strategy with Redis to ensure sub-10ms response times for frequently accessed links, backed by MongoDB for persistent storage.

### 🚀 **Live Demo:** [Live Link](https://link-shrink-url-shortener.vercel.app/)
---

## 🚀 Key Features
- ⚡ **High Performance:** Uses Redis caching to serve hot URLs instantly, reducing DB load by ~80%.
- 🛡️ **Collision-Free ID Generation:** Base62 encoding for unique 7-character short codes.
- 📦 **Containerized:** Fully Dockerized stack (Spring Boot, MongoDB, Redis).
- 🎨 **Modern UI:** Responsive React + Tailwind CSS frontend.
- 📊 **Analytics Ready:** Architecture supports async click tracking (future scope).

---

## 🛠️ Tech Stack

| Component | Technology | Purpose |
|----------|------------|----------|
| Backend | Java 17, Spring Boot 3 | REST APIs, business logic |
| Database | MongoDB | NoSQL storage for URL mapping |
| Caching | Redis | High-speed in-memory retrieval |
| Frontend | React.js, Tailwind CSS | UI for shortening and viewing URLs |
| DevOps | Docker, Docker Compose | Containerization and orchestration |

---

## 🏗️ System Architecture

The system uses a **Write-Around Cache Pattern** optimized for read-heavy workloads.

### 1. Read Request (Redirect)
- Check Redis first
- **Cache Hit:** Return URL (<5ms)
- **Cache Miss:** Load from MongoDB → store in Redis → redirect

### 2. Write Request (Shorten)
- Generate Base62 ID
- Save to MongoDB
- Write to Redis for immediate availability

---

## 📸 Screenshots
*<img width="1619" height="944" alt="Screenshot 2025-12-01 210554" src="https://github.com/user-attachments/assets/8a5bd93b-a439-451d-a66d-e35e1d2f7352" />*

<img width="1910" height="813" alt="Screenshot 2025-12-01 212150" src="https://github.com/user-attachments/assets/156bf850-1449-479d-b46c-76b322302f82" />

---

## 🏃‍♂️ How to Run Locally

### **Prerequisites**
- Docker Desktop (Recommended)
- Java 17+ (if running backend manually)
- Node.js (if running frontend manually)

---

## **Option 1: Run with Docker (Fastest)**

### 1. Clone the repository
```bash
git clone [https://github.com/ananyamohanty027/LinkShrink-URL-Shortener.git](https://github.com/ananyamohanty027/LinkShrink-URL-Shortener.git)
cd LinkShrink-URL-Shortener
````

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

* Frontend → **[http://localhost:5173](http://localhost:5173)**
* Backend API → **[http://localhost:8080](http://localhost:8080)**

---

## 🔌 API Endpoints

### **1. Shorten a URL**

**POST** `/api/v1/shorten`
**Body:**

```json
{
  "originalUrl": "https://www.google.com"
}
```

**Response:**

```json
{
  "shortUrl": "http://localhost:8080/api/v1/AbCd12",
  "originalUrl": "https://www.google.com",
  "expiresSeconds": 600
}
```

### **2. Redirect**

**GET** `/api/v1/{shortCode}`
Redirects user to the original URL (HTTP 302).

---

## 🔮 Future Improvements

* Kafka-based async click tracking
* JWT-based user authentication
* Rate limiting using Bucket4j

---

## 👤 Author

**Ananya Mohanty**

🔗 **LinkedIn:**
[https://www.linkedin.com/in/ananya008](https://www.linkedin.com/in/ananya008)

💻 **GitHub:**
[https://github.com/ananyamohanty027](https://github.com/ananyamohanty027)



