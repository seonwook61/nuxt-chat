/**
 * Phase 3: Load Test (정상 부하 테스트)
 *
 * 목표: 500 VU 동시접속 부하 테스트
 * 프로필:
 * - Ramp-up: 0→500 VU (2분)
 * - Sustain: 500 VU (5분)
 * - Ramp-down: 500→0 VU (1분)
 * 예상 실행 시간: ~8분
 *
 * 성능 목표:
 * - p95 메시지 지연: < 100ms
 * - p99 메시지 지연: < 500ms
 * - 에러율: < 1%
 *
 * 실행 명령어:
 * k6 run tests/load/load-test.js
 *
 * 결과 분석:
 * k6 run tests/load/load-test.js -o json=results/load-test.json
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

// 테스트 설정
export const options = {
  scenarios: {
    loadTest: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 500 },   // Ramp-up: 0 → 500 VU (2분)
        { duration: '5m', target: 500 },   // Sustain: 500 VU (5분)
        { duration: '1m', target: 0 },     // Ramp-down: 500 → 0 VU (1분)
      ],
      gracefulStop: '30s',
    },
  },

  thresholds: {
    // 성능 임계값
    'ws_connection_time': ['p(95)<500', 'p(99)<1000'],
    'message_delivery_time': ['p(95)<100', 'p(99)<500'],
    'message_receive_time': ['p(95)<150', 'p(99)<600'],
    'ws_error_rate': ['rate<0.01'],  // 1% 이하

    // VU 수 확인
    'active_connections': ['value<501'],
  },

  ext: {
    loadimpact: {
      name: 'Realtime Chat - Load Test (500 VU)',
      tags: { testType: 'load', phase: 3 },
    },
  },
};

/**
 * 향상된 WebSocket 클라이언트
 */
class ChatClient {
  constructor(userId, username, roomId) {
    this.userId = userId;
    this.username = username;
    this.roomId = roomId;
    this.socket = null;
    this.connected = false;
    this.messageCount = 0;
    this.messageQueue = [];
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

          // STOMP CONNECT 프레임
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
            // STOMP 연결 성공 → 즉시 구독 및 입장
            this.subscribeToRoom(socket);
            this.joinRoom(socket);
          } else if (message.includes('MESSAGE')) {
            // 메시지 수신
            this.handleMessageReceived(message);
          }
        });

        socket.on('close', () => {
          this.connected = false;
          activeConnections.add(-1);
        });

        socket.on('error', (error) => {
          errorRate.add(true);
          console.error(`User ${this.userId} error:`, error);
        });
      });

      return this.socket;
    } catch (error) {
      errorRate.add(true);
      console.error('Connection failed:', error);
      return null;
    }
  }

  subscribeToRoom(socket) {
    const subscribeFrame =
      `SUBSCRIBE\n` +
      `id:sub-${this.userId}\n` +
      `destination:/topic/room/${this.roomId}\n` +
      `\n` +
      `\0`;

    socket.send(subscribeFrame);
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
        metadata: {},
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
    if (!this.connected || !socket) {
      this.messageQueue.push(content);  // 큐잉
      return false;
    }

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
        sendTime: startTime,  // 송신 시간 기록 (수신 지연 계산용)
      };

      const sendFrame =
        `SEND\n` +
        `destination:/app/chat.send\n` +
        `\n` +
        `${JSON.stringify(message)}\n` +
        `\0`;

      socket.send(sendFrame);
      messagesSent.add(1);

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
      // MESSAGE 프레임에서 body 추출
      const bodyStart = message.indexOf('\n\n') + 2;
      const bodyEnd = message.indexOf('\0');
      const body = message.substring(bodyStart, bodyEnd);

      const payload = JSON.parse(body);

      // 송신 시간과 현재 시간의 차이 계산
      if (payload.sendTime) {
        const receiveTime = new Date().getTime() - payload.sendTime;
        messageReceiveTime.add(receiveTime);
      }

      messagesReceived.add(1);
    } catch (error) {
      // 파싱 실패는 무시
    }
  }

  leaveRoom(socket) {
    if (!socket) return;

    try {
      const leaveFrame =
        `SEND\n` +
        `destination:/app/chat.leave\n` +
        `\n` +
        `{"roomId":"${this.roomId}","userId":"${this.userId}","eventType":"USER_LEFT"}\n` +
        `\0`;

      socket.send(leaveFrame);
    } catch (error) {
      // 무시
    }
  }

  close() {
    if (this.socket) {
      this.leaveRoom(this.socket);
      this.socket.close();
    }
  }
}

/**
 * 메인 테스트 함수
 */
export default function (data) {
  // 사용자 정보
  const userId = `user-${__VU}-${new Date().getTime()}`;
  const username = `LoadUser_${__VU}`;

  // VU를 10개 방에 분산 배치 (각 방에 ~50명씩)
  const roomNumber = Math.floor((__VU - 1) / 50) + 1;
  const roomId = `load-room-${roomNumber}`;

  const client = new ChatClient(userId, username, roomId);

  // 1. WebSocket 연결
  const socket = client.connect();

  check(socket, {
    'connection established': (s) => s !== null,
  });

  sleep(1);

  // 2. Sustain 기간 동안 메시지 송수신
  const sustainDuration = 5 * 60;  // 5분 (초)
  const messageInterval = 10;      // 10초마다 메시지
  const startTime = new Date().getTime();

  while ((new Date().getTime() - startTime) < sustainDuration * 1000) {
    group('message_exchange', () => {
      if (socket && client.connected) {
        const messages = [
          'Performance test message',
          'Load testing with k6',
          'WebSocket latency check',
          'Multi-user scenario',
          'Real-time messaging',
          'Concurrent connections',
          'Message throughput test',
          'System stability check',
        ];

        const randomMessage = messages[Math.floor(Math.random() * messages.length)];
        const success = client.sendMessage(socket, randomMessage);

        check(success, {
          'message sent': (s) => s === true,
        });
      }
    });

    sleep(messageInterval);
  }

  // 3. 정리
  client.close();

  check(client.connected, {
    'client disconnected': (c) => !c,
  });
}

/**
 * 테스트 완료 후 요약
 */
export function teardown(data) {
  console.log('\n=== LOAD TEST SUMMARY ===');
  console.log(`Total messages sent: ${messagesSent.value || 0}`);
  console.log(`Total messages received: ${messagesReceived.value || 0}`);
  console.log(`Error rate: ${(errorRate.value * 100 || 0).toFixed(2)}%`);
  console.log('========================\n');
}
