import WebKit
import UIKit
import StoreKit
import PhotosUI
import LocalAuthentication
import AVFoundation
import SafariServices

class WeakScriptMessageDelegate: NSObject, WKScriptMessageHandler {
    weak var delegate: WKScriptMessageHandler?
    init(_ delegate: WKScriptMessageHandler) { self.delegate = delegate; super.init() }
    func userContentController(_ controller: WKUserContentController, didReceive message: WKScriptMessage) {
        self.delegate?.userContentController(controller, didReceive: message)
    }
}

public class NativeWebBridge: NSObject, WKScriptMessageHandler, UIImagePickerControllerDelegate, UINavigationControllerDelegate, PHPickerViewControllerDelegate {
    
    private weak var webView: WKWebView?
    private var customHandlers: [String: ([String: Any], @escaping (Any?, String?) -> Void) -> Void] = [:]
    private var mediaCallback: ((Any?, String?) -> Void)?
    
    public init(webView: WKWebView) {
        self.webView = webView
        super.init()
        webView.configuration.userContentController.add(WeakScriptMessageDelegate(self), name: "iosInterface")
        registerDefaultHandlers()
        
        NotificationCenter.default.addObserver(self, selector: #selector(appDidEnterBackground), name: UIApplication.didEnterBackgroundNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(appWillEnterForeground), name: UIApplication.willEnterForegroundNotification, object: nil)
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
        webView?.configuration.userContentController.removeScriptMessageHandler(forName: "iosInterface")
    }
    
    @objc private func appDidEnterBackground() { sendCommandToWeb(action: "event.app.pause") }
    @objc private func appWillEnterForeground() { sendCommandToWeb(action: "event.app.resume") }
    
    public func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        guard message.name == "iosInterface", let b64 = message.body as? String, let data = Data(base64Encoded: b64),
              let json = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: Any] else { return }
        
        guard let type = json["type"] as? String, let id = json["id"] as? String, let action = json["action"] as? String else { return }
        let payload = json["payload"] as? [String: Any] ?? [:]
        
