<template>
  <div class="harness-container">
    <header class="header">
      <h1>NativeWebBridge 🌉</h1>
      <p>Cross-Platform Interactive Test Harness</p>
    </header>

    <main class="layout">
      <section class="controls">
        
        <div class="panel custom-panel">
          <h2>🛠 Custom Call Builder</h2>
          <div class="input-group">
            <label>Action Name</label>
            <input v-model="customAction" type="text" placeholder="e.g. system.ui.showToast" />
          </div>
          <div class="input-group">
            <label>Payload (JSON)</label>
            <textarea v-model="customPayload" rows="3"></textarea>
          </div>
          <div class="button-group">
            <button @click="sendCustom('request')" class="btn-primary">Send as Request (Expects Return)</button>
            <button @click="sendCustom('command')" class="btn-secondary">Send as Command (Fire & Forget)</button>
          </div>
        </div>

        <div class="panel methods-panel">
          <h2>📱 Predefined Methods</h2>
          
          <div class="method-group">
            <h3>System & Info</h3>
            <button @click="execute('core.signalWebReady', () => bridge.core.signalWebReady())">Signal Web Ready</button>
            <button @click="execute('info.getComplete', () => bridge.info.getComplete())">Get Device Info</button>
            <button @click="execute('app.openSettings', () => bridge.app.openSettings())">Open Settings</button>
            <button @click="execute('app.requestRating', () => bridge.app.requestRating())">Request Rating</button>
            <button @click="execute('app.exit', () => bridge.app.exit())">Exit App</button>
          </div>

          <div class="method-group">
            <h3>UI & Feedback</h3>
            <button @click="execute('ui.showToast', () => bridge.ui.showToast('Hello from Vue 3!', 'short'))">Show Toast</button>
            <button @click="execute('ui.showAlert', () => bridge.ui.showAlert('Warning', 'This is a test alert', 'Got it'))">Show Alert</button>
            <button @click="execute('ui.showConfirm', () => bridge.ui.showConfirm('Proceed?', 'Do you want to continue?'))">Show Confirm</button>
            <button @click="execute('ui.getTheme', () => bridge.ui.getTheme())">Get OS Theme</button>
          </div>

          <div class="method-group">
            <h3>Media & Share</h3>
            <button @click="execute('media.takePhoto', () => bridge.media.takePhoto({ base64: true }))">Take Photo (Camera)</button>
            <button @click="execute('media.pickImage', () => bridge.media.pickImage({ base64: true }))">Pick Image (Gallery)</button>
            <button @click="execute('share.text', () => bridge.share.text('Check out NativeWebBridge!'))">Share Text</button>
            <button @click="execute('clipboard.copy', () => bridge.clipboard.copy('Copied from Bridge!'))">Copy to Clipboard</button>
          </div>

          <div class="method-group">
            <h3>Hardware & Communication</h3>
            <button @click="execute('hardware.triggerHaptic', () => bridge.hardware.triggerHaptic('heavy'))">Haptic (Heavy)</button>
            <button @click="execute('hardware.toggleFlashlight (ON)', () => bridge.hardware.toggleFlashlight(true))">Flashlight ON</button>
            <button @click="execute('hardware.toggleFlashlight (OFF)', () => bridge.hardware.toggleFlashlight(false))">Flashlight OFF</button>
            <button @click="execute('communication.openBrowser', () => bridge.communication.openBrowser('https://vuejs.org'))">Open Browser</button>
          </div>

          <div class="method-group">
            <h3>Storage & Security</h3>
            <button @click="execute('storage.setItem', () => bridge.storage.setItem('testKey', 'VueData123'))">Set Storage</button>
            <button @click="execute('storage.getItem', () => bridge.storage.getItem('testKey'))">Get Storage</button>
            <button @click="execute('biometrics.authenticate', () => bridge.biometrics.authenticate('Please verify identity'))">Biometrics Auth</button>
            <button @click="execute('permissions.check', () => bridge.permissions.check('camera'))">Check Camera Perm</button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { bridge } from 'native-web-bridge';

// --- STATE ---
const customAction = ref('system.ui.showToast');
const customPayload = ref('{\n  "message": "Custom payload works!",\n  "duration": "short"\n}');

interface LogEntry {
  time: string;
  action: string;
  data: any;
  isError?: boolean;
  isEvent?: boolean;
}
const logs = ref<LogEntry[]>([]);

// --- METHODS ---
const addLog = (action: string, data: any, isError = false, isEvent = false) => {
  logs.value.unshift({
    time: new Date().toLocaleTimeString([], { hour12: false }),
    action,
    data,
    isError,
    isEvent
  });
};

const formatData = (data: any) => {
  if (data === undefined) return 'undefined';
  if (data === null) return 'null';
  if (typeof data === 'object') return JSON.stringify(data, null, 2);
  return String(data);
};

