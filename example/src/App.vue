<template>
  <div class="harness-container">
    <header class="header">
      <h1>NativeWebBridge 🌉</h1>
      <p>Cross-Platform Interactive Test Harness & API Reference</p>
    </header>

    <main class="layout">
      <section class="controls">
        <div class="panel custom-panel">
          <h2>🛠 Custom Call Builder</h2>
          <div class="input-group"><label>Action Name</label><input v-model="customAction" type="text" /></div>
          <div class="input-group"><label>Payload (JSON)</label><textarea v-model="customPayload" rows="3"></textarea></div>
          <div class="button-group">
            <span class="action-badge"><button @click="sendCustom('request')" class="btn-primary">Send Custom Request</button><button class="btn-info" @click="openDoc('customRequest')">?</button></span>
            <span class="action-badge"><button @click="sendCustom('command')" class="btn-secondary">Send Custom Command</button><button class="btn-info" @click="openDoc('customCommand')">?</button></span>
          </div>
        </div>

        <div class="panel methods-panel">
          <h2>📱 Predefined Methods</h2>
          
          <div class="method-group">
            <h3>System & App Controls</h3>
            <span class="action-badge"><button @click="execute('core.signalWebReady', () => bridge.core.signalWebReady())">Signal Web Ready</button><button class="btn-info" @click="openDoc('signalWebReady')">?</button></span>
            <span class="action-badge"><button @click="execute('info.getComplete', () => bridge.info.getComplete())">Get Device Info</button><button class="btn-info" @click="openDoc('getComplete')">?</button></span>
            <span class="action-badge"><button @click="execute('device.getLanguage', () => bridge.device.getLanguage())">Get Language</button><button class="btn-info" @click="openDoc('getLanguage')">?</button></span>
            <span class="action-badge"><button @click="execute('device.getBatteryLevel', () => bridge.device.getBatteryLevel())">Get Battery</button><button class="btn-info" @click="openDoc('getBatteryLevel')">?</button></span>
            <span class="action-badge"><button @click="execute('screen.setKeepScreenOn', () => bridge.screen.setKeepScreenOn(true))">Keep Screen On</button><button class="btn-info" @click="openDoc('setKeepScreenOn')">?</button></span>
            <span class="action-badge"><button @click="execute('app.openSettings', () => bridge.app.openSettings())">Open Settings</button><button class="btn-info" @click="openDoc('openSettings')">?</button></span>
            <span class="action-badge"><button @click="execute('app.requestRating', () => bridge.app.requestRating())">Request Rating</button><button class="btn-info" @click="openDoc('requestRating')">?</button></span>
            <span class="action-badge"><button @click="execute('app.forceUpdate', () => bridge.app.forceUpdate())">Force Update</button><button class="btn-info" @click="openDoc('forceUpdate')">?</button></span>
            <span class="action-badge"><button @click="execute('app.exit', () => bridge.app.exit())">Exit App</button><button class="btn-info" @click="openDoc('exitApp')">?</button></span>
            <span class="action-badge">
              <button @click="execute('app.clearCaches', () => bridge.app.clearCaches())">Clear Caches</button>
              <button class="btn-info" @click="openDoc('clearCaches')">?</button>
            </span>
          </div>

          <div class="method-group">
            <h3>UI & Feedback</h3>
            <span class="action-badge"><button @click="execute('ui.setTheme(dark)', () => bridge.ui.setTheme('dark'))">Set Theme (Dark)</button><button class="btn-info" @click="openDoc('setTheme')">?</button></span>
            <span class="action-badge"><button @click="execute('ui.getTheme', () => bridge.ui.getTheme())">Get OS Theme</button><button class="btn-info" @click="openDoc('getTheme')">?</button></span>
            <span class="action-badge"><button @click="execute('ui.showToast', () => bridge.ui.showToast('Hello!', 'short'))">Show Toast</button><button class="btn-info" @click="openDoc('showToast')">?</button></span>
            <span class="action-badge"><button @click="execute('ui.showAlert', () => bridge.ui.showAlert('Warning', 'Test alert'))">Show Alert</button><button class="btn-info" @click="openDoc('showAlert')">?</button></span>
            <span class="action-badge"><button @click="execute('ui.showConfirm', () => bridge.ui.showConfirm('Proceed?', 'Continue?'))">Show Confirm</button><button class="btn-info" @click="openDoc('showConfirm')">?</button></span>
          </div>

          <div class="method-group">
            <h3>Media, Files & Contacts</h3>
            <span class="action-badge"><button @click="execute('media.takePhoto', () => bridge.media.takePhoto({ base64: true }))">Take Photo</button><button class="btn-info" @click="openDoc('takePhoto')">?</button></span>
            <span class="action-badge"><button @click="execute('media.pickImage', () => bridge.media.pickImage({ base64: true }))">Pick Image</button><button class="btn-info" @click="openDoc('pickImage')">?</button></span>
            <span class="action-badge"><button @click="execute('media.downloadImage', () => bridge.media.downloadImage('https://via.placeholder.com/150'))">Download Image</button><button class="btn-info" @click="openDoc('downloadImage')">?</button></span>
            <span class="action-badge"><button @click="execute('file.pick', () => bridge.file.pick())">Pick File</button><button class="btn-info" @click="openDoc('pickFile')">?</button></span>
            <span class="action-badge"><button @click="execute('contacts.pick', () => bridge.contacts.pick())">Pick Contact</button><button class="btn-info" @click="openDoc('pickContact')">?</button></span>
            <span class="action-badge"><button @click="execute('audio.playSystemSound', () => bridge.audio.playSystemSound())">Play Sound</button><button class="btn-info" @click="openDoc('playSystemSound')">?</button></span>
          </div>

          <div class="method-group">
            <h3>Sharing & Comms</h3>
            <span class="action-badge"><button @click="execute('share.text', () => bridge.share.text('Hello World!'))">Share Text</button><button class="btn-info" @click="openDoc('shareText')">?</button></span>
            <span class="action-badge"><button @click="execute('share.link', () => bridge.share.link('https://google.com'))">Share Link</button><button class="btn-info" @click="openDoc('shareLink')">?</button></span>
            <span class="action-badge"><button @click="execute('share.image', shareTestImage)">Share Image</button><button class="btn-info" @click="openDoc('shareImage')">?</button></span>
            <span class="action-badge"><button @click="execute('share.video', shareTestVideo)">Share Video</button><button class="btn-info" @click="openDoc('shareVideo')">?</button></span>
            <span class="action-badge"><button @click="execute('communication.dial', () => bridge.communication.dialNumber('1234567890'))">Dial Number</button><button class="btn-info" @click="openDoc('dial')">?</button></span>
            <span class="action-badge"><button @click="execute('communication.openBrowser', () => bridge.communication.openBrowser('https://google.com'))">Open Browser</button><button class="btn-info" @click="openDoc('openBrowser')">?</button></span>
          </div>

          <div class="method-group">
            <h3>Hardware, Network & Storage</h3>
            <span class="action-badge"><button @click="execute('network.getStatus', () => bridge.network.getStatus())">Get Network</button><button class="btn-info" @click="openDoc('getStatus')">?</button></span>
            <span class="action-badge"><button @click="execute('location.getCurrent', () => bridge.location.getCurrent())">Get GPS</button><button class="btn-info" @click="openDoc('getCurrentLocation')">?</button></span>
            <span class="action-badge"><button @click="execute('hardware.triggerHaptic', () => bridge.hardware.triggerHaptic('heavy'))">Haptic</button><button class="btn-info" @click="openDoc('triggerHaptic')">?</button></span>
            <span class="action-badge"><button @click="execute('hardware.toggleFlashlight', () => bridge.hardware.toggleFlashlight(true))">Flashlight ON</button><button class="btn-info" @click="openDoc('toggleFlashlight')">?</button></span>
            <span class="action-badge"><button @click="execute('clipboard.copy', () => bridge.clipboard.copy('Copied!'))">Copy Clip</button><button class="btn-info" @click="openDoc('copyClip')">?</button></span>
            <span class="action-badge"><button @click="execute('clipboard.read', () => bridge.clipboard.read())">Read Clip</button><button class="btn-info" @click="openDoc('readClip')">?</button></span>
            <span class="action-badge"><button @click="execute('storage.setItem', () => bridge.storage.setItem('key', '123'))">Set Storage</button><button class="btn-info" @click="openDoc('setItem')">?</button></span>
            <span class="action-badge"><button @click="execute('storage.getItem', () => bridge.storage.getItem('key'))">Get Storage</button><button class="btn-info" @click="openDoc('getItem')">?</button></span>
            <span class="action-badge"><button @click="execute('secureStorage.setItem', () => bridge.secureStorage.setItem('sec', '123'))">Set Secure</button><button class="btn-info" @click="openDoc('setSecure')">?</button></span>
            <span class="action-badge"><button @click="execute('secureStorage.getItem', () => bridge.secureStorage.getItem('sec'))">Get Secure</button><button class="btn-info" @click="openDoc('getSecure')">?</button></span>
          </div>

          <div class="method-group">
            <h3>Auth & Notifications</h3>
            <span class="action-badge"><button @click="execute('permissions.check(camera)', () => bridge.permissions.check('camera'))">Check Camera Perm</button><button class="btn-info" @click="openDoc('checkPerm')">?</button></span>
            <span class="action-badge"><button @click="execute('permissions.request(camera)', () => bridge.permissions.request('camera'))">Req Camera Perm</button><button class="btn-info" @click="openDoc('requestPermission')">?</button></span>
            <span class="action-badge"><button @click="execute('permissions.check(notifications)', () => bridge.permissions.check('notifications'))">Check Push Perm</button><button class="btn-info" @click="openDoc('checkPush')">?</button></span>
            <span class="action-badge"><button @click="execute('permissions.request(notifications)', () => bridge.permissions.request('notifications'))">Req Push Perm</button><button class="btn-info" @click="openDoc('reqPush')">?</button></span>
            <span class="action-badge"><button @click="execute('biometrics.authenticate', () => bridge.biometrics.authenticate('Unlock'))">Biometrics Auth</button><button class="btn-info" @click="openDoc('biometricsAuth')">?</button></span>
            <span class="action-badge"><button @click="execute('notifications.getToken', () => bridge.notifications.getToken())">Get Push Token</button><button class="btn-info" @click="openDoc('getToken')">?</button></span>
            <span class="action-badge"><button @click="execute('notifications.setToken', () => bridge.notifications.setToken('xyz.123'))">Set Push Token</button><button class="btn-info" @click="openDoc('setToken')">?</button></span>
          </div>

          <div class="method-group">
            <h3>Background Events (Docs)</h3>
            <span class="action-badge"><button class="btn-event-doc" @click="openDoc('eventAppPause')">App Pause / Resume</button></span>
            <span class="action-badge"><button class="btn-event-doc" @click="openDoc('eventTheme')">Theme / Keyboard</button></span>
            <span class="action-badge"><button class="btn-event-doc" @click="openDoc('eventDeepLink')">Deep Links / Push / Network</button></span>
            <span class="action-badge"><button class="btn-event-doc" @click="openDoc('eventCustom')">Custom Listeners</button></span>
          </div>
        </div>
      </section>

      <section class="terminal-container">
        <div class="terminal-header"><span>Logs & Responses</span><button @click="logs = []" class="btn-clear">Clear</button></div>
        <div class="terminal-body">
          <div v-if="logs.length === 0" class="empty-log">Awaiting bridge events...</div>
          <div v-for="(log, idx) in logs" :key="idx" class="log-entry" :class="{ 'log-error': log.isError, 'log-event': log.isEvent }">
            <div class="log-time">[{{ log.time }}]</div><div class="log-action">{{ log.action }}</div><pre class="log-data">{{ formatData(log.data) }}</pre>
          </div>
        </div>
      </section>
    </main>

    <div v-if="activeDoc" class="modal-overlay" @click="activeDoc = null">
      <div class="modal-content" @click.stop>
        <div class="modal-header"><h3>📘 {{ activeDoc.title }}</h3><button class="btn-close-modal" @click="activeDoc = null">✕</button></div>
        <div class="modal-body">
          <div class="doc-section"><h4>Why?</h4><p>{{ activeDoc.why }}</p></div>
          <div class="doc-section"><h4>When?</h4><p>{{ activeDoc.when }}</p></div>
          <div class="doc-section"><h4>How?</h4><pre class="code-block">{{ activeDoc.how }}</pre></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { bridge } from 'native-web-bridge';

