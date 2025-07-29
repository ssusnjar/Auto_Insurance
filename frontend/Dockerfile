FROM node:18-alpine AS build

WORKDIR /app

COPY vite-project/package.json vite-project/package-lock.json* ./

RUN npm install

COPY vite-project/ .

RUN npm run build

FROM nginx:stable-alpine

RUN rm -rf /usr/share/nginx/html/*

COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]







