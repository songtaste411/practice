# 第 1 层：本机改代码

这一层最快。数据库在内存里，关掉程序数据就没了。适合写病例、改策略、看日志。

## 你需要

- JDK 21
- Maven 3.9 以上
- Node.js 20 以上（只要网页）

这台电脑的默认 `java` 可能还是 8。用项目脚本会改用已安装的 JDK 21。

## 启动后端

在项目根目录：

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\run-api.ps1
```

看到日志里有「老兵诊所已启动」和「演示账号 demo / demo123」就通了。

检查：http://localhost:8080/actuator/health  
应返回 `{"status":"UP"}`。

## 启动网页

另开一个终端：

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\run-web.ps1
```

打开 http://localhost:5173  
账号 `demo`，密码 `demo123`。

网页的 `/api` 会被转到 8080，不用改地址。

## 这一层没有的东西

Redis 关着。商品缓存走的是「每次查数据库」的实现。  
要看 Redis，用第 3 层。
