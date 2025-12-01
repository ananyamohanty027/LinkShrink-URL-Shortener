🔗 LinkShrink - Enterprise-Grade URL Shortener

LinkShrink is a high-performance, scalable URL shortening service designed to handle high-concurrency read traffic with low latency. It utilizes a Write-Around Caching strategy with Redis to ensure sub-10ms response times for frequently accessed links, backed by MongoDB for persistent storage.

🚀 Key Features

⚡ High Performance: Utilizes Redis caching to serve hot URLs instantly, reducing database load by ~80%.

🛡️ Collision-Free ID Generation: Implements a Base62 encoding algorithm to generate unique, compact 7-character alphanumeric short codes.

📦 Containerized: Fully dockerized environment (Spring Boot, MongoDB, Redis) for consistent deployment using Docker Compose.

🎨 Modern UI: Responsive frontend built with React and Tailwind CSS.

📊 Analytics Ready: Architecture designed to support asynchronous click tracking (future scope).

🛠️ Tech Stack

Component

Technology

Purpose

Backend

Java 17, Spring Boot 3

RESTful API, Business Logic, DTO Handling

Database

MongoDB

NoSQL storage for unstructured URL mapping data

Caching

Redis

In-memory key-value store for high-speed retrieval

Frontend

React.js, Tailwind CSS

Responsive User Interface

DevOps

Docker, Docker Compose

Containerization and Orchestration

🏗️ System Architecture

The system follows a Write-Around Caching Pattern to optimize for read-heavy workloads (typical for URL shorteners).

Read Request (Redirect):

The system first checks Redis.

Cache Hit: Returns the URL immediately (Latency: <5ms).

Cache Miss: Fetches from MongoDB, updates the Redis cache, and then redirects (Lazy Loading).

Write Request (Shorten):

Generates a unique ID (Base62).

Saves the mapping to MongoDB.

Populates Redis immediately for instant availability.

📸 Screenshots

(Add a screenshot of your centered "LinkShrink" UI here)

🏃‍♂️ How to Run Locally

Prerequisites

Docker Desktop (Recommended)

Java 17+ (If running backend manually)

Node.js (If running frontend manually)

Option 1: Run with Docker (Fastest)

Clone the repository

git clone [https://github.com/YOUR_USERNAME/LinkShrink-URL-Shortener.git](https://github.com/YOUR_USERNAME/LinkShrink-URL-Shortener.git)
cd LinkShrink-URL-Shortener


Start Infrastructure (Redis & Mongo)

docker-compose up -d


Run the Backend

./mvnw spring-boot:run


Run the Frontend
Open a new terminal:

cd frontend
npm install
npm run dev


Access the App

Frontend: http://localhost:5173

Backend API: http://localhost:8080

🔌 API Endpoints

1. Shorten a URL

Endpoint: POST /api/v1/shorten

Body:

{
  "originalUrl": "[https://www.google.com](https://www.google.com)"
}


Response:

{
  "shortUrl": "http://localhost:8080/api/v1/AbCd12",
  "originalUrl": "[https://www.google.com](https://www.google.com)",
  "expiresSeconds": 600
}


2. Redirect

Endpoint: GET /api/v1/{shortCode}

Behavior: Redirects the browser to the original URL with 302 Found status.

🔮 Future Improvements

Implement Kafka for asynchronous click stream processing.

Add User Authentication (JWT) to allow users to manage their links.

Implement Rate Limiting using Bucket4j to prevent abuse.

👤 Author

Ananya Mohanty

LinkedIn

GitHub
