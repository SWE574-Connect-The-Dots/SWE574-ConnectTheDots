import PropTypes from "prop-types";
import { useTranslation } from "../contexts/TranslationContext";
import ReactFlow, { Controls, Background } from "reactflow";
import "reactflow/dist/style.css";
import CircularNode from "./CircularNode";

const nodeTypes = {
  circular: CircularNode,
};

const SpaceGraph = ({ nodes, edges, loading, error }) => {
  const { t } = useTranslation();
  
  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner" />
        <p>{t("graph.loadingGraph")}</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <p className="error-message">{t("common.error")}: {error}</p>
      </div>
    );
  }

  return (
    <div className="graph-container">
      <ReactFlow nodes={nodes} edges={edges} nodeTypes={nodeTypes} fitView>
        <Background />
        <Controls />
      </ReactFlow>
    </div>
  );
};

SpaceGraph.propTypes = {
  nodes: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      type: PropTypes.string.isRequired,
      position: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
      }).isRequired,
      data: PropTypes.shape({
        label: PropTypes.string.isRequired,
        wikidata_id: PropTypes.string,
      }).isRequired,
    })
  ).isRequired,
  edges: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      source: PropTypes.string.isRequired,
      target: PropTypes.string.isRequired,
      label: PropTypes.string,
      animated: PropTypes.bool,
      markerEnd: PropTypes.shape({
        type: PropTypes.string.isRequired,
      }),
    })
  ).isRequired,
  loading: PropTypes.bool.isRequired,
  error: PropTypes.string,
};

export default SpaceGraph;
