# DevSo — Developers' Social Networking Service

개발자들이 **게시글(SNS)**을 작성/공유하고, **팀원 모집(Recruit)**을 올리며, **프로필/팔로우/채팅**으로 소통할 수 있는 웹 서비스입니다.

## 개요
- **프로젝트 목적**: 개발자 커뮤니티형 SNS + 팀원 모집 플랫폼을 하나의 서비스로 통합
- **핵심 키워드**: 게시글/댓글(대댓글)·멘션, 팔로우 피드, 트렌딩, 조회수 정책, 소프트 삭제, 팀원모집 필터/북마크, 실시간 채팅

## 기술 스택
### Frontend
- ![React](https://img.shields.io/badge/React-19.2.0-61DAFB?logo=react&logoColor=000)
- ![Vite](https://img.shields.io/badge/Vite-7.2.4-646CFF?logo=vite&logoColor=fff)
- ![React Router](https://img.shields.io/badge/React_Router-7.11.0-CA4245?logo=reactrouter&logoColor=fff)
- ![Axios](https://img.shields.io/badge/Axios-1.13.2-5A29E4)
- ![MUI](https://img.shields.io/badge/MUI-7.3.6-007FFF?logo=mui&logoColor=fff)
- ![Tiptap](https://img.shields.io/badge/Tiptap-3.14.0-000000)
- ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.1.18-06B6D4?logo=tailwindcss&logoColor=fff)
- ![SweetAlert2](https://img.shields.io/badge/SweetAlert2-11.26.17-6C63FF)

### Backend
- ![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=fff)
- ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.8-6DB33F?logo=springboot&logoColor=fff)
- ![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?logo=springsecurity&logoColor=fff)
- ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-3.5.8-6DB33F)
- ![MySQL](https://img.shields.io/badge/MySQL-8.x-4479A1?logo=mysql&logoColor=fff)
- ![JWT](https://img.shields.io/badge/JWT-jjwt_0.13.0-000000?logo=jsonwebtokens&logoColor=fff)
- ![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-1f2937)
- ![Swagger](https://img.shields.io/badge/Swagger-springdoc_2.8.3-85EA2D?logo=swagger&logoColor=000)

### 선택 이유(간단)
- **React + Vite**: 빠른 개발/빌드, 컴포넌트 기반 UI 구성에 적합
- **Spring Boot + JPA**: CRUD/도메인 모델링/트랜잭션 처리에 안정적
- **Spring Security + JWT**: Stateless 인증/인가 구성에 적합
- **MySQL**: 관계형 데이터(게시글/댓글/멘션/팔로우/북마크 등) 모델링에 적합

## 주요 기능
### SNS(게시글)
- **마크다운/에디터**: Tiptap 기반 작성, 코드블록/인라인코드 렌더링
- **이미지 처리**: 웹에서 복사/붙여넣기 시 서버 업로드 후 URL 치환
- **조회수 정책**: 24시간 기준(로그인: userId / 비로그인: IP+쿠키) 1회 카운트
- **트렌딩/피드**: 트렌딩(전체기간), 피드(팔로잉 사용자 게시글)
- **댓글/대댓글(2-depth)** + **멘션 저장(comment_mentions)**
- **소프트 삭제**: `deletedAt` 기반 삭제/조회 필터링

### 팀원 모집(Recruit)
- 타입/포지션/스택/진행방식/모집중 여부 등 **필터링**
- **북마크**
- 모집글 상세/댓글

### 유저/소통
- 프로필 조회/수정, 팔로우/언팔로우
- 채팅(웹소켓/위젯)

### 대시보드
- `/dashboard`에서 게시글/모집글/유저를 샘플 회전목마 형태로 미리보기 및 이동

## 배포 URL
- (추가 예정)

## 프로젝트 실행 방법
### 1) Backend 실행
```bash
cd devSO-back
./gradlew bootRun
```
- 기본 포트: `8080`
- Swagger: `http://localhost:8080/swagger-ui.html`

### 2) Frontend 실행
```bash
cd devSO-front
npm install
npm run dev
```
- 기본 포트: `5173`
- 기본 홈: `http://localhost:5173/` → 대시보드

## 환경 변수
### Frontend (`devSO-front`)
- `VITE_API_URL` (기본값: `http://localhost:8080`)
- `VITE_KAKAO_CLIENT_ID`
- `VITE_KAKAO_REDIRECT_URI` (예: `http://localhost:5173/oauth/kakao/callback`)

### Backend (`devSO-back`)
- `KAKAO_CLIENT_ID`
- `KAKAO_CLIENT_SECRET`
- `GEMINI_API_KEY`

> DB 설정은 `devSO-back/src/main/resources/application.properties`의 `spring.datasource.*`를 사용합니다.

## 기타
### 성과/학습
- 테마 통일(Recruit/SNS/Auth/Navbar/Dashboard) 및 CSS 우선순위/구조 개선
- 조회수/좋아요/댓글/멘션/소프트삭제 등 “정책 기반” 기능을 전/후단 동기화
- 붙여넣기(이미지/HTML/마크다운) 처리와 에디터 확장(Tiptap Extension) 경험

### 아쉬운 점 / 향후 계획
- “인기 유저” 전용 API(팔로워 수 상위) 추가
- 대시보드 추천 알고리즘(태그/관심사 기반) 및 캐싱
- e2e 테스트/CI, 배포 파이프라인 구성

### 연락처
- (추가 예정)


