import http from 'k6/http';
import { check, sleep } from 'k6';

// TEST CONFIGURATION
export const options = {
  stages: [
    { duration: '10s', target: 100 },  // Ramp up to 100 users
    { duration: '30s', target: 500 },  // Stay at 500 users (High Load)
    { duration: '10s', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<50'], // 95% of requests must be faster than 50ms
    http_req_failed: ['rate<0.01'],  // Error rate must be < 1%
  },
};

export default function () {
  // Replace with your actual short code after running the app once
  const SHORT_CODE = "AbCd12"; 
  const BASE_URL = "http://localhost:8080"; // Or your Render URL

  // Test the Redirect (Read-Heavy)
  const res = http.get(`${BASE_URL}/${SHORT_CODE}`);

  check(res, {
    'status is 302 or 200': (r) => r.status === 302 || r.status === 200,
    'latency < 50ms': (r) => r.timings.duration < 50,
  });

  sleep(1);
}
