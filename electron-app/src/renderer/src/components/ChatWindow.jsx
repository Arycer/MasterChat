import { useState, useEffect, useRef } from 'react';
import './ChatWindow.css'; // Importar los estilos

function ChatWindow({ activeTab, messages, ws, username }) {
  const [inputText, setInputText] = useState('');
  const [selectedImage, setSelectedImage] = useState(null);
  const [isFadingOut, setIsFadingOut] = useState(false);
  const messagesEndRef = useRef(null); // Ref para el final del chat

  // Efecto para desplazar el scroll al final al actualizar los mensajes
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  const handleSendMessage = () => {
    if (inputText.trim() && ws) {
      const message = {
        type: activeTab === 'general' ? 'chat_message' : 'private_message',
        sender: username,
        receiver: activeTab === 'general' ? null : activeTab,
        content: inputText
      };
      ws.send(JSON.stringify(message));
      setInputText('');
    }
  };

  const parseMessageContent = (content) => {
    if (!content || typeof content !== 'string') return <span>(Mensaje vacío)</span>;
    const urlRegex = /(https?:\/\/\S+)/gi;
    return content.split(urlRegex).map((part, index) => {
      if (part.match(urlRegex) && part.includes('http')) {
        return (
          <img
            key={index}
            src={part}
            alt="Imagen enviada"
            className="chat-image"
            onClick={() => setSelectedImage(part)}
            onError={(e) => e.target.style.display = 'none'}
          />
        );
      }
      return <span key={index}>{part}</span>;
    });
  };

  const handleCloseImage = () => {
    setIsFadingOut(true);
    setTimeout(() => {
      setSelectedImage(null);
      setIsFadingOut(false);
    }, 250);
  };

  return (
    <div className="chat-window">
      <div className="messages">
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`message ${msg.sender === username ? 'own-message' : 'other-message'}`}
          >
            {
              msg.sender === username ?
                <strong>Tú:</strong> :
                <strong>{msg.sender}:</strong>
            }

            <div>{parseMessageContent(msg.text)}</div>
            <span className="message-time">{msg.time}</span>
          </div>
        ))}
        {/* Este div asegura que el scroll se mantenga al final */}
        <div ref={messagesEndRef} />
      </div>
      <div className="input-area">
        <input
          type="text"
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
          placeholder="Escribe un mensaje..."
        />
        <button onClick={handleSendMessage}>Enviar</button>
      </div>
      {selectedImage && (
        <div className="image-modal" onClick={handleCloseImage}>
          <img
            src={selectedImage}
            alt="Imagen ampliada"
            className={`large-image ${isFadingOut ? 'fade-out' : ''}`}
          />
        </div>
      )}
    </div>
  );
}

export default ChatWindow;
