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
        
        query = """
        // 1. Find matching nodes (by ID or text search)
        OPTIONAL MATCH (n:Node {space_id: $space_id})
        WHERE (size($node_ids) > 0 AND any(id IN $node_ids WHERE n.pg_id = id)) OR
              (size($node_text_queries) > 0 AND any(term IN $node_text_queries WHERE n.label CONTAINS term OR n.description CONTAINS term)) OR
              (size($property_value_node_ids) > 0 AND any(id IN $property_value_node_ids WHERE n.pg_id = id)) OR
              (size($property_value_node_ids) = 0 AND size($property_node_ids) > 0 AND any(id IN $property_node_ids WHERE n.pg_id = id))
        WITH collect(n) as matchingNodes
        
        // 2. Find matching edges
        OPTIONAL MATCH (source:Node {space_id: $space_id})-[r]->(target:Node {space_id: $space_id})
        WHERE size($edge_queries) > 0 AND 
              any(term IN $edge_queries WHERE type(r) CONTAINS term OR r.label CONTAINS term)
        WITH matchingNodes, collect(r) as matchingEdges, collect(source) + collect(target) as edgeEndpointNodes
        
        // 3. Combine to get seed nodes
        WITH matchingNodes + edgeEndpointNodes as initialNodes
        UNWIND initialNodes as n
        WITH collect(DISTINCT n) as seedNodes
        
        // 4. Expand and collect paths (up to depth)
        UNWIND seedNodes as seed
        MATCH path = (seed)-[*0..""" + str(depth) + """]-(neighbor:Node {space_id: $space_id})
        WHERE none(r IN relationships(path) WHERE type(r) = 'IN_SPACE')
        WITH path, $property_value_node_ids as propValNodeIds
        LIMIT 5000
        WITH collect(path) as paths, propValNodeIds
        
        // 5. Extract unique nodes with matched property value flag
        UNWIND paths as p
        UNWIND nodes(p) as n
        WITH collect(DISTINCT n) as uniqueNodes, paths, propValNodeIds
        
        // 6. Extract unique edges (handling empty relationships safely)
        UNWIND paths as p
        UNWIND (CASE WHEN size(relationships(p)) > 0 THEN relationships(p) ELSE [null] END) as r
        WITH uniqueNodes, collect(DISTINCT r) as uniqueRels, propValNodeIds
        
        // 7. Clean up nulls from edges
        WITH uniqueNodes, [r IN uniqueRels WHERE r IS NOT NULL] as finalEdges, propValNodeIds
        
        RETURN uniqueNodes, finalEdges as allEdges, propValNodeIds
        """
        
        result_data = {'nodes': [], 'edges': []}
        
        # Convert queries to lists if they're strings
        if node_queries is None:
            node_queries = []
        elif isinstance(node_queries, str):
            node_queries = [q.strip() for q in node_queries.split(',') if q.strip()]
        
        # Separate node IDs and text queries
        node_ids = []
        node_text_queries = []
        for query_item in node_queries:
            try:
                # Try to convert to int (it's a node ID from pg_id)
                node_id = int(query_item)
                node_ids.append(node_id)
            except (ValueError, TypeError):
                # It's a text search term
                node_text_queries.append(query_item)
            
        if edge_queries is None:
            edge_queries = []
        elif isinstance(edge_queries, str):
            edge_queries = [q.strip() for q in edge_queries.split(',') if q.strip()]
        
        logger.info(f"Graph search called with space_id={space_id}, node_ids={node_ids}, node_text_queries={node_text_queries}, edge_queries={edge_queries}, property_queries={property_queries}, property_node_ids={property_node_ids}, property_values={property_values}, property_value_node_ids={property_value_node_ids}")
        
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
                    edges = record.get('allEdges', [])
                    prop_val_node_ids = record.get('propValNodeIds', [])
                    logger.info(f"Graph search found {len(nodes)} nodes and {len(edges)} edges")
                    logger.info(f"Property node IDs (has property): {property_node_ids}")
                    logger.info(f"Property value node IDs (has property value): {property_value_node_ids}")
                    logger.info(f"Property value node IDs from query: {prop_val_node_ids}")
                    
                    for node in nodes:
                        node_id = node.get('pg_id')
                        matched_property = node_id in property_node_ids
                        matched_property_value = node_id in property_value_node_ids
                        
                        if matched_property:
                            logger.info(f"✓ Node {node_id} ({node.get('label')}) matched PROPERTY filter")
                        if matched_property_value:
                            logger.info(f"✓✓ Node {node_id} ({node.get('label')}) matched PROPERTY VALUE filter")
                            
                        result_data['nodes'].append({
                            'id': str(node_id), # Ensure string ID for frontend
                            'label': node.get('label'),
                            'description': node.get('description'),
                            'group': 'node', # Helper for visualization
                            'matchedProperty': matched_property,  # Mark nodes that matched property filter
                            'matchedPropertyValue': matched_property_value  # Mark nodes that matched property value filter
                        })
                        
                    for edge in edges:
                        result_data['edges'].append({
                            'id': str(edge.get('pg_id')),
                            'label': edge.get('label', edge.type),
                            'source': str(edge.start_node.get('pg_id')),
                            'target': str(edge.end_node.get('pg_id'))
                        })
                        
        except Exception as e:
            logger.error(f"Graph search failed: {e}")
        
        logger.info(f"Returning {len(result_data['nodes'])} nodes and {len(result_data['edges'])} edges")
        return result_data
