#!/usr/bin/env python
"""
Migration script to transfer data from PostgreSQL to Neo4j.
Migrates Spaces, Nodes, Edges, and their Properties.

Usage:
    python manage.py shell < migrate_to_neo4j.py
    or
    python migrate_to_neo4j.py (if run as standalone)
"""

import os
import sys
import django
from django.conf import settings

# Setup Django
if __name__ == '__main__':
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'backend.settings')
    django.setup()

from api.models import Space, Node, Edge, Property, EdgeProperty, User
from api.neo4j_db import Neo4jConnection
from neo4j import GraphDatabase
import logging
from datetime import datetime

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class PostgresToNeo4jMigrator:
    """
    Migrates data from PostgreSQL to Neo4j.
    """

    def __init__(self):
        self.driver = Neo4jConnection.get_driver()
        self.migrated_nodes = 0
        self.migrated_edges = 0
        self.migrated_spaces = 0
        self.errors = []

    def clear_neo4j(self, confirm=False):
        """
        WARNING: Clears all data from Neo4j. Use with caution!
        """
        if not confirm:
            response = input(
                "⚠️  WARNING: This will DELETE ALL data in Neo4j. Continue? (yes/no): "
            )
            if response.lower() != 'yes':
                logger.info("Clearing cancelled.")
                return False

        query = "MATCH (n) DETACH DELETE n"
        try:
            with self.driver.session() as session:
                session.run(query)
            logger.info("✅ Neo4j database cleared successfully.")
            return True
        except Exception as e:
            logger.error(f"❌ Failed to clear Neo4j: {e}")
            return False

    def migrate_users(self):
        """
        Migrate all users from PostgreSQL to Neo4j.
        Includes user profile information.
        """
        logger.info("👤 Starting User migration...")
        users = User.objects.all()

        for user in users:
            try:
                # Get profile if it exists
                profile = None
                try:
                    profile = user.profile
                except:
                    pass

                properties = {
                    'pg_id': user.id,
                    'username': user.username,
                    'email': user.email,
                    'first_name': user.first_name or '',
                    'last_name': user.last_name or '',
                    'is_active': user.is_active,
                    'is_staff': user.is_staff,
                    'is_superuser': user.is_superuser,
                    'date_joined': user.date_joined.isoformat() if user.date_joined else '',
                    'last_login': user.last_login.isoformat() if user.last_login else '',
                }

                # Add profile information if available
                if profile:
                    properties.update({
                        'user_type': profile.user_type,
                        'profession': profile.profession or '',
                        'bio': profile.bio or '',
                        'dob': profile.dob.isoformat() if profile.dob else None,
                        'created_at': profile.created_at.isoformat(),
                        'updated_at': profile.updated_at.isoformat(),
                        'country': profile.country or '',
                        'city': profile.city or '',
                        'latitude': profile.latitude,
                        'longitude': profile.longitude,
                        'location_name': profile.location_name or '',
                        'report_count': profile.report_count,
                        'is_reported': profile.is_reported,
                        'is_archived': profile.is_archived,
                    })

                query = """
                MERGE (u:User {pg_id: $pg_id})
                SET u += $properties
                RETURN u
                """

                with self.driver.session() as session:
                    session.run(query, pg_id=user.id, properties=properties)

                self.migrated_users += 1
                logger.info(
                    f"  ✓ User '{user.username}' (ID: {user.id}) migrated."
                )

            except Exception as e:
                error_msg = f"Failed to migrate User {user.id}: {e}"
                logger.error(f"  ❌ {error_msg}")
                self.errors.append(error_msg)

        logger.info(f"✅ User migration complete. Total: {self.migrated_users}")

    def migrate_spaces(self):
        """
        Migrate all spaces from PostgreSQL to Neo4j.
        """
        logger.info("📦 Starting Space migration...")
        spaces = Space.objects.all()

        for space in spaces:
            try:
                properties = {
                    'pg_id': space.id,
                    'title': space.title,
                    'description': space.description,
                    'created_at': space.created_at.isoformat(),
                    'creator_id': space.creator.id,
                    'creator_username': space.creator.username,
                    'country': space.country or '',
                    'city': space.city or '',
                    'district': space.district or '',
                    'latitude': space.latitude,
                    'longitude': space.longitude,
                    'is_archived': space.is_archived,
                    'report_count': space.report_count,
                    'is_reported': space.is_reported,
                }

                query = """
                MERGE (s:Space {pg_id: $pg_id})
                SET s += $properties
                RETURN s
                """

                with self.driver.session() as session:
                    session.run(query, pg_id=space.id, properties=properties)

                self.migrated_spaces += 1
                logger.info(
                    f"  ✓ Space '{space.title}' (ID: {space.id}) migrated."
                )

            except Exception as e:
                error_msg = f"Failed to migrate Space {space.id}: {e}"
                logger.error(f"  ❌ {error_msg}")
                self.errors.append(error_msg)

        logger.info(f"✅ Space migration complete. Total: {self.migrated_spaces}")

    def migrate_nodes(self):
        """
        Migrate all nodes from PostgreSQL to Neo4j.
        Uses the same create_node logic as current saves.
        """
        logger.info("📍 Starting Node migration...")
        nodes = Node.objects.all()

        for node in nodes:
            try:
                # Build properties dict matching current save logic
                properties = {
                    'description': node.description or '',
                    'wikidata_id': node.wikidata_id or '',
                    'country': node.country or '',
                    'city': node.city or '',
                    'district': node.district or '',
                    'street': node.street or '',
                    'latitude': node.latitude if node.latitude is not None else None,
                    'longitude': node.longitude if node.longitude is not None else None,
                    'location_name': node.location_name or '',
                }

                # Add node properties from Property model (matching API logic)
                node_properties = Property.objects.filter(node=node)
                for prop in node_properties:
                    # Use property label (matching current API logic)
                    key = prop.property_label or prop.property_id
                    value = prop.value_text
                    if key and value:
                        # Sanitize key for Neo4j (same as API: remove non-alphanumeric except underscore)
                        safe_key = "".join(x for x in key if x.isalnum() or x == "_")
                        if safe_key:
                            properties[safe_key] = str(value)

                # Use Neo4jConnection.create_node (same as current save logic)
                Neo4jConnection.create_node(
                    node_id=node.id,
                    label=node.label,
                    space_id=node.space.id,
                    properties=properties
                )

                self.migrated_nodes += 1
                prop_count = node_properties.count()
                logger.info(
                    f"  ✓ Node '{node.label}' (ID: {node.id}) with {prop_count} properties "
                    f"in Space '{node.space.title}' migrated."
                )

            except Exception as e:
                error_msg = f"Failed to migrate Node {node.id}: {e}"
                logger.error(f"  ❌ {error_msg}")
                self.errors.append(error_msg)

        logger.info(f"✅ Node migration complete. Total: {self.migrated_nodes}")

    def migrate_node_properties(self):
        """
        Node properties are now migrated together with nodes as Neo4j properties.
        This method is kept for backward compatibility but does nothing.
        """
        logger.info("ℹ️  Node properties already migrated with nodes (skipping)")

    def migrate_edges(self):
        """
        Migrate all edges (relationships) from PostgreSQL to Neo4j.
        Uses the same create_edge logic as current saves.
        """
        logger.info("🔗 Starting Edge migration...")
        edges = Edge.objects.all()

        for edge in edges:
            try:
                # Use Neo4jConnection.create_edge (same as current save logic)
                Neo4jConnection.create_edge(
                    edge_id=edge.id,
                    source_node_id=edge.source.id,
                    target_node_id=edge.target.id,
                    relation_label=edge.relation_property,
                    properties={'wikidata_property_id': edge.wikidata_property_id or ''}
                )

                self.migrated_edges += 1
                logger.info(
                    f"  ✓ Edge {edge.source.label} -[{edge.relation_property}]-> "
                    f"{edge.target.label} (ID: {edge.id}) migrated."
                )

            except Exception as e:
                error_msg = (
                    f"Failed to migrate Edge {edge.id} "
                    f"({edge.source.id} -> {edge.target.id}): {e}"
                )
                logger.error(f"  ❌ {error_msg}")
                self.errors.append(error_msg)

        logger.info(f"✅ Edge migration complete. Total: {self.migrated_edges}")

    def migrate_edge_properties(self):
        """
        Skip edge properties migration - they are only stored in PostgreSQL.
        """
        logger.info("ℹ️  Edge properties are stored in PostgreSQL only (skipping)")

    def verify_migration(self):
        """
        Verify that the migration was successful by comparing counts.
        """
        logger.info("🔍 Verifying migration...")

        try:
            with self.driver.session() as session:
                spaces_query = "MATCH (s:Space) RETURN count(s) as count"
                nodes_query = "MATCH (n:Node) RETURN count(n) as count"
                edges_query = "MATCH ()-[r]->() RETURN count(r) as count"

                spaces_result = session.run(spaces_query).single()
                nodes_result = session.run(nodes_query).single()
                edges_result = session.run(edges_query).single()

                neo4j_spaces = spaces_result['count'] if spaces_result else 0
                neo4j_nodes = nodes_result['count'] if nodes_result else 0
                neo4j_edges = edges_result['count'] if edges_result else 0

            postgres_spaces = Space.objects.count()
            postgres_nodes = Node.objects.count()
            postgres_edges = Edge.objects.count()

            logger.info("📊 Migration Verification Report:")
            logger.info(f"  Spaces:  PostgreSQL={postgres_spaces}, Neo4j={neo4j_spaces}")
            logger.info(f"  Nodes:   PostgreSQL={postgres_nodes}, Neo4j={neo4j_nodes}")
            logger.info(f"  Edges:   PostgreSQL={postgres_edges}, Neo4j={neo4j_edges}")

            if (postgres_spaces == neo4j_spaces and
                postgres_nodes == neo4j_nodes and
                postgres_edges == neo4j_edges):
                logger.info("✅ Verification PASSED! All data migrated successfully.")
                return True
            else:
                logger.warning("⚠️  Verification FAILED! Counts don't match.")
                return False

        except Exception as e:
            logger.error(f"❌ Verification error: {e}")
            return False

    def print_summary(self):
        """
        Print a summary of the migration.
        """
        logger.info("\n" + "="*60)
        logger.info("📋 MIGRATION SUMMARY")
        logger.info("="*60)
        logger.info(f"Spaces migrated:   {self.migrated_spaces}")
        logger.info(f"Nodes migrated:    {self.migrated_nodes}")
        logger.info(f"Edges migrated:    {self.migrated_edges}")
        logger.info(f"Errors encountered: {len(self.errors)}")

        if self.errors:
            logger.info("\n⚠️  ERRORS:")
            for error in self.errors[:10]:  # Show first 10 errors
                logger.info(f"  - {error}")
            if len(self.errors) > 10:
                logger.info(f"  ... and {len(self.errors) - 10} more errors")

        logger.info("="*60 + "\n")

    def run_full_migration(self, clear_first=False):
        """
        Run the complete migration process.
        """
        logger.info("\n" + "="*60)
        logger.info("🚀 STARTING POSTGRESQL TO NEO4J MIGRATION")
        logger.info("="*60 + "\n")

        start_time = datetime.now()

        # Clear Neo4j if requested
        if clear_first:
            if not self.clear_neo4j():
                logger.error("Migration aborted.")
                return False

        # Run migrations in order (spaces first, then nodes, then edges)
        self.migrate_spaces()
        self.migrate_nodes()
        self.migrate_node_properties()  # Skip - properties in PostgreSQL only
        self.migrate_edges()
        self.migrate_edge_properties()  # Skip - properties in PostgreSQL only

        # Verify and report
        self.verify_migration()
        self.print_summary()

        end_time = datetime.now()
        duration = (end_time - start_time).total_seconds()
        logger.info(f"⏱️  Migration completed in {duration:.2f} seconds\n")

        return True


def main():
    """
    Main entry point for the migration script.
    """
    import argparse

    parser = argparse.ArgumentParser(
        description='Migrate data from PostgreSQL to Neo4j'
    )
    parser.add_argument(
        '--clear',
        action='store_true',
        help='Clear Neo4j before migration (DESTRUCTIVE)'
    )
    parser.add_argument(
        '--verify-only',
        action='store_true',
        help='Only verify existing migration without running migration'
    )

    args = parser.parse_args()

    migrator = PostgresToNeo4jMigrator()

    if args.verify_only:
        logger.info("Running verification only...\n")
        migrator.verify_migration()
    else:
        migrator.run_full_migration(clear_first=args.clear)


if __name__ == '__main__':
    main()
else:
    # When run via Django shell
    migrator = PostgresToNeo4jMigrator()
    migrator.run_full_migration(clear_first=False)
