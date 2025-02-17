import {useEffect, useState} from 'react';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import ChatWindow from './components/ChatWindow';
import UsernamePrompt from './components/UsernamePrompt';
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
  const [isUsernameRequested, setIsUsernameRequested] = useState(false);

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

      if (message.type === 'username_request') {
        setIsUsernameRequested(true);
      }

      if (message.type === 'session') {
        setUsername(message.content);
        setIsUsernameRequested(false);
      }

      if (message.type === 'error' && message.content === 'already_existing_username') {
        alert('El nombre de usuario ya está en uso. Por favor, elige otro.');
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

  const sendUsername = (username) => {
    if (ws) {
      ws.send(JSON.stringify({ type: 'username_response', content: username }));
    }
  };

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

  if (!username) {
    return (
      <div className="app">
        {isUsernameRequested ? (
          <UsernamePrompt onUsernameSubmit={sendUsername} />
        ) : (
          <div className="loading">Conectando...</div>
        )}
      </div>
    );
  }

  return (
    <div className="app">
      <Header
        activeTab={activeTab}
        openTabs={openTabs}
        unreadMessages={unreadMessages}
        onTabClick={clickTab}
        onCloseTab={closeChat}
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
