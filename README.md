> [!NOTE]
> **Fork notice (secondary development)**: This repository is a fork of [nadavramon/job-tracker-app](https://github.com/nadavramon/job-tracker-app) (MIT) with the full upstream history preserved. Secondary development by [Fpj5085](https://github.com/Fpj5085) adds four modules on top of the upstream baseline: **interview management, HR communication records, multi-currency offer comparison, and AI job-match scoring (pure-Java weighted scoring, no LLM inference for matching)** — see [`docs/CODE_GUIDE.md`](docs/CODE_GUIDE.md) for a guided tour of both upstream and fork code. The AI-extraction feature remains mock-tested upstream (no real API key wired), same as upstream; see the honest-limitations note in this fork's docs.

<div align="center">

  <h1>Job Tracker App</h1>

  <p>
    <strong>A full-stack web application to track job applications, manage statuses, and optimize your job hunt — with AI-powered extraction.</strong>
  </p>

  <p>
      <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21">
    <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-4-brightgreen?logo=springboot" alt="Spring Boot 4"></a>
    <a href="https://nextjs.org/"><img src="https://img.shields.io/badge/Next.js-16-black?logo=next.js" alt="Next.js 16"></a>
    <img src="https://img.shields.io/badge/TypeScript-5-blue?logo=typescript" alt="TypeScript">
    <img src="https://img.shields.io/badge/PostgreSQL-17-336791?logo=postgresql&logoColor=white" alt="PostgreSQL 17">
    <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License"></a>
  </p>
</div>

---

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#key-features">Key Features</a></li>
    <li><a href="#tech-stack">Tech Stack</a></li>
    <li><a href="#project-structure">Project Structure</a></li>
    <li><a href="#getting-started">Getting Started</a></li>
    <li><a href="#api-reference">API Reference</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
  </ol>
</details>

## About The Project

The Job Tracker App helps job seekers stay organized during their search. It provides a secure dashboard to log applications, track progress through hiring stages, view analytics, and even auto-extract job details from postings using AI.

## Key Features

- **AI-Powered Job Extraction** — Paste a job posting URL or raw text and let Claude automatically extract company, role, location, and more.
- **Interactive Dashboard** — Summary stats bar, monthly application trends, and status breakdown charts powered by Recharts.
- **Portal Credentials Vault** — Store login credentials for company portals, encrypted at rest with AES-256-GCM.
- **Interview Management** — Schedule interviews per application (stage, mode, timezone), track results, and get a 24-hour reminder banner on the dashboard.
- **HR Communication Log** — Record every email/phone/portal exchange with next-action tracking, due items pinned first.
- **Multi-Currency Offer Comparison** — Normalize offers into a base currency with a built-in FX table, weight-adjusted scoring and side-by-side comparison.
- **Job Match Score (bilingual)** — Paste a Chinese or English JD and get a weighted match score against each application (pure-Java algorithm, works offline).
- **Secure Authentication** — JWT-based stateless auth with HttpOnly cookies. Supports email and username login.
- **Search, Filter & Sort** — Full-text search, status filtering, multi-column sorting, and server-side pagination.
- **Modern UI** — Responsive design with Light/Dark/System themes, Lucide React icons, custom toast notifications, and entrance animations.
- **Data Integrity** — Input validation (JSR 380), soft-delete for applications, and strict user data isolation.

## Tech Stack

### Frontend
| Category | Technologies |
|:---|:---|
| Core | Next.js 16 (Pages Router), React 19, TypeScript 5 |
| Styling & UI | Tailwind CSS 4, Recharts, Lucide React |
| AI | Anthropic SDK, Zod (structured outputs) |
| State & Fetching | Axios, React Context API |
| Testing | Jest 30, React Testing Library |

### Backend
| Category | Technologies |
|:---|:---|
| Core | Java 21, Spring Boot 4, Spring Security |
| Database | PostgreSQL 17, Hibernate / JPA |
| Auth & Security | JJWT 0.12, Bucket4j (rate limiting) |
| Testing | JUnit 5, Mockito, JaCoCo |

### DevOps
| Category | Technologies |
|:---|:---|
| CI | GitHub Actions — path-filtered backend + frontend jobs |

## Project Structure

```
job-tracker-app/
├── backend/
│   └── src/main/java/com/jobtracker/
│       ├── controller/        # REST controllers (Auth, Application, User)
│       ├── service/           # Business logic
│       ├── repository/        # Spring Data JPA repositories
│       ├── model/             # JPA entities
│       ├── dto/               # Request/Response DTOs
│       ├── security/          # JWT filter, SecurityConfig
│       └── exception/         # Global exception handling
├── frontend/
│   ├── app/                   # Next.js pages (Pages Router)
│   │   ├── (app)/             # Authenticated pages (dashboard, applications, settings)
│   │   ├── (auth)/            # Public pages (login, register)
│   │   └── api/ai/            # AI extraction API route
│   ├── components/            # Reusable React components
│   ├── lib/                   # Services, utilities, AI agents
│   └── __tests__/             # Jest test files
├── .github/workflows/         # CI pipeline
└── dev.sh                     # Start both servers locally
```

## Getting Started

### Prerequisites

- Java 21
- Node.js 22+
- PostgreSQL 17

### Quick Start

The fastest way to start both backend and frontend:

```bash
git clone https://github.com/nadavramon/job-tracker-app.git
cd job-tracker-app
./dev.sh
```

This launches the API on `http://localhost:8080` and the client on `http://localhost:3000`.

### Environment Variables

**Backend** — set in `backend/src/main/resources/application-local.properties` or as env vars:

| Variable | Description |
|:---|:---|
| `DB_URL` | JDBC connection string (e.g. `jdbc:postgresql://localhost:5432/job_tracker`) |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key for signing JWTs |
| `ENCRYPTION_SECRET` | Secret key for AES-256-GCM credential encryption |

**Frontend** — set in `frontend/.env.local`:

| Variable | Description |
|:---|:---|
| `NEXT_PUBLIC_API_URL` | Backend URL (default: `http://localhost:8080`) |
| `ANTHROPIC_API_KEY` | API key for Claude AI extraction |

### Manual Start (alternative)

```bash
# Backend
cd backend
./mvnw spring-boot:run

# Frontend (separate terminal)
cd frontend
npm install
npm run dev
```

## API Reference

<details>
<summary>Click to expand API Endpoints</summary>

### Auth (Public)
| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Login (email or username) and receive JWT cookie |
| `POST` | `/auth/logout` | Clear auth cookie and log out |

### Applications (Authenticated)
| Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/applications` | List applications (supports `search`, `status`, pagination, sorting) |
| `GET` | `/applications/stats` | Get aggregated statistics (totals by status) |
| `GET` | `/applications/{id}` | Get a single application |
| `GET` | `/applications/{id}/credentials` | Get decrypted portal credentials |
| `POST` | `/applications` | Create a new application |
| `PATCH` | `/applications/{id}` | Update specific fields |
| `DELETE` | `/applications/{id}` | Soft-delete an application |

### User Profile (Authenticated)
| Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/me` | Get current user profile & preferences |
| `PATCH` | `/me` | Update profile (username, email, password, theme) |
| `DELETE` | `/me` | Delete account (soft-deletes user and all applications) |

### AI (Authenticated via cookie)
| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/api/ai/extract` | Extract job details from text or URL (rate-limited) |

### Interviews / Communications / Offers / Match (Authenticated, fork additions)
| Method | Endpoint | Description |
|:---|:---|:---|
| `GET/POST` | `/applications/{id}/interviews` | List / schedule interviews; `PATCH/DELETE` on `/interviews/{iid}` |
| `GET` | `/interviews/upcoming?hours=24` | Interviews due in the next N hours (dashboard banner) |
| `GET/POST` | `/applications/{id}/communications` | List / record HR communications |
| `GET/POST` | `/applications/{id}/offers` | List / add offers; `PATCH/DELETE` on `/offers/{oid}` |
| `GET` | `/offers/compare` | All offers normalized to the base currency, sorted |
| `POST` | `/applications/{id}/match` | Bilingual JD match score for this application |

</details>

## Roadmap

- [x] Phase 1: Core REST API & Spring Security
- [x] Phase 2: Frontend Foundation, Theme System, and UI Primitives
- [x] Phase 3: Interactive Dashboard with Recharts
- [x] Phase 4: AI-Powered Smart Application Entry
- [x] Phase 5: Portal Credentials Vault (encrypted)
- [x] Phase 6: HttpOnly Cookie Auth & UI Refresh
- [ ] Phase 7: Refresh Token Rotation
- [x] Phase 7: Refresh Token Rotation (upstream spec 0001)
- [x] Fork: Interview Management, HR Communication Log, Offer Comparison, Job Match Scoring
- [x] Fork: Docker Compose deployment (jt-postgres / jt-app / jt-frontend, localhost-only)
- [ ] Cloud Deployment

## Known Limitations

- **AI extraction** requires a personal Anthropic API key (set in Settings) and is mock-tested only — no real LLM call is exercised in tests or CI.
- **Job match scoring** is a lightweight keyword-weight algorithm (contains-based, bilingual); it is not semantic search — quality depends on JD wording.
- **FX rates are static** (built-in table, env-configurable); they do not update from the market.
- **CI note**: `next/font/google` requires network access to fonts.googleapis.com at build time; in restricted networks remove the two Google font imports in `frontend/app/layout.tsx` (font variables are already aliased in `globals.css`).
- The fork has been validated on Windows 11 + Docker Desktop; Linux/macOS paths in the Compose file are untested.

## AI-Agent Collaboration Statement

This fork was developed with an AI coding agent (multi-agent pipeline) doing the heavy lifting: scaffolding, repetitive boilerplate, test generation and docs drafts. The human author was responsible for requirements, acceptance review, key design decisions (e.g. static FX table, browser-polling reminders instead of a message queue, no history rewrite), security-sensitive choices and the final code review of every commit. See `docs/CODE_GUIDE.md` for a module-by-module tour written for the author's own interview preparation.

## Contributing

Contributions are welcome!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 二次开发说明（Fork 注记）

本节由二次开发团队增补，用于区分上游原有功能与后续新增贡献。上游代码版权归 Nadav Ramon 所有，项目继续遵循 MIT License（见 LICENSE 文件）。

### 上游原有功能（本轮未改动）

应用追踪仪表盘、AI 岗位抽取、门户凭据保险箱（AES-256-GCM）、JWT 认证、搜索/过滤/排序、明暗主题，以及 specs/0001-refresh-token-hardening 已完成的 refresh-token 加固。

### 本轮贡献（A2，2026-11-14）

A2 新增三个业务模块（全部含单测 + Testcontainers IT，前端 383 用例 / 后端 214+8 全绿）：面试管理（interviews 表 + 时间线 + 24h 浏览器端轮询提醒）、HR 沟通记录（communications 表 + 到期置顶）、Offer 薪酬比较（offers 表 + 内置汇率归一化年包 + 权重可调对比页）。上游原有功能未改动；A1 的 P0 基线验证记录保留在 HANDOFF.md。

### 后续计划（Backlog，待评审）

AI 岗位匹配中文化（jieba-wasm、中文 JD prompt、i18n 字典）。方案详见 HANDOFF.md 的 Backlog 小节，涉及代码改动时将在独立分支进行并保留上游提交历史。

## 一键部署（Docker Compose，仅本地）

```bash
# 1. 克隆并进入仓库
git clone https://github.com/nadavramon/job-tracker-app.git
cd job-tracker-app

# 2. 准备环境变量（.env 不入库；填入 32+ 位 JWT_SECRET / ENCRYPTION_SECRET）
cp .env.example .env

# 3. 构建后端 jar（容器内 Maven 下载在本项目网络环境不可用，故由宿主机构建）
cd backend && ./mvnw -B -DskipTests package && cd ..

# 4. 一键启动（jt-postgres 55432 / jt-app 8081 / jt-frontend 3101，全部仅绑定 127.0.0.1）
docker compose up -d --build

# 5. 访问
#    前端: http://localhost:3101   后端 API: http://localhost:8081
#    停止（保留数据）: docker compose down    停止并清库: docker compose down -v
```

以上命令已在 Windows 11 + Docker Desktop 实际验证（见 HANDOFF.md A3 章节：Flyway 自动迁移、三层联动 API/psql 证据、持久化与从零重建验证）。

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Author

Nadav Ramon

Project Link: [https://github.com/nadavramon/job-tracker-app](https://github.com/nadavramon/job-tracker-app)