const customAction = ref('custom.business.scanBarcode');
const customPayload = ref('{\n  "type": "qr_code"\n}');
const activeDoc = ref<DocEntry | null>(null);
const logs = ref<any[]>([]);

interface DocEntry { title: string; why: string; when: string; how: string; }
const docDB: Record<string, DocEntry> = {
  requestRating: { 
    title: 'Request Rating', 
    why: 'Triggers the native OS rating dialog (SKStoreReviewController on iOS, In-App Review API on Android) without forcing the user to leave your app.', 
    when: 'Best used after a highly positive user interaction, such as successfully completing a purchase, finishing a difficult level, or saving a milestone.', 
    how: "try {\n  await bridge.app.requestRating();\n  console.log('Rating prompt displayed');\n} catch (err) {\n  console.warn('OS blocked rating prompt:', err);\n}" 
  },
  forceUpdate: { 
    title: 'Force Update', 
    why: 'Directs the user out of the app directly to your specific App Store or Google Play Store listing.', 
    when: 'Use this when your web API introduces breaking changes and the user\'s current app version is too old to safely continue.', 
    how: "const isDeprecated = checkVersion();\nif (isDeprecated) {\n  await bridge.app.forceUpdate('https://play.google.com/store/apps/details?id=com.yourapp');\n}" 
  },
  setTheme: { 
    title: 'Set Theme', 
    why: 'Forces the Native OS container components (like status bars and alert dialogs) to perfectly match your Web UI\'s color scheme.', 
    when: 'Call this the moment a user manually toggles the dark/light mode switch inside your web app settings.', 
    how: "const newTheme = userPrefersDark ? 'dark' : 'light';\nawait bridge.ui.setTheme(newTheme);\ndocument.body.classList.toggle('dark-mode', userPrefersDark);" 
  },
  downloadImage: { 
    title: 'Download Image', 
    why: 'Bypasses browser download restrictions and saves an image securely to the user\'s native Camera Roll / Photo Gallery.', 
    when: 'Perfect for exporting generated content like custom memes, QR codes, digital tickets, or payment receipts.', 
    how: "const ticketUrl = 'https://myapp.com/ticket-123.png';\nawait bridge.media.downloadImage(ticketUrl);\nawait bridge.ui.showToast('Saved to Camera Roll!');" 
  },
  shareText: { 
    title: 'Share Text', 
    why: 'Invokes the native OS share sheet (UIActivityViewController / Intent.ACTION_SEND) to distribute raw text.', 
    when: 'Use this to let users quickly text or tweet a funny quote, a promo code, or a high score.', 
    how: "const promo = 'Use code SAVE20 for 20% off!';\nawait bridge.share.text(promo, 'Discount Code');" 
  },
  shareLink: { 
    title: 'Share Link', 
    why: 'Shares a formatted URL via the native OS, often triggering rich link previews in apps like iMessage or WhatsApp.', 
    when: 'Ideal for "Invite a Friend", sharing a specific product page, or directing users to a shared workspace.', 
    how: "const referralUrl = 'https://myapp.com/invite/omar99';\nawait bridge.share.link(referralUrl, 'Join me on the app!');" 
  },
  dial: { 
    title: 'Dial Number', 
    why: 'Suspends your app and opens the native Phone dialer with the number pre-filled.', 
    when: 'Bind this to "Call Support", "Contact Driver", or "Call Restaurant" buttons.', 
    how: "const driverPhone = '+1234567890';\nawait bridge.communication.dialNumber(driverPhone);" 
  },
  openBrowser: { 
    title: 'Open External Browser', 
    why: 'Escapes the WebView container to open a link in the user\'s default external browser (Safari/Chrome).', 
    when: 'Crucial for viewing external Privacy Policies, Terms of Service, or third-party payment gateways like PayPal.', 
    how: "const tosUrl = 'https://company.com/terms';\nawait bridge.communication.openBrowser(tosUrl);" 
  },
  setSecure: { 
    title: 'Set Secure Storage', 
    why: 'Saves sensitive string data into heavily encrypted, hardware-backed storage (iOS Keychain / Android EncryptedSharedPreferences).', 
    when: 'Always use this immediately after login to securely store JWTs, OAuth tokens, or PIN codes. Never use Web LocalStorage for these!', 
    how: "const authResponse = await login(user, pass);\nawait bridge.secureStorage.setItem('jwt_token', authResponse.token);" 
  },
  getSecure: { 
    title: 'Get Secure Storage', 
    why: 'Retrieves hardware-encrypted tokens seamlessly.', 
    when: 'Call this during your app\'s initial boot sequence to check if a user session still exists and auto-login.', 
    how: "const token = await bridge.secureStorage.getItem('jwt_token');\nif (token) {\n  store.commit('authenticate', token);\n}" 
  },
  copyClip: { 
    title: 'Copy to Clipboard', 
    why: 'Writes text directly to the native OS clipboard without relying on the finicky Web Clipboard API.', 
    when: 'Use for "Copy to Clipboard" buttons next to referral codes, wallet addresses, or API keys.', 
    how: "const walletAddress = '0x123abc...';\nawait bridge.clipboard.copy(walletAddress);\nawait bridge.ui.showToast('Address copied!');" 
  },
  readClip: { 
    title: 'Read Clipboard', 
    why: 'Reads the user\'s current clipboard text directly from the OS.', 
    when: 'Extremely useful for auto-detecting and pasting One Time Passwords (OTPs), meeting links, or tracking numbers when the user opens the app.', 
    how: "const clipText = await bridge.clipboard.read();\nif (clipText.startsWith('https://zoom.us')) {\n  joinMeeting(clipText);\n}" 
  },
  getToken: { 
    title: 'Get Push Token', 
    why: 'Retrieves the unique APNs (iOS) or FCM (Android) device token stored by your native wrapper.', 
    when: 'Call this right after the user successfully registers or logs in, so you can save their hardware token to your database for targeting.', 
    how: "const pushToken = await bridge.notifications.getToken();\nif (pushToken) {\n  await api.post('/users/devices', { token: pushToken });\n}" 
  },
  setToken: { 
    title: 'Set Push Token', 
    why: 'Allows the native wrapper to pass its newly generated FCM/APNs token down into the web environment.', 
    when: 'Typically triggered by a custom native listener the moment the Firebase SDK initializes in the background.', 
    how: "bridge.custom.listen('native_token_refresh', async (payload) => {\n  await bridge.notifications.setToken(payload.token);\n});" 
  },
  checkPerm: { 
    title: 'Check Permission', 
    why: 'Silently verifies if the OS has granted your app access to protected hardware without triggering a user popup.', 
    when: 'Use this when rendering your UI. If camera access is denied, you can show a generic image upload button instead of a live viewfinder.', 
    how: "const canUseCamera = await bridge.permissions.check('camera');\nshowScannerUI.value = canUseCamera;" 
  },
  checkPush: { 
    title: 'Check Notification Permission', 
    why: 'Checks if the user has silenced or blocked your app\'s alerts at the OS settings level.', 
    when: 'Run this on the settings page to accurately reflect whether the user is actually receiving your alerts.', 
    how: "const hasAlerts = await bridge.permissions.check('notifications');\nif (!hasAlerts) {\n  showEnableAlertsBanner = true;\n}" 
  },
  reqPush: { 
    title: 'Request Notification Permission', 
    why: 'Triggers the native OS modal asking the user to allow banners, sounds, and badges.', 
    when: 'Never ask on boot! Ask immediately after the user completes a key action (like ordering food) so they understand *why* you need to notify them.', 
    how: "const granted = await bridge.permissions.request('notifications');\nif (granted) {\n  console.log('User opted into alerts!');\n}" 
  },
  shareImage: { 
    title: 'Share Image', 
    why: 'Generates a secure, temporary file URI from a Base64 string and opens the native Share Sheet.', 
    when: 'Allow users to export custom-generated certificates, edited photos, or data charts to Instagram, WhatsApp, or Email.', 
    how: "const base64Canvas = myCanvas.toDataURL().split(',')[1];\nawait bridge.share.image(base64Canvas, 'chart_export.png');" 
  },
  shareVideo: { 
    title: 'Share Video', 
    why: 'Securely writes a Base64 video buffer to the native cache and exposes it to other apps via the Share Sheet.', 
    when: 'Exporting recorded screen captures, generated animations, or downloaded media files directly to TikTok or Messages.', 
    how: "const videoB64 = await renderVideo();\nawait bridge.share.video(videoB64, 'my_recording.mp4');" 
  },
  customRequest: { 
    title: 'Custom Request', 
    why: 'Sends a payload to a developer-defined Kotlin/Swift handler and *waits* for a JSON response.', 
    when: 'Use this when you integrate a proprietary 3rd-party Native SDK (like a Bluetooth scanner or a custom payment gateway) and need data back.', 
    how: "const scanResult = await bridge.custom.invoke('bt.scanBarcode', { timeout: 5000 });\nconsole.log('Barcode:', scanResult.data);" 
  },
  customCommand: { 
    title: 'Custom Command', 
    why: 'Fires a payload to the native OS and immediately moves on without waiting for a reply (Fire & Forget).', 
    when: 'Perfect for high-volume, non-blocking tasks like sending analytics events to a native Firebase/Mixpanel SDK.', 
    how: "bridge.custom.send('analytics.trackEvent', {\n  event: 'checkout_completed',\n  value: 99.99\n});" 
  },
  signalWebReady: { 
    title: 'Signal Web Ready', 
    why: 'Unlocks the native message queue. Prevents race conditions where Android/iOS fire boot events before Vue is ready to hear them.', 
    when: 'Call this exactly ONCE in your root App.vue `onMounted` lifecycle hook.', 
    how: "onMounted(() => {\n  bridge.core.signalWebReady();\n  console.log('Bridge is open for business!');\n});" 
  },
  getComplete: { 
    title: 'Get Device Info', 
    why: 'Gathers a comprehensive dictionary of hardware specifications and OS versions.', 
    when: 'Attach this payload to crash reports, customer support tickets, or API headers for highly targeted debugging.', 
    how: "const info = await bridge.info.getComplete();\napi.post('/logs/crash', {\n  error: e.message,\n  device: info.deviceModel,\n  os: info.osVersion\n});" 
  },
  getLanguage: { 
    title: 'Get OS Language', 
    why: 'Reads the exact locale tag directly from the user\'s hardware (e.g., "en-US", "fr-FR").', 
    when: 'Use this on boot to automatically set your Vue-i18n locale to match the user\'s preference without making them choose.', 
    how: "const osLocale = await bridge.device.getLanguage();\ni18n.global.locale.value = osLocale.split('-')[0];" 
  },
  getBatteryLevel: { 
    title: 'Get Battery Level', 
    why: 'Reads the real-time battery percentage from the device hardware.', 
    when: 'Use this to degrade gracefully. If the battery is below 15%, pause heavy WebGL animations, stop background polling, or switch to dark mode.', 
    how: "const battery = await bridge.device.getBatteryLevel();\nif (battery < 15) {\n  disableHeavyAnimations();\n}" 
  },
  setKeepScreenOn: { 
    title: 'Keep Screen On', 
    why: 'Overrides the OS idle timer, preventing the phone from going to sleep and locking.', 
    when: 'Critical for displaying a QR code for a cashier to scan, during a live video call, or while following a cooking recipe.', 
    how: "await bridge.screen.setKeepScreenOn(true);\n// Later, when done:\nawait bridge.screen.setKeepScreenOn(false);" 
  },
  openSettings: { 
    title: 'Open App Settings', 
    why: 'Deep-links the user directly to your specific app\'s page inside the native OS Settings app.', 
    when: 'Use this as a fallback. If a user denies camera permission twice, the OS blocks further prompts. You must guide them here to flip the switch manually.', 
    how: "const granted = await bridge.permissions.request('location');\nif (!granted) {\n  showUI('Please enable location in settings', () => bridge.app.openSettings());\n}" 
  },
  exitApp: { 
    title: 'Exit Application', 
    why: 'Programmatically kills the Android Activity.', 
    when: 'Use on a strict "Logout & Quit" button, or if a critical forced update requires the app to completely restart. (Note: Ignored on iOS per Apple rules).', 
    how: "await logoutUser();\nawait bridge.app.exit();" 
  },
  getStatus: { 
    title: 'Network Status', 
    why: 'Provides real-time boolean connectivity and connection type (wifi vs. cellular).', 
    when: 'Check this before initiating massive file uploads or video streams to warn users if they are about to consume expensive cellular data.', 
    how: "const net = await bridge.network.getStatus();\nif (net.type === 'cellular') {\n  await bridge.ui.showAlert('Warning', 'Large download on cellular data!');\n}" 
  },
  getCurrentLocation: { 
    title: 'Get GPS Location', 
    why: 'Fetches highly accurate latitude and longitude coordinates directly from the native CoreLocation/LocationManager.', 
    when: 'Ideal for delivery tracking, mapping stores, or checking in. Make sure you call permissions.request("location") first!', 
    how: "try {\n  const { lat, lng } = await bridge.location.getCurrent();\n  map.setCenter({ lat, lng });\n} catch (e) {\n  console.error('GPS unavailable', e);\n}" 
  },
  triggerHaptic: { 
    title: 'Trigger Haptic Feedback', 
    why: 'Fires the physical vibration motors in the device (Taptic Engine / Vibrator API).', 
    when: 'Sprinkle this on primary UI interactions: "light" for typing, "medium" for pull-to-refresh, and "heavy" for error states or success checkmarks.', 
    how: "submitForm();\nawait bridge.hardware.triggerHaptic('medium');" 
  },
  toggleFlashlight: { 
    title: 'Toggle Flashlight', 
    why: 'Takes control of the device\'s rear camera LED.', 
    when: 'Place a flashlight toggle button directly inside your custom barcode or document scanning UI to assist users in dark environments.', 
    how: "let isFlashOn = false;\ntoggleBtn.onClick = async () => {\n  isFlashOn = !isFlashOn;\n  await bridge.hardware.toggleFlashlight(isFlashOn);\n};" 
  },
  showToast: { 
    title: 'Show Toast', 
    why: 'Displays a brief, unobtrusive OS-level text overlay that disappears automatically.', 
    when: 'Perfect for non-critical confirmations that shouldn\'t interrupt the user\'s flow (e.g., "Profile Saved", "Link Copied").', 
    how: "await saveProfile();\nawait bridge.ui.showToast('Profile successfully saved!', 'short');" 
  },
  showAlert: { 
    title: 'Show Native Alert', 
    why: 'Summons a native, blocking dialog box (UIAlertController / AlertDialog) that requires user dismissal.', 
    when: 'Use for critical, unavoidable information like "Payment Failed" or "Session Expired". Native alerts feel much more official than web modals.', 
    how: "try {\n  await processPayment();\n} catch (e) {\n  await bridge.ui.showAlert('Transaction Failed', e.message, 'Understood');\n}" 
  },
  showConfirm: { 
    title: 'Show Native Confirm', 
    why: 'Displays a native dialog with two distinct choices, returning a boolean based on the user\'s selection.', 
    when: 'Always use this before destructive actions (deleting an account, clearing data) or before navigating away from unsaved form changes.', 
    how: "const isSure = await bridge.ui.showConfirm('Delete Account', 'This is permanent. Are you sure?', 'Delete', 'Cancel');\nif (isSure) { deleteUser(); }" 
  },
  getTheme: { 
    title: 'Get OS Theme', 
    why: 'Checks if the user\'s OS is currently running in Dark Mode or Light Mode.', 
    when: 'Call this on application boot to immediately initialize your CSS variables or Tailwind classes to match the system.', 
    how: "const systemTheme = await bridge.ui.getTheme();\nif (systemTheme === 'dark') {\n  enableDarkMode();\n}" 
  },
  takePhoto: { 
    title: 'Take Photo', 
    why: 'Opens the full-screen native Camera application and returns the captured image as a Base64 string.', 
    when: 'Use for Avatar updates, KYC document scanning, or attaching photo receipts to expense reports.', 
    how: "const photo = await bridge.media.takePhoto({ base64: true });\nif (photo?.base64) {\n  avatarUrl.value = `data:image/jpeg;base64,${photo.base64}`;\n}" 
  },
  pickImage: { 
    title: 'Pick Image', 
    why: 'Opens the native Photo Library / Gallery so the user can select existing media.', 
    when: 'Whenever a user needs to upload a profile picture or select an existing photo from their camera roll.', 
    how: "const selection = await bridge.media.pickImage({ base64: true });\nif (selection) {\n  uploadToS3(selection.base64);\n}" 
  },
  pickFile: { 
    title: 'Pick File', 
    why: 'Triggers the native OS file browser (Files app / Document Picker) to select PDFs, Docs, or Data files.', 
    when: 'Provides a far superior, native user experience compared to the standard HTML `<input type="file">`.', 
    how: "const file = await bridge.file.pick();\nif (file) {\n  console.log(`Uploading ${file.name}...`);\n  await uploadBase64(file.base64);\n}" 
  },
  pickContact: { 
    title: 'Pick Contact', 
    why: 'Opens the native Address Book and securely extracts the selected person\'s name and phone number.', 
    when: 'Essential for features like "Invite a Friend", "Emergency Contact", or quickly transferring funds to an acquaintance.', 
    how: "const contact = await bridge.contacts.pick();\nif (contact) {\n  inviteInput.value = contact.phoneNumber;\n  await bridge.ui.showToast(`Inviting ${contact.name}...`);\n}" 
  },
  playSystemSound: { 
    title: 'Play System Sound', 
    why: 'Triggers the default OS notification ping.', 
    when: 'Use this audibly alert the user when a long-running background task finishes, a timer expires, or a new real-time chat message arrives.', 
    how: "chatSocket.on('new_message', async (msg) => {\n  renderMessage(msg);\n  await bridge.audio.playSystemSound();\n});" 
  },
  requestPermission: { 
    title: 'Request Permission', 
    why: 'Safely triggers the native OS modal asking the user to grant access to restricted hardware/software.', 
    when: 'Always call this *before* attempting to use the Camera, Location, or Contacts APIs. Handle the boolean result gracefully.', 
    how: "const granted = await bridge.permissions.request('camera');\nif (granted) {\n  openBarcodeScanner();\n} else {\n  showManualEntryForm();\n}" 
  },
  setItem: { 
    title: 'Set Storage', 
    why: 'Saves standard key-value data directly to the native OS storage (UserDefaults / SharedPreferences).', 
    when: 'Perfect for saving non-sensitive preferences (like "hasSeenTutorial") that must survive even if the WebView cache is completely wiped.', 
    how: "await bridge.storage.setItem('has_seen_onboarding', true);\nconsole.log('Preference saved natively');" 
  },
  getItem: { 
    title: 'Get Storage', 
    why: 'Retrieves standard key-value data from the native OS.', 
    when: 'Check this on boot to determine routing, such as skipping the welcome carousel if the user has been here before.', 
    how: "const hasSeen = await bridge.storage.getItem('has_seen_onboarding');\nif (!hasSeen) {\n  router.push('/welcome');\n}" 
  },
  biometricsAuth: { 
    title: 'Biometrics Authentication', 
    why: 'Summons FaceID, TouchID, or the Android Biometric Prompt to verify the physical user\'s identity.', 
    when: 'Force authentication before exposing highly sensitive screens like Payment Methods, Password Vaults, or Security Settings.', 
    how: "const passed = await bridge.biometrics.authenticate('Verify identity to view wallet');\nif (passed) {\n  showWalletKey();\n} else {\n  router.push('/dashboard');\n}" 
  },
  eventAppPause: { 
    title: 'App Pause & Resume', 
    why: 'Listens for background/foreground lifecycle events broadcasted by the OS.', 
    when: 'Use `onAppPause` to pause videos, halt WebGL loops, or close WebSockets. Use `onAppResume` to reconnect and fetch fresh data.', 
    how: "bridge.events.onAppResume(async () => {\n  console.log('User came back!');\n  await fetchLatestData();\n});" 
  },
  eventTheme: { 
    title: 'Theme & Keyboard Events', 
    why: 'Listens for sudden UI state changes, like the virtual keyboard sliding up or the OS theme changing at sunset.', 
    when: 'Use `onKeyboardChanged` to dynamically hide sticky bottom navbars so they don\'t overlap the input field.', 
    how: "bridge.events.onKeyboardChanged(({ isVisible }) => {\n  document.getElementById('bottom-nav').style.display = isVisible ? 'none' : 'flex';\n});" 
  },
  eventDeepLink: { 
    title: 'Deep Links & Network Events', 
    why: 'Catches critical OS broadcasts, like the user losing WiFi or clicking an external URL that opens your app.', 
    when: 'Use `onDeepLink` to intercept custom URLs (myapp://product/123) and programmatically drive your Vue Router to the right page.', 
    how: "bridge.events.onDeepLink(({ url }) => {\n  const route = extractPath(url);\n  router.push(route);\n});" 
  },
  eventCustom: { 
    title: 'Custom Event Listener', 
    why: 'Attaches a persistent listener to ingest continuous, real-time data pushed by custom native code.', 
    when: 'Use this to pipe data from complex native background services, like live Bluetooth scanner streams or constant GPS tracking.', 
    how: "bridge.custom.listen('native.sensor.heartrate', (payload) => {\n  bpm.value = payload.rate;\n  updateChart(payload.rate);\n});" 
  },
  clearCaches: { 
    title: 'Clear App Caches', 
    why: 'A nuclear option that wipes all WebView data, including the Network Cache, LocalStorage, IndexedDB, and Cookies.', 
    when: 'Use this immediately before triggering a full page reload to force the native wrapper to download your newest Vue.js UI update.', 
    how: "await bridge.app.clearCaches();\n// Cache busted, safe to reload:\nwindow.location.assign('https://myapp.com/?t=' + Date.now());" 
  }
};

