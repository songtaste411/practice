# 第 3 层：Compose 一键全套

这一层才像生产：Postgres、Redis、后端、网页。

```powershell
docker compose up -d --build
```

| 你打开什么 | 地址 |
| --- | --- |
| 网页 | http://localhost:18080 |
| 后端健康检查 | http://localhost:8080/actuator/health |
| Postgres | localhost:5432，库 `prodclinic`，账号密码都是 `clinic` |
| Redis | localhost:6379 |

数据在项目下的 `volumes/`。想清空重来：

```powershell
docker compose down
Remove-Item -Recurse -Force .\volumes
docker compose up -d --build
```

账号仍是 `demo` / `demo123`。第一次启动会自动写入三件商品。

这一层 Redis 是开的。商品名字和价格会缓存 30 秒。库存不进缓存。
