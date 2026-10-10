# 第 2 层：打成镜像

业务和依赖分开。这里只把后端、网页打成镜像，数据库仍建议用第 3 层一起起。

国内机器默认用华为云镜像加速。拉不动时，把 Dockerfile 第一行的 `ARG` 改成官方镜像名，例如 `maven:3.9.9-eclipse-temurin-21`。

```powershell
docker build -t prod-clinic-api:latest ./backend
docker build -t prod-clinic-web:latest ./frontend
```

后端镜像自己运行时会走默认的内存库，因为没人给它 `SPRING_PROFILES_ACTIVE=docker`。  
要连 Postgres，用第 3 层，或者：

```powershell
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker prod-clinic-api:latest
```

这时容器里的主机名 `postgres`、`redis` 必须真的能解析到。单独 run 一个后端是不够的。
