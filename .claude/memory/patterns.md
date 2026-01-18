# Code Patterns

## 백엔드 패턴

### API 응답 형식 (Spring Boot 예시)
```java
// 성공 응답
public record ApiResponse<T>(T data, Map<String, Object> meta) {}

public record ErrorResponse(String code, String message, List<String> details) {}

// 컨트롤러 메서드 예시
@GetMapping("/health")
public ResponseEntity<ApiResponse<String>> health() {
    ApiResponse<String> response = new ApiResponse<>("ok", Map.of("timestamp", Instant.now()));
    return ResponseEntity.ok(response);
}
```

### 서비스 레이어 패턴
```java
@Service
@RequiredArgsConstructor
public class ChatService {

    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;
    private final RedisTemplate<String, List<ChatMessage>> redisTemplate;

    public void publishMessage(ChatMessage message) {
        // Kafka 토픽으로 메시지 전송
        kafkaTemplate.send("chat-messages", message.roomId(), message);
        // Redis에 최근 메시지 캐시
        String key = "room:" + message.roomId() + ":recent";
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, Duration.ofMinutes(10));
    }
}
```

## 프론트엔드 패턴

### Nuxt 컴포넌트 구조
```text
app/
  components/
    ChatMessage.vue
    ChatInput.vue
    ChatRoom.vue
  composables/
    useChat.ts
  pages/
    chat/
      index.vue
```

### Pinia 스토어 패턴
```typescript
export const useChatStore = defineStore('chat', {
  state: () => ({
    messages: [] as ChatMessage[],
    isConnected: false,
  }),
  actions: {
    async connect(roomId: string) {
      // socket.io-client 연결 초기화 및 이벤트 리스너 설정
    },
    async sendMessage(content: string) {
      // 서버로 메시지 전송
    },
  },
});
```
