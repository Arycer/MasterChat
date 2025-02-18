import { useState } from 'react';
import './ServerForm.css';

function ServerForm({ server, onSubmit, onCancel }) {
  const [username, setUsername] = useState(server ? server.username : '');
  const [ip, setIp] = useState(server ? server.ip : '');
  const [port, setPort] = useState(server ? server.port : '');

  const handleSubmit = (e) => {
    e.preventDefault();
    const newServer = { username, ip, port };
    onSubmit(newServer);
  };

  return (
    <form onSubmit={handleSubmit} className="server-form">
      <h2>{server ? 'Editar Servidor' : 'Añadir Servidor'}</h2>
      <label>
        Nombre de Usuario:
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
      </label>
      <label>
        IP:
        <input
          type="text"
          value={ip}
          onChange={(e) => setIp(e.target.value)}
          required
        />
      </label>
      <label>
        Puerto:
        <input
          type="text"
          value={port}
          onChange={(e) => setPort(e.target.value)}
          required
        />
      </label>
      <div className="button-container">
        <button type="button" className="cancel-button" onClick={onCancel}>Cancelar</button>
        <button type="submit" className={server ? 'edit-mode' : ''}>
          {server ? 'Guardar Cambios' : 'Añadir Servidor'}
        </button>
      </div>
    </form>
  );
}

export default ServerForm;
