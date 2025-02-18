import { useState } from 'react';
import './ServerManager.css';

function ServerManager({ onConnectServer, onEditServer, onDeleteServer, onAddServer, servers }) {
  return (
    <div className="server-manager">
      <h2>Servidores de Chat</h2>
      <ul>
        {servers.map((server, index) => (
          <li key={index} className="server-item">
            <span>{server.username} - {server.ip} - {server.port}</span>
            <button onClick={() => onEditServer(server)}>Editar</button>
            <button onClick={() => onDeleteServer(server)}>Borrar</button>
            <button onClick={() => onConnectServer(server)}>Conectar</button>
          </li>
        ))}
      </ul>
      <button className="add-server-button" onClick={onAddServer}>Añadir Servidor</button>
    </div>
  );
}

export default ServerManager;
