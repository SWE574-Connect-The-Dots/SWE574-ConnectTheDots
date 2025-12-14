# PostgreSQL to Neo4j Migration Guide

## Overview

This script migrates all data from your PostgreSQL database (`mydb`) to Neo4j, including:
- **Spaces** - Graph spaces containing nodes and edges
- **Nodes** - Individual entities with their properties
- **Edges** - Relationships between nodes with their properties
- **Properties** - All node and edge properties

## Prerequisites

Ensure you have:
1. ✅ PostgreSQL running with data in the `mydb` database
2. ✅ Neo4j running and accessible at `bolt://neo4j:7687`
3. ✅ Neo4j credentials configured in `.env`:
   ```
   NEO4J_URI=bolt://neo4j:7687
   NEO4J_USER=neo4j
   NEO4J_PASSWORD=password
   ```
4. ✅ All backend dependencies installed

## Usage

### Method 1: Using Django Management Shell (Recommended)

```bash
cd backend
python manage.py shell < migrate_to_neo4j.py
```

### Method 2: As Standalone Script

```bash
cd backend
python migrate_to_neo4j.py [OPTIONS]
```

### Method 3: With Docker

```bash
# If running in Docker
docker compose exec api python manage.py shell < migrate_to_neo4j.py
```

## Options

When running as standalone:

```bash
# Clear Neo4j before migration (WARNING: DESTRUCTIVE)
python migrate_to_neo4j.py --clear

# Only verify existing migration without running it
python migrate_to_neo4j.py --verify-only
```

## What Gets Migrated

### 1. Spaces
- Title, description, location data
- Creator information
- Tags and metadata
- Report counts and archive status

### 2. Nodes
- Label and Wikidata ID
- Location information (coordinates, city, country)
- Description
- Creator and creation timestamp
- Archive and report status
- Linked to their parent Space via `IN_SPACE` relationship

### 3. Node Properties
- All custom properties attached to nodes
- Stored as arrays on the node

### 4. Edges (Relationships)
- Source and target node connections
- Relation type (dynamically created as Neo4j relationship labels)
- Wikidata property ID
- Creation timestamp

### 5. Edge Properties
- All custom properties attached to edges
- Stored as arrays on the relationship

## Database Schema in Neo4j

After migration, your Neo4j database will have:

```
(Space) 
├── [IN_SPACE] → (Node)
│   ├── [RELATED_TO|custom_relation] → (Node)
│   ├── properties: []
│   └── {pg_id, label, wikidata_id, ...}
├── properties: []
└── {pg_id, title, creator_username, ...}
```

### Node Labels
- `Space` - Represents a graph space
- `Node` - Represents an entity node
- `Entity` - Additional label for all nodes

### Relationship Types
- `IN_SPACE` - Links nodes to their parent space
- Custom relations - Created from PostgreSQL edge relation_property field

## Example Queries

After migration, you can run Neo4j queries:

```cypher
# Find all nodes in a space
MATCH (s:Space {pg_id: 1})<-[IN_SPACE]-(n:Node)
RETURN n.label, n.description

# Find connections between nodes
MATCH (source:Node)-[r]->(target:Node)
WHERE source.pg_id = 123
RETURN type(r) as relationship, target.label

# Count entities by space
MATCH (s:Space)<-[IN_SPACE]-(n:Node)
RETURN s.title, count(n) as node_count
ORDER BY node_count DESC

# Find nodes with specific properties
MATCH (n:Node)
WHERE ANY(prop IN n.properties WHERE prop.property_id = "P31")
RETURN n.label, n.properties
```

## Monitoring Progress

The script provides real-time logging:
- ✓ Shows each space, node, and edge as it's migrated
- ❌ Reports any errors with details
- 📊 Provides verification report comparing counts
- ⏱️ Shows total migration time

## Example Output

```
============================================================
🚀 STARTING POSTGRESQL TO NEO4J MIGRATION
============================================================

📦 Starting Space migration...
  ✓ Space 'Climate Science' (ID: 1) migrated.
  ✓ Space 'Historical Events' (ID: 2) migrated.
✅ Space migration complete. Total: 2

📍 Starting Node migration...
  ✓ Node 'Global Warming' (ID: 1) in Space 'Climate Science' migrated.
  ✓ Node 'Ice Age' (ID: 2) in Space 'Climate Science' migrated.
✅ Node migration complete. Total: 2

🔗 Starting Edge migration...
  ✓ Edge Global Warming -[CAUSED_BY]-> Ice Age (ID: 1) migrated.
✅ Edge migration complete. Total: 1

🔍 Verifying migration...
📊 Migration Verification Report:
  Spaces:  PostgreSQL=2, Neo4j=2
  Nodes:   PostgreSQL=2, Neo4j=2
  Edges:   PostgreSQL=1, Neo4j=1
✅ Verification PASSED! All data migrated successfully.

⏱️ Migration completed in 3.45 seconds
```

## Troubleshooting

### Issue: "Neo4j connection refused"
- Ensure Neo4j container is running: `docker compose ps`
- Check credentials in `.env`
- Verify `NEO4J_URI` is correct

### Issue: "Property node not found"
- This is expected if not all edges reference existing nodes
- Check for orphaned nodes in PostgreSQL

### Issue: "Script hangs"
- Press `Ctrl+C` to cancel
- Check Neo4j logs: `docker compose logs neo4j`
- Verify database connectivity

### Issue: "Counts don't match after migration"
- Review error messages in the output
- Rerun with `--verify-only` to check
- Check Neo4j logs for constraint violations

## Rollback

To rollback the migration:

1. **In Neo4j** - Clear all data:
   ```cypher
   MATCH (n) DETACH DELETE n
   ```

2. **In PostgreSQL** - Data remains unchanged
   (Only reads from PostgreSQL, doesn't modify it)

## Performance Notes

Migration time depends on:
- Number of spaces, nodes, and edges
- Database performance
- Network latency (if remote Neo4j)

Typical performance:
- 100 spaces: ~2-3 seconds
- 1000 nodes: ~5-10 seconds
- 10000 edges: ~15-30 seconds

## Safety Considerations

✅ **Safe**: The script is read-only from PostgreSQL
❌ **Not Safe**: Use `--clear` only if you want to empty Neo4j
⚠️ **Verify**: Always run `--verify-only` after migration to confirm

## Advanced Options

### Parallel Execution (Future Enhancement)
For large datasets, batch migration in parallel:

```python
# Modify migrate_nodes() with batch processing
BATCH_SIZE = 1000
for batch in chunks(nodes, BATCH_SIZE):
    # Process batch in parallel
```

### Custom Mapping
To customize property mapping, edit the property dictionaries in:
- `migrate_spaces()` - Space properties
- `migrate_nodes()` - Node properties
- `migrate_edges()` - Edge properties

## Support

If you encounter issues:
1. Check the error messages in the log output
2. Verify connectivity: `docker compose exec neo4j cypher-shell -u neo4j -p password`
3. Review Neo4j logs: `docker compose logs neo4j`
4. Ensure all models are correctly imported

---

**Last Updated**: December 2025
**Version**: 1.0
