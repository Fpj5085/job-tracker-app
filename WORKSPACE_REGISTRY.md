# WORKSPACE REGISTRY — A 项目容器与端口约定

> 本文件是容器命名/端口的权威约定（A3 建立；此前任务单引用的注册表不存在，故以此为准）。

| 资源 | 约定 |
|---|---|
| 数据库容器 | jt-postgres（postgres:17，卷 jt-pgdata） |
| 后端容器 | jt-app（Spring Boot，端口 8081） |
| 前端容器 | jt-frontend（Next.js standalone，端口 3101） |
| 数据库主机端口 | 127.0.0.1:55432 |
| 全部端口绑定 | 仅 127.0.0.1，禁止公网暴露 |
| Compose 项目 | K:\Fpj Project\A-job-tracker\docker-compose.yml |
| 演示数据 | 仅合成数据 / demo@example.com，凭据保险箱只用 DemoPortal 虚构凭据 |