const openDoc = (key: string) => { activeDoc.value = docDB[key]; };
const addLog = (action: string, data: any, isError = false, isEvent = false) => logs.value.unshift({ time: new Date().toLocaleTimeString([], { hour12: false }), action, data, isError, isEvent });
const formatData = (d: any) => d === undefined ? 'undefined' : d === null ? 'null' : typeof d === 'object' ? JSON.stringify(d, null, 2) : String(d);

const shareTestImage = async () => bridge.share.image("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==", "red.png");
const shareTestVideo = async () => bridge.share.video("AAAAIGZ0eXBpc29tAAACAGlzb21pc28yYXZjMQAAAAxtZGF0AAAAAHAAAABobW9vdgAAAGxtdmhk", "dummy.mp4");

const execute = async (action: string, call: () => any) => { try { addLog(action, (await call()) || 'Success'); } catch (e: any) { addLog(action, e.message || e, true); } };
const sendCustom = async (type: 'request' | 'command') => {
  let p: any; try { p = JSON.parse(customPayload.value); } catch { return addLog('Parse Error', 'Invalid JSON', true); }
  if (type === 'command') { bridge.custom.send(customAction.value, p); addLog(`Command: ${customAction.value}`, 'Dispatched'); } 
  else { try { addLog(`Request: ${customAction.value}`, await bridge.custom.invoke(customAction.value, p)); } catch (e: any) { addLog(`Request`, e.message || e, true); } }
};

