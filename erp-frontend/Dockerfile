############################
# 1) deps: 의존성 설치 (캐시)
############################
FROM node:20-alpine AS deps
WORKDIR /app

# package*만 먼저 복사 → 의존성 레이어 캐시화
COPY package.json package-lock.json ./

# NPM 캐시를 BuildKit 캐시로 재사용 (다음 빌드부터 빨라짐)
RUN --mount=type=cache,target=/root/.npm \
    npm install --no-audit --no-fund

############################
# 2) build: 실제 빌드
############################
FROM node:20-alpine AS build
WORKDIR /app

# 캐시된 node_modules 재사용
COPY --from=deps /app/node_modules ./node_modules
# 나머지 소스 복사
COPY . .

# NPM 캐시 재사용하며 빌드
RUN --mount=type=cache,target=/root/.npm \
    npm run build

############################
# 3) runtime: Nginx로 정적 서빙
############################
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# 한국시간 적용
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# SPA 라우팅/프록시 등 커스텀 설정이 있으면 함께 복사
# 없으면 아래 라인은 삭제 가능
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 정적 산출물만 복사 → 런타임 이미지 작고 빠름
COPY --from=build /app/dist ./

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]