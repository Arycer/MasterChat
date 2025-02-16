import {useState} from 'react';

function UsernamePrompt({onUsernameSubmit}) {
  const [username, setUsername] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (username.trim()) {
      onUsernameSubmit(username);
    }
  };

  return (
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
        <button type="submit">Continuar</button>
      </form>
    </div>
  );
}

export default UsernamePrompt;