let unsubs: Array<() => void> = [];
onMounted(() => {
  unsubs.push(bridge.events.onAppPause(() => addLog('EVENT: app.pause', 'Backgrounded', false, true)));
  unsubs.push(bridge.events.onAppResume(() => addLog('EVENT: app.resume', 'Foregrounded', false, true)));
  unsubs.push(bridge.events.onThemeChanged((p) => addLog('EVENT: themeChanged', p, false, true)));
  unsubs.push(bridge.events.onPushNotification((p) => addLog('EVENT: pushNotification', p, false, true)));
  unsubs.push(bridge.events.onBackButton(() => addLog('EVENT: backButton', 'Pressed', false, true)));
  unsubs.push(bridge.events.onDeepLink((p) => addLog('EVENT: deepLink', p, false, true)));
  unsubs.push(bridge.events.onNetworkChanged((p) => addLog('EVENT: networkChanged', p, false, true)));
  unsubs.push(bridge.events.onKeyboardChanged((p) => addLog('EVENT: keyboardChanged', p, false, true)));
  unsubs.push(bridge.custom.listen('custom.event.proprietaryData', (p) => addLog('EVENT: custom', p, false, true)));
  bridge.core.signalWebReady(); addLog('System', 'Bridge initialized.');
});
onUnmounted(() => unsubs.forEach(u => u()));
</script>

