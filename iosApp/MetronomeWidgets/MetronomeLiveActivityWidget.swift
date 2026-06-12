import ActivityKit
import SwiftUI
import WidgetKit

struct MetronomeLiveActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: MetronomeActivityAttributes.self) { context in
            LockScreenView(state: context.state, isStale: context.isStale)
                .activityBackgroundTint(Color.black.opacity(0.55))
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("\(context.state.bpm)")
                            .font(.system(size: 34, weight: .heavy, design: .rounded))
                        Text("\(context.state.tempoName) · \(context.state.timeSignature)")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                    .padding(.leading, 4)
                }
                DynamicIslandExpandedRegion(.trailing) {
                    TimerView(state: context.state)
                        .font(.system(.title3, design: .rounded).weight(.semibold))
                        .frame(maxWidth: 64)
                        .padding(.trailing, 4)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    Button(intent: TogglePlaybackIntent()) {
                        Label(
                            context.state.isPlaying ? "Pause" : "Play",
                            systemImage: context.state.isPlaying ? "pause.fill" : "play.fill"
                        )
                        .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                    .tint(.accentColor)
                }
            } compactLeading: {
                Image(systemName: context.state.isPlaying ? "play.fill" : "pause.fill")
            } compactTrailing: {
                CompactValueView(state: context.state)
            } minimal: {
                CompactValueView(state: context.state)
            }
        }
    }
}

private struct CompactValueView: View {
    let state: MetronomeActivityAttributes.ContentState

    var body: some View {
        if let view = timerText {
            view.monospacedDigit().frame(maxWidth: 52)
        } else {
            Text("\(state.bpm)")
        }
    }

    private var timerText: Text? {
        if state.isPlaying, state.timerKind == "countdown", let end = state.timerEnd {
            return Text(timerInterval: Date.now...max(end, Date.now), countsDown: true)
        }
        if state.isPlaying, state.timerKind == "stopwatch", let start = state.timerStart {
            return Text(timerInterval: start...Date.distantFuture, countsDown: false)
        }
        if let frozen = state.timerFrozenSeconds, state.timerKind != "none" {
            return Text(Self.format(frozen))
        }
        return nil
    }

    static func format(_ seconds: Int) -> String {
        String(format: "%d:%02d", seconds / 60, seconds % 60)
    }
}

private struct TimerView: View {
    let state: MetronomeActivityAttributes.ContentState

    var body: some View {
        if state.timerKind == "none" {
            EmptyView()
        } else if state.isPlaying, state.timerKind == "countdown", let end = state.timerEnd {
            ProgressView(timerInterval: Date.now...max(end, Date.now)) {
                EmptyView()
            } currentValueLabel: {
                Text(timerInterval: Date.now...max(end, Date.now), countsDown: true)
            }
            .progressViewStyle(.circular)
        } else if state.isPlaying, state.timerKind == "stopwatch", let start = state.timerStart {
            Text(timerInterval: start...Date.distantFuture, countsDown: false)
        } else if let frozen = state.timerFrozenSeconds {
            Text(CompactValueView.format(frozen))
        }
    }
}

private struct LockScreenView: View {
    let state: MetronomeActivityAttributes.ContentState
    let isStale: Bool

    var body: some View {
        if isStale {
            Text("Session ended")
                .font(.callout)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity)
                .padding()
        } else {
            HStack(spacing: 16) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("\(state.bpm) BPM")
                        .font(.system(size: 28, weight: .heavy, design: .rounded))
                    Text("\(state.tempoName) · \(state.timeSignature)")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                TimerView(state: state)
                    .font(.system(.title3, design: .rounded).weight(.semibold))
                Button(intent: TogglePlaybackIntent()) {
                    Image(systemName: state.isPlaying ? "pause.fill" : "play.fill")
                        .font(.title3)
                }
                .buttonStyle(.bordered)
                .clipShape(Circle())
            }
            .padding()
        }
    }
}
