# Project Memory

## 기본 정보
- 프로젝트명: realtime-chat-1000 (실시간 채팅 시스템)
- 기술 스택: Spring Boot + Nuxt 3 + Kafka + Redis
- 시작일: 2026-01-18

## 아키텍처
- 백엔드: Spring Boot (Java 17+) + Kafka (메시징) + Redis (캐시 및 Pub/Sub) + WebSocket API (socket.io 호환)
- 프론트엔드: Nuxt 3 + TypeScript + TailwindCSS + Pinia + socket.io-client
- 데이터베이스: Redis Streams(채팅 로그), 필요 시 PostgreSQL(영구 저장)

## 특이사항
- 인증: JWT 기반 간단 인증(옵션) 또는 무인증(라이브 채팅 전용)
- 외부 서비스: Kafka 클러스터와 Redis 클러스터 (Render 또는 자체 호스팅)
- 캐싱: 최근 메시지 10분 TTL, 사용자 상태 정보 5분 TTL
