import PropTypes from 'prop-types';
import { Handle, Position } from 'reactflow';

const CircularNode = ({ data, selected }) => {
  const nodeSize = data.size || 80;
  const fontSize = Math.max(10, Math.min(14, Math.round(nodeSize * 0.15)));
  const nodeColor = data.instanceTypeColor || data.color || 'var(--color-success)';
  
  const isFiltered = data.isFiltered || false;
  const matchedProperty = data.matchedProperty || false;  // Check if node matched property filter
  const matchedPropertyValue = data.matchedPropertyValue || false;  // Check if node matched property value filter
  const matchedNode = data.matchedNode || false;  // Check if node was directly searched
  const isMatched = matchedNode || matchedProperty || matchedPropertyValue;  // Any type of match
  const greyedOutColor = 'var(--color-gray-300)';
  const greyedOutBorder = 'var(--color-gray-400)';
  
  // Choose highlight color based on match type
  const getHighlightColor = () => {
    if (matchedNode) return '#0076B5';  // Blue for directly searched nodes
    if (matchedPropertyValue) return '#FFD700';  // Gold for property value matches
    if (matchedProperty) return '#90EE90';  // Light green for property matches
    return nodeColor;
  };
  
  const finalColor = isFiltered ? greyedOutColor : nodeColor;
  const finalBorderColor = isFiltered ? greyedOutBorder : (isMatched ? getHighlightColor() : nodeColor);
  
  const selectedColor = selected 
    ? `color-mix(in srgb, ${finalColor} 100%, #000 20%)` 
    : finalColor;

  const tooltipParts = [data.label];
  if (data.instanceTypeLabel) {
    tooltipParts.push(`(${data.instanceTypeLabel})`);
  }
  if (data.wikidata_id) {
    tooltipParts.push(`[${data.wikidata_id}]`);
  }
  if (matchedPropertyValue) {
    tooltipParts.push('✓✓ Matches property VALUE');
  } else if (matchedProperty) {
    tooltipParts.push('✓ Has searched property');
  }
  const tooltipText = tooltipParts.join(' ');

  const handleStyle = {
    opacity: 0,
  };

  return (
    <div
      style={{
        width: `${nodeSize}px`,
        height: `${nodeSize}px`,
        borderRadius: "50%",
        border: selected 
          ? `4px solid ${finalBorderColor}` 
          : isMatched 
            ? `4px solid ${finalBorderColor}`  // Thicker gold border for matched nodes
            : `3px solid ${finalBorderColor}`,
        backgroundColor: selected ? selectedColor : finalColor,
        color: isFiltered ? 'var(--color-text-secondary)' : 'var(--color-white)',
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        padding: "10px",
        fontSize: `${fontSize}px`,
        fontWeight: "600",
        textAlign: "center",
        wordBreak: "break-word",
        lineHeight: "1.2",
        boxShadow: isFiltered 
          ? "0 1px 3px rgba(0,0,0,0.1)"
          : isMatched
            ? `0 4px 16px ${getHighlightColor()}80`  // Glow with highlight color
            : selected 
              ? `0 4px 12px ${nodeColor}66` 
              : "0 2px 8px rgba(0,0,0,0.15)",
        transition: "all 0.2s ease",
        position: "relative",
        opacity: isFiltered ? 0.4 : 1,
        cursor: isFiltered ? 'default' : 'pointer',
      }}
      title={tooltipText}
    >
      {isMatched && (
        <div style={{
          position: 'absolute',
          top: '-8px',
          right: '-8px',
          width: '24px',
          height: '24px',
          borderRadius: '50%',
          backgroundColor: '#FFD700',
          color: '#000',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '14px',
          fontWeight: 'bold',
          boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
          zIndex: 10
        }}>
          ✓
        </div>
      )}
      <Handle 
        type="target" 
        position={Position.Top} 
        id="top"
        style={handleStyle}
      />
      <Handle 
        type="target" 
        position={Position.Right} 
        id="right"
        style={handleStyle}
      />
      <Handle 
        type="target" 
        position={Position.Bottom} 
        id="bottom"
        style={handleStyle}
      />
      <Handle 
        type="target" 
        position={Position.Left} 
        id="left"
        style={handleStyle}
      />
      <Handle 
        type="source" 
        position={Position.Top} 
        id="top"
        style={handleStyle}
      />
      <Handle 
        type="source" 
        position={Position.Right} 
        id="right"
        style={handleStyle}
      />
      <Handle 
        type="source" 
        position={Position.Bottom} 
        id="bottom"
        style={handleStyle}
      />
      <Handle 
        type="source" 
        position={Position.Left} 
        id="left"
        style={handleStyle}
      />
      
      <div style={{ 
        textShadow: '0 1px 2px rgba(0,0,0,0.3)',
        userSelect: 'none',
      }}>
        {data.label}
      </div>
      
      {/* Tag below node for matched properties */}
      {matchedPropertyValue && (
        <div style={{
          position: 'absolute',
          bottom: '-30px',
          left: '50%',
          transform: 'translateX(-50%)',
          backgroundColor: '#FFD700',
          color: '#000',
          padding: '4px 8px',
          borderRadius: '4px',
          fontSize: '11px',
          fontWeight: 'bold',
          whiteSpace: 'nowrap',
          boxShadow: '0 2px 6px rgba(0,0,0,0.3)',
          zIndex: 1000
        }}>
          ✓ Property Value
        </div>
      )}
      {matchedProperty && !matchedPropertyValue && (
        <div style={{
          position: 'absolute',
          bottom: '-30px',
          left: '50%',
          transform: 'translateX(-50%)',
          backgroundColor: '#4CAF50',
          color: '#fff',
          padding: '4px 8px',
          borderRadius: '4px',
          fontSize: '11px',
          fontWeight: 'bold',
          whiteSpace: 'nowrap',
          boxShadow: '0 2px 6px rgba(0,0,0,0.3)',
          zIndex: 1000
        }}>
          ✓ Property
        </div>
      )}
    </div>
  );
};

CircularNode.propTypes = {
  data: PropTypes.shape({
    label: PropTypes.string.isRequired,
    wikidata_id: PropTypes.string,
    size: PropTypes.number,
    degree: PropTypes.number,
    color: PropTypes.string,
    country: PropTypes.string,
    city: PropTypes.string,
    district: PropTypes.string,
    street: PropTypes.string,
    latitude: PropTypes.number,
    longitude: PropTypes.number,
    location_name: PropTypes.string,
    description: PropTypes.string,
    instanceTypeColor: PropTypes.string,
    instanceTypeLabel: PropTypes.string,
    isFiltered: PropTypes.bool,
    matchedProperty: PropTypes.bool,
    matchedPropertyValue: PropTypes.bool
  }).isRequired,
  selected: PropTypes.bool,
};

export default CircularNode; 
