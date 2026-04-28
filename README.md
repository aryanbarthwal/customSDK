# customSDK

`customSDK` is a standalone Kotlin Multiplatform SDK project that packages Android-first shared functionality for API calls, Server-Sent Events, local database storage, and an SDK-owned Android UI entry point exposed through `MySdk`.

## Requirements in the consumer project

To use this SDK in another Android project, the consumer app must do all of the following.

### 1. Add JitPack in `settings.gradle.kts`

Gradle needs a repository that knows how to build and serve artifacts from GitHub tags. For this project, that repository is JitPack.

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

If the consumer project uses strict repository filtering, include both groups because the published Kotlin Multiplatform metadata resolves Android artifacts through a nested JitPack group:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") {
            content {
                includeGroup("com.github.aryanbarthwal")
                includeGroupByRegex("com\\.github\\.aryanbarthwal\\.customSDK.*")
            }
        }
    }
}
```

### 2. Add the SDK dependency in the app module

This tells Gradle to fetch the SDK built from the GitHub tag.

```kotlin
dependencies {
    implementation("com.github.aryanbarthwal:customSDK:v1.0.0")
}
```

### 3. Add Android permission in the host app manifest

The SDK performs network calls, so the consumer app must allow internet access.

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 4. Initialize the SDK before use

`MySdk` must be initialized once before calling API, DB, UI, or SSE functions.

```kotlin
MySdk.init(
    SdkConfig(
        baseUrl = "https://jsonplaceholder.typicode.com",
        apiKey = null,
    ),
)
```

The SDK handles its Android context bootstrap internally, so no extra host-side setup is needed before `MySdk.init(...)`.

## Minimal setup example

### `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

### `app/build.gradle.kts`

```kotlin
dependencies {
    implementation("com.github.aryanbarthwal:customSDK:v1.0.0")
}
```

### `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Basic usage

```kotlin
import com.example.mysdk.MySdk
import com.example.mysdk.SdkConfig

MySdk.init(
    SdkConfig(
        baseUrl = "https://jsonplaceholder.typicode.com",
        apiKey = null,
    ),
)

MySdk.openSdk()
```

## Public API example

```kotlin
MySdk.closeSdk()
MySdk.isSdkVisible()
MySdk.fetchDemoPost()
MySdk.insertName("Test")
MySdk.getSavedNames()
MySdk.connectToServerSentEvents(...)
```

## How installation works

When another Android project adds:

```kotlin
implementation("com.github.aryanbarthwal:customSDK:v1.0.0")
```

the flow is:

1. Gradle checks the repositories declared in `settings.gradle.kts`.
2. JitPack receives the request for `aryanbarthwal/customSDK` at tag `v1.0.0`.
3. JitPack builds the GitHub repository and publishes Maven/Gradle artifacts for that tag.
4. Gradle downloads those artifacts into the local Gradle cache.
5. Android Studio indexes the classes from the downloaded library, so imports such as `import com.example.mysdk.MySdk` work without copying SDK source code into the app.

This is why the SDK does not appear as a normal source folder inside the consumer app repository. The consumer app links against the published library artifact, typically visible under **External Libraries** or in the Gradle cache, not as a checked-in local project.

## iOS installation via Swift Package Manager

The iOS side is distributed as an XCFramework wrapped by a Swift Package.

### Consumer project requirements

1. In Xcode, choose **Add Package Dependency**
2. Use the repository URL:

```text
https://github.com/aryanbarthwal/customSDK.git
```

3. Select the desired version tag
4. Add the `customSDK` package product to your iOS app target

### Import in Swift

```swift
import MySdkShared
```

### Basic Swift usage

```swift
let sdk = MySdk.shared

sdk.doInit(
    config: SdkConfig(
        baseUrl: "https://jsonplaceholder.typicode.com",
        apiKey: nil
    )
)

sdk.openSdk()
```

### Notes for iOS

- The current iOS bridge in this repository is functionality-first.
- API, SSE, database, and visibility/open-close state are available from the shared SDK.
- The current iOS `openSdk()` / `closeSdk()` behavior manages SDK visibility state; host-side iOS UI can react to that state while the iOS presentation layer evolves further.
