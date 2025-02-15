import { useEffect, useState } from 'react';

function App() {
    const [messages, setMessages] = useState([]);
    const [users, setUsers] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const [ws, setWs] = useState(null);

    function sendUsername() {
        const username = prompt('Ingresa tu nombre de usuario:');
        const message = {
            type: 'username_response',
            content: username
        }
        ws.send(JSON.stringify(message));
    }

    useEffect(() => {
        // Conectar al servidor WebSocket local
        // obtener el puerto de la variable de entrono WS_PORT

        const socket = new WebSocket('ws://localhost:8888');

        socket.onopen = () => {
            console.log('Conectado al servidor WebSocket');
        };

        socket.onmessage = (event) => {
            const message = JSON.parse(event.data);
            console.log('Mensaje recibido:', message);

            if (message.type === 'username_request') {
                sendUsername();
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
    }, []);

    const handleSendMessage = () => {
        if (ws && newMessage.trim()) {
            // Enviar mensaje al servidor WebSocket
            ws.send(`chat_message:${newMessage}`);
            setNewMessage('');
        }
    };

    const handleMessageChange = (event) => {
        setNewMessage(event.target.value);
    };

    const handleKeyPress = (event) => {
        if (event.key === 'Enter') {
            handleSendMessage();
        }
    };

    return (
        <div className="chat-container">
            <h2>Usuarios Conectados</h2>
            <ul>
                {users.map((user, index) => (
                    <li key={index}>{user}</li>
                ))}
            </ul>

            <h2>Mensajes</h2>
            <div className="messages">
                {messages.map((message, index) => (
                    <div key={index} className="message">
                        {message}
                    </div>
                ))}
            </div>

            <div className="message-input">
                <input
                    type="text"
                    value={newMessage}
                    onChange={handleMessageChange}
                    onKeyPress={handleKeyPress}
                    placeholder="Escribe un mensaje..."
                />
                <button onClick={handleSendMessage}>Enviar</button>
            </div>
        </div>
    );
}

export default App;
