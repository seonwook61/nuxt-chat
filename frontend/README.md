# Chat Frontend

Real-time chat application built with Nuxt 3, supporting 1000+ concurrent users.

## Tech Stack

- Nuxt 3
- Vue 3 + TypeScript
- TailwindCSS
- Pinia (State Management)
- Socket.io-client (WebSocket)
- Vitest (Unit Tests)
- Playwright (E2E Tests)

## Getting Started

### Install Dependencies

```bash
npm install
# or
pnpm install
```

### Development

```bash
npm run dev
# or
pnpm dev
```

Visit http://localhost:3000

### Build

```bash
npm run build
# or
pnpm build
```

### Preview Production Build

```bash
npm run preview
# or
pnpm preview
```

## Environment Variables

Copy `.env.example` to `.env` and configure:

```
NUXT_PUBLIC_API_BASE=http://localhost:3001
NUXT_PUBLIC_WS_URL=http://localhost:3001
```

## Testing

### Unit Tests

```bash
npm run test
# or
pnpm test
```

### E2E Tests

```bash
npm run test:e2e
# or
pnpm test:e2e
```

## Docker

### Build Image

```bash
docker build -t chat-frontend .
```

### Run Container

```bash
docker run -p 3000:3000 -e NUXT_PUBLIC_API_BASE=http://backend:3001 chat-frontend
```

## Project Structure

```
app/
  pages/              # Page components
  components/         # Reusable components
    chat/            # Chat-specific components
  composables/        # Vue composables
  stores/            # Pinia stores
  types/             # TypeScript types
  plugins/           # Nuxt plugins
test/                # Test files
```

## Features (Roadmap)

- [x] Basic UI structure
- [ ] Socket.io connection (Phase 2)
- [ ] Real-time messaging
- [ ] Room management
- [ ] Message persistence
- [ ] Rate limiting
- [ ] Virtual scrolling
