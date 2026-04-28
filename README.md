# customSDK

`customSDK` is a standalone Kotlin Multiplatform SDK project that packages Android-first shared functionality for API calls, Server-Sent Events, local database storage, and an SDK-owned Android UI entry point exposed through `MySdk`.

## Installation

This SDK is intended to be installed through JitPack.

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

dependencies {
    implementation("com.github.aryanbarthwal:customSDK:<version>")
}
```

## Basic usage

```kotlin
MySdk.init(
    SdkConfig(
        baseUrl = "https://jsonplaceholder.typicode.com",
        apiKey = null,
    ),
)

MySdk.openSdk()
```

## Android requirements

- Add the `INTERNET` permission to the host app manifest.
- The SDK auto-initializes its Android context internally; no extra host-side setup is required before calling `MySdk.init(...)`.
