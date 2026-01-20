/**
 * Phase 3: Smoke Test (기본 동작 확인)
 *
 * 목표: 10 VU (Virtual Users) × 1분간 기본 메시지 송수신 동작 확인
 * 예상 실행 시간: ~1분
 *
 * 실행 명령어:
 * k6 run tests/load/smoke-test.js
 */

import http from 'k6/http';
import ws from 'k6/ws';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// 커스텀 메트릭
const connectionTime = new Trend('ws_connection_time');
const messageDeliveryTime = new Trend('message_delivery_time');
const errorRate = new Rate('ws_error_rate');
const messagesSent = new Counter('messages_sent');
const messagesReceived = new Counter('messages_received');

// 테스트 설정
export const options = {
  scenarios: {
    smokeTest: {
      executor: 'constant-vus',
      vus: 10,                    // 10명 가상 사용자
      duration: '1m',             // 1분 지속
      gracefulStop: '10s',        // 종료 대기
    },
  },
  thresholds: {
    'ws_connection_time': ['p(95)<500', 'p(99)<1000'],
    'message_delivery_time': ['p(95)<200', 'p(99)<500'],
    'ws_error_rate': ['rate<0.05'],  // 에러율 5% 이하
  },
  ext: {
    loadimpact: {
      name: 'Realtime Chat - Smoke Test',
      tags: { testType: 'smoke', phase: 3 },
    },
  },
};

/**
 * WebSocket 클라이언트 구현
 */
class ChatClient {
  constructor(userId, username) {
    this.userId = userId;
    this.username = username;
    this.roomId = `room-${__VU % 5}`;  // VU별로 5개 방에 분산
    this.socket = null;
    this.connected = false;
    this.messageCount = 0;
  }

  connect() {
    const startTime = new Date();

    try {
      this.socket = new ws.RawSocket((socket) => {
        socket.on('open', () => {
          const connectTime = new Date() - startTime;
          connectionTime.add(connectTime);
          this.connected = true;

          // STOMP CONNECT 프레임 전송
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
            // STOMP 연결 성공
            this.subscribeToRoom(socket);
          }
        });

        socket.on('close', () => {
          this.connected = false;
        });

        socket.on('error', (error) => {
          errorRate.add(true);
        });
      });

      return this.socket;
    } catch (error) {
      errorRate.add(true);
      return null;
    }
  }

  subscribeToRoom(socket) {
    // STOMP SUBSCRIBE 프레임
    const subscribeFrame =
      `SUBSCRIBE\n` +
      `id:sub-${this.userId}\n` +
      `destination:/topic/room/${this.roomId}\n` +
      `\n` +
      `\0`;

    socket.send(subscribeFrame);
  }

  sendMessage(socket, content) {
    if (!this.connected || !socket) return false;

    const startTime = new Date().getTime();

    try {
      const message = {
        messageId: `msg-${this.messageCount++}`,
        roomId: this.roomId,
        userId: this.userId,
        username: this.username,
        content: content,
        timestamp: new Date().toISOString(),
        type: 'TEXT',
      };

      // STOMP SEND 프레임
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
}

/**
 * 메인 테스트 함수
 */
export default function (data) {
  // 사용자 정보
  const userId = `user-${__VU}-${new Date().getTime()}`;
  const username = `User_${__VU}`;
  const client = new ChatClient(userId, username);

  // 1. WebSocket 연결
  const socket = client.connect();

  check(socket, {
    'connected': (s) => s !== null,
  });

  sleep(1);

  // 2. 채팅방 입장
  if (socket) {
    client.joinRoom(socket);
    sleep(1);
  }

  // 3. 메시지 송수신 반복 (1분간)
  const testDuration = 60;  // 초
  const messageInterval = 5;  // 5초마다 메시지 전송
  const startTime = new Date().getTime();

  while ((new Date().getTime() - startTime) < testDuration * 1000) {
    // 메시지 전송
    if (socket) {
      const messages = [
        'Hello, Chat!',
        'Smoke test message',
        'Testing STOMP protocol',
        'Real-time messaging works!',
        'Latency check',
      ];

      const randomMessage = messages[Math.floor(Math.random() * messages.length)];
      client.sendMessage(socket, randomMessage);
    }

    sleep(messageInterval);
  }

  // 4. 정리
  if (socket) {
    const leaveFrame =
      `SEND\n` +
      `destination:/app/chat.leave\n` +
      `\n` +
      `{"roomId":"${client.roomId}","userId":"${userId}","eventType":"USER_LEFT"}\n` +
      `\0`;

    socket.send(leaveFrame);
    socket.close();
  }
}

/**
 * 테스트 요약 (콘솔 출력)
 */
export function teardown(data) {
  console.log('=== SMOKE TEST SUMMARY ===');
  console.log(`Total messages sent: ${messagesSent.value || 0}`);
  console.log(`Error rate: ${(errorRate.value * 100 || 0).toFixed(2)}%`);
}
