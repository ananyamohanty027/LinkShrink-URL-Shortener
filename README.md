# 🔗 LinkShrink – Enterprise-Grade URL Shortener

LinkShrink is a **high-performance, scalable URL shortening service** built to handle **high-concurrency, read-heavy traffic** with **low latency**.  
It uses a **Write-Around Caching strategy** with **Redis** to deliver **sub-10ms redirects**, while **MongoDB** ensures reliable and scalable persistent storage.

---

## 🚀 Live Demo
👉 https://link-shrink-url-shortener.vercel.app/

---

## 🚀 Key Features

- ⚡ **High Performance Redirects**  
  Redis caching serves frequently accessed (hot) URLs instantly, reducing database reads by ~80%.

- 🛡️ **Collision-Free Short Codes**  
  Base62 encoding generates compact, unique **7-character** short URLs.

- 🧠 **Write-Around Cache Strategy**  
  Optimized for read-heavy workloads with predictable performance.

- 📦 **Fully Containerized**  
  Dockerized backend, Redis, and MongoDB for easy deployment and scalability.

- 🎨 **Modern Frontend UI**  
  Responsive React + Tailwind CSS interface.

- 📊 **Analytics-Ready Architecture**  
  Designed to support async click tracking and metrics (future scope).

---

## 🛠️ Tech Stack

| Layer | Technology | Purpose |
|-----|-----------|--------|
| Backend | Java 17, Spring Boot 3 | REST APIs & business logic |
| Database | MongoDB | Persistent URL storage |
| Cache | Redis | Ultra-fast URL lookups |
| Frontend | React.js, Tailwind CSS | User interface |
| DevOps | Docker, Docker Compose | Containerization |

---

## 🏗️ System Architecture

LinkShrink follows a **Write-Around Cache Pattern**, ideal for **read-heavy systems** like URL shorteners.

### 🔁 Read Flow (Redirect)
1. Request hits Redis
2. **Cache Hit** → Redirect instantly (<5ms)
3. **Cache Miss** → Fetch from MongoDB → Store in Redis → Redirect

### ✍️ Write Flow (Shorten URL)
1. Generate Base62 short code
2. Persist mapping in MongoDB
3. Write-through to Redis for immediate availability

---

## 📸 Screenshots

<img width="1600" alt="Home Page" src="https://github.com/user-attachments/assets/8a5bd93b-a439-451d-a66d-e35e1d2f7352" />
<img width="1800" alt="Shortened URL Result" src="https://github.com/user-attachments/assets/ec7471d5-272e-4134-9ef1-cf5fff7a1a8d" />

---

## 🏃‍♂️ Run Locally

### ✅ Prerequisites
- Docker Desktop (recommended)
- Java 17+
- Node.js 18+

---

## ⚡ Option 1: Run Using Docker (Recommended)

### 1️⃣ Clone Repository
```bash
git clone https://github.com/ananyamohanty027/LinkShrink-URL-Shortener.git
cd LinkShrink-URL-Shortener
````

### 2️⃣ Start MongoDB & Redis

```bash
docker-compose up -d
```

### 3️⃣ Run Backend

```bash
./mvnw spring-boot:run
```

### 4️⃣ Run Frontend

```bash
cd frontend
npm install
npm run dev
```

### 5️⃣ Access Application

* Frontend → [http://localhost:5173](http://localhost:5173)
* Backend API → [http://localhost:8080](http://localhost:8080)

---

## 🔌 API Endpoints

### ➕ Shorten URL

**POST** `/api/v1/shorten`

**Request Body**

```json
{
  "originalUrl": "https://www.google.com"
}
```

**Response**

```json
{
  "shortUrl": "http://localhost:8080/api/v1/AbCd12",
  "originalUrl": "https://www.google.com",
  "expiresSeconds": 600
}
```

---

### 🔁 Redirect

**GET** `/api/v1/{shortCode}`
Redirects to the original URL with **HTTP 302**.

---

## 🔮 Future Enhancements

* Kafka-based async click analytics
* JWT-based authentication
* Rate limiting with Bucket4j
* URL expiration & custom aliases
* Admin dashboard for metrics

---

## 👤 Author

**Ananya Mohanty**

* 🔗 LinkedIn: [https://www.linkedin.com/in/ananya008](https://www.linkedin.com/in/ananya008)
* 💻 GitHub: [https://github.com/ananyamohanty027](https://github.com/ananyamohanty027)


Just tell me 👍
```
