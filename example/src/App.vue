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
          <div class="input-group">
            <label>Action Name</label>
            <input v-model="customAction" type="text" placeholder="e.g. custom.business.scanBarcode" />
          </div>
          <div class="input-group">
            <label>Payload (JSON)</label>
            <textarea v-model="customPayload" rows="3"></textarea>
          </div>
          <div class="button-group">
            <span class="action-badge">
              <button @click="sendCustom('request')" class="btn-primary">Send Custom Request</button>
              <button class="btn-info" @click="openDoc('customRequest')">?</button>
            </span>
            <span class="action-badge">
              <button @click="sendCustom('command')" class="btn-secondary">Send Custom Command</button>
              <button class="btn-info" @click="openDoc('customCommand')">?</button>
            </span>
          </div>
        </div>

        <div class="panel methods-panel">
          <h2>📱 Predefined Methods</h2>
          
          <div class="method-group">
            <h3>System & Device</h3>
            <span class="action-badge"><button @click="execute('core.signalWebReady', () => bridge.core.signalWebReady())">Signal Web Ready</button><button class="btn-info" @click="openDoc('signalWebReady')">?</button></span>
            <span class="action-badge"><button @click="execute('info.getComplete', () => bridge.info.getComplete())">Get Device Info</button><button class="btn-info" @click="openDoc('getComplete')">?</button></span>
            <span class="action-badge"><button @click="execute('device.getLanguage', () => bridge.device.getLanguage())">Get Language</button><button class="btn-info" @click="openDoc('getLanguage')">?</button></span>
            <span class="action-badge"><button @click="execute('device.getBatteryLevel', () => bridge.device.getBatteryLevel())">Get Battery</button><button class="btn-info" @click="openDoc('getBatteryLevel')">?</button></span>
            <span class="action-badge"><button @click="execute('screen.setKeepScreenOn', () => bridge.screen.setKeepScreenOn(true))">Keep Screen On</button><button class="btn-info" @click="openDoc('setKeepScreenOn')">?</button></span>
            <span class="action-badge"><button @click="execute('app.openSettings', () => bridge.app.openSettings())">Open Settings</button><button class="btn-info" @click="openDoc('openSettings')">?</button></span>
            <span class="action-badge"><button @click="execute('app.exit', () => bridge.app.exit())">Exit App</button><button class="btn-info" @click="openDoc('exitApp')">?</button></span>
          </div>

          <div class="method-group">
            <h3>Hardware, Network & Location</h3>
            <span class="action-badge"><button @click="execute('network.getStatus', () => bridge.network.getStatus())">Get Network Status</button><button class="btn-info" @click="openDoc('getStatus')">?</button></span>
            <span class="action-badge"><button @click="execute('location.getCurrent', () => bridge.location.getCurrent())">Get GPS Location</button><button class="btn-info" @click="openDoc('getCurrentLocation')">?</button></span>
            <span class="action-badge"><button @click="execute('hardware.triggerHaptic', () => bridge.hardware.triggerHaptic('heavy'))">Haptic (Heavy)</button><button class="btn-info" @click="openDoc('triggerHaptic')">?</button></span>
            <span class="action-badge"><button @click="execute('hardware.toggleFlashlight', () => bridge.hardware.toggleFlashlight(true))">Flashlight ON</button><button class="btn-info" @click="openDoc('toggleFlashlight')">?</button></span>
          </div>

          <div class="method-group">
            <h3>UI & Feedback</h3>
            <span class="action-badge"><button @click="execute('ui.showToast', () => bridge.ui.showToast('Hello from Vue 3!', 'short'))">Show Toast</button><button class="btn-info" @click="openDoc('showToast')">?</button></span>
            <span class="action-badge"><button @click="execute('ui.showAlert', () => bridge.ui.showAlert('Warning', 'This is a test alert', 'Got it'))">Show Alert</button><button class="btn-info" @click="openDoc('showAlert')">?</button></span>
            <span class="action-badge"><button @click="execute('ui.showConfirm', () => bridge.ui.showConfirm('Proceed?', 'Do you want to continue?'))">Show Confirm</button><button class="btn-info" @click="openDoc('showConfirm')">?</button></span>
            <span class="action-badge"><button @click="execute('ui.getTheme', () => bridge.ui.getTheme())">Get OS Theme</button><button class="btn-info" @click="openDoc('getTheme')">?</button></span>
          </div>

          <div class="method-group">
            <h3>Media, Files & Contacts</h3>
            <span class="action-badge"><button @click="execute('media.takePhoto', () => bridge.media.takePhoto({ base64: true }))">Take Photo</button><button class="btn-info" @click="openDoc('takePhoto')">?</button></span>
            <span class="action-badge"><button @click="execute('media.pickImage', () => bridge.media.pickImage({ base64: true }))">Pick Image</button><button class="btn-info" @click="openDoc('pickImage')">?</button></span>
            <span class="action-badge"><button @click="execute('file.pick', () => bridge.file.pick())">Pick File</button><button class="btn-info" @click="openDoc('pickFile')">?</button></span>
            <span class="action-badge"><button @click="execute('contacts.pick', () => bridge.contacts.pick())">Pick Contact</button><button class="btn-info" @click="openDoc('pickContact')">?</button></span>
            <span class="action-badge"><button @click="execute('audio.playSystemSound', () => bridge.audio.playSystemSound())">Play Sound</button><button class="btn-info" @click="openDoc('playSystemSound')">?</button></span>
          </div>

          <div class="method-group">
            <h3>Storage, Auth & Permissions</h3>
            <span class="action-badge"><button @click="execute('permissions.request(camera)', () => bridge.permissions.request('camera'))">Req Camera Perm</button><button class="btn-info" @click="openDoc('requestPermission')">?</button></span>
            <span class="action-badge"><button @click="execute('permissions.request(location)', () => bridge.permissions.request('location'))">Req Location Perm</button><button class="btn-info" @click="openDoc('requestPermission')">?</button></span>
            <span class="action-badge"><button @click="execute('storage.setItem', () => bridge.storage.setItem('testKey', 'VueData123'))">Set Storage</button><button class="btn-info" @click="openDoc('setItem')">?</button></span>
            <span class="action-badge"><button @click="execute('storage.getItem', () => bridge.storage.getItem('testKey'))">Get Storage</button><button class="btn-info" @click="openDoc('getItem')">?</button></span>
            <span class="action-badge"><button @click="execute('biometrics.authenticate', () => bridge.biometrics.authenticate('Please verify identity'))">Biometrics Auth</button><button class="btn-info" @click="openDoc('biometricsAuth')">?</button></span>
          </div>
          
          <div class="method-group">
            <h3>Background Events (Docs)</h3>
            <span class="action-badge"><button class="btn-event-doc" @click="openDoc('eventAppPause')">App Pause / Resume</button></span>
            <span class="action-badge"><button class="btn-event-doc" @click="openDoc('eventTheme')">Theme / Keyboard</button></span>
            <span class="action-badge"><button class="btn-event-doc" @click="openDoc('eventDeepLink')">Deep Links / Push</button></span>
            <span class="action-badge"><button class="btn-event-doc" @click="openDoc('eventCustom')">Custom Listeners</button></span>
          </div>
        </div>
      </section>

      <section class="terminal-container">
        <div class="terminal-header">
          <span>Logs & Responses</span>
          <button @click="logs = []" class="btn-clear">Clear</button>
        </div>
        <div class="terminal-body">
          <div v-if="logs.length === 0" class="empty-log">Awaiting bridge events...</div>
          <div 
            v-for="(log, idx) in logs" 
            :key="idx" 
            class="log-entry" 
            :class="{ 'log-error': log.isError, 'log-event': log.isEvent }"
          >
            <div class="log-time">[{{ log.time }}]</div>
            <div class="log-action">{{ log.action }}</div>
            <pre class="log-data">{{ formatData(log.data) }}</pre>
          </div>
        </div>
      </section>
    </main>

    <div v-if="activeDoc" class="modal-overlay" @click="activeDoc = null">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>📘 {{ activeDoc.title }}</h3>
          <button class="btn-close-modal" @click="activeDoc = null">✕</button>
        </div>
        <div class="modal-body">
          <div class="doc-section">
            <h4>Why use this?</h4>
            <p>{{ activeDoc.why }}</p>
          </div>
          <div class="doc-section">
            <h4>When to call this?</h4>
            <p>{{ activeDoc.when }}</p>
          </div>
          <div class="doc-section">
            <h4>How to use it? (TypeScript)</h4>
            <pre class="code-block">{{ activeDoc.how }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { bridge } from 'native-web-bridge';

// --- STATE ---
const customAction = ref('custom.business.scanBarcode');
const customPayload = ref('{\n  "type": "qr_code"\n}');
const activeDoc = ref<DocEntry | null>(null);

interface LogEntry { time: string; action: string; data: any; isError?: boolean; isEvent?: boolean; }
const logs = ref<LogEntry[]>([]);

// --- DOCUMENTATION DICTIONARY ---
interface DocEntry { title: string; why: string; when: string; how: string; }
const docDB: Record<string, DocEntry> = {
  customRequest: { title: 'Custom Request', why: 'To trigger proprietary Android logic and wait for a response.', when: 'Use this when your web app needs data from a custom Android API (e.g., a Bluetooth scanner).', how: "const result = await bridge.custom.invoke('custom.scan', { type: 'qr' });" },
  customCommand: { title: 'Custom Command', why: 'To trigger native Android logic without waiting for a reply.', when: 'Use this for "Fire and Forget" actions like tracking analytics natively.', how: "bridge.custom.send('custom.track', { event: 'click' });" },
  signalWebReady: { title: 'Signal Web Ready', why: 'To prevent Race Conditions and dropped messages during boot.', when: 'Call this exactly ONCE in your onMounted hook when the Vue/React app is ready to receive events.', how: 'bridge.core.signalWebReady();' },
  getComplete: { title: 'Get Device Info', why: 'To gather analytics and hardware specifications.', when: 'Use this to attach device models and OS versions to bug reports or API headers.', how: 'const info = await bridge.info.getComplete();\nconsole.log(info.deviceName);' },
  getLanguage: { title: 'Get OS Language', why: 'To localize your web app instantly.', when: 'Use this on boot to set your i18n locale to match the user\'s native OS preference.', how: "const locale = await bridge.device.getLanguage(); // e.g. 'en-US'" },
  getBatteryLevel: { title: 'Get Battery Level', why: 'To adapt performance based on power availability.', when: 'Use this to pause heavy WebGL animations if the user\'s battery drops below 15%.', how: 'const level = await bridge.device.getBatteryLevel(); // 1-100' },
  setKeepScreenOn: { title: 'Keep Screen On', why: 'To prevent the phone from sleeping.', when: 'Use this when displaying a barcode for a cashier to scan, or during a video call.', how: 'await bridge.screen.setKeepScreenOn(true);' },
  openSettings: { title: 'Open App Settings', why: 'To help the user fix disabled permissions.', when: 'Use this if permissions.request() fails and you need the user to manually enable it.', how: 'await bridge.app.openSettings();' },
  exitApp: { title: 'Exit Application', why: 'To programmatically close the Android app.', when: 'Use this on a "Logout & Quit" button, or if a critical update forces app closure.', how: 'await bridge.app.exit();' },
  getStatus: { title: 'Get Network Status', why: 'To check internet connectivity.', when: 'Use this before attempting large file uploads to warn the user if they are on cellular data.', how: 'const net = await bridge.network.getStatus();\nif (net.type === "cellular") warnUser();' },
  getCurrentLocation: { title: 'Get GPS Location', why: 'To get precise hardware coordinates.', when: 'Use this for maps or delivery tracking. Note: Requires Location Permissions to be granted first!', how: 'const { lat, lng } = await bridge.location.getCurrent();' },
  triggerHaptic: { title: 'Trigger Haptic Feedback', why: 'To make the web app feel like a real native app.', when: 'Use this on primary button clicks, pull-to-refresh, or success/error states.', how: "await bridge.hardware.triggerHaptic('heavy');" },
  toggleFlashlight: { title: 'Toggle Flashlight', why: 'To assist users in dark environments.', when: 'Use this inside a custom QR/Barcode scanner UI.', how: 'await bridge.hardware.toggleFlashlight(true);' },
  showToast: { title: 'Native Toast', why: 'To show unobtrusive OS-level alerts.', when: 'Use this for quick success messages like "Profile Saved" that shouldn\'t block the UI.', how: "await bridge.ui.showToast('Profile Saved!', 'short');" },
  showAlert: { title: 'Native Alert', why: 'To block the UI and force the user to read a message.', when: 'Use this for critical errors (e.g., "Payment Failed").', how: "await bridge.ui.showAlert('Error', 'Payment Failed', 'OK');" },
  showConfirm: { title: 'Native Confirm', why: 'To get a boolean Yes/No from the user safely.', when: 'Use this before deleting data or navigating away from unsaved changes.', how: "const isYes = await bridge.ui.showConfirm('Delete?', 'Are you sure?');" },
  getTheme: { title: 'Get OS Theme', why: 'To match Android\'s Dark/Light mode.', when: 'Use this on boot to set your CSS variables.', how: "const theme = await bridge.ui.getTheme(); // 'dark' | 'light'" },
  takePhoto: { title: 'Take Photo', why: 'To open the native Camera app.', when: 'Use this for profile picture uploads or receipt scanning.', how: "const img = await bridge.media.takePhoto({ base64: true });\nimgSrc.value = `data:image/jpeg;base64,${img.base64}`;" },
  pickImage: { title: 'Pick Image', why: 'To open the native Gallery app.', when: 'Use this to let users select an existing photo from their phone.', how: "const img = await bridge.media.pickImage({ base64: true });" },
  pickFile: { title: 'Pick File', why: 'To select PDFs, Docs, or other files.', when: 'Use this to handle file uploads natively instead of relying on <input type="file">.', how: 'const file = await bridge.file.pick();\nconsole.log(file.name, file.base64);' },
  pickContact: { title: 'Pick Contact', why: 'To read the user\'s address book natively.', when: 'Use this for "Invite a Friend" or "Select Recipient" features.', how: 'const contact = await bridge.contacts.pick();\nconsole.log(contact.name, contact.phoneNumber);' },
  playSystemSound: { title: 'Play System Sound', why: 'To alert the user audibly.', when: 'Use this when a background task finishes, or a new chat message arrives.', how: 'await bridge.audio.playSystemSound();' },
  requestPermission: { title: 'Request Permission', why: 'To dynamically prompt the user for OS access.', when: 'Use this BEFORE calling location or camera APIs to ensure the OS hasn\'t blocked you.', how: "const granted = await bridge.permissions.request('camera');\nif (granted) openCamera();" },
  setItem: { title: 'Set Secure Storage', why: 'To save data permanently to the device (persists through app restarts).', when: 'Use this to store JWT Auth Tokens, user preferences, or offline drafts.', how: "await bridge.storage.setItem('userToken', 'abc.123');" },
  getItem: { title: 'Get Secure Storage', why: 'To retrieve permanently saved data.', when: 'Use this on boot to check if the user is already logged in.', how: "const token = await bridge.storage.getItem('userToken');" },
  biometricsAuth: { title: 'Biometrics Auth', why: 'To secure sensitive areas of the app using FaceID/TouchID.', when: 'Use this before exposing payment details, passwords, or secure settings.', how: "const passed = await bridge.biometrics.authenticate('Unlock Vault');" },
  eventAppPause: { title: 'App Pause & Resume', why: 'To react to the user leaving or returning to the app.', when: 'Use onAppPause to stop videos/heavy polling. Use onAppResume to fetch fresh data.', how: "bridge.events.onAppResume(() => {\n  console.log('App is foregrounded again!');\n  fetchFreshData();\n});" },
  eventTheme: { title: 'Theme & Keyboard Events', why: 'To dynamically fix UI overlaps and color clashes.', when: 'Use onKeyboardChanged to hide sticky bottom navbars when typing. Use onThemeChanged to swap CSS.', how: "bridge.events.onKeyboardChanged(({ isVisible }) => {\n  hideBottomNav.value = isVisible;\n});" },
  eventDeepLink: { title: 'Deep Links & Push', why: 'To route the user based on external triggers.', when: 'Use onDeepLink to handle URL clicks (myapp://profile/1). Use onPushNotification to handle Firebase alerts.', how: "bridge.events.onDeepLink(({ url }) => {\n  router.push(parseUrl(url));\n});" },
  eventCustom: { title: 'Custom Event Listener', why: 'To listen for background data pushed continuously by Android.', when: 'Use this to ingest real-time data from native trackers, sensors, or ongoing services.', how: "bridge.custom.listen('driver.location.update', (data) => {\n  mapMarker.setLatLng(data.lat, data.lng);\n});" },
};

const openDoc = (key: string) => { activeDoc.value = docDB[key]; };

// --- METHODS ---
const addLog = (action: string, data: any, isError = false, isEvent = false) => {
  logs.value.unshift({ time: new Date().toLocaleTimeString([], { hour12: false }), action, data, isError, isEvent });
};

const formatData = (data: any) => {
  if (data === undefined) return 'undefined';
  if (data === null) return 'null';
  if (typeof data === 'object') return JSON.stringify(data, null, 2);
  return String(data);
};

const execute = async (actionName: string, bridgeCall: () => any) => {
  try { addLog(actionName, (await bridgeCall()) || 'Success (No payload)'); } 
  catch (error: any) { addLog(actionName, error.message || error, true); }
};

const sendCustom = async (type: 'request' | 'command') => {
  let parsedPayload: any;
  try { parsedPayload = JSON.parse(customPayload.value); } 
  catch (e) { addLog('JSON Parse Error', 'Invalid payload JSON format', true); return; }

  if (type === 'command') {
    bridge.custom.send(customAction.value, parsedPayload);
    addLog(`Command: ${customAction.value}`, 'Dispatched (Fire & Forget)');
  } else {
    try { addLog(`Request: ${customAction.value}`, await bridge.custom.invoke(customAction.value, parsedPayload)); } 
    catch (error: any) { addLog(`Request: ${customAction.value}`, error.message || error, true); }
  }
};

// --- LIFECYCLE & EVENTS ---
let unsubs: Array<() => void> = [];

onMounted(() => {
  unsubs.push(bridge.events.onAppPause(() => addLog('EVENT: app.pause', 'App went to background', false, true)));
  unsubs.push(bridge.events.onAppResume(() => addLog('EVENT: app.resume', 'App returned to foreground', false, true)));
  unsubs.push(bridge.events.onThemeChanged((p) => addLog('EVENT: themeChanged', p, false, true)));
  unsubs.push(bridge.events.onPushNotification((p) => addLog('EVENT: pushNotification', p, false, true)));
  unsubs.push(bridge.events.onBackButton(() => addLog('EVENT: backButton', 'Hardware back button pressed', false, true)));
  unsubs.push(bridge.events.onDeepLink((p) => addLog('EVENT: deepLink', p, false, true)));
  unsubs.push(bridge.events.onNetworkChanged((p) => addLog('EVENT: networkChanged', p, false, true)));
  unsubs.push(bridge.events.onKeyboardChanged((p) => addLog('EVENT: keyboardChanged', p, false, true)));
  unsubs.push(bridge.custom.listen('custom.event.proprietaryData', (p) => addLog('EVENT: custom', p, false, true)));

  bridge.core.signalWebReady();
  addLog('System', 'Bridge initialized. Listening for events...');
});

onUnmounted(() => { unsubs.forEach(unsub => unsub()); });
</script>

<style scoped>
/* Base Reset & Layout */
.harness-container { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; color: #333; }
.header { text-align: center; margin-bottom: 30px; }
.header h1 { margin: 0 0 5px 0; color: #2c3e50; }
.header p { margin: 0; color: #666; }
.layout { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; align-items: start; }
@media (max-width: 768px) { .layout { grid-template-columns: 1fr; } }

/* Panels */
.panel { background: #f8f9fa; border-radius: 8px; padding: 20px; margin-bottom: 20px; border: 1px solid #e9ecef; }
.panel h2 { margin-top: 0; font-size: 1.2rem; color: #2c3e50; border-bottom: 2px solid #e9ecef; padding-bottom: 10px; }

/* Custom Call Builder */
.input-group { margin-bottom: 15px; }
.input-group label { display: block; font-weight: bold; margin-bottom: 5px; font-size: 0.9rem; }
.input-group input, .input-group textarea { width: 100%; padding: 10px; border: 1px solid #ced4da; border-radius: 4px; font-family: monospace; font-size: 0.9rem; box-sizing: border-box; }
.button-group { display: flex; gap: 10px; }

/* Predefined Methods & Badges */
.method-group { margin-bottom: 20px; }
.method-group h3 { font-size: 1rem; color: #495057; margin-bottom: 10px; }
.action-badge { display: inline-flex; align-items: stretch; margin: 0 8px 8px 0; border-radius: 4px; overflow: hidden; border: 1px solid #ced4da; }
.action-badge button { border: none; margin: 0; border-radius: 0; }

button { background: #fff; padding: 8px 12px; cursor: pointer; font-size: 0.85rem; transition: all 0.2s; color: #333; }
button:hover { background: #e9ecef; }
.btn-info { background: #e9ecef; padding: 8px 10px; font-weight: bold; color: #495057; border-left: 1px solid #ced4da !important; }
.btn-info:hover { background: #dee2e6; color: #000; }
.btn-event-doc { background: #e3f2fd; color: #0d47a1; font-weight: 500; width: 100%; }
.btn-event-doc:hover { background: #bbdefb; }

button.btn-primary { background: #42b883; color: white; border-right: 1px solid #33a06f; }
button.btn-primary:hover { background: #33a06f; }
button.btn-secondary { background: #6c757d; color: white; border-right: 1px solid #5a6268; }
button.btn-secondary:hover { background: #5a6268; }

/* Terminal */
.terminal-container { background: #1e1e1e; border-radius: 8px; overflow: hidden; display: flex; flex-direction: column; height: 80vh; position: sticky; top: 20px; }
.terminal-header { background: #333; color: #fff; padding: 10px 15px; display: flex; justify-content: space-between; align-items: center; font-weight: bold; font-size: 0.9rem; }
.btn-clear { background: transparent; color: #ff6b6b; border: 1px solid #ff6b6b; margin: 0; border-radius: 4px; }
.btn-clear:hover { background: #ff6b6b; color: white; }
.terminal-body { padding: 15px; overflow-y: auto; flex-grow: 1; display: flex; flex-direction: column-reverse; }
.empty-log { color: #888; text-align: center; margin-top: 20px; font-style: italic; }
.log-entry { margin-bottom: 15px; font-family: monospace; font-size: 0.85rem; padding-bottom: 15px; border-bottom: 1px solid #333; }
.log-time { color: #888; margin-bottom: 2px; }
.log-action { color: #42b883; font-weight: bold; margin-bottom: 5px; }
.log-data { margin: 0; color: #d4d4d4; white-space: pre-wrap; word-break: break-all; }
.log-error .log-action { color: #ff6b6b; }
.log-error .log-data { color: #ffaaaa; }
.log-event .log-action { color: #fca311; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.6); display: flex; justify-content: center; align-items: center; z-index: 1000; backdrop-filter: blur(2px); }
.modal-content { background: #fff; width: 90%; max-width: 500px; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.2); animation: popIn 0.2s ease-out; }
.modal-header { background: #f8f9fa; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e9ecef; }
.modal-header h3 { margin: 0; color: #2c3e50; font-size: 1.2rem; }
.btn-close-modal { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: #6c757d; padding: 0; }
.btn-close-modal:hover { color: #dc3545; background: none; }
.modal-body { padding: 20px; }
.doc-section { margin-bottom: 20px; }
.doc-section h4 { margin: 0 0 8px 0; color: #495057; font-size: 0.95rem; text-transform: uppercase; letter-spacing: 0.5px; }
.doc-section p { margin: 0; color: #333; line-height: 1.5; font-size: 0.95rem; }
.code-block { background: #1e1e1e; color: #d4d4d4; padding: 12px; border-radius: 6px; font-family: monospace; font-size: 0.85rem; overflow-x: auto; white-space: pre-wrap; }
@keyframes popIn { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>