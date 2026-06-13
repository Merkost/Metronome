import ActivityKit
import Foundation

struct MetronomeActivityAttributes: ActivityAttributes {
    struct ContentState: Codable, Hashable {
        var isPlaying: Bool
        var bpm: Int
        var tempoName: String
        var timeSignature: String
        var timerKind: String
        var timerStart: Date?
        var timerEnd: Date?
        var timerFrozenSeconds: Int?
    }
}
