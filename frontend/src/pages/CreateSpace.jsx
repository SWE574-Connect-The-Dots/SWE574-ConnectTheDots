import React, { useState } from 'react';
import api from '../axiosConfig';
import { useNavigate } from 'react-router-dom';

const CreateSpace = () => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [searchLoading, setSearchLoading] = useState(false);
  const navigate = useNavigate();

  const [tagQuery, setTagQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [selectedTags, setSelectedTags] = useState([]);
  const [showTagSearch, setShowTagSearch] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const searchWikidata = async () => {
    if (!tagQuery.trim()) return;
    
    setSearchLoading(true);
    try {
      const response = await api.get(`/tags/search_wikidata/?query=${encodeURIComponent(tagQuery)}`, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      setSearchResults(response.data);
    } catch (err) {
      setError('Failed to search Wikidata');
      console.error('Error searching Wikidata:', err);
    } finally {
      setSearchLoading(false);
    }
  };

  const handleTagSelect = (entity) => {
    if (!selectedTags.find(tag => tag.wikidata_id === entity.id)) {
      setSelectedTags([...selectedTags, {
        name: entity.label,
        wikidata_id: entity.id,
        wikidata_label: entity.label
      }]);
    }
    setTagQuery('');
    setSearchResults([]);
  };

  const handleTagRemove = (tagIndex) => {
    setSelectedTags(selectedTags.filter((_, index) => index !== tagIndex));
  };

  const handleTagNameChange = (index, newName) => {
    const updatedTags = [...selectedTags];
    updatedTags[index].name = newName;
    setSelectedTags(updatedTags);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const tagsPromises = selectedTags.map(tag => 
        api.post('/tags/', {
          name: tag.name,
          wikidata_id: tag.wikidata_id,
          wikidata_label: tag.wikidata_label
        }, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }).catch(err => {
          if (err.response?.status === 409) {
            return { data: { name: tag.name } };
          }
          throw err;
        })
      );

      const tagResponses = await Promise.all(tagsPromises);
      const tagNames = tagResponses.map(res => res.data.name);

      const response = await api.post('/spaces/', {
        title: formData.title,
        description: formData.description,
        tags: tagNames
      }, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      navigate(`/spaces/${response.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create space');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-space-container">
      <h2>Create a New Collaboration Space</h2>
      {error && <div className="error-message">{error}</div>}
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="title">Title:</label>
          <input
            type="text"
            id="title"
            name="title"
            value={formData.title}
            onChange={handleChange}
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="description">Description:</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            required
            rows="5"
          />
        </div>
        
        <div className="form-group">
          <label>Tags:</label>
          
          {/* Display selected tags */}
          <div className="selected-tags">
            {selectedTags.map((tag, index) => (
              <div key={index} className="selected-tag">
                <input
                  type="text"
                  value={tag.name}
                  onChange={(e) => handleTagNameChange(index, e.target.value)}
                  className="tag-name-input"
                />
                <span className="tag-wikidata-id">({tag.wikidata_id})</span>
                <button 
                  type="button" 
                  onClick={() => handleTagRemove(index)}
                  className="remove-tag-btn"
                >
                  ×
                </button>
              </div>
            ))}
          </div>
          
          {/* Tag search interface */}
          <button 
            type="button" 
            onClick={() => setShowTagSearch(!showTagSearch)}
            className="toggle-tag-search-btn"
          >
            {showTagSearch ? 'Hide Tag Search' : 'Add Tags from Wikidata'}
          </button>
          
          {showTagSearch && (
            <div className="tag-search-container">
              <div className="search-input-container">
                <input
                  type="text"
                  value={tagQuery}
                  onChange={(e) => setTagQuery(e.target.value)}
                  placeholder="Search for tags in Wikidata..."
                  className="tag-search-input"
                />
                <button 
                  type="button" 
                  onClick={searchWikidata}
                  disabled={searchLoading}
                  className="search-wikidata-btn"
                >
                  {searchLoading ? 'Searching...' : 'Search'}
                </button>
              </div>
              
              {searchResults.length > 0 && (
                <div className="search-results">
                  <h4>Select from results:</h4>
                  <ul className="wikidata-results-list">
                    {searchResults.map(entity => (
                      <li 
                        key={entity.id} 
                        onClick={() => handleTagSelect(entity)}
                        className="wikidata-result-item"
                      >
                        <strong>{entity.label}</strong>
                        {entity.description && <p className="entity-description">{entity.description}</p>}
                        <span className="entity-id">ID: {entity.id}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}
        </div>
        
        <button type="submit" disabled={loading} className="create-space-btn">
          {loading ? 'Creating...' : 'Create Space'}
        </button>
      </form>
    </div>
  );
};

export default CreateSpace;