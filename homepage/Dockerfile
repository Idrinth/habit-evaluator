# CI stage: use --target=prebuilt with pre-built assets in build context
FROM nginx:alpine AS prebuilt
COPY build /usr/share/nginx/html
EXPOSE 80

# Local development: full multi-stage build (default)
FROM node:25-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
