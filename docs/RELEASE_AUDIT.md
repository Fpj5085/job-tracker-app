# 发布审计报告（A-GH1，2026-11-22）

> 范围：本 fork（upstream nadavramon/job-tracker-app + A1-A6 二次开发）公开前的发布审计。本轮不创建 origin、不推送、不重写历史。

## 1. 整改项逐条状态

| # | 整改项 | 状态 | 证据 |
|---|---|---|---|
| A-1 | 8 个曾跟踪日志文件的隐私检查闭环 | ✅ 完成（结论见 §2） | 本报告 §2 的逐文件扫描 |
| A-2 | LICENSE 版权行核对 | ✅ 完成 | 上游 `Copyright (c) nadavramon/job-tracker-app contributors (upstream)` + 二开 `Copyright (c) 2026 Feng Pinjun (secondary development: ...)` 两行齐备，MIT 正文完整（LICENSE:1-23） |
| A-3 | 演示密钥迁出 | ✅ 已由 A6（22a8bea）完成 | `start-a5-local.ps1` 从 gitignored `.env.local` 读取；模板 `.env.local.example` 只含 change-me 占位 |
| A-4 | fork notice | ✅ 已由 A6 完成 | README 顶部 NOTE 块 |
| A-5 | 构建日志去跟踪 | ✅ 已由 A6 完成（10 个文件，未重写历史） | `git show 22a8bea --stat`：10 个 .log 全部从跟踪中移除 |
| A-6 | README 专业化 | ✅ 本轮完成 | Key Features 增补 4 个 fork 功能、API Reference 增补 6 个 fork 端点、Roadmap 勾选 Phase 7 + fork 三项、新增 Known Limitations 与 AI-Agent Collaboration Statement 两节 |
| A-7 | CI 方案设计（零密钥） | ✅ 已有 | `.github/workflows/ci.yml`：backend 用 `services: postgres:17` 容器（DB_URL/DB_USERNAME/DB_PASSWORD 全部 CI 占位值，JWT_SECRET 为 CI 测试值）+ `./mvnw clean verify`；frontend `npm ci / lint / test / build`；`ci-gate` 汇总门禁。文件已在库中，推送后才真实运行 |
| A-8 | 等待总控批准后建公开仓库 | ⏳ 进行中（本报告即 GO/NO-GO 依据） | — |

## 2. 日志历史隐私扫描结论

**对象**：git 历史中曾出现过的全部 10 个日志/证据文件（backend/build-a4-test.log、frontend-build-a4.log、frontend-test-a4.log、docs/demo/{compose-build,compose-build-frontend,compose-up,disk-cleanup,persistence-down,persistence-down-v,persistence-up}.log）+ 2 个 API 证据文本（api-evidence.txt、a5-api-evidence.txt，均曾入库）。

**方法**：`git log -p -- <file>` 逐文件全历史 + `git log -p --all` 全库关键词扫描，两轮共 12 个模式：JWT/JWE 形（`eyJ…`、`jwt=`、`Set-Cookie:`、`Bearer `）、演示密码（Password123!）、本地密钥（jtdemo-local-2026、a5-local-jwt-secret-key-32-characters!、a5-local-encryption…）、演示账号（demo@example.com、a2demoalice）、云 API Key 形（sk-ant/LTAI/AKIA/ghp_/xoxb）、个人 PII（Fpj5085、C:\Users\28953、手机号/私人邮箱正则）。

**结论**：
1. **无任何真实凭据/Cookie/Token/求职隐私进入历史**。所有命中均为：
   - 测试代码中的**虚构**凭据（A2P1ModulesIT 的 Password123!/a2alice、AiExtractionServiceTest 的 sk-ant-test-key mock）；
   - 文档中**明示为演示账号**的合成数据（HANDOFF/README 的 demo@example.com / a2demoalice / Password123!，均为本地栈演示值，公开后仍为无效值）；
   - 构建日志中的本机路径（C:\Users\28953.m2 等，Windows 用户名 28953 泄露，风险极低但如实列出）；
   - A5 本地栈启动脚本曾跟踪过的**演示密钥字面量**（jtdemo-local-2026 / a5-local-jwt-secret-key-32-characters!）——A6 已把它们迁到 gitignored .env.local，但 **git 历史中 7818ffd/22a8bea 两处 diff 仍含这些字面量**（`git log -S` 可检出）。
