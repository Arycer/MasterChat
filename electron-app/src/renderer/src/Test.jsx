import ServerManager from "./components/ServerManager";
import ServerForm from "./components/ServerForm";

function Test() {
  const servers = [
    {
      username: 'Arycer',
      ip: 'localhost',
      port: 3000,
    },
    {
      username: 'Pakito',
      ip: 'localhost',
      port: 3001,
    },
  ];

  function onConnectServer(server) {
    console.log('Conectar a:', server);
  }

  function onEditServer(server) {
    console.log('Editar:', server);
  }

  function onDeleteServer(server) {
    console.log('Borrar:', server);
  }

  function onAddServer() {
    console.log('Añadir Servidor');
  }

  //return <ServerManager servers={servers} onConnectServer={onConnectServer} onEditServer={onEditServer} onDeleteServer={onDeleteServer} onAddServer={onAddServer} />;

  function onSubmit(server) {
    console.log('Submit:', server);
  }

  return <ServerForm server={servers[0]} onSubmit={onSubmit} />;
}

export default Test;
