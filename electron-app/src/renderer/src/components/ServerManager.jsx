import { useState } from 'react';
import UsernamePrompt from './UsernamePrompt';
import './ServerManager.css';

function ServerManager({ onConnectServer, onEditServer, onDeleteServer, onAddServer, servers, discoveryList, onDirectConnect }) {
  const [showPrompt, setShowPrompt] = useState(false);
  const [selectedServer, setSelectedServer] = useState(null);

  const handleConnectClick = (server) => {
    setSelectedServer(server);
    setShowPrompt(true);
  };

  const handleUsernameSubmit = (username) => {
    if (selectedServer) {
      console.log('Conectar:', selectedServer, username);
      setShowPrompt(false);
      setSelectedServer(null);
      onDirectConnect(selectedServer, username);
    }
  };

  return (
    <div className="server-manager">
      <h2>Servidores de Chat</h2>

      <ul>
        <li className="servers-desc">
          <span>Nombre de Usuario - IP - Puerto</span>
        </li>
        {servers.map((server, index) => (
          <li key={`saved-${index}`} className="server-item">
            <span>{server.username} - {server.ip} - {server.port}</span>
            <button onClick={() => onEditServer(server)}>Editar</button>
            <button onClick={() => onDeleteServer(server)}>Borrar</button>
            <button onClick={() => onConnectServer(server)}>Conectar</button>
          </li>
        ))}
      </ul>

      <button className="add-server-button" onClick={onAddServer}>Añadir Servidor</button>

      {discoveryList.length > 0 && (
        <>
          <h2>Servidores Descubiertos</h2>
          <ul>
            <li className="servers-desc">
              <span>IP - Puerto</span>
            </li>
            {discoveryList.map((server, index) => {
              const [ip, port] = server.split(':');
              return (
                <li key={`discovered-${index}`} className="server-item">
                  <span>{ip} - {port}</span>
                  <button onClick={() => handleConnectClick({ ip, port })}>Conectar</button>
                </li>
              );
            })}
          </ul>
        </>
      )}

      {showPrompt && <UsernamePrompt onUsernameSubmit={handleUsernameSubmit} onClose={() => setShowPrompt(false)} />}
    </div>
  );
}

export default ServerManager;
