import {app, BrowserWindow, ipcMain, shell} from 'electron'
import {join} from 'path'
import {electronApp, is, optimizer} from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
import path from "node:path";
import {spawn} from "child_process";
import net from "node:net";

let javaProcess = null;

function findAvailablePort(startPort = 3000) {
  return new Promise((resolve) => {
    const server = net.createServer();
    server.listen(startPort, () => {
      const port = server.address().port;
      server.close(() => resolve(port));
    });

    server.on("error", () => {
      resolve(findAvailablePort(startPort + 1)); // Si el puerto está ocupado, probar el siguiente
    });
  });
}

function waitForWebSocketStartup(javaProcess) {
  return new Promise((resolve) => {
    javaProcess.stdout.on("data", (data) => {
      const output = data.toString();
      console.log(`JAR: ${output}`);

      if (output.includes("Servidor WebSocket iniciado")) {
        resolve(); // Resuelve la promesa cuando el mensaje aparece
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

  const port = await findAvailablePort(3000); // Busca un puerto libre a partir del 3000
  const jarPath = is.dev
    ? path.join(__dirname, "../../resources/ChatClient-1.0-SNAPSHOT.jar")  // Ruta en desarrollo
    : path.join(process.resourcesPath, "resources", "ChatClient-1.0-SNAPSHOT.jar"); // Ruta en producción

  javaProcess = spawn("java", ["-jar", jarPath, port.toString()], {
    stdio: ["pipe", "pipe", "pipe"],
  });

  javaProcess.stdout.on("data", (data) => console.log(`JAR: ${data}`));
  javaProcess.stderr.on("data", (data) => console.error(`JAR Error: ${data}`));

  javaProcess.on("exit", (code, signal) => {
    console.error(`El proceso Java se cerró con código: ${code}, señal: ${signal}`);
  });

  javaProcess.on("error", (err) => {
    console.error(`Error al iniciar el proceso Java: ${err}`);
  });

  javaProcess.stdout.on("data", (data) => console.log(`JAR: ${data}`));
  javaProcess.stderr.on("data", (data) => console.error(`JAR Error: ${data}`));
  await waitForWebSocketStartup(javaProcess);

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
