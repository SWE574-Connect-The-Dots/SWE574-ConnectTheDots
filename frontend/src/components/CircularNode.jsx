import PropTypes from 'prop-types';
import { Handle, Position } from 'reactflow';

const CircularNode = ({ data, selected }) => {
  const nodeSize = data.size || 80;
  const fontSize = Math.max(10, Math.min(14, Math.round(nodeSize * 0.15)));
  const nodeColor = data.color || 'var(--color-success)';
  
  const selectedColor = selected 
    ? `color-mix(in srgb, ${nodeColor} 100%, #000 20%)` 
    : nodeColor;

  const handleStyle = {
    opacity: 0,
  };

  return (
    <div
      style={{
        width: `${nodeSize}px`,
        height: `${nodeSize}px`,
        borderRadius: "50%",
        border: selected ? `4px solid ${nodeColor}` : `3px solid ${nodeColor}`,
        backgroundColor: selected ? selectedColor : nodeColor,
        color: 'var(--color-white)',
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
        boxShadow: selected 
          ? `0 4px 12px ${nodeColor}66` 
          : "0 2px 8px rgba(0,0,0,0.15)",
        transition: "all 0.2s ease",
        position: "relative",
      }}
    >
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
  }).isRequired,
  selected: PropTypes.bool,
};

export default CircularNode; 
