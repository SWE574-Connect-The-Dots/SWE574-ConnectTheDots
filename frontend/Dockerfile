FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install --legacy-peer-deps
COPY . .
RUN npm run build

ARG API_URL=http://localhost:8000/api
RUN sed -i "s|PLACEHOLDER_API_URL|${API_URL}|g" /app/dist/env.js

# Serve static files
FROM nginx:1.28-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80