        if type == "request" || type == "command" {
            if let handler = customHandlers[action] {
                handler(payload) { resultPayload, errorMsg in
                    if type == "request" { self.sendResponse(id: id, payload: resultPayload, error: errorMsg) }
                }
            } else if type == "request" {
                self.sendResponse(id: id, payload: nil, error: "Not implemented on iOS")
            }
        }
    }
    
    private func sendResponse(id: String, payload: Any?, error: String?) {
        var response: [String: Any] = ["id": id, "type": "response"]
        if let payload = payload { response["payload"] = payload }
        if let error = error { response["error"] = error }
        
        guard let data = try? JSONSerialization.data(withJSONObject: response),
              let b64 = data.base64EncodedString() as String? else { return }
        DispatchQueue.main.async { self.webView?.evaluateJavaScript("window.NativeBridgeReceiver.receiveMessage('\(b64)')", completionHandler: nil) }
    }
    
    public func sendCommandToWeb(action: String, payload: [String: Any]? = nil) {
        var request: [String: Any] = ["id": UUID().uuidString, "type": "command", "action": action]
        if let payload = payload { request["payload"] = payload }
        
        guard let data = try? JSONSerialization.data(withJSONObject: request),
              let b64 = data.base64EncodedString() as String? else { return }
        DispatchQueue.main.async { self.webView?.evaluateJavaScript("window.NativeBridgeReceiver.receiveMessage('\(b64)')", completionHandler: nil) }
    }
    
    public func registerHandler(action: String, handler: @escaping ([String: Any], @escaping (Any?, String?) -> Void) -> Void) {
        customHandlers[action] = handler
    }
    
    private func registerDefaultHandlers() {
        // --- CORE & INFO ---
        registerHandler(action: "system.core.webReady") { _, cb in cb(["success": true], nil) }
        registerHandler(action: "system.info.getComplete") { _, cb in
            cb([
                "appVersion": Bundle.main.infoDictionary?["CFBundleShortVersionString"] ?? "Unknown",
                "osName": UIDevice.current.systemName, "osVersion": UIDevice.current.systemVersion,
                "deviceName": UIDevice.current.name, "deviceModel": UIDevice.current.model, "platform": "ios"
            ], nil)
        }
        
        // --- PERMISSIONS ---
        registerHandler(action: "system.permissions.check") { p, cb in cb(true, nil) } // Implement specific AVAuth checks as needed
        registerHandler(action: "system.permissions.request") { p, cb in cb(true, nil) } 

        // --- APP ---
        registerHandler(action: "system.app.openSettings") { _, cb in
            DispatchQueue.main.async { UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!) }; cb(true, nil)
        }
        registerHandler(action: "system.app.requestRating") { _, cb in
            DispatchQueue.main.async {
                if let scene = UIApplication.shared.connectedScenes.first(where: { $0.activationState == .foregroundActive }) as? UIWindowScene {
                    SKStoreReviewController.requestReview(in: scene)
                }
            }; cb(true, nil)
        }
        registerHandler(action: "system.app.forceUpdate") { p, cb in
            if let urlStr = p["url"] as? String, let url = URL(string: urlStr) { DispatchQueue.main.async { UIApplication.shared.open(url) } }; cb(true, nil)
        }
        registerHandler(action: "system.app.exit") { _, cb in
            UIControl().sendAction(#selector(URLSessionTask.suspend), to: UIApplication.shared, for: nil); cb(true, nil)
        }

        // --- UI ---
        registerHandler(action: "system.ui.setTheme") { p, cb in cb(true, nil) } // Web controls UI usually, native overrides require window manipulation
        registerHandler(action: "system.ui.getTheme") { _, cb in
            let isDark = UITraitCollection.current.userInterfaceStyle == .dark; cb(isDark ? "dark" : "light", nil)
        }
        registerHandler(action: "system.ui.showToast") { p, cb in
            // Basic iOS Toast implementation
            DispatchQueue.main.async {
                if let view = self.topVC()?.view, let msg = p["message"] as? String {
                    let toast = UILabel(frame: CGRect(x: 20, y: view.frame.size.height - 100, width: view.frame.size.width - 40, height: 40))
                    toast.backgroundColor = UIColor.black.withAlphaComponent(0.8)
                    toast.textColor = UIColor.white; toast.textAlignment = .center; toast.text = msg
                    toast.layer.cornerRadius = 10; toast.clipsToBounds = true
                    view.addSubview(toast)
                    UIView.animate(withDuration: 0.5, delay: (p["duration"] as? String == "long" ? 3.5 : 2.0), options: .curveEaseOut, animations: { toast.alpha = 0.0 }, completion: { _ in toast.removeFromSuperview() })
                }
            }; cb(true, nil)
        }
        registerHandler(action: "system.ui.showAlert") { p, cb in
            DispatchQueue.main.async {
                let alert = UIAlertController(title: p["title"] as? String, message: p["message"] as? String, preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: p["buttonText"] as? String ?? "OK", style: .default) { _ in cb(true, nil) })
                self.topVC()?.present(alert, animated: true)
            }
        }
        registerHandler(action: "system.ui.showConfirm") { p, cb in
            DispatchQueue.main.async {
                let alert = UIAlertController(title: p["title"] as? String, message: p["message"] as? String, preferredStyle: .alert)
                alert.addAction(UIAlertAction(title: p["okText"] as? String ?? "Yes", style: .default) { _ in cb(true, nil) })
                alert.addAction(UIAlertAction(title: p["cancelText"] as? String ?? "No", style: .cancel) { _ in cb(false, nil) })
                self.topVC()?.present(alert, animated: true)
            }
        }

        // --- NOTIFICATIONS ---
        registerHandler(action: "system.notifications.getToken") { _, cb in cb(UserDefaults.standard.string(forKey: "APNSToken"), nil) }
        registerHandler(action: "system.notifications.setToken") { p, cb in
            if let t = p["token"] as? String { UserDefaults.standard.set(t, forKey: "APNSToken") }; cb(true, nil)
        }

        // --- MEDIA ---
        registerHandler(action: "system.media.takePhoto") { p, cb in
            self.mediaCallback = cb
            DispatchQueue.main.async {
                let picker = UIImagePickerController(); picker.sourceType = .camera; picker.delegate = self
                self.topVC()?.present(picker, animated: true)
            }
        }
        registerHandler(action: "system.media.pickImage") { p, cb in
            self.mediaCallback = cb
            DispatchQueue.main.async {
                var config = PHPickerConfiguration(); config.filter = .images; config.selectionLimit = 1
                let picker = PHPickerViewController(configuration: config); picker.delegate = self
                self.topVC()?.present(picker, animated: true)
            }
        }
        
        // --- SHARE ---
        registerHandler(action: "system.share.text") { p, cb in
            DispatchQueue.main.async {
                let vc = UIActivityViewController(activityItems: [p["text"] as? String ?? ""], applicationActivities: nil)
                self.topVC()?.present(vc, animated: true); cb(true, nil)
            }
        }
        registerHandler(action: "system.share.link") { p, cb in
            if let urlStr = p["url"] as? String, let url = URL(string: urlStr) {
                DispatchQueue.main.async { self.topVC()?.present(UIActivityViewController(activityItems: [url], applicationActivities: nil), animated: true) }
            }; cb(true, nil)
        }

        // --- COMMUNICATION ---
        registerHandler(action: "system.communication.dial") { p, cb in
            if let num = p["phoneNumber"] as? String, let url = URL(string: "tel://\(num)") {
                DispatchQueue.main.async { UIApplication.shared.open(url) }
            }; cb(true, nil)
        }
        registerHandler(action: "system.communication.openBrowser") { p, cb in
            if let urlStr = p["url"] as? String, let url = URL(string: urlStr) {
                DispatchQueue.main.async { self.topVC()?.present(SFSafariViewController(url: url), animated: true) }
            }; cb(true, nil)
        }

        // --- HARDWARE ---
        registerHandler(action: "system.hardware.haptic") { p, cb in
            DispatchQueue.main.async {
                let style: UIImpactFeedbackGenerator.FeedbackStyle = (p["style"] as? String == "heavy") ? .heavy : ((p["style"] as? String == "light") ? .light : .medium)
                UIImpactFeedbackGenerator(style: style).impactOccurred()
            }; cb(true, nil)
        }
        registerHandler(action: "system.hardware.flashlight") { p, cb in
            guard let device = AVCaptureDevice.default(for: .video), device.hasTorch else { return cb(false, "No flashlight") }
            do {
                try device.lockForConfiguration()
                device.torchMode = (p["on"] as? Bool == true) ? .on : .off
                device.unlockForConfiguration(); cb(true, nil)
            } catch { cb(false, error.localizedDescription) }
        }

        // --- STORAGE ---
        registerHandler(action: "system.storage.setItem") { p, cb in
            if let k = p["key"] as? String { UserDefaults.standard.set(p["value"], forKey: k) }; cb(true, nil)
        }
        registerHandler(action: "system.storage.getItem") { p, cb in
            if let k = p["key"] as? String { cb(UserDefaults.standard.object(forKey: k), nil) } else { cb(nil, nil) }
        }

        // --- SECURE STORAGE (Keychain) ---
        registerHandler(action: "system.secureStorage.setItem") { p, cb in
            guard let key = p["key"] as? String, let val = p["value"] as? String, let data = val.data(using: .utf8) else { return cb(nil, "Invalid") }
            let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword, kSecAttrAccount as String: key, kSecValueData as String: data]
            SecItemDelete(query as CFDictionary); SecItemAdd(query as CFDictionary, nil); cb(true, nil)
        }
        registerHandler(action: "system.secureStorage.getItem") { p, cb in
            guard let key = p["key"] as? String else { return cb(nil, "Invalid") }
            let query: [String: Any] = [kSecClass as String: kSecClassGenericPassword, kSecAttrAccount as String: key, kSecReturnData as String: kCFBooleanTrue!, kSecMatchLimit as String: kSecMatchLimitOne]
            var item: AnyObject?; if SecItemCopyMatching(query as CFDictionary, &item) == noErr, let d = item as? Data { cb(String(data: d, encoding: .utf8), nil) } else { cb(nil, nil) }
        }

        // --- BIOMETRICS ---
        registerHandler(action: "system.biometrics.authenticate") { p, cb in
            let ctx = LAContext(); var err: NSError?
            if ctx.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &err) {
                ctx.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: p["reason"] as? String ?? "Authenticate") { s, _ in cb(s, nil) }
            } else { cb(false, "Biometrics unavailable") }
        }

        // --- CLIPBOARD ---
        registerHandler(action: "system.clipboard.copy") { p, cb in
            if let t = p["text"] as? String { UIPasteboard.general.string = t }; cb(true, nil)
        }
    }
    
    private func topVC() -> UIViewController? {
        let keyWindow = UIApplication.shared.connectedScenes.filter { $0.activationState == .foregroundActive }.compactMap { $0 as? UIWindowScene }.first?.windows.filter { $0.isKeyWindow }.first
        var top = keyWindow?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
    
    // --- Media Delegates ---
    public func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        picker.dismiss(animated: true)
        if let image = info[.originalImage] as? UIImage, let data = image.jpegData(compressionQuality: 0.6) {
            mediaCallback?(["base64": data.base64EncodedString()], nil)
        } else { mediaCallback?(nil, "Failed") }
        mediaCallback = nil
    }
    public func imagePickerControllerDidCancel(_ picker: UIImagePickerController) { picker.dismiss(animated: true); mediaCallback?(nil, "Cancelled"); mediaCallback = nil }
    public func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)
        guard let provider = results.first?.itemProvider, provider.canLoadObject(ofClass: UIImage.self) else { mediaCallback?(nil, "Cancelled"); mediaCallback = nil; return }
        provider.loadObject(ofClass: UIImage.self) { image, _ in
            if let img = image as? UIImage, let data = img.jpegData(compressionQuality: 0.6) { self.mediaCallback?(["base64": data.base64EncodedString()], nil) } else { self.mediaCallback?(nil, "Failed") }
            self.mediaCallback = nil
        }
    }
}