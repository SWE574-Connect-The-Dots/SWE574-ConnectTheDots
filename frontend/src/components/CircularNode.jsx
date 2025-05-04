import PropTypes from 'prop-types';
import { Handle, Position } from 'reactflow';

const CircularNode = ({ data }) => {
  return (
    <div
      style={{
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
      }}
    >
      <Handle type="target" position={Position.Top} style={{ opacity: 0 }} />
      <div>{data.label}</div>
      <Handle type="source" position={Position.Bottom} style={{ opacity: 0 }} />
    </div>
  );
};

CircularNode.propTypes = {
  data: PropTypes.shape({
    label: PropTypes.string.isRequired,
    wikidata_id: PropTypes.string,
  }).isRequired,
};

export default CircularNode; 