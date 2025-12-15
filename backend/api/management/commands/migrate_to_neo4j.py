from django.core.management.base import BaseCommand
from api.models import Space, Node, Edge, Property, EdgeProperty
from api.neo4j_db import Neo4jConnection
import logging
from datetime import datetime

logger = logging.getLogger(__name__)


class Command(BaseCommand):
    help = 'Migrate data from PostgreSQL to Neo4j database'

    def add_arguments(self, parser):
        parser.add_argument(
            '--clear',
            action='store_true',
            help='Clear Neo4j before migration (DESTRUCTIVE - requires confirmation)',
        )
        parser.add_argument(
            '--verify-only',
            action='store_true',
            help='Only verify existing migration without running migration',
        )

    def handle(self, *args, **options):
        from migrate_to_neo4j import PostgresToNeo4jMigrator

        migrator = PostgresToNeo4jMigrator()

        if options['verify_only']:
            self.stdout.write(
                self.style.SUCCESS('🔍 Running verification only...\n')
            )
            migrator.verify_migration()
        else:
            migrator.run_full_migration(clear_first=options['clear'])
            self.stdout.write(
                self.style.SUCCESS(
                    '\n✅ Migration completed successfully!'
                )
            )
