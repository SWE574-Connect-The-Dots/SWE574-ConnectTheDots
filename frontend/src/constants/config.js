export const API_ENDPOINTS = {
  SPACES: '/spaces',
  NODES: (spaceId) => `/spaces/${spaceId}/nodes/`,
  EDGES: (spaceId) => `/spaces/${spaceId}/edges/`,
  SNAPSHOTS: (spaceId) => `/spaces/${spaceId}/snapshots/`,
  WIKIDATA_SEARCH: '/spaces/wikidata-search/',
  WIKIDATA_PROPERTIES: (entityId) => `/spaces/wikidata-entity-properties/${entityId}/`,
};

export const GRAPH_CONFIG = {
  NODE_STYLE: {
    width: "80px",
    height: "80px",
    borderRadius: "50%",
    border: "2px solid #1a73e8",
    backgroundColor: "white",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "10px",
    fontSize: "12px",
    textAlign: "center",
    wordBreak: "break-word",
    lineHeight: "1.2",
    boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
  },
  LAYOUT: {
    NODES_PER_ROW: 4,
    NODE_SPACING: 200,
    ROW_SPACING: 200,
    START_X: 200,
    START_Y: 100,
  },
};

export const ERROR_MESSAGES = {
  GRAPH_DATA: 'Failed to fetch graph data',
  WIKIDATA_SEARCH: 'Failed to search Wikidata',
  ENTITY_PROPERTIES: 'Failed to fetch entity properties',
}; 