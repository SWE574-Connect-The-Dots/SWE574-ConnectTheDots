import { getStraightPath } from 'reactflow';

const CenterEdge = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  style = {},
  markerEnd,
  label,
  labelStyle,
}) => {
  // Calculate the center-to-center path
  const [edgePath, labelX, labelY] = getStraightPath({
    sourceX,
    sourceY,
    targetX,
    targetY,
  });

  // Calculate the angle between nodes
  const dx = targetX - sourceX;
  const dy = targetY - sourceY;
  const distance = Math.sqrt(dx * dx + dy * dy);
  
  // Assuming circular nodes with radius around 80px (adjust based on your node size)
  const nodeRadius = 80;
  
  // Calculate the points where the line should start and end (at the edge of circles)
  const startX = sourceX + (dx / distance) * nodeRadius;
  const startY = sourceY + (dy / distance) * nodeRadius;
  const endX = targetX - (dx / distance) * nodeRadius;
  const endY = targetY - (dy / distance) * nodeRadius;

  // Create the path from edge to edge
  const adjustedPath = `M ${startX},${startY} L ${endX},${endY}`;

  return (
    <>
      <path
        id={id}
        style={style}
        className="react-flow__edge-path"
        d={adjustedPath}
        markerEnd={markerEnd}
      />
      {label && (
        <text>
          <textPath
            href={`#${id}`}
            style={labelStyle}
            startOffset="50%"
            textAnchor="middle"
          >
            {label}
          </textPath>
        </text>
      )}
    </>
  );
};

export default CenterEdge;