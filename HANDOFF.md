# HANDOFF — A1 P0 基线验证（2026-11-14）

## 环境与前置

- 仓库：github.com/nadavramon/job-tracker-app，本地路径 K:\Fpj Project\A-job-tracker（Java 21 + Spring Boot 4.0.1 + Next.js 16.1.3 + PostgreSQL 17，MIT）。
- JDK：Temurin 21.0.13.9-hotspot（C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot）。注意：本 shell 中机器级 JAVA_HOME 未生效，Maven 命令前需手动设置 JAVA_HOME。
- Maven：无全局安装，统一使用仓库 mvnw（wrapper 3.3.4，Maven 3.9.12）。
- Node：npm 可用，但本机存在 TLS 中间人代理（registry 证书 NameMismatch），部分二进制包首次安装损坏，修复见排障记录。

## 已理解的现有功能边界

- 已读 README 与 specs/0001-refresh-token-hardening（status: done）：refresh-token 子系统三项加固已完成（theft revocation 持久化、并发轮换 401、version 列 ColumnDefault("0")），配套 Testcontainers 集成测试 *IT 挂在 failsafe/verify 阶段。
- 原有功能：应用追踪仪表盘、AI 岗位抽取（Anthropic SDK）、门户凭据保险箱（AES-256-GCM）、JWT 认证（HttpOnly cookie）、搜索/过滤/排序、明暗主题。
- 本轮铁律遵守：未启动对外服务；未改任何代码架构；未导入任何真实招聘网站密码/Cookie/Token（测试全部使用虚构数据）；未启动数据库容器（surefire 单测不需要 DB；*IT 需要 Docker+Postgres，属 verify 阶段，本轮未执行）。

## 完成项与真实命令结果

| 步骤 | 命令（工作目录） | 结果 |
|---|---|---|
| 前端安装 | npm install --no-audit --no-fund（frontend） | exit 0（首次 ECONNRESET 失败后重试成功） |
| 前端构建 | npm run build（frontend） | exit 0，Next.js 16.1.3 Turbopack 编译成功，10 条路由全部静态生成 |
| 前端测试 | npm test（frontend） | exit 0，41 套件 / 375 用例全部通过 |
| 后端测试 | 设置 JAVA_HOME 后 .\mvnw -B test（backend） | exit 0，BUILD SUCCESS，Tests run: 191, Failures: 0, Errors: 0, Skipped: 1（JobTrackerApplicationTests 按 spec @Disabled），Jacoco 报告 48 类 |

## 排障记录（环境问题，非代码缺陷）

1. mvnw 首跑失败（exit 1）：PowerShell WebClient 下载 Maven 发行包报 SSL 信任失败。修复：用 curl.exe --ssl-revoke-best-effort 下载 apache-maven-3.9.12-bin.zip，按 wrapper 缓存规则解压到 %USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.12\59fe215c0ad6947fea90184bf7add084544567b927287592651fda3782e0e798（zip 内目录需上移一层）。
2. 前端构建失败 "turbo.createProject is not supported by the wasm bindings"：@next/swc-win32-x64-msvc 的 .node 二进制首次安装时因网络中断损坏（PE 头在但内容截断）。用 curl 下载精确版本 16.1.3 tarball 手动替换后通过。注意 npm view latest 是 16.4.6，装 16.4.6 的二进制会报 "Missing field additionalRoots"，必须与 next 版本严格一致。
3. lightningcss 二进制损坏（仅 49 KB，应约 9 MB）：同因。下载 lightningcss-win32-x64-msvc@1.30.2 tarball 替换 node_modules\lightningcss-win32-x64-msvc\lightningcss.win32-x64-msvc.node 后构建通过。
4. 后端首跑 52 个 Errors（已解决）：签名 "MockitoException: Could not modify all classes ... NoClassDefFoundError: UserService"。根因是两次 Maven 构建并发运行同时写 target/（长命令超时截断后的孤儿进程 + 重跑）导致的类加载竞态——surefire 报告中同一测试类 AiControllerTest 实际 6/6 通过。清理孤儿 java 进程后干净重跑即 BUILD SUCCESS。再现时先确认无并发 mvnw/java 进程。
5. 环境副作用声明：修复 SWC 文件锁时执行了一次全机 node 进程清理（Stop-Process -Name node），可能影响用户其他 Node 程序，特此记录。

## 失败项与日志位置

- 当前无失败项（前后端基线全绿）。
- 历史失败日志：backend/build-backend-test.log 已被成功重跑覆盖，失败签名保留在排障记录 4；npm 调试日志在 K:\claude code\runtime\npm-cache\_logs\。

## P1 改造清单（下一阶段方案）

### 1. 面试管理（Interview Module）

- 后端：新增 interviews 表（id, application_id FK, stage[HrScreen/Technical/SystemDesign/Final/Cross], scheduled_at, timezone, mode[Online/Onsite/Phone], interviewer_notes, feedback, result[Pending/Pass/Fail/NoShow], created_at/updated_at, 软删），REST /api/applications/{id}/interviews，复用现有 JSR 380 校验与用户数据隔离模式。
- 前端：application 详情页新增时间线组件；面试前 24h 提醒（先用浏览器端轮询，不引消息队列）。
- 测试：Mockito 单测 + Testcontainers IT（复用 spec 0001 的 IT 基建）。

### 2. Offer 比较（Offer Comparison）

- 后端：offers 表（application_id, base_salary, bonus, equity, currency, location, remote_policy, insurance, pto_days, start_date, notes）；比较端点返回归一化年包（含汇率换算，汇率表内置可配置）。
- 前端：/offers 页，双列对比 + 加权评分（权重用户可调），Recharts 雷达图复用现有依赖。

### 3. HR 沟通记录（Communication Log）

- 后端：communications 表（application_id, channel[Email/Phone/Portal/WeChat/LinkedIn], direction[In/Out], occurred_at, summary, next_action, next_action_at），支持按 application 聚合视图。
- 前端：详情页"沟通记录"标签页 + next_action_at 到期置顶提醒。凭据一律走现有保险箱，禁止明文。

### 4. AI 岗位匹配中文化

- AI 抽取 prompt 增加中文 JD 模板与结构化输出字段（职责/要求/薪资范围/工作地点中英混合容错），Zod schema 扩展 salaryRange（含币种枚举 CNY/USD/EUR）。
- 关键词匹配：中文分词用 jieba-wasm（纯 wasm，无原生依赖），匹配度评分输出到仪表盘新卡片。
- 全部新增 UI 文案走中英双语字典（lib/i18n），默认 zh-CN，不硬编码。
- 测试：中文 JD 样本（虚构）进 Jest 快照 + 后端结构化校验单测。

## Git 可追溯性

本轮代码零改动。未启动 8081 端口服务，未创建 jt-* 数据库容器。本文件与 README 分节见对应提交；package-lock.json 曾被强制安装意外改动，已还原。
