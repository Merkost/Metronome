import AppIntents
#if canImport(ComposeApp) && !WIDGET_EXTENSION
import ComposeApp
#endif

struct TogglePlaybackIntent: AudioPlaybackIntent {
    static var title: LocalizedStringResource = "Play or pause metronome"

    func perform() async throws -> some IntentResult {
        #if canImport(ComposeApp) && !WIDGET_EXTENSION
        LiveActivityBridgeKt.togglePlayback()
        #endif
        return .result()
    }
}
