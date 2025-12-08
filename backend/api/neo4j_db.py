from neo4j import GraphDatabase
from django.conf import settings
import logging

logger = logging.getLogger(__name__)

class Neo4jConnection:
    _driver = None

    @classmethod
    def get_driver(cls):
        if cls._driver is None:
            cls._driver = GraphDatabase.driver(
                settings.NEO4J_URI,
                auth=(settings.NEO4J_USER, settings.NEO4J_PASSWORD)
            )
        return cls._driver

    @classmethod
    def close(cls):
        if cls._driver:
            cls._driver.close()

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
        
        
        safe_label = "".join(x for x in relation_label if x.isalnum() or x == "_")
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
