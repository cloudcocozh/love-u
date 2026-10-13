# Source export verification / 源码导出核验

Date / 日期：2026-10-13 · Version / 版本：1.9.0 (21)

| Check / 检查 | Result / 结果 |
| --- | --- |
| Java domain regression suites / 核心回归 | 15 passed / 15 项通过 |
| Relay protocol tests / 中转协议 | 11 passed, isolated localhost / 11 项通过，隔离本机测试 |
| Debug and unsigned release build / 调试及未签名发布构建 | Passed using the exported checkout / 导出副本构建通过 |
| Release Lint | 0 errors, 64 warnings / 0 错误，64 警告 |
| Translation resources / 语言包 | 8 additional packs, 1,374 matching keys each / 8 个附加语言包，各 1,374 个一致的键 |
| New documentation relative links / 新文档相对链接 | Resolved / 已检查目标存在 |
| Tracked upload list / 上传清单 | No private deployment address, data backup, vault, signing key, local SDK path or APK / 不含私人部署地址、备份、资料库、签名密钥、本机 SDK 路径及 APK |

The Android source and resources match the development checkout except for `CoupleApi.java`, which reads the configured relay URL and uses system DNS. Build configuration, build scripts and the CSV inspection helper were made portable. Existing developer history and device-only diagnostic tooling were deliberately excluded. Release signing is left to the operator.

Android 主源码与资源保持开发版本内容，唯一调整的主代码文件为 `CoupleApi.java`：从构建配置读取服务地址，使用系统 DNS。构建配置、脚本与 CSV 检查工具改为通用形式。未上传开发历史和设备专用诊断工具，发布签名由构建者自行配置。

The initial sandboxed relay test run could not open loopback sockets. The same tests passed after running with local networking permitted. No tests connected to a production relay or used real personal records.

首次受限环境中的服务测试被本机回环网络权限阻止；允许本地网络后，同一组测试通过。没有连接生产中转服务，也未使用真实个人记录。

This check did not install an app, change production services, validate a fresh machine with no dependency cache, test Docker deployment, or verify two real phones across networks. Existing Lint warnings and independent security/native-language review remain outstanding.

本次未安装手机、修改生产服务、验证完全无依赖缓存的新机器、实测 Docker 部署或验证两部真实手机跨网络。现有 Lint 警告、独立安全审计和完整母语审校仍未完成。
