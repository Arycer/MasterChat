function Sidebar({users, onUserClick, currentUser}) {
  return (
    <div className="sidebar">
      <h3>Usuarios Conectados</h3>
      <ul>
        {users
          .map((user) => (
            <li key={user} onClick={() => onUserClick(user)}>
              {user}
            </li>
          ))}
      </ul>
      <div className="current-user">
        <strong>Tu nombre:</strong> {currentUser}
      </div>
    </div>
  );
}

export default Sidebar;
