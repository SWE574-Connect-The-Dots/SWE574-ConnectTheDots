#!/usr/bin/env python
"""
Test Neo4j connection and verify database accessibility.
This script tests the connection to the Neo4j instance before running migration.

Usage:
    python test_neo4j_connection.py
"""

import os
import sys
import django

# Setup Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'backend.settings')
django.setup()

from django.conf import settings
from neo4j import GraphDatabase
from neo4j.exceptions import AuthError, ServiceUnavailable
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def test_neo4j_connection():
    """
    Test connection to Neo4j and verify basic operations.
    """
    print("\n" + "="*70)
    print("🔍 NEO4J CONNECTION TEST")
    print("="*70 + "\n")
    
    # Display connection parameters (hide password)
    print(f"📍 Neo4j URI:      {settings.NEO4J_URI}")
    print(f"👤 Neo4j User:     {settings.NEO4J_USER}")
    print(f"🔑 Neo4j Password: {'*' * len(settings.NEO4J_PASSWORD)}")
    print()
    
    try:
        # Test 1: Create driver
        print("Test 1: Creating Neo4j driver...")
        driver = GraphDatabase.driver(
            settings.NEO4J_URI,
            auth=(settings.NEO4J_USER, settings.NEO4J_PASSWORD),
            encrypted=False,
            connection_timeout=30,  # 30 seconds timeout
            max_connection_lifetime=3600  # 1 hour
        )
        print("  ✅ Driver created successfully")
        
        # Test 2: Verify connectivity
        print("\nTest 2: Verifying connectivity...")
        driver.verify_connectivity()
        print("  ✅ Connection verified successfully")
        
        # Test 3: Run a simple query
        print("\nTest 3: Running test query...")
        with driver.session() as session:
            result = session.run("RETURN 'Hello Neo4j!' as message, timestamp() as time")
            record = result.single()
            print(f"  ✅ Query executed: {record['message']}")
            print(f"     Server time: {record['time']}")
        
        # Test 4: Check database info
        print("\nTest 4: Checking database information...")
        with driver.session() as session:
            # Get node count
            result = session.run("MATCH (n) RETURN count(n) as count")
            node_count = result.single()['count']
            
            # Get relationship count
            result = session.run("MATCH ()-[r]->() RETURN count(r) as count")
            rel_count = result.single()['count']
            
            print(f"  ℹ️  Current nodes in database: {node_count}")
            print(f"  ℹ️  Current relationships in database: {rel_count}")
        
        # Test 5: Check indexes
        print("\nTest 5: Checking existing indexes...")
        with driver.session() as session:
            result = session.run("SHOW INDEXES")
            indexes = list(result)
            if indexes:
                print(f"  ℹ️  Found {len(indexes)} indexes")
                for idx in indexes:
                    print(f"     - {idx.get('name', 'unnamed')}")
            else:
                print("  ℹ️  No indexes found")
        
        # Test 6: Check constraints
        print("\nTest 6: Checking existing constraints...")
        with driver.session() as session:
            result = session.run("SHOW CONSTRAINTS")
            constraints = list(result)
            if constraints:
                print(f"  ℹ️  Found {len(constraints)} constraints")
                for con in constraints:
                    print(f"     - {con.get('name', 'unnamed')}")
            else:
                print("  ℹ️  No constraints found")
        
        # Close driver
        driver.close()
        
        print("\n" + "="*70)
        print("✅ ALL TESTS PASSED - NEO4J IS READY FOR MIGRATION")
        print("="*70 + "\n")
        return True
        
    except AuthError as e:
        print(f"\n❌ AUTHENTICATION ERROR: {e}")
        print("   Check your NEO4J_USER and NEO4J_PASSWORD in .env file")
        return False
        
    except ServiceUnavailable as e:
        print(f"\n❌ CONNECTION ERROR: {e}")
        print("   Possible issues:")
        print("   1. Neo4j server is not running")
        print("   2. Incorrect URI (check NEO4J_URI)")
        print("   3. Network connectivity issues (firewall, security groups)")
        print("   4. Neo4j is not listening on the specified port")
        return False
        
    except Exception as e:
        print(f"\n❌ UNEXPECTED ERROR: {e}")
        import traceback
        traceback.print_exc()
        return False


def test_postgres_connection():
    """
    Test PostgreSQL connection to ensure source database is accessible.
    """
    print("\n" + "="*70)
    print("🔍 POSTGRESQL CONNECTION TEST")
    print("="*70 + "\n")
    
    try:
        from django.db import connection
        
        print("Test 1: Testing PostgreSQL connection...")
        with connection.cursor() as cursor:
            cursor.execute("SELECT version();")
            version = cursor.fetchone()[0]
            print(f"  ✅ Connected to PostgreSQL")
            print(f"     Version: {version[:50]}...")
        
        print("\nTest 2: Checking data counts...")
        from api.models import Space, Node, Edge, Property
        
        space_count = Space.objects.count()
        node_count = Node.objects.count()
        edge_count = Edge.objects.count()
        property_count = Property.objects.count()
        
        print(f"  ℹ️  Spaces:     {space_count}")
        print(f"  ℹ️  Nodes:      {node_count}")
        print(f"  ℹ️  Edges:      {edge_count}")
        print(f"  ℹ️  Properties: {property_count}")
        
        print("\n" + "="*70)
        print("✅ POSTGRESQL IS READY")
        print("="*70 + "\n")
        return True
        
    except Exception as e:
        print(f"\n❌ POSTGRESQL ERROR: {e}")
        return False


if __name__ == '__main__':
    print("\n🚀 Starting connection tests...\n")
    
    postgres_ok = test_postgres_connection()
    neo4j_ok = test_neo4j_connection()
    
    if postgres_ok and neo4j_ok:
        print("🎉 All systems ready! You can proceed with migration.")
        print("   Run: python migrate_to_neo4j.py --clear")
        sys.exit(0)
    else:
        print("⚠️  Please fix connection issues before running migration.")
        sys.exit(1)
