/**
 * Phase 3: Stress Test (극한 부하 테스트)
 *
 * 목표: 1000 VU 동시접속 극한 부하 테스트
 * 프로필:
 * - Ramp-up: 0→1000 VU (2분)
 * - Sustain: 1000 VU (10분)
 * - Ramp-down: 1000→0 VU (1분)
 * 예상 실행 시간: ~13분
 *
 * 성능 목표 (극한 상황):
 * - p95 메시지 지연: < 250ms
 * - p99 메시지 지연: < 1000ms
 * - 에러율: < 5%
 * - 메모리: < 4GB
 * - CPU: < 90%
 *
 * 실행 명령어:
 * k6 run tests/load/stress-test.js
 *
 * 주의: 이 테스트는 시스템에 높은 부하를 줍니다.
 * 미리 모니터링 도구를 준비하세요 (Task Manager, htop 등).
 */

import http from 'k6/http';
import ws from 'k6/ws';
import { check, sleep, group } from 'k6';
import { Trend, Rate, Counter, Gauge } from 'k6/metrics';

// 커스텀 메트릭
const connectionTime = new Trend('ws_connection_time');
const messageDeliveryTime = new Trend('message_delivery_time');
const messageReceiveTime = new Trend('message_receive_time');
const errorRate = new Rate('ws_error_rate');
const messagesSent = new Counter('messages_sent');
const messagesReceived = new Counter('messages_received');
const activeConnections = new Gauge('active_connections');
const connectionErrors = new Counter('connection_errors');

// 테스트 설정
export const options = {
  scenarios: {
    stressTest: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 1000 },   // Ramp-up: 0 → 1000 VU (2분)
        { duration: '10m', target: 1000 },  // Sustain: 1000 VU (10분)
        { duration: '1m', target: 0 },      // Ramp-down: 1000 → 0 VU (1분)
      ],
      gracefulStop: '30s',
    },
  },

  thresholds: {
    // 극한 상황에서의 임계값
    'ws_connection_time': ['p(95)<1000', 'p(99)<2000'],
    'message_delivery_time': ['p(95)<250', 'p(99)<1000'],
    'message_receive_time': ['p(95)<300', 'p(99)<1500'],
    'ws_error_rate': ['rate<0.05'],  // 5% 이하

    'active_connections': ['value<1001'],
  },

  ext: {
    loadimpact: {
      name: 'Realtime Chat - Stress Test (1000 VU)',
      tags: { testType: 'stress', phase: 3 },
    },
  },

  // k6 Cloud 설정 (선택사항)
  // tags: {
  //   name: 'Stress Test - 1000 VU',
  // },
};

/**
 * 스트레스 테스트용 WebSocket 클라이언트
 */
class StressTestClient {
  constructor(userId, username, roomId) {
    this.userId = userId;
    this.username = username;
    this.roomId = roomId;
    this.socket = null;
    this.connected = false;
    this.messageCount = 0;
    this.lastMessageTime = 0;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 3;
  }

