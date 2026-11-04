import ActivityKit
import Foundation

enum LiveActivityTimerKind: String, Codable, Hashable {
    case none
    case countdown
    case stopwatch
}

struct MetronomeActivityAttributes: ActivityAttributes {
    struct ContentState: Codable, Hashable {
        var isPlaying: Bool
        var bpm: Int
        var tempoName: String
        var timeSignature: String
        var timerKind: LiveActivityTimerKind
        var timerStart: Date?
        var timerEnd: Date?
        var timerFrozenSeconds: Int?
    }
}
