FROM node:20-alpine AS base

# Build stage
FROM base AS builder
WORKDIR /app

# Copy package files
COPY package.json pnpm-lock.yaml* package-lock.json* yarn.lock* ./

# Install dependencies
RUN corepack enable && \
    if [ -f pnpm-lock.yaml ]; then pnpm install --frozen-lockfile; \
    elif [ -f package-lock.json ]; then npm ci; \
    elif [ -f yarn.lock ]; then yarn install --frozen-lockfile; \
    else npm install; fi

# Copy app files
COPY . .

# Build the application
RUN if [ -f pnpm-lock.yaml ]; then pnpm run build; \
    else npm run build; fi

# Production stage
FROM base AS runner
WORKDIR /app

# Copy built app from builder
COPY --from=builder /app/.output /app/.output

# Set environment
ENV NODE_ENV=production
ENV NUXT_HOST=0.0.0.0
ENV NUXT_PORT=3000

EXPOSE 3000

# Start the application
CMD ["node", ".output/server/index.mjs"]
