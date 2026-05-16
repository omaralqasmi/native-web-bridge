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
  requestRating: { title: 'Request Rating', why: 'Prompts users to review your app.', when: 'Call this after user achieves a milestone natively.', how: 'await bridge.app.requestRating();' },
  forceUpdate: { title: 'Force Update', why: 'Directs user to App Store/Play Store.', when: 'When the API version is deprecated.', how: "await bridge.app.forceUpdate('https://...');" },
  setTheme: { title: 'Set Theme', why: 'Forces Native UI to match Web UI.', when: 'When user overrides device theme in your settings.', how: "await bridge.ui.setTheme('dark');" },
  downloadImage: { title: 'Download Image', why: 'Saves images to user camera roll natively.', when: 'Exporting generated memes or receipts.', how: "await bridge.media.downloadImage('https://...');" },
  shareText: { title: 'Share Text', why: 'Invokes OS share sheet.', when: 'Sharing quotes or plain urls.', how: "await bridge.share.text('Check this out!');" },
  shareLink: { title: 'Share Link', why: 'Shares formatted URLs.', when: 'Sharing invites.', how: "await bridge.share.link('https://google.com');" },
  dial: { title: 'Dial Number', why: 'Opens Native Phone app.', when: 'User clicks Customer Support.', how: "await bridge.communication.dialNumber('123456');" },
  openBrowser: { title: 'Open Browser', why: 'Escapes WebView to Chrome/Safari.', when: 'Opening external privacy policies.', how: "await bridge.communication.openBrowser('https://...');" },
  setSecure: { title: 'Secure Storage Set', why: 'Saves tokens to Keychain/EncryptedPrefs.', when: 'After login.', how: "await bridge.secureStorage.setItem('jwt', '...');" },
  getSecure: { title: 'Secure Storage Get', why: 'Retrieves encrypted tokens.', when: 'On app boot.', how: "await bridge.secureStorage.getItem('jwt');" },
  copyClip: { title: 'Copy Clipboard', why: 'Copies text natively.', when: 'Sharing referral codes.', how: "await bridge.clipboard.copy('CODE123');" },
  readClip: { title: 'Read Clipboard', why: 'Reads text from OS.', when: 'Auto-pasting OTPs.', how: "await bridge.clipboard.read();" },
  getToken: { title: 'Get Push Token', why: 'Reads FCM/APNs token.', when: 'Registering devices to backend.', how: "await bridge.notifications.getToken();" },
  setToken: { title: 'Set Push Token', why: 'Stores FCM/APNs token natively.', when: 'Fired by Firebase SDK.', how: "await bridge.notifications.setToken('xyz...');" },
  checkPerm: { title: 'Check Permission', why: 'Verifies OS access safely.', when: 'Before opening custom camera UI.', how: "await bridge.permissions.check('camera');" },
  checkPush: { title: 'Check Notification Permission', why: 'To see if the user has silenced or blocked your alerts.', when: 'On boot, so you can show a banner asking them to enable alerts in Settings if disabled.', how: "const isEnabled = await bridge.permissions.check('notifications');" },
  reqPush: { title: 'Request Notification Permission', why: 'To ask the OS to allow banners, sounds, and badges.', when: 'After the user completes a key action (like placing an order) so they understand WHY you need to notify them.', how: "const granted = await bridge.permissions.request('notifications');" },
  shareImage: { title: 'Share Image', why: 'Share asset natively.', when: 'Sharing photos.', how: "await bridge.share.image(base64, 'export.png');" },
  shareVideo: { title: 'Share Video', why: 'Share video file natively.', when: 'Exporting screen captures.', how: "await bridge.share.video(base64, 'export.mp4');" },
  customRequest: { title: 'Custom Request', why: 'Trigger Android/iOS logic.', when: 'Using custom native SDKs.', how: "await bridge.custom.invoke('custom.scan');" },
  customCommand: { title: 'Custom Command', why: 'Trigger logic without reply.', when: 'Fire and Forget tasks.', how: "bridge.custom.send('track');" },
  signalWebReady: { title: 'Signal Web Ready', why: 'Prevent boot race conditions.', when: 'In onMounted.', how: 'bridge.core.signalWebReady();' },
  getComplete: { title: 'Get Device Info', why: 'Analytics & specs.', when: 'Bug reports.', how: 'await bridge.info.getComplete();' },
  getLanguage: { title: 'Get OS Language', why: 'Localize app.', when: 'On boot.', how: "await bridge.device.getLanguage();" },
  getBatteryLevel: { title: 'Get Battery', why: 'Adapt performance.', when: 'Running heavy 3D.', how: 'await bridge.device.getBatteryLevel();' },
  setKeepScreenOn: { title: 'Keep Screen On', why: 'Prevent sleep.', when: 'Displaying barcode.', how: 'await bridge.screen.setKeepScreenOn(true);' },
  openSettings: { title: 'Open Settings', why: 'Fix permissions.', when: 'Permission denied.', how: 'await bridge.app.openSettings();' },
  exitApp: { title: 'Exit App', why: 'Close app.', when: 'Logout.', how: 'await bridge.app.exit();' },
  getStatus: { title: 'Network Status', why: 'Check internet.', when: 'Before upload.', how: 'await bridge.network.getStatus();' },
  getCurrentLocation: { title: 'Get GPS', why: 'Coords.', when: 'Maps.', how: 'await bridge.location.getCurrent();' },
  triggerHaptic: { title: 'Haptics', why: 'Native feel.', when: 'Button click.', how: "await bridge.hardware.triggerHaptic('heavy');" },
  toggleFlashlight: { title: 'Flashlight', why: 'Illuminate.', when: 'Scanner UI.', how: 'await bridge.hardware.toggleFlashlight(true);' },
  showToast: { title: 'Toast', why: 'Quick alert.', when: 'Saved profile.', how: "await bridge.ui.showToast('Saved');" },
  showAlert: { title: 'Alert', why: 'Blocking alert.', when: 'Error.', how: "await bridge.ui.showAlert('Err', 'Msg');" },
  showConfirm: { title: 'Confirm', why: 'Yes/No.', when: 'Delete items.', how: "await bridge.ui.showConfirm('Del?', 'Sure?');" },
  getTheme: { title: 'Get OS Theme', why: 'Match OS.', when: 'Boot.', how: "await bridge.ui.getTheme();" },
  takePhoto: { title: 'Take Photo', why: 'Camera.', when: 'Profile pic.', how: "await bridge.media.takePhoto();" },
  pickImage: { title: 'Pick Image', why: 'Gallery.', when: 'Upload photo.', how: "await bridge.media.pickImage();" },
  pickFile: { title: 'Pick File', why: 'Documents.', when: 'Upload PDF.', how: 'await bridge.file.pick();' },
  pickContact: { title: 'Pick Contact', why: 'Address book.', when: 'Invites.', how: 'await bridge.contacts.pick();' },
  playSystemSound: { title: 'Play Sound', why: 'Audible alert.', when: 'Message received.', how: 'await bridge.audio.playSystemSound();' },
  requestPermission: { title: 'Request Perm', why: 'Prompt user.', when: 'Before APIs.', how: "await bridge.permissions.request('camera');" },
  setItem: { title: 'Set Storage', why: 'Save data.', when: 'Offline cache.', how: "await bridge.storage.setItem('k', 'v');" },
  getItem: { title: 'Get Storage', why: 'Read data.', when: 'Boot.', how: "await bridge.storage.getItem('k');" },
  biometricsAuth: { title: 'Biometrics', why: 'FaceID/TouchID.', when: 'Unlock vault.', how: "await bridge.biometrics.authenticate();" },
  eventAppPause: { title: 'App Pause/Resume', why: 'Lifecycle.', when: 'Stop/start video.', how: "bridge.events.onAppResume(() => {});" },
  eventTheme: { title: 'Theme/Keyboard', why: 'UI states.', when: 'Swap CSS.', how: "bridge.events.onKeyboardChanged((p) => {});" },
  eventDeepLink: { title: 'Links/Push/Network', why: 'System broadcasts.', when: 'Routing.', how: "bridge.events.onDeepLink((p) => {});" },
  eventCustom: { title: 'Custom Listener', why: 'Native pushes.', when: 'Location tracking.', how: "bridge.custom.listen('data', (p) => {});" },
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