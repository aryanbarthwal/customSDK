// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "customSDK",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(
            name: "customSDK",
            targets: ["MySdkShared"],
        ),
    ],
    targets: [
        .binaryTarget(
            name: "MySdkShared",
            path: "spm/MySdkShared.xcframework",
        ),
    ],
)
