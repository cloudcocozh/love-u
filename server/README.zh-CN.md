# Love U 情侣中转服务

[English / HTTP 协议](README.md) · [返回项目说明](../README.md)

服务使用 Python 标准库及 SQLite，处理账户、公钥、邀请配对和加密信封转发。服务本身不负责解密消息；它仍可看到关系和投递元数据。Android 端点对点通道属于可选能力，不代替离线中转队列。

## 本地运行

需要 Python 3.11 或更高版本：

```sh
python -m unittest discover -s server -v
python server/service.py --db server/relay.sqlite
```

默认仅监听 `127.0.0.1:8878`。测试使用独立临时数据库和合成账号，不接入现有个人资料。

## Docker 部署

```sh
cd server
docker compose up -d --build
```

`compose.yaml` 将数据保存到命名卷，并仅向宿主机回环地址暴露端口。不要直接把未加密的 HTTP 端口发布到互联网。使用可信 HTTPS 反向代理，例如将 `https://relay.example.com/love-u/` 转发到 `http://127.0.0.1:8878/`，转发时去掉 `/love-u` 前缀。

在 Android 工程的 `local.properties` 设置相同的 `loveu.relayUrl` 后重新构建。两部手机必须使用同一服务。访问 `/love-u/health` 应返回 `{"status":"ok"}`。

反向代理应限制连接数和并发，保留客户端正常访问所需的超时，并避免记录 Authorization 请求头、令牌或请求体。妥善保护数据卷并定期备份。该实现面向小规模私人部署，尚未经过大型公开服务的容量验收。

## 队列与保留

- 接收方应先持久化消息，再确认接收。
- 同一配对中的相同消息 ID 和内容可安全重试；不同内容会冲突。
- 信封保留期为七天，过期项在认证请求时清理。超出期限的离线消息不保证送达。
- 解除配对会清除服务器对应配对的消息队列，不会删除手机内的个人历史。
- 默认限制为每个来源 IP/账号每分钟 120 请求、接收方 500 条未确认信封、每对 10,000 条保留信封、总计 10,000 账号。详见服务端常量与英文协议。

可选的 `smoke.py` 会在指定服务器创建两个合成账号并测试配对、收发及解除配对；账号行会保留，仅在你管理的测试服务上主动执行。

```sh
python server/smoke.py https://relay.example.com/love-u
```
