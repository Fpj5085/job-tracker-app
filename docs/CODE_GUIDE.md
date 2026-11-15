# A 项目代码学习指南（CODE_GUIDE）

> 目标读者：项目作者本人 + 面试准备。按模块逐文件讲清楚：这个类干什么、关键注解、事务边界在哪、为什么这么设计。配套仓库 K:\Fpj Project\A-job-tracker（上游 nadavramon/job-tracker-app + A 系列二次开发）。

## 0. 全局架构一张图

```
浏览器 (Next.js 16, localhost:3101)
   │  HttpOnly Cookie: jwt（登录）/ refresh（续期）
   ▼
Spring Boot 4 (localhost:8081)
   SecurityFilterChain: RateLimitFilter → JwtAuthenticationFilter → Controller
   Controller → Service（事务边界在这里）→ Repository (JPA) → PostgreSQL 17
   启动时：Flyway V1-V4 迁移 → Hibernate ddl-auto=update 兜底
```

关键约定：所有业务表继承 BaseEntity（createdAt/updatedAt 审计 + deletedAt 软删），实体上 @SQLRestriction("deleted_at IS NULL") 让查询自动过滤软删行。

## 1. Auth 模块

- **AuthController / AuthService**：注册/登录/登出/刷新。登录成功后用 ResponseCookie 下发两个 HttpOnly Cookie（jwt 15 分钟、refresh 7 天）。密码 BCrypt 加密。
- **JwtAuthenticationFilter**（config/）：放在 UsernamePasswordAuthenticationFilter 之前，从 Cookie 里取 jwt 并校验，把 Authentication 塞进 SecurityContext。位置很关键：它在所有 Controller 之前，所以 Controller 里不需要再解析 token。
- **JwtService**：生成/解析 token，含 jti（对应 refresh_token 轮换族谱）。
- **RefreshTokenService**：轮换刷新令牌。 theft 检测（同族令牌被重复使用）时用 @Transactional(noRollbackFor = TokenTheftException.class) 让族谱撤销提交——这是 spec 0001 修复的经典事务坑，见下文 Q1。
- **SecurityConfig**：声明式安全链。注意它显式提供了空 UserDetailsService，避免 Boot 自动生成随机密码用户。

## 2. Application 模块（用户数据隔离的样板）

- **ApplicationController**：REST 风格 /applications，@PageableDefault 服务端分页；入参用 @Valid + JSR 380 注解校验。
- **ApplicationService**：所有读方法 getCurrentUser() 后做归属判断。两种写法并存（故意保留作教学对比）：
  - getApplicationByUser：先 findById 再手动比 userId → 数据不存在 404、存在但不是你的 403；
  - getAllApplicationsByUser：直接 findByUser(user) 查询，天然隔离。
- **ApplicationRepository**：@Query JPQL 组合搜索/过滤/分页；softDeleteAllByUser 用 @Modifying @Query 批量软删（注意 @Modifying 查询必须带 @Transactional）。
- **EncryptionService**：门户凭据 AES-256-GCM 加密存库（凭据保险箱）。密钥来自环境变量 ENCRYPTION_SECRET，不入代码。

## 3. A2 三模块（面试管理 / HR 沟通 / Offer 比较）

- **Interview / Communication / Offer 实体**：结构相同——ManyToOne(application, user) 双外键、枚举 @Enumerated(STRING)、审计+软删继承。user_id 冗余存一份，使“按用户聚合”查询不用 join。
- **InterviewService**：读方法 @Transactional(readOnly = true)（Hibernate 关闭脏检查快照，省性能）；写方法 @Transactional。所有“是否存在 + 是否属于你”逻辑收敛到 getOwnedApplication / getOwnedInterview 私有方法，Controller 不重复校验。
- **OfferService.normalizedAnnualPackage**：归一化年包 = (月薪×12 + 年奖金 + 年度股权) × 汇率。汇率是 application.properties 里的静态表（offer.currency-rate.*），刻意不调外部 API——本地部署零依赖。比较结果按分降序。
- **InterviewReminderController**：GET /interviews/upcoming?hours=24 专供前端浏览器轮询（每 5 分钟），刻意不引入消息队列/推送——当前阶段复杂度不划算。

## 4. A4 岗位匹配（JobMatchService）

- 纯 Java 算法：关键词加权命中。权重 职位 40 / 地点 25 / 公司 15 / 类型 10 / 附加关键词 每个 10（上限 40）。命中判定是 contains（大小写不敏感），天然兼容中英文——中文不用分词也能匹配“全栈工程师”这类整词。
- match() 事务只读；score() 是 package-private 纯函数，单测直接调用不碰数据库。
- AI 抽取中文化：SYSTEM_PROMPT 增加中文 JD 规则（职责/要求/薪资范围→salaryMin/Max/Currency CNY/USD/EUR），TOOL_SCHEMA 加了对应字段；本机无 Anthropic Key，测试全部走 mock，真实调用只在用户配置 Key 后发生。

## 5. 部署（A3）

- **docker-compose.yml**：jt-postgres(55432)/jt-app(8081)/jt-frontend(3101)，全部 127.0.0.1 绑定。健康检查 pg_isready + depends_on: service_healthy 保证应用启动时库已就绪。
- **Flyway 策略**：baseline-on-migrate=true + baseline-version=1。新库：V1 建基线表 → V2-V4 加业务表；旧库（已有数据无 history 表）：先 baseline 再补 V2-V4，不会重放 V1。测试里 Flyway 关闭，因为 RefreshTokenVersionColumnMigrationIT 需要用裸 SQL 造“没有 version 列的遗留表”来验证 ddl-auto=update 的加列行为。

