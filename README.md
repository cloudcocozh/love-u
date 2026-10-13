# Love U

**私密记录，认真关怀。** 一款 Android 亲密生活与经期记录应用。

**简体中文 · [English](README.en.md)**

当前源码版本：**1.9.0** · Android **8.0+** · Java · 可自托管情侣中转服务

## 功能

| 模块 | 内容 |
| --- | --- |
| 快速记录 | 日期与具体时间、一天多条记录、计时、稍后补充、默认模板、备注与可选定位 |
| 日历与检索 | 按日查看次数、月份浏览、日期及条件筛选、历史记录搜索 |
| 洞察 | 年度/月度对比、全年柱状图、日历热力图、时长和星期分布、已知与未知数据区分 |
| 经期 | 主动记录自己或伴侣的经期、补记与删除、长期历史、周期分析、下次预计日期和倒计时 |
| 爱人 | 邀请配对、安全码核对、加密消息、离线队列、预约与双方确认、偏好默契互动 |
| 共同记录 | 分享单条或批量历史、经期分享、点赞、吐槽和针对记录的留言；后台消息通知可关闭 |
| 隐私 | 本地加密存储、PIN、指纹解锁、防截图、口令加密备份与 CSV 导入 |
| 外观 | 保留经典界面，增加 WorkBuddy 界面；三套配色、白天/黑夜模式 |
| 语言 | 简体中文、繁体中文、英语、日语、韩语、西班牙语、法语、德语、葡萄牙语，以及跟随系统 |

语言在「我的 → 语言 / Language」切换。私人备注、消息、地址与自定义内容保留原文，不发送到翻译服务。部分长帮助文案使用机器辅助翻译，仍需要母语审校。

## 使用与安装

**[下载 Android 安装包](https://github.com/cloudcocozh/love-u/releases/latest)**（私有仓库，需要获授权的 GitHub 账号）。

源码树不包含 APK、签名私钥或任何个人记录；已签名 APK 单独放在版本下载页。新安装从空白记录开始。版本页的原签名分发包使用现有私人 NAS 中转，通用源码构建则需要配置自己的中转地址。

1. 从维护者取得可信的签名 APK，或按下方步骤自行构建。
2. 在 Android 安装提示中，按系统要求允许当前来源安装。
3. 首次打开选择外观、语言和个人偏好，按需要设置 PIN/指纹。
4. 只有需要自动地址时才授予定位权限；后台消息通知可以自行开启或关闭。
5. 情侣功能需要双方使用相同的中转服务，邀请配对后应当面对面或通过可信渠道核对安全码。

升级前导出口令加密备份。**相同包名的覆盖升级必须使用相同签名；不要为了安装不同签名的构建而卸载有记录的版本。** 本仓库不提供原发布签名，重新构建的调试包通常不能覆盖已安装的正式版本。

本应用当前提供 Android 源码，未提供原生 iOS 工程。通用 Android 构建包含 `arm64-v8a`、`armeabi-v7a`、`x86`、`x86_64` 库；支持范围不等于每款手机都已实测。

## 编译

需要 JDK **17**、Android SDK Platform **35** 与 Build Tools **35.0.0**。仓库提供 Gradle **8.10.2** Wrapper，Android Gradle Plugin 为 **8.7.3**。首次构建需要联网下载 Gradle 和 Maven 依赖。

```powershell
git clone https://github.com/cloudcocozh/love-u.git
cd love-u
# 设置 JAVA_HOME 和 ANDROID_HOME，或在 local.properties 填写 sdk.dir。
.\gradlew.bat assembleDebug
```

macOS / Linux 使用 `./gradlew assembleDebug`。生成的调试 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

### 配置自己的情侣服务

将 `local.properties.example` 复制为 `local.properties`，填写 SDK 路径和你的 HTTPS 服务地址：

```properties
sdk.dir=C:/Android/Sdk
loveu.relayUrl=https://relay.example.com/love-u
```

`local.properties` 已排除出 Git。未配置时，构建使用不可解析的示例地址，个人本地记录可用，情侣联网功能不可用。公开源码使用系统 DNS，不含原私人部署的固定 IP 或备用 DNS 规则。

服务部署见 **[中文部署说明](server/README.zh-CN.md)** / **[English relay guide](server/README.md)**。

### 测试与发布包

```powershell
# 15 项 Java 核心测试，然后构建调试/发布包并运行发布 Lint。
.\build.ps1
# 中转服务的隔离本地测试；Python 3.11+，仅标准库。
python -m unittest discover -s server -v
```

`build.ps1` 需要 Windows PowerShell 和 `JAVA_HOME`，首次会下载并校验固定版本的测试依赖。已有缓存时可用 `-Offline`；也可用 `-JsonJar <路径>` 指定经校验的 JSON 测试库。其他平台可用 Wrapper 完成 Android 构建和 Lint。

```powershell
.\gradlew.bat assembleRelease lintRelease
```

发布产物为 `app/build/outputs/apk/release/app-release-unsigned.apk`。请用自己妥善保存的发布密钥签名；不要将密钥或密码提交到仓库。此源码导出没有改变原开发目录的签名和手机安装。

## 数据和隐私

- 日记默认保存在设备的加密资料库中，Android 系统自动备份关闭；请主动保存口令加密备份。
- 共享记录是用户发起的加密消息。中转服务保存账户公钥、配对关系、令牌哈希及加密信封，也能观察连接和投递元数据；这不等于匿名通信。
- 备份会排除情侣账号凭据和设备解锁信息。换机恢复日记后，需要重新配置解锁与配对。
- 地址解析依赖系统定位/地理编码服务，精度和地址完整度因设备、权限和环境而异。
- 经期日期是基于已有记录的估算，缺失或不规律的历史会影响结果；不用于诊断或避孕判断。
- 加密实现尚未经过独立安全审计。可选直连受网络条件影响，不保证任何网络都能建立点对点通道。

本次上传排除了历史 Git 提交、私人记录、备份、密钥、私人服务器配置、设备诊断工具和内部安装日志。仓库只含当前应用源码、通用服务端、合成测试和必要资源。两部真实手机跨网络及厂商后台策略仍需要独立验收，不能用本地测试替代。

## 目录

```text
app/src/main/       Android 应用、界面、业务逻辑与离线语言包
app/libs/           固定版本 WebRTC 库
tests/              15 项核心 Java 测试及 CSV 检查辅助程序
server/             Python 中转服务、Docker 配置与协议测试
docs/               构建核验与第三方资源说明
build.ps1           Windows 核心测试及构建入口
```

## 许可与反馈

当前仓库尚未授予开源许可证；访问源码不代表取得再分发或商业使用授权。第三方库和字体仍按各自许可证使用，见 [第三方资源说明](docs/THIRD_PARTY.md)。

反馈问题时请描述版本、设备系统、操作步骤与期望结果；截图请先去除私人记录、地址、邀请码和安全码。
