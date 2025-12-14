# Quick Migration Reference

## TL;DR - Just Run This:

### Option 1: Simplest Way
```bash
cd backend
python manage.py migrate_to_neo4j
```

### Option 2: With Django Shell
```bash
cd backend
python manage.py shell < migrate_to_neo4j.py
```

### Option 3: In Docker
```bash
docker compose exec api python manage.py migrate_to_neo4j
```

---

## Common Tasks

### Check if migration will work (verify only)
```bash
python manage.py migrate_to_neo4j --verify-only
```

### Clear Neo4j and start fresh (DANGEROUS!)
```bash
python manage.py migrate_to_neo4j --clear
```

### Check Neo4j connection
```bash
docker compose exec neo4j cypher-shell -u neo4j -p password "RETURN 1"
```

### See what was migrated in Neo4j
```bash
docker compose exec neo4j cypher-shell -u neo4j -p password << EOF
MATCH (s:Space) RETURN s.title, s.pg_id;
MATCH (n:Node) RETURN count(n);
MATCH ()-[r]->() RETURN count(r);
EOF
```

### Stop and restart services
```bash
docker compose down
docker compose up -d
```

---

## What Gets Migrated

| Item | From PostgreSQL | To Neo4j |
|------|-----------------|----------|
| Spaces | `Space` table | `:Space` nodes |
| Nodes | `Node` table | `:Node:Entity` nodes |
| Edges | `Edge` table | Relationships between nodes |
| Properties | `Property` table | Arrays on nodes |
| Edge Props | `EdgeProperty` table | Arrays on relationships |

---

## Data Structure in Neo4j

```
(Space {pg_id: 1, title: "Climate"})
    ↓ [IN_SPACE]
(Node {pg_id: 1, label: "Global Warming"}) 
    → [CAUSES] → 
(Node {pg_id: 2, label: "Ice Melt"})
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Connection refused | `docker compose ps` - check if Neo4j is running |
| Verification fails | Rerun migration, check for errors in output |
| Script hangs | Press Ctrl+C, check Neo4j logs: `docker compose logs neo4j` |
| Want to redo | Run with `--clear` flag (warns you first) |

---

## Performance Estimate

- PostgreSQL read: ~100ms per 1000 records
- Neo4j write: ~50-100ms per 1000 records
- Total time: Usually **< 30 seconds** for typical datasets

---

## Files Created

1. **`migrate_to_neo4j.py`** - Main migration script
2. **`MIGRATION_GUIDE.md`** - Complete documentation
3. **`api/management/commands/migrate_to_neo4j.py`** - Django management command
4. **This file** - Quick reference

---

**Next Steps:**
1. Ensure both databases are running: `docker compose up -d`
2. Run: `python manage.py migrate_to_neo4j`
3. Verify: `python manage.py migrate_to_neo4j --verify-only`
4. Query in Neo4j browser: http://localhost:7474 (if available)

For detailed documentation, see `MIGRATION_GUIDE.md`
