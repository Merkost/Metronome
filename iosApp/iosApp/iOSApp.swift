import SwiftUI
import FirebaseCore
import ComposeApp

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        InitKoinKt.doInitKoin(liveActivityController: MetronomeLiveActivityManager())
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
