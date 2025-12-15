from neo4j import GraphDatabase
from neo4j.exceptions import AuthError, ServiceUnavailable
from django.conf import settings
import logging

logger = logging.getLogger(__name__)

class Neo4jConnection:
    _driver = None

    @classmethod
    def get_driver(cls):
        if cls._driver is None:
            try:
                logger.info(f"Connecting to Neo4j at {settings.NEO4J_URI}")
                cls._driver = GraphDatabase.driver(
                    settings.NEO4J_URI,
                    auth=(settings.NEO4J_USER, settings.NEO4J_PASSWORD),
                    encrypted=False,
                    connection_timeout=30,
                    max_connection_lifetime=3600
                )
                logger.info("Neo4j driver created")
            except Exception as e:
                logger.error(f"Failed to create Neo4j driver: {e}", exc_info=True)
                cls._driver = None
                raise
        return cls._driver

    @classmethod
    def close(cls):
        if cls._driver:
            cls._driver.close()
            cls._driver = None

    @staticmethod
    def create_node(node_id, label, space_id, properties=None):
        """
        Creates a node in Neo4j linked to a Space ID.
        """
        if properties is None:
            properties = {}
        
        
        properties['pg_id'] = node_id  # Link back to Postgres ID
        properties['space_id'] = space_id
        properties['label'] = label
        
        query = """
        MERGE (s:Space {id: $space_id})
        CREATE (n:Node)
        SET n += $properties
        SET n:`""" + "Entity" + """` 
        MERGE (n)-[:IN_SPACE]->(s)
        RETURN n
        """
        
        try:
            driver = Neo4jConnection.get_driver()
            with driver.session() as session:
                session.run(query, space_id=space_id, properties=properties)
        except Exception as e:
            logger.error(f"Failed to create node in Neo4j: {e}")

    @staticmethod
    def create_edge(edge_id, source_node_id, target_node_id, relation_label, properties=None):
        """
        Creates an edge between two nodes in Neo4j.
        """
        if properties is None:
            properties = {}
        
        properties['pg_id'] = edge_id
        properties['label'] = relation_label
        
        
        safe_label = relation_label.replace("`", "``")
        if not safe_label:
            safe_label = "RELATED_TO"

        query = """
        MATCH (source:Node {pg_id: $source_id})
        MATCH (target:Node {pg_id: $target_id})
        CREATE (source)-[r:`""" + safe_label + """`]->(target)
        SET r += $properties
        RETURN r
        """
        
        try:
            driver = Neo4jConnection.get_driver()
            with driver.session() as session:
                session.run(query, source_id=source_node_id, target_id=target_node_id, properties=properties)
        except Exception as e:
            logger.error(f"Failed to create edge in Neo4j: {e}")

    @staticmethod
    def update_node(node_id, properties):
        """
        Updates properties of a node in Neo4j.
        """
        query = """
        MATCH (n:Node {pg_id: $node_id})
        SET n += $properties
        RETURN n
        """
        try:
            driver = Neo4jConnection.get_driver()
            with driver.session() as session:
                session.run(query, node_id=node_id, properties=properties)
        except Exception as e:
            logger.error(f"Failed to update node in Neo4j: {e}")

    @staticmethod
    def update_edge(edge_id, properties):
        """
        Updates properties of an edge in Neo4j.
        """
        query = """
        MATCH ()-[r]->()
        WHERE r.pg_id = $edge_id
        SET r += $properties
        RETURN r
        """
        try:
            driver = Neo4jConnection.get_driver()
            with driver.session() as session:
                session.run(query, edge_id=edge_id, properties=properties)
        except Exception as e:
            logger.error(f"Failed to update edge in Neo4j: {e}")

    @staticmethod
    def delete_node(node_id):
        """
        Deletes a node and its relationships in Neo4j.
        """
        query = """
        MATCH (n:Node {pg_id: $node_id})
        DETACH DELETE n
        """
        try:
            driver = Neo4jConnection.get_driver()
            with driver.session() as session:
                session.run(query, node_id=node_id)
        except Exception as e:
            logger.error(f"Failed to delete node in Neo4j: {e}")

    @staticmethod
    def delete_edge(edge_id):
        """
        Deletes an edge in Neo4j.
        """
        query = """
        MATCH ()-[r]->()
        WHERE r.pg_id = $edge_id
        DELETE r
        """
        try:
            driver = Neo4jConnection.get_driver()
            with driver.session() as session:
                session.run(query, edge_id=edge_id)
        except Exception as e:
            logger.error(f"Failed to delete edge in Neo4j: {e}")

    @staticmethod
    def delete_node_property(node_id, property_key):
        """
        Deletes a specific property from a node in Neo4j.
        """
        query = """
        MATCH (n:Node {pg_id: $node_id})
        REMOVE n.`""" + property_key + """`
        RETURN n
        """
        try:
            driver = Neo4jConnection.get_driver()
            with driver.session() as session:
                session.run(query, node_id=node_id)
        except Exception as e:
            logger.error(f"Failed to delete node property in Neo4j: {e}")

    @staticmethod
    def search_graph(space_id, node_queries=None, edge_queries=None, property_queries=None, property_values=None, depth=1):
        """
        Search for nodes and edges in a specific space using node IDs, text search terms, and property filters.
        Returns a subgraph containing matching nodes, matching edges, their neighbors up to 'depth' levels,
        and all edges between them.
        
        Args:
            space_id: The space ID to search in
            node_queries: List of node IDs or search terms (supports both)
            edge_queries: List of edge search terms
            property_queries: List of property names/ids to filter nodes by
            property_values: List of property value IDs to filter nodes by
            depth: Number of relationship levels to include (1 = direct connections only)
        """
        # Parse parameters first - convert comma-separated strings to lists
        if property_queries is None:
            property_queries = []
        elif isinstance(property_queries, str):
            property_queries = [q.strip() for q in property_queries.split(',') if q.strip()]
        
        if property_values is None:
            property_values = []
        elif isinstance(property_values, str):
            property_values = [q.strip() for q in property_values.split(',') if q.strip()]
        
        # First, if property_queries are provided, get matching node IDs from Postgres
        property_node_ids = []
        if property_queries:
            from .models import Property, Node as DjangoNode
            logger.info(f"Searching for nodes with properties: {property_queries}")
            property_matches = Property.objects.filter(
                node__space_id=space_id,
                property_id__in=property_queries if isinstance(property_queries, list) else [property_queries]
            ).values_list('node__id', flat=True).distinct()
            property_node_ids = list(property_matches)
            logger.info(f"Found {len(property_node_ids)} nodes with these properties: {property_node_ids}")
        
        # If property_values are provided, get matching node IDs from Postgres
        property_value_node_ids = []
        if property_values:
            from .models import Property, Node as DjangoNode
            # Property value IDs are strings like "P569:1893-04-04T00:00:00Z"
            values_list = property_values if isinstance(property_values, list) else [property_values]
            
            logger.info(f"Searching for nodes with property values: {values_list}")
            
            # Query using the value_text field which contains the full value string
            property_value_matches = Property.objects.filter(
                node__space_id=space_id,
                value_text__in=values_list
            ).values_list('node__id', flat=True).distinct()
            property_value_node_ids = list(property_value_matches)
            logger.info(f"Found {len(property_value_node_ids)} nodes matching property values: {property_value_node_ids}")
        
        # Convert queries to lists and parse before building query
        if node_queries is None:
            node_queries = []
        elif isinstance(node_queries, str):
            node_queries = [q.strip() for q in node_queries.split(',') if q.strip()]
        
        # Separate node IDs and text queries
        node_ids = []
        node_text_queries = []
        for query_item in node_queries:
            try:
                node_id = int(query_item)
                node_ids.append(node_id)
            except (ValueError, TypeError):
                node_text_queries.append(query_item)
            
        if edge_queries is None:
            edge_queries = []
        elif isinstance(edge_queries, str):
            edge_queries = [q.strip() for q in edge_queries.split(',') if q.strip()]
        
        # Determine search type
        has_edge_search = len(edge_queries) > 0
        has_node_search = len(node_ids) > 0 or len(node_text_queries) > 0 or len(property_node_ids) > 0 or len(property_value_node_ids) > 0
        
        # For edge search: depth-1 (depth=1 means show only matched edge)
        # For node search: full depth
        edge_depth = max(0, depth - 1)
        node_depth = depth
        
        logger.info(f"Search type: edge={has_edge_search}, node={has_node_search}, node_depth={node_depth}, edge_depth={edge_depth}")
        
        query = """
        // 1. Find matching nodes (by ID or text search)
        OPTIONAL MATCH (n:Node {space_id: $space_id})
        WHERE (size($node_ids) > 0 AND any(id IN $node_ids WHERE n.pg_id = id)) OR
              (size($node_text_queries) > 0 AND any(term IN $node_text_queries WHERE n.label CONTAINS term OR n.description CONTAINS term)) OR
              (size($property_value_node_ids) > 0 AND any(id IN $property_value_node_ids WHERE n.pg_id = id)) OR
              (size($property_value_node_ids) = 0 AND size($property_node_ids) > 0 AND any(id IN $property_node_ids WHERE n.pg_id = id))
        WITH collect(DISTINCT n) as matchingNodes
        
        // 2. Find matching edges
        OPTIONAL MATCH (edgeSource:Node {space_id: $space_id})-[matchedEdge]->(edgeTarget:Node {space_id: $space_id})
        WHERE size($edge_queries) > 0 AND 
              any(term IN $edge_queries WHERE type(matchedEdge) CONTAINS term OR matchedEdge.label CONTAINS term)
        WITH matchingNodes, 
             collect(DISTINCT edgeSource) as edgeSources,
             collect(DISTINCT edgeTarget) as edgeTargets
        
        // 3. Expand from matched nodes with node_depth
        UNWIND matchingNodes as matchedNode
        OPTIONAL MATCH nodePath = (matchedNode)-[*0..""" + str(node_depth) + """]-(nodeNeighbor:Node {space_id: $space_id})
        WHERE none(r IN relationships(nodePath) WHERE type(r) = 'IN_SPACE')
        WITH edgeSources, edgeTargets, 
             collect(DISTINCT {node: nodeNeighbor, depth: length(nodePath)}) as nodeResults
        
        // 4. Expand from edge endpoints with edge_depth
        WITH (edgeSources + edgeTargets) as edgeEndpoints, nodeResults
        UNWIND edgeEndpoints as endpoint
        OPTIONAL MATCH edgePath = (endpoint)-[*0..""" + str(edge_depth) + """]-(edgeNeighbor:Node {space_id: $space_id})
        WHERE none(r IN relationships(edgePath) WHERE type(r) = 'IN_SPACE')
        WITH nodeResults, collect(DISTINCT {node: edgeNeighbor, depth: length(edgePath)}) as edgeResults
        
        // 5. Combine and flatten results
        WITH nodeResults + edgeResults as allResults
        UNWIND allResults as result
        WITH result.node as neighbor, min(result.depth) as minDepth
        WHERE neighbor IS NOT NULL
        WITH collect(DISTINCT neighbor) as uniqueNodes, 
             collect(DISTINCT {id: neighbor.pg_id, depth: minDepth}) as nodeDepths
        
        // 6. Extract all edges between the discovered nodes
        UNWIND uniqueNodes as n1
        UNWIND uniqueNodes as n2
        OPTIONAL MATCH (n1)-[r]->(n2)
        WHERE n1 <> n2 AND r IS NOT NULL AND type(r) <> 'IN_SPACE'
        WITH uniqueNodes, nodeDepths, collect(DISTINCT r) as finalEdges
        
        // 7. Get matching edges for highlighting
        OPTIONAL MATCH (s:Node {space_id: $space_id})-[mr]->(t:Node {space_id: $space_id})
        WHERE size($edge_queries) > 0 AND 
              any(term IN $edge_queries WHERE type(mr) CONTAINS term OR mr.label CONTAINS term)
        WITH uniqueNodes, nodeDepths, finalEdges, collect(DISTINCT mr.pg_id) as matchedEdgeIds
        
        RETURN uniqueNodes, nodeDepths, finalEdges, matchedEdgeIds
        """
        
        result_data = {'nodes': [], 'edges': []}
        
        logger.info(f"Graph search executing with space_id={space_id}, node_ids={node_ids}, node_text_queries={node_text_queries}, edge_queries={edge_queries}, property_node_ids={property_node_ids}, property_value_node_ids={property_value_node_ids}, node_depth={node_depth}, edge_depth={edge_depth}")
        
        try:
            driver = Neo4jConnection.get_driver()
            with driver.session() as session:
                result = session.run(query, 
                                    space_id=space_id, 
                                    node_ids=node_ids,  # Numeric node IDs
                                    node_text_queries=node_text_queries,  # Text search terms
                                    edge_queries=edge_queries,
                                    property_node_ids=property_node_ids,  # Node IDs with matching properties
                                    property_value_node_ids=property_value_node_ids)  # Node IDs with matching property values
                record = result.single()
                
                if record:
                    nodes = record.get('uniqueNodes', [])
                    node_depths_list = record.get('nodeDepths', [])
                    edges = record.get('finalEdges', [])
                    matched_edge_ids = record.get('matchedEdgeIds', [])
                    
                    # Create a map of node_id -> depth
                    node_depth_map = {item['id']: item['depth'] for item in node_depths_list}
                    
                    logger.info(f"Graph search found {len(nodes)} nodes and {len(edges)} edges")
                    logger.info(f"Property node IDs (has property): {property_node_ids}")
                    logger.info(f"Property value node IDs (has property value): {property_value_node_ids}")
                    
                    # Fetch properties for all nodes in the result
                    from .models import Property
                    node_ids_list = [node.get('pg_id') for node in nodes]
                    properties_by_node = {}
                    if node_ids_list:
                        properties = Property.objects.filter(
                            node__id__in=node_ids_list
                        ).select_related('node').values(
                            'node__id', 'property_id', 'property_label', 'value_text', 'value_id'
                        )
                        for prop in properties:
                            node_db_id = prop['node__id']
                            if node_db_id not in properties_by_node:
                                properties_by_node[node_db_id] = []
                            properties_by_node[node_db_id].append({
                                'property_id': prop['property_id'],
                                'label': prop['property_label'],
                                'value_text': prop['value_text'],
                                'value_id': prop['value_id']
                            })
                    
                    for node in nodes:
                        node_id = node.get('pg_id')
                        matched_node = node_id in node_ids  # Directly searched by ID
                        matched_property = node_id in property_node_ids
                        matched_property_value = node_id in property_value_node_ids
                        # Check if node label matches text queries
                        matched_text = any(term.lower() in (node.get('label') or '').lower() or 
                                         term.lower() in (node.get('description') or '').lower() 
                                         for term in node_text_queries) if node_text_queries else False
                        
                        if matched_node or matched_text:
                            logger.info(f"🎯 Node {node_id} ({node.get('label')}) matched NODE search")
                        if matched_property:
                            logger.info(f"✓ Node {node_id} ({node.get('label')}) matched PROPERTY filter")
                        if matched_property_value:
                            logger.info(f"✓✓ Node {node_id} ({node.get('label')}) matched PROPERTY VALUE filter")
                            
                        result_data['nodes'].append({
                            'id': str(node_id), # Ensure string ID for frontend
                            'label': node.get('label'),
                            'description': node.get('description'),
                            'group': 'node', # Helper for visualization
                            'matchedNode': matched_node or matched_text,  # Mark nodes that were directly searched
                            'matchedProperty': matched_property,  # Mark nodes that matched property filter
                            'matchedPropertyValue': matched_property_value,  # Mark nodes that matched property value filter
                            'depth': node_depth_map.get(node_id, 0),  # Actual depth from seed nodes
                            'properties': properties_by_node.get(node_id, [])  # Include properties for this node
                        })
                        
                    for edge in edges:
                        edge_id = edge.get('pg_id')
                        matched_edge = edge_id in matched_edge_ids
                        if matched_edge:
                            logger.info(f"🔗 Edge {edge_id} ({edge.get('label')}) matched EDGE search")
                        result_data['edges'].append({
                            'id': str(edge_id),
                            'label': edge.get('label', edge.type),
                            'source': str(edge.start_node.get('pg_id')),
                            'target': str(edge.end_node.get('pg_id')),
                            'matchedEdge': matched_edge  # Mark edges that matched search
                        })
                        
        except Exception as e:
            logger.error(f"Graph search failed: {e}")
        
        logger.info(f"Returning {len(result_data['nodes'])} nodes and {len(result_data['edges'])} edges")
        return result_data