---

# 面试常见问题 × 本项目答案

### Q1：@Transactional 什么场景会失效？你项目里踩过吗？
A：常见失效：同类内部自调用（绕过代理）、方法非 public、异常被 catch 吞掉、rollbackFor 默认只回滚 RuntimeException。本项目 spec 0001 是个真实案例：rotateRefreshToken 抛 RuntimeException 想触发 401，但同时把同事务里“撤销攻击者令牌族”的 UPDATE 也回滚了——安全操作直接失效。解法是 @Transactional(noRollbackFor = TokenTheftException.class)，让撤销提交、异常继续抛。另一个坑在 A3 修 Flyway 时确认过：@Modifying 的 JPQL 更新必须加 @Transactional（ApplicationRepository.softDeleteAllByUser）。

### Q2：JWT 放 Cookie 还是 localStorage？为什么？过滤器链怎么排？
A：jwt 放 HttpOnly Cookie（防 XSS 偷 token），refresh 放受限 Path=/auth/refresh 的独立 Cookie；localStorage 只存一个 username 作为“会话是否活跃”的前端标志（不是凭证）。过滤器链顺序：RateLimitFilter → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter，自定义过滤器必须插在标准过滤器之前才能让后续授权读到 SecurityContext。logout 时两个 Cookie 都要显式清（Max-Age=0）。

### Q3：软删怎么实现？查询会不会查到已删数据？
A：BaseEntity 有 deletedAt，实体类统一 @SQLRestriction("deleted_at IS NULL")（Hibernate 6 的注解），所有 JPQL/派生查询自动追加过滤条件，业务代码零感知。删除操作只是 setDeletedAt(now) + save。注意两点：@SQLRestriction 对 findById 也生效（删除后 findById 返回 empty，天然 404）；批量软删要用 @Modifying @Query 而不是循环 save。

### Q4：用户数据隔离怎么做？Controller 还是 Service 校验？
A：Service 层统一做。模式是：findById → 不存在抛 ResourceNotFoundException(404) → 存在但 user 不匹配抛 ResourceOwnershipException(403)。为什么放 Service：Controller 只做协议转换，避免每个端点重复校验逻辑；另外列表类查询直接 findByUser(user)，从 SQL 层就只可能看到自己的数据。越权测试（IT 里另一个用户访问 403）是回归护栏。

### Q5：Flyway 版本号怎么管理？ddl-auto 和 Flyway 冲突吗？
A：V1 是基线（users/applications/refresh_tokens），V2-V4 是后续新增表，新脚本只追加不改历史。baseline-on-migrate=true 解决“老库没有 history 表”的接入问题：老库先打 baseline 标记（默认版本 1），从 V2 开始补跑。ddl-auto=update 保留作兜底（比如给 refresh_tokens 补 version 列这类历史遗留），两者可共存——Flyway 先跑建表，update 只做增量补列。

### Q6：为什么 Offer 汇率用静态配置而不是调汇率 API？
A：三个理由：本地部署零外部依赖（离线可用）；汇率精度对求职决策足够（误差远小于 offer 谈判空间）；不引入网络故障面。实现上用 @Value 注入 offer.currency-rate.*，换汇率只需改环境变量。面试里这是“复杂度权衡”的好例子：技术方案要匹配产品阶段。

### Q7：测试金字塔在本项目怎么落地的？
A：三层——Mockito 单测（Service 纯逻辑，如 JobMatchService.score() 直接测算法，不碰 DB）；@WebMvcTest 切片测试（Controller + Security 链）；Testcontainers IT（真 Postgres + MockMvc，覆盖事务/约束/越权这些 mock 测不出的东西）。关键经验：所有集成测试用例都要有“越权访问”用例；Mockito 严格模式（UnnecessaryStubbing）会逼你保持测试干净。

### Q8：容器化部署时踩过什么坑？
A：三个真实的：① 网络 MITM 代理让容器内 Maven/npm 下载随机失败——解法是宿主机构建产物（jar/二进制 tarball）COPY 进镜像；② Alpine 容器缺 linux-musl 原生二进制（lightningcss/next-swc）——从 registry 下载对应平台 tarball 在构建期解包；③ docker.io 认证被劫持——换国内镜像源拉基础镜像。教训：可重复部署的关键是让构建路径在网络受限环境下也能走通。

## 附：文件索引（A2-A4 新增）

| 文件 | 一句话职责 |
|---|---|
| service/InterviewService | 面试 CRUD + upcoming 提醒窗口查询 |
| service/CommunicationService | HR 沟通记录 CRUD |
| service/OfferService | Offer CRUD + compareOffers 归一化比较 |
| service/JobMatchService | 中英 JD 匹配评分（纯函数核心） |
| service/AiExtractionService | Anthropic 抽取（中文 JD 规则 + salary 字段） |
| controller/Interview*Controller | /applications/{id}/interviews、/interviews/upcoming |
| controller/CommunicationController | /applications/{id}/communications |
| controller/OfferController / OfferComparisonController | /applications/{id}/offers、/offers/compare |
| controller/JobMatchController | /applications/{id}/match |
| db/migration/V1-V4 | 基线 + 面试/沟通/Offer 表迁移 |
| frontend/lib/i18n.ts | 中英双语字典（默认 zh-CN） |
| frontend/components/detail/* | 详情页时间线/沟通/Offer 面板 |
| frontend/app/(app)/offers/page.tsx | 权重可调的 Offer 对比页 |
| frontend/components/dashboard/JobMatchCard.tsx | 岗位匹配评分卡片 |