  connect() {
    const startTime = new Date();

    try {
      this.socket = new ws.RawSocket((socket) => {
        socket.on('open', () => {
          const connectTime = new Date() - startTime;
          connectionTime.add(connectTime);
          this.connected = true;
          activeConnections.add(1);
          this.reconnectAttempts = 0;

          // STOMP CONNECT
          const connectFrame =
            `CONNECT\n` +
            `accept-version:1.0,1.1,1.2\n` +
            `heart-beat:4000,4000\n` +
            `\n` +
            `\0`;

          socket.send(connectFrame);
        });

        socket.on('message', (message) => {
          if (message.includes('CONNECTED')) {
            this.subscribeToRoom(socket);
            this.joinRoom(socket);
          } else if (message.includes('MESSAGE')) {
            this.handleMessageReceived(message);
          }
        });

        socket.on('close', () => {
          this.connected = false;
          activeConnections.add(-1);

          // 자동 재연결 시도
          if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            sleep(1);  // 1초 대기 후 재연결
            this.connect();
          }
        });

        socket.on('error', (error) => {
          errorRate.add(true);
          connectionErrors.add(1);
        });
      });

      return this.socket;
    } catch (error) {
      errorRate.add(true);
      connectionErrors.add(1);
      return null;
    }
  }

  subscribeToRoom(socket) {
    try {
      const subscribeFrame =
        `SUBSCRIBE\n` +
        `id:sub-${this.userId}\n` +
        `destination:/topic/room/${this.roomId}\n` +
        `\n` +
        `\0`;

      socket.send(subscribeFrame);
    } catch (error) {
      errorRate.add(true);
    }
  }

  joinRoom(socket) {
    try {
      const event = {
        eventId: `evt-${this.userId}-${Date.now()}`,
        roomId: this.roomId,
        eventType: 'USER_JOINED',
        userId: this.userId,
        username: this.username,
        timestamp: new Date().toISOString(),
        metadata: { stressTest: true },
      };

      const sendFrame =
        `SEND\n` +
        `destination:/app/chat.join\n` +
        `\n` +
        `${JSON.stringify(event)}\n` +
        `\0`;

      socket.send(sendFrame);
    } catch (error) {
      errorRate.add(true);
    }
  }

  sendMessage(socket, content) {
    if (!this.connected || !socket) return false;

    try {
      const startTime = new Date().getTime();

      const message = {
        messageId: `msg-${this.userId}-${this.messageCount++}`,
        roomId: this.roomId,
        userId: this.userId,
        username: this.username,
        content: content,
        timestamp: new Date().toISOString(),
        type: 'TEXT',
        sendTime: startTime,
      };

      const sendFrame =
        `SEND\n` +
        `destination:/app/chat.send\n` +
        `\n` +
        `${JSON.stringify(message)}\n` +
        `\0`;

      socket.send(sendFrame);
      messagesSent.add(1);
      this.lastMessageTime = startTime;

      const deliveryTime = new Date().getTime() - startTime;
      messageDeliveryTime.add(deliveryTime);

      return true;
    } catch (error) {
      errorRate.add(true);
      return false;
    }
  }

  handleMessageReceived(message) {
    try {
      const bodyStart = message.indexOf('\n\n') + 2;
      const bodyEnd = message.indexOf('\0');
      const body = message.substring(bodyStart, bodyEnd);

      const payload = JSON.parse(body);

      if (payload.sendTime) {
        const receiveTime = new Date().getTime() - payload.sendTime;
        messageReceiveTime.add(receiveTime);
      }

      messagesReceived.add(1);
    } catch (error) {
      // 파싱 실패
    }
  }

  close() {
    if (this.socket) {
      try {
        const leaveFrame =
          `SEND\n` +
          `destination:/app/chat.leave\n` +
          `\n` +
          `{"roomId":"${this.roomId}","userId":"${this.userId}","eventType":"USER_LEFT"}\n` +
          `\0`;

        this.socket.send(leaveFrame);
      } catch (error) {
        // 무시
      }

      this.socket.close();
    }
  }
}

/**
 * 메인 테스트 함수
 */
export default function (data) {
  // 사용자 정보
  const userId = `stress-${__VU}-${new Date().getTime()}`;
  const username = `StressUser_${__VU}`;

  // 1000명을 20개 방에 분산 (각 방에 50명씩)
  const roomNumber = Math.floor((__VU - 1) / 50) + 1;
  const roomId = `stress-room-${roomNumber}`;

  const client = new StressTestClient(userId, username, roomId);

  // 1. 연결 시도
  const socket = client.connect();

  check(socket, {
    'stress test connected': (s) => s !== null,
  });

  if (!socket) {
    connectionErrors.add(1);
    return;  // 연결 실패 시 종료
  }

  sleep(1);

  // 2. 극한 부하 지속 (10분)
  const sustainDuration = 10 * 60;  // 10분 (초)
  const messageInterval = 15;       // 15초마다 메시지 전송
  const startTime = new Date().getTime();

  let messagesSentThisVU = 0;

  while ((new Date().getTime() - startTime) < sustainDuration * 1000) {
    group('stress_message_exchange', () => {
      if (socket && client.connected) {
        const stressMessages = [
          `Stress test msg ${messagesSentThisVU}`,
          'Load peak scenario',
          'System under pressure',
          'Performance benchmark',
          'Concurrent load test',
          'Throughput measurement',
          '1000 users connected',
          'Latency analysis',
        ];

        const message = stressMessages[messagesSentThisVU % stressMessages.length];
        const success = client.sendMessage(socket, message);

        messagesSentThisVU++;

        check(success, {
          'stress message sent': (s) => s === true,
        });
      }
    });

    sleep(messageInterval);
  }

  // 3. 정리
  client.close();

  check(!client.connected, {
    'stress test cleaned up': (c) => c === true,
  });
}

/**
 * 테스트 요약
 */
export function teardown(data) {
  console.log('\n=== STRESS TEST SUMMARY ===');
  console.log(`Total messages sent: ${messagesSent.value || 0}`);
  console.log(`Total messages received: ${messagesReceived.value || 0}`);
  console.log(`Connection errors: ${connectionErrors.value || 0}`);
  console.log(`Error rate: ${(errorRate.value * 100 || 0).toFixed(2)}%`);
  console.log('============================\n');
}
