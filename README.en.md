# Love U

**Private records. Thoughtful care.** An Android intimacy and period journal.

**[简体中文](README.md) · English**

Source version **1.9.0** · Android **8.0+** · Java · Self-hostable couple relay

## Features

| Area | Features |
| --- | --- |
| Quick logging | Exact date and time, multiple entries per day, stopwatch, fill in later, templates, notes and optional location |
| Calendar and search | Daily counts, month browsing, date and field filters, historical search |
| Insights | Annual/monthly comparisons, yearly bars, calendar heatmap, duration and weekday distributions, explicit unknown values |
| Period care | Record your own or a partner's periods, backfill and delete entries, retain history, analyze cycles, estimate the next date and countdown |
| Partner | Invitation pairing, safety-code verification, encrypted messages, offline queue, plans with mutual confirmation and preference matching |
| Shared entries | Share individual or bulk history and periods, reactions and per-entry comments; optional background notifications |
| Privacy | Encrypted local storage, PIN and biometric unlock, screenshot protection, passphrase-encrypted backups and CSV import |
| Appearance | Classic interface plus the optional WorkBuddy interface; three palettes and light/dark modes |
| Languages | Simplified/Traditional Chinese, English, Japanese, Korean, Spanish, French, German, Portuguese and system preference |

Choose a language under **Me → Language**. Personal notes, messages, addresses and custom content stay in their original language and are not sent to translation services. Some longer help text is machine-assisted and still needs native-language review.

## Install and use

**[Download the Android APK](https://github.com/cloudcocozh/love-u/releases/latest)** (public download; no GitHub sign-in required).

The source tree contains no APKs, release private keys or personal records. Signed APKs are available separately on the release page. New installations start empty. The original signed distribution uses the existing private NAS relay; portable source builds require your own relay configuration.

1. Obtain a trusted signed APK from the maintainer or build your own.
2. Follow Android's installation prompts for the selected source.
3. Select your appearance, language and preferences, then optionally configure PIN/biometrics.
4. Grant location permission only when you want automatic addresses. Background message notifications can be enabled or disabled.
5. Both partners must use the same relay. Compare the safety codes in person or through a trusted channel before confirming the pairing.

Export a passphrase-encrypted backup before upgrading. **In-place upgrades require the same package name and signing key. Do not uninstall an installation containing records just to install a differently signed build.** The original release key is not provided; a locally built debug APK will usually not update an existing distributed app.

This repository provides Android source, not a native iOS project. Universal Android builds include `arm64-v8a`, `armeabi-v7a`, `x86` and `x86_64`. Supported configurations do not imply that every phone model has been tested.

## Build

Install JDK **17**, Android SDK Platform **35** and Build Tools **35.0.0**. The repository includes the Gradle **8.10.2** Wrapper and uses Android Gradle Plugin **8.7.3**. The first build needs network access for Gradle and Maven dependencies.

```powershell
git clone https://github.com/cloudcocozh/love-u.git
cd love-u
# Set JAVA_HOME and ANDROID_HOME, or specify sdk.dir in local.properties.
.\gradlew.bat assembleDebug
```

On macOS/Linux, run `./gradlew assembleDebug`. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Configure your relay

Copy `local.properties.example` to `local.properties`, then set your SDK path and HTTPS endpoint:

```properties
sdk.dir=C:/Android/Sdk
loveu.relayUrl=https://relay.example.com/love-u
```

Git ignores `local.properties`. Without a configured endpoint, the build uses a non-resolving example address: personal offline logging works, but networked couple features do not. This export uses system DNS and does not include the private deployment's fixed IP or alternative DNS rules.

See the **[English relay guide](server/README.md)** or **[中文部署说明](server/README.zh-CN.md)**.

### Tests and release build

```powershell
# Run 15 Java core suites, build debug/release APKs and run release Lint.
.\build.ps1
# Isolated local relay tests: Python 3.11+, standard library only.
python -m unittest discover -s server -v
```

`build.ps1` requires Windows PowerShell and `JAVA_HOME`. On first use it downloads and verifies a pinned test dependency. Use `-Offline` with an existing cache, or `-JsonJar <path>` to supply the verified JSON test library. Other platforms can build Android and run Lint with the Wrapper.

```powershell
.\gradlew.bat assembleRelease lintRelease
```

The release output is `app/build/outputs/apk/release/app-release-unsigned.apk`. Sign it with your own protected release key. Never commit signing keys or passwords. Preparing this export did not change the original development checkout's signing or any phone installation.

## Data and privacy

- Personal history is stored in an encrypted on-device vault. Android automatic backup is disabled; keep your own passphrase-encrypted backups.
- Shared entries are user-initiated encrypted messages. The relay stores public keys, pairing metadata, token hashes and encrypted envelopes. It can observe connection and delivery metadata; encryption does not provide anonymity.
- Exported backups omit couple account credentials and device unlock settings. Reconfigure authentication and pairing after restoring on a new device.
- Address lookup uses system location/geocoding services. Accuracy and address detail depend on the device, permissions and environment.
- Period forecasts are estimates based on recorded history. Missing or irregular history affects them; they are not a diagnostic or contraceptive tool.
- The cryptographic implementation has not undergone an independent security audit. Optional direct connections depend on network conditions and are not guaranteed on every network.

The export excludes historical Git commits, private records, backups, keys, personal server configuration, device diagnostic utilities and internal installation logs. It includes current app source, a generic relay, synthetic tests and required resources. Real phones on different networks and vendor background restrictions still require separate acceptance testing.

## Structure

```text
app/src/main/       Android app, UI, domain logic and offline translations
app/libs/           Pinned WebRTC library
tests/              15 core Java suites and a CSV inspection helper
server/             Python relay, Docker configuration and protocol tests
docs/               Build verification and third-party notices
build.ps1           Windows test and build entry point
```

## License and feedback

No open-source license has been granted for this project yet. Access to the source does not grant redistribution or commercial-use rights. Third-party libraries and fonts retain their own licenses; see [third-party notices](docs/THIRD_PARTY.md).

When reporting a problem, include the app version, Android version, steps and expected result. Remove private entries, addresses, invitation codes and safety codes from screenshots first.
