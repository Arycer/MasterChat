import { useState } from 'react';

function ChatWindow({ activeTab, messages, ws, username }) {
  const [inputText, setInputText] = useState('');

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

  return (
    <div className="chat-window">
      <div className="messages">
        {messages.map((msg, index) => (
          <div key={index} className="message">
            <strong>{msg.sender}:</strong> {msg.text}
            <span className="message-time">{msg.time}</span>
          </div>
        ))}
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
    </div>
  );
}

export default ChatWindow;
