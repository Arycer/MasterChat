import {app, BrowserWindow, ipcMain, shell} from 'electron'
import {join} from 'path'
import {electronApp, is, optimizer} from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
import path from "node:path";
import {spawn} from "child_process";

let javaProcess = null;

function waitForWebSocketStartup(javaProcess) {
  return new Promise((resolve) => {
    javaProcess.stdout.on("data", (data) => {
      const output = data.toString();
      // Expresión regular para capturar el puerto desde el mensaje
      const match = output.match(/Servidor WebSocket iniciado en el puerto (\d+)/);

      if (match) {
        const port = parseInt(match[1], 10); // Extrae y convierte el puerto a número
        resolve(port); // Resuelve la promesa con el puerto
      }
    });

    javaProcess.stderr.on("data", (data) => {
      console.error(`JAR Error: ${data}`);
    });
  });
}

function createWindow() {
  const mainWindow = new BrowserWindow({
    width: 900,
    height: 670,
    show: false,
    autoHideMenuBar: true,
    ...(process.platform === 'linux' ? {icon} : {}),
    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      sandbox: false
    }
  })

  mainWindow.on('ready-to-show', () => {
    mainWindow.show()
  })

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return {action: 'deny'}
  })

  if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL'])
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }
}

app.whenReady().then(async () => {
  electronApp.setAppUserModelId('com.electron')

  app.on('browser-window-created', (_, window) => {
    optimizer.watchWindowShortcuts(window)
  })

  const jarPath = is.dev
    ? path.join(__dirname, "../../resources/ChatClient-1.0-SNAPSHOT.jar")  // Ruta en desarrollo
    : path.join(process.resourcesPath, "resources", "ChatClient-1.0-SNAPSHOT.jar"); // Ruta en producción

  javaProcess = spawn("java", ["-jar", jarPath], {
    stdio: ["pipe", "pipe", "pipe"],
  });

  javaProcess.on("exit", (code, signal) => {
    console.error(`El proceso Java se cerró con código: ${code}, señal: ${signal}`);
  });

  javaProcess.on("error", (err) => {
    console.error(`Error al iniciar el proceso Java: ${err}`);
  });

  javaProcess.stdout.on("data", (data) => console.log(`JAR: ${data}`));
  javaProcess.stderr.on("data", (data) => console.error(`JAR Error: ${data}`));
  let port = await waitForWebSocketStartup(javaProcess);

  ipcMain.on('ping', () => console.log('pong'))
  ipcMain.handle('get-port', () => port);

  createWindow()

  app.on('activate', function () {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

app.on('before-quit', () => {
  if (javaProcess) {
    console.log('Cerrando proceso Java...');
    javaProcess.kill(); // Mata el proceso Java
  }
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})
