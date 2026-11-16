# 공채커밍 (GongchaeComing)

> 공공기관 취업 준비생을 위한 맞춤형 채용 정보 제공 서비스

<img width="677" height="571" alt="스크린샷 2026-11-16 163831" src="https://github.com/user-attachments/assets/d9ad6571-012d-42a5-b871-7bb8e4cfa3b5" />

공공데이터포털의 **ALIO(공공기관 경영정보 공개시스템) 채용 API**로 공공기관 채용 공고를 모아, 조건별 검색 · 통계 · 관심 공고 관리 · 카카오톡 알림을 한곳에서 제공합니다.

<br>

## 주요 기능

| 기능 | 설명 |
| --- | --- |
| **채용 공고 검색** | 키워드, 기관, 채용 상태, 지역, 채용 구분, 고용 형태, NCS 직무 분류 등으로 필터링 및 페이지 조회 |
| **채용 통계** | 월별 · 연도별 공고 수, NCS 직무별, 기관별, 채용 구분별, 고용 형태별, 지역별 통계 |
| **공고 자동 동기화** | 서버 시작 시 시드 데이터 적재 후 ALIO API와 동기화, 이후 10분마다 신규 공고 수집 (진행 상태는 SSE로 실시간 표시) |
| **관심 공고** | 공고를 즐겨찾기에 추가 · 삭제하고 마이페이지에서 모아보기 |
| **카카오톡 알림** | 카카오 계정 연동 후 ① 희망 조건에 맞는 신규 공고 알림 ② 지정한 시각에 관심 공고 리마인드 |
| **회원 관리** | 이메일 회원가입 · 로그인, 아이디 찾기, 이메일 인증코드 기반 비밀번호 재설정, 닉네임 · 비밀번호 변경, 희망 조건 설정 |
| **알림 내역** | 발송된 알림의 성공 · 실패 이력 조회 |

<br>

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.5, Spring Security, Spring Data JPA, Spring Validation, Spring Mail |
| View | Thymeleaf, Vanilla JS, CSS |
| Database | H2 (MySQL 모드) |
| Build | Gradle |
| 외부 API | 공공데이터포털 ALIO 채용 / 공공기관 목록 API, Kakao 로그인 · 메시지 API |
| Etc | Lombok, JUnit 5 |

<br>

## 프로젝트 구조

```
src/main/java/com/gongchae/gongchae_coming
├── alio           # ALIO 채용 공고 · 공공기관 연동, 검색, 통계, 동기화
│   ├── client       # ALIO API 클라이언트
│   ├── controller   # 채용 공고 API · 화면 컨트롤러
│   ├── domain       # AlioRecruitment, PublicInstitution, 동기화 상태
│   ├── scheduler    # 10분 주기 공고 동기화
│   └── service      # 검색 · 통계 · 시드 데이터 import/export
├── auth           # 로그인 · 회원가입 화면, 인증(UserDetailsService)
├── member         # 회원, 희망 조건, 비밀번호 재설정(메일 인증)
├── favorite       # 관심 공고
├── kakao          # 카카오 OAuth 연동, 메시지 템플릿 · 발송
├── notification   # 신규 공고 / 관심 공고 리마인드 알림, 알림 이력
└── global         # Security 설정, 공통 예외 처리

src/main/resources
├── application.yaml
├── seed/alio-recruitments.json   # 초기 적재용 채용 공고 시드 데이터
├── templates/                    # Thymeleaf 화면
└── static/                       # css, js, images
```

<br>

## 화면 구성

| 경로 | 화면 |
| --- | --- |
| `/` | 랜딩(홈) |
| `/recruitments` | 채용 공고 목록 · 검색 |
| `/statistics` | 채용 통계 |
| `/login`, `/signup` | 로그인, 회원가입 |
| `/reset-password` | 비밀번호 재설정 |
| `/mypage` | 마이페이지 (관심 공고, 희망 조건, 카카오 연동, 알림 설정) |

<br>

## 주요 API

<details>
<summary>펼쳐 보기</summary>

**채용 공고** `/api/recruitments`

