/**
 * Global configuration for Wikidata instance type GROUPS (P31)
 * 
 * This config implements a grouping system that maps hundreds of specific
 * Wikidata instance types into ~10 broad categories for visualization.
 * 
 * For example, "megacity", "big city", "port city" all map to "City" group.
 * 
 * Color palette is optimized for color blindness accessibility.
 * 
 * @example
 * import { INSTANCE_TYPE_GROUPS, getGroupForType } from '../config/instanceTypes';
 */

// Instance type groups for visualization
export const INSTANCE_TYPE_GROUPS = {
  HUMAN: {
    id: 'HUMAN',
    label: 'Human',
    types: ['Q5', 'Q215627'],
    color: '#0072B2',
    icon: '👤',
    priority: 100,
    description: 'Individual persons'
  },
  CITY: {
    id: 'CITY',
    label: 'City',
    types: ['Q515', 'Q1549591', 'Q1637706', 'Q5119', 'Q200250', 'Q1093829', 'Q7930989', 'Q2514025'],
    color: '#E69F00',
    icon: '🏙️',
    priority: 85,
    description: 'Cities and urban areas'
  },
  COUNTRY: {
    id: 'COUNTRY',
    label: 'Country',
    types: ['Q6256', 'Q3024240', 'Q859563', 'Q1520223'],
    color: '#56B4E9',
    icon: '🌍',
    priority: 90,
    description: 'Countries and sovereign states'
  },
  SETTLEMENT: {
    id: 'SETTLEMENT',
    label: 'Settlement',
    types: ['Q486972', 'Q532', 'Q3957', 'Q5084', 'Q3191695', 'Q15221371'],
    color: '#F0E442',
    icon: '🏘️',
    priority: 75,
    description: 'Villages, towns, and small settlements'
  },
  ORGANIZATION: {
    id: 'ORGANIZATION',
    label: 'Organization',
    types: ['Q43229', 'Q4830453', 'Q783794', 'Q891723', 'Q219577', 'Q7210356', 'Q31855', 'Q2085381', 'Q294163'],
    color: '#D55E00',
    icon: '🏢',
    priority: 70,
    description: 'Organizations, companies, and institutions'
  },
  GEOGRAPHIC_FEATURE: {
    id: 'GEOGRAPHIC_FEATURE',
    label: 'Geographic Feature',
    types: ['Q8502', 'Q23442', 'Q4022', 'Q23397', 'Q39594', 'Q185113', 'Q39816', 'Q54050', 'Q177634'],
    color: '#8B4513',
    icon: '⛰️',
    priority: 60,
    description: 'Natural geographic features'
  },
  SPECIES: {
    id: 'SPECIES',
    label: 'Species',
    types: ['Q16521', 'Q7432', 'Q34740', 'Q35409', 'Q36602', 'Q37517'],
    color: '#009E73',
    icon: '🌿',
    priority: 50,
    description: 'Biological taxa and species'
  },
  WORK: {
    id: 'WORK',
    label: 'Work',
    types: ['Q11424', 'Q571', 'Q7725634', 'Q13442814', 'Q732577', 'Q2188189', 'Q386724', 'Q3305213', 'Q860861'],
    color: '#CC79A7',
    icon: '📄',
    priority: 55,
    description: 'Creative and scholarly works'
  },
  BUILDING: {
    id: 'BUILDING',
    label: 'Building',
    types: ['Q41176', 'Q16560', 'Q44494', 'Q16970', 'Q34627', 'Q44539', 'Q33506', 'Q483110', 'Q5003624'],
    color: '#808000',
    icon: '🏛️',
    priority: 65,
    description: 'Buildings and structures'
  },
  ADMINISTRATIVE: {
    id: 'ADMINISTRATIVE',
    label: 'Administrative Division',
    types: ['Q15042037', 'Q10864048', 'Q56061', 'Q842112', 'Q192611', 'Q174844', 'Q82794'],
    color: '#000080',
    icon: '🗺️',
    priority: 80,
    description: 'Administrative territorial entities'
  }
};
export const UNKNOWN_COLOR = '#94A3B8';

const TYPE_TO_GROUP_MAP = {};
Object.values(INSTANCE_TYPE_GROUPS).forEach(group => {
  group.types.forEach(typeId => {
    TYPE_TO_GROUP_MAP[typeId] = group.id;
  });
});

/**
 * Get group for a specific Wikidata type ID
 * @param {string} typeId - Wikidata Q-ID (e.g., 'Q515')
 * @returns {object|null} Group object or null if not found
 */
export const getGroupForType = (typeId) => {
  const groupId = TYPE_TO_GROUP_MAP[typeId];
  return groupId ? INSTANCE_TYPE_GROUPS[groupId] : null;
};

/**
 * Get color for a type (returns group color)
 * @param {string} typeId - Wikidata Q-ID
 * @returns {string} Hex color code
 */
export const getInstanceTypeColor = (typeId) => {
  const group = getGroupForType(typeId);
  return group ? group.color : UNKNOWN_COLOR;
};

/**
 * Get group color by group ID
 * @param {string} groupId - Group ID (e.g., 'CITY')
 * @returns {string} Hex color code
 */
export const getGroupColor = (groupId) => {
  const group = INSTANCE_TYPE_GROUPS[groupId];
  return group ? group.color : UNKNOWN_COLOR;
};

/**
 * Get priority for sorting (higher = more important)
 * @param {string} typeId - Wikidata Q-ID
 * @returns {number} Priority value
 */
export const getInstanceTypePriority = (typeId) => {
  const group = getGroupForType(typeId);
  return group ? group.priority : 0;
};

/**
 * Priority-ordered list of group IDs (highest to lowest)
 * @returns {string[]} Array of group IDs
 */
export const INSTANCE_TYPE_PRIORITY_ORDER = Object.values(INSTANCE_TYPE_GROUPS)
  .sort((a, b) => b.priority - a.priority)
  .map(g => g.id);

/**
 * Get all groups as an array
 * @returns {object[]} Array of group objects
 */
export const getAllGroups = () => {
  return Object.values(INSTANCE_TYPE_GROUPS);
};

/**
 * Check if a type ID is mapped to any group
 * @param {string} typeId - Wikidata Q-ID
 * @returns {boolean}
 */
export const isTypeMapped = (typeId) => {
  return typeId in TYPE_TO_GROUP_MAP;
};

/**
 * Get group by group ID
 * @param {string} groupId - Group ID (e.g., 'CITY')
 * @returns {object|null} Group object or null
 */
export const getGroupById = (groupId) => {
  return INSTANCE_TYPE_GROUPS[groupId] || null;
};
