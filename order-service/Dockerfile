# order-service/Dockerfile
 
# Stage 1: Install all deps (including dev)
FROM node:20-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci --silent
 
# Stage 2: Prune to production deps only
FROM node:20-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm prune --production
 
# Stage 3: Minimal runtime
FROM node:20-alpine
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=builder --chown=app:app /app/node_modules ./node_modules
COPY --from=builder --chown=app:app /app/src ./src
COPY --from=builder --chown=app:app /app/package.json .
 
EXPOSE 3000
CMD ["node", "src/index.js"]


