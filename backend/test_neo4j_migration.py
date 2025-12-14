#!/usr/bin/env python
"""
Test script to verify Neo4j migration and run example queries.

Usage:
    python manage.py shell < test_neo4j_migration.py
    or
    python test_neo4j_migration.py
"""

import os
import sys
import django

if __name__ == '__main__':
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'backend.settings')
    django.setup()

from api.neo4j_db import Neo4jConnection
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class Neo4jTestQueries:
    """
    Test and verify Neo4j migration with example queries.
    """

    def __init__(self):
        self.driver = Neo4jConnection.get_driver()

    def test_connection(self):
        """Test basic Neo4j connection."""
        logger.info("🔌 Testing Neo4j connection...")
        try:
            with self.driver.session() as session:
                result = session.run("RETURN 1 as test")
                value = result.single()[0]
            logger.info("✅ Neo4j connection successful!")
            return True
        except Exception as e:
            logger.error(f"❌ Connection failed: {e}")
            return False

    def count_entities(self):
        """Count entities in Neo4j."""
        logger.info("\n📊 Counting entities...")
        try:
            with self.driver.session() as session:
                spaces = session.run("MATCH (s:Space) RETURN count(s) as count").single()['count']
                nodes = session.run("MATCH (n:Node) RETURN count(n) as count").single()['count']
                edges = session.run("MATCH ()-[r]->() RETURN count(r) as count").single()['count']

            logger.info(f"  Spaces:  {spaces}")
            logger.info(f"  Nodes:   {nodes}")
            logger.info(f"  Edges:   {edges}")
            return {'spaces': spaces, 'nodes': nodes, 'edges': edges}
        except Exception as e:
            logger.error(f"❌ Error counting entities: {e}")
            return None

    def show_spaces(self):
        """Show all spaces."""
        logger.info("\n🗂️  All Spaces:")
        try:
            query = """
            MATCH (s:Space)
            RETURN s.pg_id as id, s.title as title, s.creator_username as creator
            ORDER BY s.title
            """
            with self.driver.session() as session:
                results = session.run(query)
                count = 0
                for record in results:
                    count += 1
                    logger.info(f"  [{record['id']}] {record['title']} (creator: {record['creator']})")
                if count == 0:
                    logger.info("  (No spaces found)")
        except Exception as e:
            logger.error(f"❌ Error fetching spaces: {e}")

    def show_nodes_by_space(self, space_id=None):
        """Show nodes grouped by space."""
        logger.info("\n📍 Nodes by Space:")
        try:
            query = """
            MATCH (s:Space)<-[IN_SPACE]-(n:Node)
            RETURN s.title as space, count(n) as node_count
            ORDER BY space
            """
            with self.driver.session() as session:
                results = session.run(query)
                count = 0
                for record in results:
                    count += 1
                    logger.info(f"  {record['space']}: {record['node_count']} nodes")
                if count == 0:
                    logger.info("  (No nodes found)")
        except Exception as e:
            logger.error(f"❌ Error fetching nodes: {e}")

    def show_sample_nodes(self, limit=5):
        """Show sample nodes with properties."""
        logger.info(f"\n🔍 Sample Nodes (first {limit}):")
        try:
            query = f"""
            MATCH (n:Node)
            RETURN n.pg_id as id, n.label as label, n.description as description
            LIMIT {limit}
            """
            with self.driver.session() as session:
                results = session.run(query)
                count = 0
                for record in results:
                    count += 1
                    desc = (record['description'][:50] + "...") if record['description'] else "No description"
                    logger.info(f"  [{record['id']}] {record['label']}: {desc}")
                if count == 0:
                    logger.info("  (No nodes found)")
        except Exception as e:
            logger.error(f"❌ Error fetching sample nodes: {e}")

    def show_relationships(self, limit=10):
        """Show sample relationships between nodes."""
        logger.info(f"\n🔗 Sample Relationships (first {limit}):")
        try:
            query = f"""
            MATCH (source:Node)-[r]->(target:Node)
            RETURN source.label as source, type(r) as relation, target.label as target
            LIMIT {limit}
            """
            with self.driver.session() as session:
                results = session.run(query)
                count = 0
                for record in results:
                    count += 1
                    logger.info(f"  {record['source']} -[{record['relation']}]-> {record['target']}")
                if count == 0:
                    logger.info("  (No relationships found)")
        except Exception as e:
            logger.error(f"❌ Error fetching relationships: {e}")

    def show_connection_analysis(self):
        """Show connection analysis - which nodes have the most connections."""
        logger.info("\n🌐 Connection Analysis (top connected nodes):")
        try:
            query = """
            MATCH (n:Node)
            WITH n, (size((n)-[]-())) as connection_count
            RETURN n.label as label, connection_count
            ORDER BY connection_count DESC
            LIMIT 5
            """
            with self.driver.session() as session:
                results = session.run(query)
                count = 0
                for record in results:
                    count += 1
                    logger.info(f"  {record['label']}: {record['connection_count']} connections")
                if count == 0:
                    logger.info("  (No nodes found)")
        except Exception as e:
            logger.error(f"❌ Error in connection analysis: {e}")

    def show_wikidata_usage(self):
        """Show Wikidata IDs usage."""
        logger.info("\n🔗 Wikidata Usage:")
        try:
            query = """
            MATCH (n:Node)
            WHERE n.wikidata_id IS NOT NULL AND n.wikidata_id <> ''
            RETURN count(n) as nodes_with_wikidata, 
                   count(DISTINCT n.wikidata_id) as unique_wikidata_ids
            """
            with self.driver.session() as session:
                result = session.run(query).single()
                logger.info(f"  Nodes with Wikidata IDs: {result['nodes_with_wikidata']}")
                logger.info(f"  Unique Wikidata IDs: {result['unique_wikidata_ids']}")
        except Exception as e:
            logger.error(f"❌ Error fetching Wikidata usage: {e}")

    def test_cypher_query(self):
        """Test custom Cypher query."""
        logger.info("\n🧪 Custom Query Test:")
        logger.info("Testing: Match all IN_SPACE relationships")
        try:
            query = "MATCH (n:Node)-[r:IN_SPACE]->(s:Space) RETURN count(r) as count"
            with self.driver.session() as session:
                result = session.run(query).single()
                count = result['count']
            logger.info(f"  ✅ Found {count} nodes in spaces")
        except Exception as e:
            logger.error(f"  ❌ Query failed: {e}")

    def run_all_tests(self):
        """Run all test queries."""
        logger.info("\n" + "="*60)
        logger.info("🧪 NEO4J MIGRATION TEST SUITE")
        logger.info("="*60)

        if not self.test_connection():
            logger.error("Cannot proceed - connection failed")
            return False

        self.count_entities()
        self.show_spaces()
        self.show_nodes_by_space()
        self.show_sample_nodes()
        self.show_relationships()
        self.show_connection_analysis()
        self.show_wikidata_usage()
        self.test_cypher_query()

        logger.info("\n" + "="*60)
        logger.info("✅ Test suite completed!")
        logger.info("="*60 + "\n")

        return True


def main():
    """Main entry point."""
    import argparse

    parser = argparse.ArgumentParser(
        description='Test Neo4j migration with example queries'
    )
    parser.add_argument(
        '--quick',
        action='store_true',
        help='Run only connection test and counts'
    )

    args = parser.parse_args()

    tester = Neo4jTestQueries()

    if args.quick:
        if tester.test_connection():
            tester.count_entities()
    else:
        tester.run_all_tests()


if __name__ == '__main__':
    main()
else:
    # When run via Django shell
    tester = Neo4jTestQueries()
    tester.run_all_tests()
