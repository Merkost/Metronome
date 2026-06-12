import ActivityKit
import ComposeApp
import Foundation

final class MetronomeLiveActivityManager: LiveActivityController {
    init() {
        end()
    }

    func start(snapshot: LiveActivitySnapshot) {
        guard #available(iOS 17.0, *) else { return }
        Task { @MainActor in
            guard ActivityAuthorizationInfo().areActivitiesEnabled else { return }
            if !Activity<MetronomeActivityAttributes>.activities.isEmpty {
                await Self.push(snapshot)
                return
            }
            _ = try? Activity.request(
                attributes: MetronomeActivityAttributes(),
                content: Self.content(snapshot)
            )
        }
    }

    func update(snapshot: LiveActivitySnapshot) {
        guard #available(iOS 17.0, *) else { return }
        Task { @MainActor in
            await Self.push(snapshot)
        }
    }

    func end() {
        guard #available(iOS 17.0, *) else { return }
        Task { @MainActor in
            for activity in Activity<MetronomeActivityAttributes>.activities {
                await activity.end(activity.content, dismissalPolicy: .immediate)
            }
        }
    }

    @available(iOS 17.0, *)
    @MainActor
    private static func push(_ snapshot: LiveActivitySnapshot) async {
        for activity in Activity<MetronomeActivityAttributes>.activities {
            await activity.update(content(snapshot))
        }
    }

    @available(iOS 17.0, *)
    private static func content(_ s: LiveActivitySnapshot) -> ActivityContent<MetronomeActivityAttributes.ContentState> {
        ActivityContent(
            state: MetronomeActivityAttributes.ContentState(
                isPlaying: s.isPlaying,
                bpm: Int(truncating: s.bpm as NSNumber),
                tempoName: s.tempoName,
                timeSignature: s.timeSignatureLabel,
                timerKind: s.timerKind.name.lowercased(),
                timerStart: s.timerStartEpochMillis.map { Date(timeIntervalSince1970: $0.doubleValue / 1000.0) },
                timerEnd: s.timerEndEpochMillis.map { Date(timeIntervalSince1970: $0.doubleValue / 1000.0) },
                timerFrozenSeconds: s.timerFrozenMillis.map { Int(truncating: $0) / 1000 }
            ),
            staleDate: Date().addingTimeInterval(180)
        )
    }
}
