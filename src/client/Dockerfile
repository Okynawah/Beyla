FROM node:24-alpine3.21 AS builder

WORKDIR /code

COPY package*.json .

RUN npm install

COPY . .

RUN rm proxy.conf.json

RUN npm run build

FROM nginx:1.28.0

EXPOSE 80

COPY --from=builder /code/dist/client/browser /usr/share/nginx/html