2. **风险定级：低**。上述字面量仅用于一次性本地演示环境（127.0.0.1 绑定、jtdemo 卷密码、15 分钟演示 JWT），不适用于任何真实部署；公开后不构成可利用凭据。
3. **如实披露**：历史非重写，`git log -S "a5-local-jwt-secret-key"` 在公开仓库仍可检出 A5/A6 两处 diff。处置建议（需总控+发起方批准，本轮未执行）：
   - 方案甲（推荐）：公开仓库以 squash/re-init 方式发布干净快照，完整历史保留在私有侧；
   - 方案乙：接受低风险直接公开（演示密钥 + 已失效的本机路径）；
   - 方案丙：git-filter-repo 移除 7818ffd/22a8bea 中两个 ps1 的密钥行后强推（代价：改写历史）。

## 3. 测试证据（2026-11-22 复跑，A4 基线）

| 项 | 结果 | exit code | 日志 |
|---|---|---|---|
| backend `mvnw -B test` | **Tests run: 223, Failures: 0, Errors: 0, Skipped: 1**，BUILD SUCCESS | 0 | backend/build-audit-test.log |
| frontend `npm run build` | Compiled successfully in 12.2s，BUILD_ID 生成 | 0 | frontend-build-audit.log |
| frontend `npm test` | **Test Suites: 43 passed, Tests: 385 passed** | 0 | frontend-test-audit.log |
| backend `mvnw test` 附注 | Mockito self-attach 警告（agent 未显式加载），不影响结果 | — | 同上 |

## 4. LICENSE 核对

✅ MIT 正文完整；版权行两行齐备且顺序正确：上游归属在前（nadavramon/job-tracker-app contributors），二开归属在后（2026 Feng Pinjun + 贡献范围注记）。README 顶部另有 fork notice 指向上游仓库与上游 MIT。

## 5. 专业化清单（本轮完成）

- README Key Features：+4 fork 功能条目（面试管理/沟通记录/Offer 比较/双语匹配评分）
- README API Reference：+6 fork 端点（interviews、upcoming、communications、offers、offers/compare、match）
- README Roadmap：Phase 7 勾选（upstream spec 0001）、+2 条 fork 完成项（四模块、Compose 部署）、Cloud Deployment 保留未勾
- README 新增 Known Limitations 一节（AI 抽取 Key 依赖且未在 CI 验证、匹配算法为词法级、静态汇率、字体网络依赖、Compose 仅 Windows 验证）
- README 新增 AI-Agent Collaboration Statement（AI 负责脚手架/样板/测试生成/文档草稿；本人负责需求/验收/关键设计决策/安全敏感选择/逐提交评审）
- 建议仓库 Description：`Full-stack job application tracker (Spring Boot 4 + Next.js 16 + PostgreSQL 17) with interview management, HR communication log, multi-currency offer comparison and bilingual JD match scoring. Fork of nadavramon/job-tracker-app.`
- 建议 Topics：`spring-boot` `nextjs` `react` `typescript` `postgresql` `docker-compose` `flyway` `job-tracker` `job-search` `full-stack`

## 6. GO / NO-GO 自评

**GO（有条件）**——整改项全部闭环、测试 223/385 全绿、LICENSE 与专业化到位、历史扫描未发现真实凭据/隐私。两个附加条件随报告提交总控裁决：①（低风险接受项）git 历史含 A5 本地演示密钥字面量两处 diff，建议按方案甲 squash 发布或按方案乙书面接受；②（低风险披露项）构建日志含 Windows 用户名路径 2 处，同为历史不可改写项。以上均不涉及真实系统凭据或求职隐私。
**NO-GO 项**：无。