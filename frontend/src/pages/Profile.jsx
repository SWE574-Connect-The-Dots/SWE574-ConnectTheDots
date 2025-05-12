import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../axiosConfig";
import { API_ENDPOINTS } from "../constants/config";
import { formatDate } from "../utils/dateUtils";
import "../ConnectTheDots.css";

const Profile = () => {
  const [user, setUser] = useState(null);
  const [joinedSpaces, setJoinedSpaces] = useState([]);
  const [ownedSpaces, setOwnedSpaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { username } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProfileData = async () => {
      try {
        const response = await api.get(API_ENDPOINTS.PROFILE(username));
        setUser(response.data);
        setJoinedSpaces(response.data.joined_spaces);
        setOwnedSpaces(response.data.owned_spaces || []);
        setLoading(false);
      } catch (err) {
        setError("Failed to load profile data");
        setLoading(false);
      }
    };

    fetchProfileData();
  }, [username]);

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="profile-container">
      <div className="profile-header">
        <h1>{user?.user?.username}'s Profile</h1>
        {user?.profession && (
          <p className="profession">Profession: {user.profession}</p>
        )}
        {user?.bio && <p className="bio">Bio: {user.bio}</p>}
        {user?.dob && (
          <p className="dob">Date of Birth: {formatDate(user.dob)}</p>
        )}
        {user?.created_at && (
          <p className="created-at">Joined: {formatDate(user.created_at)}</p>
        )}
      </div>

      <div className="profile-content">
        <div className="owned-spaces">
          <h2>Owned Spaces</h2>
          {ownedSpaces.length > 0 ? (
            <div className="spaces-grid">
              {ownedSpaces.map((space) => (
                <div
                  key={space.id}
                  className="space-card"
                  onClick={() => navigate(`/spaces/${space.id}`)}
                >
                  <h3>{space.title}</h3>
                  <p>{space.description}</p>
                </div>
              ))}
            </div>
          ) : (
            <p>No owned spaces</p>
          )}
        </div>

        <div className="joined-spaces">
          <h2>Joined Spaces</h2>
          {joinedSpaces.length > 0 ? (
            <div className="spaces-grid">
              {joinedSpaces.map((space) => (
                <div
                  key={space.id}
                  className="space-card"
                  onClick={() => navigate(`/spaces/${space.id}`)}
                >
                  <h3>{space.title}</h3>
                  <p>{space.description}</p>
                </div>
              ))}
            </div>
          ) : (
            <p>No spaces joined yet</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;
