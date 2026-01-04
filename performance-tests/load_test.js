import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuration for 1000 virtual users (simulating high load)
export const options = {
  stages: [
    { duration: '30s', target: 500 }, // Ramp up
    { duration: '1m', target: 1000 }, // Hold steady
    { duration: '30s', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(99)<50'], // Assert P99 latency is under 50ms
  },
};

export default function () {
  // Replace with your local or deployed URL
  const res = http.get('http://localhost:8080/api/redirect/xyz123');
  
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  
  sleep(1);
}
