import { useLocation, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "axios";

const SpaceDetails = () => {
  const location = useLocation();
  const { id } = useParams();
  const [space, setSpace] = useState({
    title: location.state?.title || "",
    description: location.state?.description || "",
    tags: location.state?.tags || [],
  });

  useEffect(() => {
    if (!location.state) {
      axios
        .get(`http://localhost:8000/api/spaces/${id}/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        })
        .then((res) => {
          setSpace({
            title: res.data.title,
            description: res.data.description,
            tags: res.data.tags || [],
          });
        })
        .catch((err) => {
          console.error(err);
        });
    }
  }, [id, location.state]);

  return (
    <div>
      <h2>{space.title}</h2>
      <p>{space.description}</p>
      <ul className="tags-list">
        {space.tags.map((tag) => (
          <li key={tag} className="tag-item">
            <strong>{tag.name}</strong>
            {tag.wikidata_label && (
              <p className="tag-label">{tag.wikidata_label}</p>
            )}
            {tag.wikidata_id && (
              <span className="tag-id">ID: {tag.wikidata_id}</span>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default SpaceDetails;
