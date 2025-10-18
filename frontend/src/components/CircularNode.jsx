import PropTypes from 'prop-types';
import { Handle, Position } from 'reactflow';

const CircularNode = ({ data, selected }) => {
  return (
    <div
      style={{
        width: "80px",
        height: "80px",
        borderRadius: "50%",
        border: `3px solid ${selected ? '#7209B7' : '#3A0CA3'}`,
        backgroundColor: selected ? '#7209B7' : '#3A0CA3',
        color: '#FFFFFF',
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "10px",
        fontSize: "12px",
        fontWeight: "600",
        textAlign: "center",
        wordBreak: "break-word",
        lineHeight: "1.2",
        boxShadow: "0 2px 8px rgba(0,0,0,0.2)",
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
  selected: PropTypes.bool,
};

export default CircularNode; 