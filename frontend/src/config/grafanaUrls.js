const USE_PRODUCTION_IDS = false; // Set to true for production

// Development URLs (Test Environment)
const DEV_URLS = {
  // Overall Platform Metrics - Users
  usersDaily: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761326158916&to=1763918158916&timezone=browser&refresh=5m&theme=light&panelId=panel-1&__feature.dashboardSceneSolo=true',
  usersWeekly: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761326158916&to=1763918158916&timezone=browser&refresh=5m&theme=light&panelId=panel-2&__feature.dashboardSceneSolo=true',
  usersMonthly: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761326158916&to=1763918158916&timezone=browser&refresh=5m&theme=light&panelId=panel-3&__feature.dashboardSceneSolo=true',
  
  // Overall Platform Metrics - Spaces
  spacesDaily: 'http://localhost:300/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761326158916&to=1763918158916&timezone=browser&refresh=5m&theme=light&panelId=panel-4&__feature.dashboardSceneSolo=true',
  spacesWeekly: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761326158916&to=1763918158916&timezone=browser&refresh=5m&theme=light&panelId=panel-5&__feature.dashboardSceneSolo=true',
  spacesMonthly: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761326158916&to=1763918158916&timezone=browser&refresh=5m&theme=light&panelId=panel-6&__feature.dashboardSceneSolo=true',
  
  // Content Statistics - Nodes
  nodesDaily: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761326158916&to=1763918158916&timezone=browser&refresh=5m&theme=light&panelId=panel-7&__feature.dashboardSceneSolo=true',
  nodesWeekly: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761326158916&to=1763918158916&timezone=browser&refresh=5m&theme=light&panelId=panel-8&__feature.dashboardSceneSolo=true',
  nodesMonthly: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761326158916&to=1763918158916&timezone=browser&refresh=5m&theme=light&panelId=panel-9&__feature.dashboardSceneSolo=true',
  
  // Content Statistics - Edges
  edgesDaily: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761326158916&to=1763918158916&timezone=browser&refresh=5m&theme=light&panelId=panel-10&__feature.dashboardSceneSolo=true',
  edgesWeekly: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761330891040&to=1763922891040&timezone=browser&refresh=5m&theme=light&panelId=panel-11&__feature.dashboardSceneSolo=true',
  edgesMonthly: 'http://localhost:3001/d-solo/c82068b3-1c31-4bbe-bfac-8dee39b3e525/connectthedots-analytics-dashboard?orgId=1&from=1761330891040&to=1763922891040&timezone=browser&refresh=5m&theme=light&panelId=panel-12&__feature.dashboardSceneSolo=true',
  
  // Activity Trends - Dynamic URLs (panelId: 1=Daily, 2=Weekly, 3=Monthly)
  getUserTrendUrl: (panelId) => `http://localhost:3001/d-solo/425ed900-4b0f-42a8-ab68-184fd251ac1f/connectthedots-user-growth-analytics?orgId=1&refresh=5m&theme=light&panelId=${panelId}&__feature.dashboardSceneSolo=true`,
  getNodeTrendUrl: (panelId) => `http://localhost:3001/d-solo/4788e656-d0aa-4041-afe9-f9ce1c6ba3c5/connectthedots-node-analytics?orgId=1&refresh=5m&theme=light&panelId=${panelId}&__feature.dashboardSceneSolo=true`,
  getSpaceTrendUrl: (panelId) => `http://localhost:3001/d-solo/cd823a8f-4a1c-4904-90ad-4d55019228e1/connectthedots-space-analytics?orgId=1&refresh=5m&theme=light&panelId=${panelId}&__feature.dashboardSceneSolo=true`
};

// Production URLs 
const PROD_URLS = {
  // Overall Platform Metrics - Users
  usersDaily: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-1&__feature.dashboardSceneSolo=true',
  usersWeekly: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-2&__feature.dashboardSceneSolo=true',
  usersMonthly: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-3&__feature.dashboardSceneSolo=true',
  
  // Overall Platform Metrics - Spaces
  spacesDaily: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-4&__feature.dashboardSceneSolo=true',
  spacesWeekly: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-5&__feature.dashboardSceneSolo=true',
  spacesMonthly: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-6&__feature.dashboardSceneSolo=true',
  
  // Content Statistics - Nodes
  nodesDaily: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-7&__feature.dashboardSceneSolo=true',
  nodesWeekly: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-8&__feature.dashboardSceneSolo=true',
  nodesMonthly: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-9&__feature.dashboardSceneSolo=true',
  
  // Content Statistics - Edges
  edgesDaily: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-10&__feature.dashboardSceneSolo=true',
  edgesWeekly: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-11&__feature.dashboardSceneSolo=true',
  edgesMonthly: 'http://localhost:3001/d-solo/14063c50-1027-4924-8f33-c5c48514876c/connectthedots-analytics-dashboard?orgId=1&from=1761332336200&to=1763924336200&timezone=browser&refresh=5m&theme=light&panelId=panel-12&__feature.dashboardSceneSolo=true',
  
  // Activity Trends - Dynamic URLs
  getUserTrendUrl: (panelId) => `http://localhost:3001/d-solo/1a04ab1d-c63e-4a18-995a-5ad2b1a1f19d/connectthedots-user-growth-analytics?orgId=1&refresh=5m&theme=light&panelId=${panelId}&__feature.dashboardSceneSolo=true`,
  getNodeTrendUrl: (panelId) => `http://localhost:3001/d-solo/a7aeef5d-d489-4eca-ab2b-bea77b97c383/connectthedots-node-analytics?orgId=1&refresh=5m&theme=light&panelId=${panelId}&__feature.dashboardSceneSolo=true`,
  getSpaceTrendUrl: (panelId) => `http://localhost:3001/d-solo/6309a38f-9d56-4319-ae5a-26faebd10bbb/connectthedots-space-analytics?orgId=1&refresh=5m&theme=light&panelId=${panelId}&__feature.dashboardSceneSolo=true`
};

// Export the URLs based on environment flag
export const GRAFANA_URLS = USE_PRODUCTION_IDS ? PROD_URLS : DEV_URLS;