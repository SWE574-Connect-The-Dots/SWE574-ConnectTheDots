import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import api from "../axiosConfig";

const SpaceAnalytics = () => {
  const { t } = useTranslation();
  const { id } = useParams();
  const navigate = useNavigate();
  const [space, setSpace] = useState(null);
  const [analytics, setAnalytics] = useState({
    totalNodes: 0,
    totalEdges: 0,
    totalDiscussions: 0
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchSpaceData = async () => {
      try {
        setLoading(true);
        
        // Fetch space details
        const spaceResponse = await api.get(`/spaces/${id}/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });
        setSpace(spaceResponse.data);

        // Fetch nodes data
        const nodesResponse = await api.get(`/spaces/${id}/nodes/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        // Fetch edges data
        const edgesResponse = await api.get(`/spaces/${id}/edges/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        // Fetch discussions data
        const discussionsResponse = await api.get(`/spaces/${id}/discussions/`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        });

        // Update analytics state
        setAnalytics({
          totalNodes: Array.isArray(nodesResponse.data) ? nodesResponse.data.length : 0,
          totalEdges: Array.isArray(edgesResponse.data) ? edgesResponse.data.length : 0,
          totalDiscussions: Array.isArray(discussionsResponse.data) ? discussionsResponse.data.length : 0
        });

      } catch (err) {
        console.error("Error fetching space data:", err);
        setError("Failed to load space data");
      } finally {
        setLoading(false);
      }
    };

    fetchSpaceData();
  }, [id]);

  const canViewAnalytics = () => {
    const username = localStorage.getItem("username");
    const isStaff =
      localStorage.getItem("is_staff") === "true" ||
      (window.currentUser && window.currentUser.is_staff);
    const isSuperuser =
      localStorage.getItem("is_superuser") === "true" ||
      (window.currentUser && window.currentUser.is_superuser);
    
    return (
      (space?.creator_username && space.creator_username === username) ||
      isStaff ||
      isSuperuser
    );
  };

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px',
        color: '#4A5568'
      }}>
        Loading analytics...
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px',
        color: '#BD4902'
      }}>
        {error}
      </div>
    );
  }

  if (!canViewAnalytics()) {
    return (
      <div style={{ 
        display: 'flex', 
        flexDirection: 'column',
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px',
        color: '#BD4902',
        gap: '20px'
      }}>
        <div>You don't have permission to view analytics for this space.</div>
        <button
          onClick={() => navigate(`/spaces/${id}`)}
          style={{
            background: '#0076B5',
            color: '#FFFFFF',
            border: 'none',
            borderRadius: '4px',
            padding: '10px 20px',
            fontSize: '16px',
            cursor: 'pointer'
          }}
        >
          Back to Space
        </button>
      </div>
    );
  }

  return (
    <div style={{
      minHeight: '100vh',
      background: '#F5F5F5',
      padding: '20px'
    }}>
      {/* Header */}
      <div style={{
        background: '#FFFFFF',
        padding: '20px 30px',
        marginBottom: '20px',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <div>
          <h1 style={{ 
            margin: 0, 
            color: '#1B1F3B',
            fontSize: '28px',
            fontWeight: '600'
          }}>
            {space?.title}
          </h1>
          <p style={{ 
            margin: '5px 0 0 0', 
            color: '#4A5568',
            fontSize: '16px'
          }}>
            Space Analytics Dashboard
          </p>
        </div>
        
        <div style={{ display: 'flex', gap: '10px' }}>
          <button
            onClick={() => navigate(`/spaces/${id}`)}
            style={{
              background: '#4A5568',
              color: '#FFFFFF',
              border: 'none',
              borderRadius: '4px',
              padding: '10px 20px',
              fontSize: '14px',
              cursor: 'pointer',
              fontWeight: '500'
            }}
          >
            ← Back to Space
          </button>
        </div>
      </div>

      {/* Space Details Section */}
      <div style={{
        background: 'linear-gradient(135deg, #FFFFFF 0%, #F5F5F5 100%)',
        padding: '25px',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
        marginBottom: '20px',
        border: '1px solid #E5E5E5'
      }}>
        <h2 style={{ 
          color: '#1B1F3B', 
          marginBottom: '20px',
          fontSize: '20px',
          fontWeight: '600',
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}>
          Space Overview
        </h2>
        
        <div style={{ color: '#4A5568', lineHeight: '1.6' }}>
          {/* Description */}
          <div style={{ 
            marginBottom: '20px',
            padding: '15px',
            background: '#FFFFFF',
            borderRadius: '8px',
            border: '1px solid #E5E5E5'
          }}>
            <div style={{ fontWeight: '600', marginBottom: '8px', color: '#1B1F3B' }}>
              Description:
            </div>
            <div style={{ fontStyle: 'italic' }}>
              {space?.description || 'No description provided'}
            </div>
          </div>

          {/* Location */}
          <div style={{ 
            marginBottom: '20px',
            padding: '15px',
            background: '#FFFFFF',
            borderRadius: '8px',
            border: '1px solid #E5E5E5'
          }}>
            <div style={{ 
              fontWeight: '600', 
              marginBottom: '8px', 
              color: '#1B1F3B',
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}>
              📍 Location:
            </div>
            <div>
              {space?.country || space?.city || space?.district || space?.street ? (
                <div style={{ 
                  lineHeight: '1.5',
                  fontSize: '14px'
                }}>
                  {[space?.street, space?.district, space?.city, space?.country]
                    .filter(Boolean)
                    .join(', ')}
                </div>
              ) : (
                <div style={{ 
                  fontStyle: 'italic', 
                  color: '#656F75',
                  fontSize: '14px'
                }}>
                  Location not specified
                </div>
              )}
            </div>
          </div>

          {/* Space Members */}
          <div style={{ 
            marginBottom: '20px',
            padding: '15px',
            background: '#FFFFFF',
            borderRadius: '8px',
            border: '1px solid #E5E5E5'
          }}>
            <div style={{ 
              fontWeight: '600', 
              marginBottom: '12px', 
              color: '#1B1F3B',
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}>
              Space Management:
            </div>
            
            {/* Creator */}
            <div style={{ marginBottom: '10px' }}>
              <div style={{ 
                fontWeight: '500', 
                marginBottom: '6px', 
                color: '#4A5568',
                fontSize: '14px'
              }}>
                Creator:
              </div>
              <div style={{
                display: 'inline-block',
                padding: '6px 12px',
                background: '#215D69',
                color: '#FFFFFF',
                borderRadius: '20px',
                fontSize: '13px',
                fontWeight: '500'
              }}>
                {space?.creator_username}
              </div>
            </div>

            {/* Moderators */}
            <div>
              <div style={{ 
                fontWeight: '500', 
                marginBottom: '6px', 
                color: '#4A5568',
                fontSize: '14px'
              }}>
                Moderators:
              </div>
              <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                {space?.moderators && space.moderators.length > 0 ? (
                  space.moderators.map((moderator, index) => (
                    <div
                      key={index}
                      style={{
                        padding: '6px 12px',
                        background: '#2D6A4F',
                        color: '#FFFFFF',
                        borderRadius: '20px',
                        fontSize: '13px',
                        fontWeight: '500'
                      }}
                    >
                      {moderator.username || moderator}
                    </div>
                  ))
                ) : (
                  <div style={{ 
                    fontStyle: 'italic', 
                    color: '#656F75',
                    fontSize: '13px'
                  }}>
                    No moderators assigned
                  </div>
                )}
              </div>
            </div>
          </div>
          
          {/* Tags */}
          <div style={{ display: 'flex', gap: '15px', flexWrap: 'wrap' }}>
            <div style={{
              padding: '10px 16px',
              background: '#2D6A4F',
              color: '#FFFFFF',
              borderRadius: '25px',
              fontSize: '14px',
              fontWeight: '500'
            }}>
              {space?.tags?.length || 0} Tags
            </div>
          </div>
        </div>
      </div>

      {/* Quick Stats Section */}
      <div style={{
        background: '#FFFFFF',
        padding: '25px',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
        marginBottom: '20px'
      }}>
        <h2 style={{ 
          color: '#1B1F3B', 
          marginBottom: '20px',
          fontSize: '20px',
          fontWeight: '600'
        }}>
          Quick Stats
        </h2>
        
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '15px'
        }}>
          {/* Graph Stats */}
          <div style={{
            background: 'linear-gradient(135deg, #215D69 0%, #008296 100%)',
            padding: '20px',
            borderRadius: '8px',
            color: '#FFFFFF',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '5px' }}>
              {analytics.totalNodes}
            </div>
            <div style={{ fontSize: '14px', opacity: 0.9 }}>
              Total Nodes
            </div>
          </div>

          <div style={{
            background: 'linear-gradient(135deg, #2D6A4F 0%, #40916C 100%)',
            padding: '20px',
            borderRadius: '8px',
            color: '#FFFFFF',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '5px' }}>
              {analytics.totalEdges}
            </div>
            <div style={{ fontSize: '14px', opacity: 0.9 }}>
              Total Edges
            </div>
          </div>

          <div style={{
            background: 'linear-gradient(135deg, #0076B5 0%, #4A90E2 100%)',
            padding: '20px',
            borderRadius: '8px',
            color: '#FFFFFF',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '5px' }}>
              {analytics.totalDiscussions}
            </div>
            <div style={{ fontSize: '14px', opacity: 0.9 }}>
              Discussions
            </div>
          </div>

          <div style={{
            background: 'linear-gradient(135deg, #8F6701 0%, #B8860B 100%)',
            padding: '20px',
            borderRadius: '8px',
            color: '#FFFFFF',
            textAlign: 'center'
          }}>
            <div style={{ fontSize: '28px', fontWeight: 'bold', marginBottom: '5px' }}>
              {space?.collaborators?.length || 0}
            </div>
            <div style={{ fontSize: '14px', opacity: 0.9 }}>
              Collaborators
            </div>
          </div>
        </div>
      </div>

      {/* Main Analytics Content */}
      <div style={{
        background: '#FFFFFF',
        padding: '30px',
        borderRadius: '8px',
        boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)'
      }}>
        
        {/* Chart Section */}
        <div style={{ marginBottom: '40px' }}>
          <h2 style={{ 
            color: '#1B1F3B', 
            marginBottom: '20px',
            fontSize: '24px',
            fontWeight: '600'
          }}>
            Space Activity Over Time
          </h2>
          
          <div style={{
            width: '100%',
            height: '500px',
            background: '#F5F5F5',
            border: '2px dashed #68686B',
            borderRadius: '8px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexDirection: 'column',
            color: '#4A5568'
          }}>
            <div style={{ fontSize: '64px', marginBottom: '20px' }}>📊</div>
            <div style={{ 
              fontSize: '24px', 
              fontWeight: '600', 
              marginBottom: '12px',
              color: '#1B1F3B'
            }}>
              Grafana Dashboard Integration
            </div>
            <div style={{ 
              fontSize: '16px', 
              textAlign: 'center', 
              maxWidth: '500px',
              lineHeight: '1.5'
            }}>
              Interactive chart showing the progression of:
            </div>
            <ul style={{ 
              fontSize: '16px', 
              marginTop: '15px',
              textAlign: 'left',
              color: '#215D69'
            }}>
              <li>Number of Nodes</li>
              <li>Contributors</li>
              <li>Comments</li>
            </ul>
            <div style={{
              marginTop: '20px',
              padding: '15px 25px',
              background: '#215D69',
              color: '#FFFFFF',
              borderRadius: '6px',
              fontSize: '14px',
              fontFamily: 'monospace'
            }}>
              Grafana URL: /grafana/dashboard/space/{id}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SpaceAnalytics;