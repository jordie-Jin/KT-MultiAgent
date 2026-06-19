# 로그 분석 자동화 시스템 — 백엔드 작업 브리핑

> 최종 업데이트: 2026-06-18 · 범위: DB·환경설정·Spring Boot 조회 백엔드·시드 데이터

## 1. 개요
BGL(BlueGene/L) 슈퍼컴퓨터 로그를 분석하는 자동화 시스템의 **백엔드(Spring Boot) + DB(MySQL)** 구축.
- 3-Tier: Front ↔ **Back(Spring Boot, 본 작업)** ↔ DB(MySQL)
- 분석(이상탐지·근거·패턴군집)은 **FastAPI 분석 엔진**이 담당(별도, 미준비). 백엔드는 결과를 **조회(read)** 해 화면에 내려줌.
- 이번 데모 범위: **파일 업로드 → 분석 흐름 제외.** 시드 데이터로 조회 화면/API 시연.

## 2. 기술 스택
- Java 21, Spring Boot 4.0.7 (Web MVC, Data JPA, Security, Validation)
- MySQL 8.0 (로컬, 3306) / InnoDB / utf8mb4
- Lombok, Hibernate 7.x
- 빌드: Gradle (`./gradlew`)

## 3. 데이터베이스 (`log_analysis`)
DDL: `docs/schema.sql` · 정본 ERD: `docs/log_analysis.erd.json`

| 테이블 | 역할 | 현재 데이터 |
|---|---|---|
| `bgl_log` | 원시 로그 (id, status, label, node, node_repeat, component, log_type, log_ts, log_level, content, event_id) | 목 데이터 20건 (교체 예정) |
| `log_analysis` | AI 진단/분석 결과 (FK: log_id→bgl_log, cluster_id→pattern_cluster) | 목 데이터 7건 (FastAPI가 채울 영역) |
| `pattern_cluster` | 패턴 클러스터 (title, description, event_template, importance) | 목 데이터 4건 (FastAPI 영역) |
| **`bgl_template`** | **BGL 이벤트 템플릿 카탈로그(고정 참조)** event_id PK + event_template | **377건 적재 완료 ✅** |

- 시드 스크립트: `docs/seed.sql`(목 데이터), `docs/seed_template.sql`(템플릿 377개)
- 적재 명령: `mysql.exe -u root -p --default-character-set=utf8mb4 < docs/<파일>.sql`

### ERD 정리 이력
`log_analysis.erd.json` 의 중복 `cluster_id` 컬럼·이중 관계·고아 컬럼 제거 → cluster FK를 **점선 한 줄**(비식별, 정상 방향)로 정상화.

## 4. 환경설정
- **`.env`** (gitignore) + **`.env.example`**: `MYSQL_USER/PASSWORD/HOST/PORT/DATABASE`
- **`DotenvEnvironmentPostProcessor`** (`META-INF/spring.factories` 등록): `.env` 를 직접 읽어 주입.
  (spring-dotenv 는 Spring Boot 4 에서 자동 로딩 안 돼 자체 구현으로 대체)
- `build.gradle`: `bootRun { workingDir = projectDir }` → `.env` 탐색 보장
- `application.yaml`: datasource URL 을 env 변수로 파라미터화, `ddl-auto: none`(스키마는 schema.sql 로 관리)
- **`SecurityConfig`**: `/api/**`·H2 콘솔 permitAll, CSRF off, **CORS**(localhost:* 허용, Vite 개발서버 대비)

## 5. 백엔드 구조 (계층형, `com.project.demo`)
- `entity/` — `BglLog`, `LogAnalysis`, `PatternCluster` (Jakarta Persistence, FK는 @ManyToOne LAZY)
- `repository/` — JPA 리포지토리. apispec 기준 검색/집계 쿼리 보유
  - `BglLogRepository`: 로그 검색(다중 nullable 필터), 상세 LEFT JOIN, 대시보드 집계(레벨/컴포넌트/타입 분포, 시간 버킷 series)
  - `LogAnalysisRepository`: 분석 건수, 위험도 분포
  - `PatternClusterRepository`: 패턴 목록/상세 집계, 관련 로그
