# v1.0.0 — first public release

Fork of [nadavramon/job-tracker-app](https://github.com/nadavramon/job-tracker-app) (MIT). Full commit history preserved (A1→A5 series + this release).

## Changes (B1 → A5)

- **B1**: upstream baseline verified (backend 191 + frontend 375 tests green, container-era deployment).
- **A2**: Interview management (scheduling, stages, results, 24h dashboard reminder), HR communication log (channels, next-action tracking, due-pinned), multi-currency offer comparison (normalized annual package, weight-adjusted scoring), JPA repositories + Flyway V2-V4 migrations, 214 backend / 383 frontend tests.
- **A3**: Docker Compose deployment (jt-postgres/jt-app/jt-frontend, localhost-only), Flyway auto-migration on startup, persistence & from-scratch rebuild verification, demo evidence (screenshots + API/psql three-layer evidence).
- **A4**: Bilingual (zh-CN/en) JD match scoring — pure-Java weighted keyword algorithm + dashboard card; Chinese-aware AI extraction fields (responsibilities/requirements/salary range with CNY/USD/EUR); i18n dictionary; docs/CODE_GUIDE.md; backend 223 / frontend 385 tests green.
- **A5**: Local-stack Chinese full-flow demo (step screenshots + api evidence), offline font fix (Google Fonts unreachable in restricted networks — system font stack with aliased variables), lint-clean.
- **A6/A-GH1**: Public-release readiness — MIT LICENSE with upstream + secondary-dev copyright, demo secrets moved to gitignored .env.local, log-history privacy scan (no real credentials; two low-risk local-demo key literals disclosed in docs/RELEASE_AUDIT.md §2), README professionalization (features/API/roadmap/known-limitations/AI-agent statement), zero-secrets CI (services postgres).

## Verification

- Backend: `mvnw -B test` → 223 tests, 0 failures, 0 errors (1 skipped: disabled context test)
- Frontend: `npm run build` ✓, `npm test` → 43 suites / 385 tests green
- CI: GitHub Actions (services postgres, placeholder secrets) — green on main
- Release artifact: backend jar, SHA256 `27EC496EB5F6254C4C9DB72B348BF6C4EB09AC4B636A98996724401286822291`

## Quick start

See README (Quick Start / Docker Compose one-command deployment).