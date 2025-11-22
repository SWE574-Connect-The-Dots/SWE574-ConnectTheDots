#!/bin/bash
# Pre-commit hook to check for Django migration conflicts
# 
# To install this hook:
# 1. Copy this file to .git/hooks/pre-commit
# 2. Make it executable: chmod +x .git/hooks/pre-commit
#
# Or use the automated installer:
# bash infra/install-hooks.sh

echo "Checking for Django migration conflicts..."

# Change to the backend directory
cd backend/ 2>/dev/null || {
    echo "Backend directory not found, skipping migration check"
    exit 0
}

# Check if there are any migration files being committed
MIGRATION_FILES=$(git diff --cached --name-only | grep "migrations/.*\.py$" | grep -v "__init__.py")

if [ -n "$MIGRATION_FILES" ]; then
    echo "Found migration files in commit:"
    echo "$MIGRATION_FILES"
    
    # Check for potential conflicts using our management script
    if command -v python3 >/dev/null 2>&1 && [ -f "manage_migrations.py" ]; then
        python3 manage_migrations.py check
        if [ $? -ne 0 ]; then
            echo ""
            echo "❌ Migration conflicts detected!"
            echo "💡 Manual resolution required:"
            echo "   1. Run: cd backend && python3 manage_migrations.py analyze"
            echo "   2. Follow the manual resolution guidance"
            echo "   3. Coordinate with team member who created conflicting migration"
            echo "   4. Test resolution thoroughly"
            echo "   5. Stage and commit the resolved migration"
            echo ""
            echo "🚫 Commit blocked until conflicts are manually resolved"
            exit 1
        fi
    else
        echo "  Migration management script not available, performing basic checks..."
        
        # Basic check for duplicate migration numbers
        MIGRATION_NUMBERS=$(echo "$MIGRATION_FILES" | sed -E 's/.*\/([0-9]+)_.*/\1/' | sort)
        DUPLICATE_NUMBERS=$(echo "$MIGRATION_NUMBERS" | uniq -d)
        
        if [ -n "$DUPLICATE_NUMBERS" ]; then
            echo " Duplicate migration numbers detected: $DUPLICATE_NUMBERS"
            echo " Resolve conflicts before committing"
            exit 1
        fi
    fi
    
    echo " Migration checks passed"
else
    echo "  No migration files in this commit"
fi

echo " Pre-commit checks completed successfully"
exit 0