| Method | URI | 설명 |
| --- | --- | --- |
| GET | `/alio` | 채용 공고 목록 조회 (필터 · 페이지) |
| GET | `/alio/companies` | 기관 필터 옵션 |
| GET | `/alio/statistics/**` | 각종 통계 (summary, monthly-start-counts, yearly-start-counts, ncs-counts, company-counts, recruitment-category-counts, hire-type-counts, region-counts) |
| POST | `/alio/sync` | 수동 동기화 |
| GET | `/alio/sync-status`, `/alio/sync-events` | 동기화 상태 조회 / SSE 스트림 |
| GET | `/{recruitmentId}/redirect-url` | 원문 공고 URL 조회 |

**회원** `/api/members`

| Method | URI | 설명 |
| --- | --- | --- |
| GET | `/email-availability` | 이메일 중복 확인 |
| POST | `/signup` | 회원가입 |
| POST | `/find-id` | 아이디 찾기 |
| POST | `/password-reset-verifications`, `/password-reset-verifications/verify` | 비밀번호 재설정 인증코드 발송 / 확인 |
| POST | `/reset-password` | 비밀번호 재설정 |
| GET | `/me` | 내 정보 |
| PATCH | `/me/nickname`, `/me/password` | 닉네임 / 비밀번호 변경 |
| GET · PATCH | `/me/job-preference` | 희망 조건 조회 / 수정 |
| PATCH | `/me/notifications/favorite-reminder` | 관심 공고 리마인드 설정 |
| GET · POST · DELETE | `/me/favorite-recruitments` | 관심 공고 조회 / 추가 / 삭제 |
| GET | `/me/notifications/histories` | 알림 이력 |

**카카오** `/api/kakao`

| Method | URI | 설명 |
| --- | --- | --- |
| GET | `/authorize` | 카카오 계정 연동 시작 |
| GET | `/oauth/callback` | OAuth 콜백 |

</details>

<br>

## 실행 방법

### 1. 사전 준비

- JDK 17
- [공공데이터포털](https://www.data.go.kr/) ALIO 채용정보 · 공공기관 목록 API 서비스 키
- (선택) [Kakao Developers](https://developers.kakao.com/) 애플리케이션 — 카카오톡 알림 사용 시
- (선택) SMTP 계정 — 비밀번호 재설정 메일 발송 시

### 2. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 만듭니다. (`.env.example` 참고, `.env`는 Git에 올라가지 않습니다)

```properties
# ALIO 공공데이터 API
API_KEY=발급받은_서비스키

# Kakao
KAKAO_API_KEY=REST_API_키
KAKAO_CLIENT_SECRET_KEY=클라이언트_시크릿
KAKAO_REDIRECT_URI=http://localhost:8080/api/kakao/oauth/callback

# Mail (APP_MAIL_ENABLED=false면 메일 대신 인증코드를 로그로 출력)
APP_MAIL_ENABLED=true
MAIL_HOST=smtp.naver.com
MAIL_PORT=465
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_SMTP_AUTH=true
MAIL_SMTP_SSL_ENABLE=true
MAIL_SMTP_STARTTLS_ENABLE=false

# (선택) 기본값: H2 인메모리 DB
# DATABASE_URL=jdbc:h2:file:./data/gongchae;MODE=MySQL
# DATABASE_USERNAME=sa
# DATABASE_PASSWORD=
# APP_WEB_BASE_URL=http://localhost:8080
```

### 3. 실행

```bash
./gradlew bootRun
```

- 서비스: http://localhost:8080
- H2 콘솔: http://localhost:8080/h2-console

> 기본 설정은 H2 **인메모리** DB라 서버를 재시작하면 데이터가 초기화됩니다. 시작할 때마다 `seed/alio-recruitments.json`을 먼저 적재한 뒤 ALIO API와 동기화하므로 공고 데이터는 바로 사용할 수 있습니다.

### 4. 테스트

```bash
./gradlew test
```

<br>

## 스케줄러

| 작업 | 주기 |
| --- | --- |
| ALIO 신규 공고 동기화 및 신규 공고 알림 | 10분마다 |
| 관심 공고 리마인드 발송 | 매분 (회원이 설정한 시각에 발송) |
| 만료된 비밀번호 재설정 인증 정보 정리 | 매시 정각 |

<br>

## License

[MIT License](LICENSE)
