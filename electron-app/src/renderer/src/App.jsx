import {useEffect, useState} from 'react';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import ChatWindow from './components/ChatWindow';
import ServerManager from "./components/ServerManager";
import ServerForm from "./components/ServerForm";
import notificationSoundFile from './assets/notification.mp3'; // Agrega un sonido en esta ruta
import './App.css';


function App() {
  const [port, setPort] = useState(null);
  const [activeTab, setActiveTab] = useState('general');
  const [openTabs, setOpenTabs] = useState(['general']);
  const [messages, setMessages] = useState([]);
  const [privateMessages, setPrivateMessages] = useState({});
  const [unreadMessages, setUnreadMessages] = useState({});
  const [users, setUsers] = useState([]);
  const [ws, setWs] = useState(null);
  const [username, setUsername] = useState(null);
  const [serverList, setServerList] = useState([]);
  const [discoveryList, setDiscoveryList] = useState([]);

  const notificationSound = new Audio(notificationSoundFile);

  useEffect(() => {
    async function fetchPort() {
      const fetchedPort = await window.api.getPort();
      setPort(fetchedPort);
    }
    fetchPort();
  }, []);

  function getCurrentTime() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  function clickTab(tab) {
    setActiveTab(tab);
    setUnreadMessages((prevUnread) => ({
      ...prevUnread,
      [tab]: 0,
    }));
  }

  useEffect(() => {
    if (port === null) return;

    const socket = new WebSocket(`ws://localhost:${port}`);
    console.log('Conectando al servidor WebSocket...' + port);

    socket.onopen = () => {
      console.log('Conectado al servidor WebSocket');
    };

    socket.onmessage = (event) => {
      const message = JSON.parse(event.data);
      console.log('Mensaje recibido:', message);

      const currTime = getCurrentTime();

      if (message.type === 'chat') {
        setMessages((prevMessages) => [...prevMessages, {
          sender: message.sender,
          text: message.content,
          time: currTime
        }]);
      }

      if (message.type === 'private_chat') {
        const sender = message.sender;
        const receiver = message.receiver;
        const text = message.content;

        setPrivateMessages((prevMessages) => ({
          ...prevMessages,
          [receiver]: [...(prevMessages[receiver] || []), { sender, text, time: currTime }],
          [sender]: [...(prevMessages[sender] || []), { sender, text, time: currTime }],
        }));

        if (receiver === username) {
          setOpenTabs((prevTabs) => {
            if (!prevTabs.includes(sender)) {
              return [...prevTabs, sender];
            }
            return prevTabs;
          });

          if (activeTab !== sender) {
            setUnreadMessages((prevUnread) => ({
              ...prevUnread,
              [sender]: (prevUnread[sender] || 0) + 1,
            }));

            notificationSound.play();
          }
        }
      }

      if (message.type === 'user_list') {
        setUsers(message.content.split(','));
      }

      if (message.type === 'session') {
        console.log('Sesión iniciada con el nombre de usuario:', message.receiver);
        setUsername(message.receiver);
        setIsEdit(false);
        setFormServer(null);
      }

      if (message.type === 'error' && message.content === 'already_existing_username') {
        alert('El nombre de usuario ya está en uso. Por favor, elige otro.');
      }

      if (message.type === 'server_list') {
        // Formato: "username:address:port|username:address:port|..."
        const split = message.content.split('|');
        const servers = [];

        if (message.content === '') {
          setServerList([]);
          return;
        }

        for (let i = 0; i < split.length; i++) {
          const server = split[i].split(':');
          servers.push({
            index: i,
            username: server[0],
            ip: server[1],
            port: server[2]
          });
        }
        setServerList(servers);
      }

      if (message.type === 'discovery_list') {
        console.log('Lista de servidores descubiertos:', message.content);

        // Formato: "address:port|address:port|..."
        const split = message.content.split('|');

        if (message.content === '') {
          setDiscoveryList([]);
          return;
        }

        setDiscoveryList(split);
      }
    };

    socket.onclose = (event) => {
      console.log('Desconectado del servidor WebSocket:', event.code, event.reason);
    };

    socket.onerror = (error) => {
      console.error('Error en la conexión WebSocket:', error);
    };

    setWs(socket);

    return () => {
      socket.close();
    };
  }, [port, username, activeTab]);

  const openPrivateChat = (name) => {
    if (name === username) return;

    if (!openTabs.includes(name)) {
      setOpenTabs((prevTabs) => [...prevTabs, name]);
    }
    setActiveTab(name);

    setUnreadMessages((prevUnread) => ({
      ...prevUnread,
      [name]: 0,
    }));
  };

  const closeChat = (tab) => {
    if (tab === 'general') return;
    setOpenTabs((prevTabs) => prevTabs.filter((t) => t !== tab));
    if (activeTab === tab) {
      setActiveTab('general');
    }
  };

  function onConnectServer(server) {
    console.log('Conectar a:', server);

    const message = {
      type: 'connect_server',
      receiver: server.index,
      content: ''
    };

    if (ws) {
      ws.send(JSON.stringify(message));
    }
  }

  function onEditServer(server) {
    console.log('Editar:', server);
    setFormServer(server);
    setIsEdit(true);
  }

  function onDeleteServer(server) {
    console.log('Borrar:', server);

    const message = {
      type: 'delete_server',
      receiver: server.index,
      content: ''
    };

    if (ws) {
      ws.send(JSON.stringify(message));
    }
  }

  const [ formServer, setFormServer ] = useState(null);
  const [ isEdit, setIsEdit ] = useState(false);

  function onAddServer() {
    console.log('Añadir Servidor');
    setFormServer({
      username: '',
      ip: '',
      port: ''
    });
    setIsEdit(false);
  }

  function onDisconnect() {
    setUsername(null);
    setMessages([]);
    setPrivateMessages({});
    setUnreadMessages({});
    setUsers([]);
    setOpenTabs(['general']);
    setActiveTab('general');

    const message = {
      type: 'disconnect'
    };

    if (ws) {
      ws.send(JSON.stringify(message));
    }
  }

  function onDirectConnect(server, username) {
    console.log('Conectar directamente a:', server, username);

    const message = {
      type: 'direct_connect',
      receiver: null,
      content: `${username}:${server.ip}:${server.port}`
    };

    if (ws) {
      ws.send(JSON.stringify(message));
    }
  }

  if (!username) {
    if (!formServer) {
      return (
        <ServerManager servers={serverList} onConnectServer={onConnectServer} onEditServer={onEditServer} onDeleteServer={onDeleteServer} onAddServer={onAddServer} discoveryList={discoveryList} onDirectConnect={onDirectConnect} />
      );
    } else {
      function onSubmit(server) {
        // Formato: username:address:port
        console.log('Submit:', server);

        const serverStr = `${server.username}:${server.ip}:${server.port}`;
        const message = {
          type: isEdit ? 'edit_server' : 'add_server',
          receiver: isEdit ? formServer.index : null,
          content: serverStr
        };

        if (ws) {
          ws.send(JSON.stringify(message));
        }

        setFormServer(null);
        setIsEdit(false);
      }

      function onCancel() {
        setFormServer(null);
        setIsEdit(false);
      }

      return <ServerForm server={formServer} onSubmit={onSubmit} onCancel={onCancel} />;
    }
  }

  return (
    <div className="app">
      <Header
        activeTab={activeTab}
        openTabs={openTabs}
        unreadMessages={unreadMessages}
        onTabClick={clickTab}
        onCloseTab={closeChat}
        onDisconnect={onDisconnect}
      />
      <div className="main-content">
        <Sidebar users={users} onUserClick={openPrivateChat} currentUser={username} />
        <ChatWindow
          activeTab={activeTab}
          messages={activeTab === 'general' ? messages : privateMessages[activeTab] || []}
          ws={ws}
          username={username}
        />
      </div>
    </div>
  );
}

export default App;
