import { useLocation, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import axios from 'axios';

const SpaceDetails = () => {
  const location = useLocation();
  const { id } = useParams();
  const [space, setSpace] = useState({
    title: location.state?.title || '',
    description: location.state?.description || ''
  });

  useEffect(() => {
    if (!location.state) {
      axios.get(`http://localhost:8000/api/spaces/${id}/`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }).then((res) => {
        setSpace({
          title: res.data.title,
          description: res.data.description
        });
      }).catch((err) => {
        console.error(err);
      });
    }
  }, [id, location.state]);

  return (
    <div>
      <h2>{space.title}</h2>
      <p>{space.description}</p>
    </div>
  );
};

export default SpaceDetails;