<style scoped>
.harness-container { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; color: #333; }
.header { text-align: center; margin-bottom: 30px; } .header h1 { margin: 0 0 5px 0; color: #2c3e50; } .header p { margin: 0; color: #666; }
.layout { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; align-items: start; }
@media (max-width: 768px) { .layout { grid-template-columns: 1fr; } }
.panel { background: #f8f9fa; border-radius: 8px; padding: 20px; margin-bottom: 20px; border: 1px solid #e9ecef; }
.panel h2 { margin-top: 0; font-size: 1.2rem; color: #2c3e50; border-bottom: 2px solid #e9ecef; padding-bottom: 10px; }
.input-group { margin-bottom: 15px; } .input-group label { display: block; font-weight: bold; margin-bottom: 5px; font-size: 0.9rem; }
.input-group input, .input-group textarea { width: 100%; padding: 10px; border: 1px solid #ced4da; border-radius: 4px; font-family: monospace; font-size: 0.9rem; box-sizing: border-box; }
.button-group { display: flex; gap: 10px; }
.method-group { margin-bottom: 20px; } .method-group h3 { font-size: 1rem; color: #495057; margin-bottom: 10px; }
.action-badge { display: inline-flex; align-items: stretch; margin: 0 8px 8px 0; border-radius: 4px; overflow: hidden; border: 1px solid #ced4da; }
.action-badge button { border: none; margin: 0; border-radius: 0; background: #fff; padding: 8px 12px; cursor: pointer; font-size: 0.85rem; transition: all 0.2s; color: #333; }
.action-badge button:hover { background: #e9ecef; }
.btn-info { background: #e9ecef !important; padding: 8px 10px !important; font-weight: bold; color: #495057 !important; border-left: 1px solid #ced4da !important; }
.btn-info:hover { background: #dee2e6 !important; color: #000 !important; }
.btn-event-doc { background: #e3f2fd !important; color: #0d47a1 !important; font-weight: 500; width: 100%; } .btn-event-doc:hover { background: #bbdefb !important; }
button.btn-primary { background: #42b883 !important; color: white !important; border-right: 1px solid #33a06f !important; } button.btn-primary:hover { background: #33a06f !important; }
button.btn-secondary { background: #6c757d !important; color: white !important; border-right: 1px solid #5a6268 !important; } button.btn-secondary:hover { background: #5a6268 !important; }
.terminal-container { background: #1e1e1e; border-radius: 8px; overflow: hidden; display: flex; flex-direction: column; height: 80vh; position: sticky; top: 20px; }
.terminal-header { background: #333; color: #fff; padding: 10px 15px; display: flex; justify-content: space-between; align-items: center; font-weight: bold; font-size: 0.9rem; }
.btn-clear { background: transparent; color: #ff6b6b; border: 1px solid #ff6b6b; margin: 0; border-radius: 4px; padding: 4px 8px; cursor: pointer;} .btn-clear:hover { background: #ff6b6b; color: white; }
.terminal-body { padding: 15px; overflow-y: auto; flex-grow: 1; display: flex; flex-direction: column-reverse; }
.empty-log { color: #888; text-align: center; margin-top: 20px; font-style: italic; }
.log-entry { margin-bottom: 15px; font-family: monospace; font-size: 0.85rem; padding-bottom: 15px; border-bottom: 1px solid #333; }
.log-time { color: #888; margin-bottom: 2px; } .log-action { color: #42b883; font-weight: bold; margin-bottom: 5px; } .log-data { margin: 0; color: #d4d4d4; white-space: pre-wrap; word-break: break-all; }
.log-error .log-action { color: #ff6b6b; } .log-error .log-data { color: #ffaaaa; } .log-event .log-action { color: #fca311; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.6); display: flex; justify-content: center; align-items: center; z-index: 1000; backdrop-filter: blur(2px); }
.modal-content { background: #fff; width: 90%; max-width: 500px; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.2); animation: popIn 0.2s ease-out; }
.modal-header { background: #f8f9fa; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e9ecef; } .modal-header h3 { margin: 0; color: #2c3e50; font-size: 1.2rem; }
.btn-close-modal { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: #6c757d; padding: 0; } .btn-close-modal:hover { color: #dc3545; }
.modal-body { padding: 20px; } .doc-section { margin-bottom: 20px; } .doc-section h4 { margin: 0 0 8px 0; color: #495057; font-size: 0.95rem; text-transform: uppercase; letter-spacing: 0.5px; }
.doc-section p { margin: 0; color: #333; line-height: 1.5; font-size: 0.95rem; } .code-block { background: #1e1e1e; color: #d4d4d4; padding: 12px; border-radius: 6px; font-family: monospace; font-size: 0.85rem; overflow-x: auto; white-space: pre-wrap; }
@keyframes popIn { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>