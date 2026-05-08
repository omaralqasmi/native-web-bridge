// swift-tools-version: 5.7
import PackageDescription

let package = Package(
    name: "NativeWebBridge",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "NativeWebBridge",
            targets: ["NativeWebBridge"]),
    ],
    dependencies: [],
    targets: [
        .target(
            name: "NativeWebBridge",
            dependencies: [],
            path: "native-web-bridge-ios/Sources/NativeWebBridge"
        )
    ]
)