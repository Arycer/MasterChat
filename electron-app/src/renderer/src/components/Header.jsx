function Header({ activeTab, openTabs, onTabClick, onCloseTab }) {
  return (
    <div className="header">
      {openTabs.map((tab) => (
        <div
          key={tab}
          className={`tab ${activeTab === tab ? 'active' : ''}`}
          onClick={() => onTabClick(tab)}
        >
          {tab}
          {tab !== 'general' && (
            <button className="close-tab" onClick={(e) => { e.stopPropagation(); onCloseTab(tab); }}>
              &times;
            </button>
          )}
        </div>
      ))}
    </div>
  );
}

export default Header;
