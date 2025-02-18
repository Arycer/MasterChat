import { useState } from 'react';
import './UsernamePrompt.css';

function UsernamePrompt({ onUsernameSubmit, onClose }) {
  const [username, setUsername] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (username.trim()) {
      onUsernameSubmit(username);
    }
  };

  return (
    <div className="overlay">
      <div className="username-prompt">
        <h2>Ingresa tu nombre de usuario</h2>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Nombre de usuario"
            required
          />
          <div className="buttons">
            <button type="submit">Continuar</button>
            <button type="button" onClick={onClose} className="cancel-button">Cancelar</button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default UsernamePrompt;
