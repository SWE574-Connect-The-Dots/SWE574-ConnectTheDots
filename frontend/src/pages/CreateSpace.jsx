import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const CreateSpace = () => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    tags: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    try {
      const tagsArray = formData.tags.split(',')
        .map(tag => tag.trim())
        .filter(tag => tag !== '');
      
      const response = await axios.post('http://localhost:8000/api/spaces/', {
        title: formData.title,
        description: formData.description,
        tags: tagsArray
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
          <label htmlFor="tags">Tags (comma separated):</label>
          <input
            type="text"
            id="tags"
            name="tags"
            value={formData.tags}
            onChange={handleChange}
            placeholder="e.g., science, research, collaboration"
          />
        </div>
        
        <button type="submit" disabled={loading}>
          {loading ? 'Creating...' : 'Create Space'}
        </button>
      </form>
    </div>
  );
};

export default CreateSpace;