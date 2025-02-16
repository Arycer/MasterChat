function Header({activeTab, openTabs, unreadMessages, onTabClick, onCloseTab}) {
  return (
    <div className="header">
      {openTabs.map((tab) => (
        <div
          key={tab}
          className={`tab ${activeTab === tab ? 'active' : ''}`}
          onClick={() => onTabClick(tab)}
        >
          {tab} {unreadMessages[tab] > 0 && <span className="unread-count">({unreadMessages[tab]})</span>}
          {tab !== 'general' && (
            <button className="close-tab" onClick={(e) => {
              e.stopPropagation();
              onCloseTab(tab);
            }}>
              &times;
            </button>
          )}
        </div>
      ))}
    </div>
  );
}

export default Header;
