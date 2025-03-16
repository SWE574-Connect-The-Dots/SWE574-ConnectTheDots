import { useNavigate } from 'react-router-dom';
import React, { useState } from 'react';
import '../ConnectTheDots.css';
import AppLogo from '../assets/AppLogo.svg';

export default function Home({setIsAuthenticated}) {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
    navigate('/login');
  };

  const [searchValue, setSearchValue] = useState('');
  
  const clearSearch = () => {
    setSearchValue('');
  };
  
  const spacesData = [
    {
      id: 1,
      title: 'Lorem ipsum dolor sit amet',
      contributors: 13,
      votes: 12,
      comments: 25,
      activities: [
        'Recent Activity 1 Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.',
        'Recent Activity 2 Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.',
        'Recent Activity 3 Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.',
        'Recent Activity 4 Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.'
      ],
      tags: ['Tag 1', 'Tag 2', 'Tag 3']
    },
    {
      id: 2,
      title: 'Lorem ipsum dolor sit amet',
      contributors: 13,
      votes: 12,
      comments: 25,
      activities: [
        'Recent Activity 1 Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.',
        'Recent Activity 2 Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.',
        'Recent Activity 3 Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.',
        'Recent Activity 4 Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.'
      ],
      tags: ['Tag 1', 'Tag 2', 'Tag 3']
    }
  ];
  
  return (
    <div className="connect-dots-container">
      {/* Header */}
      <header className="header">
        <div className="logo-container">
          <img src={AppLogo} alt="Connect-The-Dots Logo" className="logo" />
          <div className="app-name">Connect-The-Dots</div>
        </div>
        
        <nav className="navigation">
          <div className="nav-item">Discover</div>
          <div className="nav-item">My Spaces</div>
          <div className="nav-item">Profile</div>
        </nav>
        
        <div className="search-container">
          <input 
            type="text" 
            placeholder="Search..." 
            value={searchValue} 
            onChange={(e) => setSearchValue(e.target.value)}
          />
          {searchValue && (
            <button className="clear-button" onClick={clearSearch}>×</button>
          )}
        </div>
        
        <button className="create-space-button">Create Space</button>
      </header>
      
      {/* Tabs */}
      <div className="tabs-container">
        <div className="tab active">Trending</div>
        <div className="tab">New</div>
      </div>
      
      {/* Space Cards */}
      <div className="spaces-container">
        {spacesData.map((space) => (
          <div key={space.id} className="space-card">
            <div className="space-header">
              <h2 className="space-title">{space.title}</h2>
              <div className="contributors">Contributors: {space.contributors}</div>
            </div>
            
            <div className="space-content">
              <div className="comments">
                <div className="comments-count">
                  <div className="comment-bubble">{space.comments}</div>
                </div>
              </div>
              
              <div className="activities">
                {space.activities.map((activity, index) => (
                  <div key={index} className="activity-item">
                    {activity}
                  </div>
                ))}
              </div>
            </div>
            
            <div className="space-footer">
              <div className="tags">
                {space.tags.map((tag, index) => (
                  <div key={index} className="tag">{tag}</div>
                ))}
              </div>
              
              <button className="join-button">JOIN</button>
            </div>
          </div>
        ))}
      </div>

      <div>
      <button onClick={handleLogout}>Logout</button>
      </div>
    </div>
  );
}