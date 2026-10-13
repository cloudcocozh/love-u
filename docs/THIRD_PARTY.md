# Third-party resources / 第三方资源

Dependencies and bundled resources retain their respective licenses. This list does not grant a license to the Love U application itself.

依赖和资源遵守各自许可证；本清单不构成对 Love U 应用源码的开源授权。

| Resource | Version / location | License notice |
| --- | --- | --- |
| WebRTC Android AAR | `app/libs/webrtc-150.7871.01.aar` | [WebRTC notices](../app/src/main/assets/licenses/WEBRTC.md), [packaging license](../app/src/main/assets/licenses/WEBRTC-SDK-PACKAGING-LICENSE.txt) |
| OkHttp | `com.squareup.okhttp3:okhttp:4.12.0` | Apache-2.0; transitive dependencies retain their own notices |
| DM Sans | `app/src/main/assets/workbuddy-fonts/DMSans.ttf` | [SIL OFL](../app/src/main/assets/workbuddy-fonts/DM-Sans-OFL.txt) |
| LoveU UI SC | Renamed subset of Noto Sans SC | [SIL OFL](../app/src/main/assets/workbuddy-fonts/Noto-Sans-SC-OFL.txt), [font source](../app/src/main/assets/workbuddy-fonts/SOURCE.md) |
| Gradle Wrapper | 8.10.2 | Generated wrapper, Apache-2.0 |
| Android Gradle Plugin | 8.7.3 | Build-time dependency, Apache-2.0 |
| JSON-java | `org.json:json:20240303` | Test-only dependency; its upstream license applies; fetched rather than bundled |

The offline translation JSON files contain interface strings, not translation models. Some draft translations were prepared with Argos Translate; no translation service is contacted at runtime.

离线 JSON 语言包仅包含界面文案，不含翻译模型。部分翻译草稿由 Argos Translate 辅助完成，运行时不访问翻译服务。