// Generic executor for predefined methods
const execute = async (actionName: string, bridgeCall: () => any) => {
  try {
    const result = await bridgeCall();
    addLog(actionName, result || 'Success (No payload)');
  } catch (error: any) {
    addLog(actionName, error.message || error, true);
  }
};

// Executor for Custom Builder
const sendCustom = async (type: 'request' | 'command') => {
  let parsedPayload: any;
  try {
    parsedPayload = JSON.parse(customPayload.value);
  } catch (e) {
    addLog('JSON Parse Error', 'Invalid payload JSON format', true);
    return;
  }

  if (type === 'command') {
    bridge.core.sendCommand(customAction.value, parsedPayload);
    addLog(`Command: ${customAction.value}`, 'Dispatched (Fire & Forget)');
  } else {
    try {
      const response = await bridge.core.request(customAction.value, parsedPayload);
      addLog(`Request: ${customAction.value}`, response);
    } catch (error: any) {
      addLog(`Request: ${customAction.value}`, error.message || error, true);
    }
  }
};

// --- LIFECYCLE & EVENTS ---
let unsubs: Array<() => void> = [];

onMounted(() => {
  // Hook up event listeners from Native
  unsubs.push(bridge.events.onAppPause(() => addLog('EVENT: app.pause', null, false, true)));
  unsubs.push(bridge.events.onAppResume(() => addLog('EVENT: app.resume', null, false, true)));
  unsubs.push(bridge.events.onThemeChanged((p) => addLog('EVENT: themeChanged', p, false, true)));
  
  // Signal to the native wrapper that the Vue app has hydrated and is ready to receive events
  bridge.core.signalWebReady();
  addLog('System', 'Bridge initialized. Listening for events...');
});

onUnmounted(() => {
  // Clean up listeners
  unsubs.forEach(unsub => unsub());
});
</script>

<style scoped>
/* Base Reset & Layout */
.harness-container {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  color: #333;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}
.header h1 { margin: 0 0 5px 0; color: #2c3e50; }
.header p { margin: 0; color: #666; }

.layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
}

@media (max-width: 768px) {
  .layout { grid-template-columns: 1fr; }
}

/* Panels */
.panel {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #e9ecef;
}
.panel h2 { margin-top: 0; font-size: 1.2rem; color: #2c3e50; border-bottom: 2px solid #e9ecef; padding-bottom: 10px; }

/* Custom Call Builder */
.input-group { margin-bottom: 15px; }
.input-group label { display: block; font-weight: bold; margin-bottom: 5px; font-size: 0.9rem; }
.input-group input, .input-group textarea {
  width: 100%; padding: 10px; border: 1px solid #ced4da; border-radius: 4px; font-family: monospace; font-size: 0.9rem;
}
.button-group { display: flex; gap: 10px; }

/* Predefined Methods */
.method-group { margin-bottom: 20px; }
.method-group h3 { font-size: 1rem; color: #495057; margin-bottom: 10px; }
button {
  background: #fff; border: 1px solid #ced4da; padding: 8px 12px; margin: 0 5px 5px 0;
  border-radius: 4px; cursor: pointer; font-size: 0.85rem; transition: all 0.2s;
}
button:hover { background: #e9ecef; }
button.btn-primary { background: #42b883; color: white; border-color: #42b883; }
button.btn-primary:hover { background: #33a06f; }
button.btn-secondary { background: #6c757d; color: white; border-color: #6c757d; }
button.btn-secondary:hover { background: #5a6268; }

/* Terminal */
.terminal-container {
  background: #1e1e1e; border-radius: 8px; overflow: hidden; display: flex; flex-direction: column;
  height: 80vh; position: sticky; top: 20px;
}
.terminal-header {
  background: #333; color: #fff; padding: 10px 15px; display: flex; justify-content: space-between; align-items: center; font-weight: bold; font-size: 0.9rem;
}
.btn-clear { background: transparent; color: #ff6b6b; border: 1px solid #ff6b6b; margin: 0; }
.btn-clear:hover { background: #ff6b6b; color: white; }

.terminal-body {
  padding: 15px; overflow-y: auto; flex-grow: 1; display: flex; flex-direction: column-reverse; /* Keeps newest at bottom naturally, but we unshift so it pushes down */
}
.empty-log { color: #888; text-align: center; margin-top: 20px; font-style: italic; }

.log-entry { margin-bottom: 15px; font-family: monospace; font-size: 0.85rem; padding-bottom: 15px; border-bottom: 1px solid #333; }
.log-time { color: #888; margin-bottom: 2px; }
.log-action { color: #42b883; font-weight: bold; margin-bottom: 5px; }
.log-data { margin: 0; color: #d4d4d4; white-space: pre-wrap; word-break: break-all; }

/* Status Colors */
.log-error .log-action { color: #ff6b6b; }
.log-error .log-data { color: #ffaaaa; }
.log-event .log-action { color: #fca311; }
</style>