- `service/` — `DashboardService`, `LogService`, `PatternService`
- `controller/` — `DashboardController`, `LogController`, `PatternController`
- `dto/` — `common`(ApiError, PageResponse), `dashboard`(Summary/분포/TimePoint), `log`(Summary/Detail/Raw/AnalysisDto/PatternRef), `pattern`(Summary/Detail/RelatedLog)
- `web/` — `ApiException`, `GlobalExceptionHandler`
- `support/` — `QuerySupport`, `RiskLevels`, `ActionParser`

## 6. 주요 API (apispec 기준)
- **대시보드**: 기간 요약(총/주의 로그), 시간대별 발생량, 레벨/컴포넌트/타입 분포, 위험도 분포
- **시스템 로그**: 목록(기간·label·level·component·keyword·isCaution·isAnalysis 필터, 페이지네이션), 상세(로그+분석+패턴 1회 조회)
- **패턴 분석**: 패턴 목록/상세, 패턴별 관련 로그
- 공통: `isCaution` = label alert(`!= '-'`), `isAnalysis` = 분석결과 존재 (독립 필터)
- 모두 GET(읽기 전용). 데이터 적재는 분석 엔진 담당.

> 참고: DB→엔티티→Repository→조회 API→시드 데이터 기본 경로는 앱 부팅+호출로 검증 완료(초기 read API 기준). 이후 apispec 기준으로 확장된 엔드포인트는 재검증 권장.

## 7. 데이터 적재 계획
1. ✅ **완료** — `bgl_template` 377개 (BGL_templates.csv) 고정 참조 적재
2. ⏭️ **다음** — 변경된 BGL 로그 수신 후: 목 데이터 전부 삭제 → `bgl_log` 2000건을 **압축 적재**(타임스탬프 조작: **2026-06-22~06-26, 5일 × 400건/일**)
3. ⏭️ `log_analysis`·`pattern_cluster` 는 **FastAPI 분석 엔진**이 채움(미준비)

## 8. 실행 방법
```
# 1) DB 스키마 (최초 1회)
mysql.exe -u root -p --default-character-set=utf8mb4 < docs/schema.sql
# 2) 템플릿 카탈로그
mysql.exe -u root -p --default-character-set=utf8mb4 < docs/seed_template.sql
# 3) (선택) 목 시드
mysql.exe -u root -p --default-character-set=utf8mb4 < docs/seed.sql
# 4) 앱 실행 (.env 의 MYSQL_PASSWORD 자동 로드)
./gradlew.bat bootRun     # http://localhost:8080
```

## 9. 알려진 이슈 / TODO
- **ERD 미반영**: `bgl_template` 가 `schema.sql`엔 추가됐지만 `log_analysis.erd.json`(erd-editor)엔 아직 없음 → 동기화 필요.
- **EventId 체계 불일치**: `BGL_2k.log_structured.csv` 의 EventId 는 `BGL_2k.log_templates.csv`(120개) 기준. 적재한 카탈로그는 `BGL_templates.csv`(377개) → 변경 데이터는 377 카탈로그 기준 파싱 필요.
- **FK 미설정**: `bgl_log.event_id → bgl_template.event_id` 참조는 논리적으로만 존재(제약 미적용). 변경 데이터 정렬 후 추가 검토.
- **응답 charset**: JSON 응답 Content-Type 에 charset 없음(PowerShell 등 일부 클라이언트에서만 한글 깨져 보임, 브라우저는 정상). 필요 시 `;charset=UTF-8` 강제.
- `bgl_template` 조회용 엔티티/엔드포인트 미생성(필요 시 추가).
