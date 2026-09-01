import ComposeApp
import StoreKit
import UIKit

final class IosInAppReviewRequester: InAppReviewRequester {
    func requestReview() -> Bool {
        guard Thread.isMainThread else { return false }

        return MainActor.assumeIsolated {
            guard let scene = UIApplication.shared.connectedScenes
                .compactMap({ $0 as? UIWindowScene })
                .first(where: { $0.activationState == .foregroundActive }) else {
                return false
            }

            if #available(iOS 18.0, *) {
                AppStore.requestReview(in: scene)
            } else {
                SKStoreReviewController.requestReview(in: scene)
            }
            return true
        }
    }
}
