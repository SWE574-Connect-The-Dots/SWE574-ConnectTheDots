"""
Manual Migration Conflict Detection and Resolution Guidance Script

This script helps detect migration conflicts and provides manual resolution guidance
in collaborative Django development environments. All conflicts must be resolved manually
to ensure team coordination and understanding.

PHILOSOPHY: Manual resolution only - no automatic fixes.
"""

import os
import sys
import subprocess
import re
from pathlib import Path

def run_command(command, cwd=None):
    """Run a shell command and return the result."""
    try:
        result = subprocess.run(
            command, 
            shell=True, 
            capture_output=True, 
            text=True, 
            cwd=cwd
        )
        return result.returncode == 0, result.stdout, result.stderr
    except Exception as e:
        return False, "", str(e)

def get_migration_files(app_name):
    """Get all migration files for a given app."""
    migrations_dir = Path(f"{app_name}/migrations")
    if not migrations_dir.exists():
        return []
    
    migration_files = []
    for file in migrations_dir.glob("*.py"):
        if file.name != "__init__.py" and not file.name.startswith("__pycache__"):
            migration_files.append(file.name)
    
    return sorted(migration_files)

def check_migration_conflicts():
    """Check for potential migration conflicts."""
    print("Checking for migration conflicts...")
    
    # Check if there are uncommitted migration files
    success, stdout, stderr = run_command("git status --porcelain")
    if success and stdout.strip():
        uncommitted_migrations = []
        for line in stdout.strip().split('\n'):
            if line and 'migrations/' in line and '.py' in line:
                uncommitted_migrations.append(line.strip())
        
        if uncommitted_migrations:
            print("Found uncommitted migration files:")
            for migration in uncommitted_migrations:
                print(f"   {migration}")
    
    # Check for duplicate migration numbers
    migrations = get_migration_files("api")
    migration_numbers = {}
    
    for migration in migrations:
        match = re.match(r'(\d+)_', migration)
        if match:
            num = int(match.group(1))
            if num not in migration_numbers:
                migration_numbers[num] = []
            migration_numbers[num].append(migration)
    
    conflicts = {num: files for num, files in migration_numbers.items() if len(files) > 1}
    
    if conflicts:
        print(f"Duplicate migration numbers detected:")
        for num, files in conflicts.items():
            print(f"   Migration {num:04d}:")
            for file in files:
                print(f"     • {file}")
        print("\nManual resolution required:")
        print("   1. Run: python3 manage_migrations.py analyze")
        print("   2. Follow the manual resolution guidance")
        print("   3. Coordinate with team member who created conflicting migration")
        return True
    else:
        print("No migration conflicts detected")
        return False

def provide_manual_resolution_guidance():
    """Provide guidance for manual migration conflict resolution."""
    print("Manual Migration Conflict Resolution Guide")
    print("=" * 50)
    
    # Show conflicting migrations
    print("\n Analyzing conflicts...")
    migrations = get_migration_files("api")
    migration_numbers = {}
    
    for migration in migrations:
        match = re.match(r'(\d+)_', migration)
        if match:
            num = int(match.group(1))
            if num not in migration_numbers:
                migration_numbers[num] = []
            migration_numbers[num].append(migration)
    
    conflicts = {num: files for num, files in migration_numbers.items() if len(files) > 1}
    
    if not conflicts:
        print("No migration conflicts detected")
        return False
    
    print(f"Found {len(conflicts)} conflict(s) requiring manual resolution:")
    
    for num, files in conflicts.items():
        print(f"\n Migration {num:04d} conflicts:")
        for file in files:
            print(f"   • {file}")
    
    print("\n MANUAL RESOLUTION OPTIONS:")
    print()
    print("1 RENAME AND SEQUENCE:")
    print("   • Best for: Independent schema changes that don't conflict")
    print("   • Steps:")
    print("     1. Choose which migration should remain as current number")
    print("     2. Rename the other to next available number (e.g., 0012)")
    print("     3. Update dependencies in the renamed file:")
    print("        dependencies = [('api', '0011_first_migration')]")
    print("     4. Test both migrations in sequence")
    print()
    print("2 DELETE AND RECREATE:")
    print("   • Best for: Simple, non-conflicting changes")
    print("   • Steps:")
    print("     1. Delete BOTH conflicting migration files")
    print("     2. Make your model changes again")
    print("     3. Run: python3 manage.py makemigrations")
    print("     4. This creates a single migration with all changes")
    print("     5. Test the new combined migration")
    print()
    print("3 MANUAL COMBINATION:")
    print("   • Best for: Complex changes needing specific order")
    print("   • Steps:")
    print("     1. Create new migration: python3 manage.py makemigrations --empty api --name combined_changes")
    print("     2. Manually edit the new migration to include operations from both files")
    print("     3. Delete the original conflicting files")
    print("     4. Test the combined migration thoroughly")
    print()
    print("  COORDINATION REQUIRED:")
    print("   • Contact the team member who created the conflicting migration")
    print("   • Discuss and agree on the resolution approach")
    print("   • Ensure both developers understand the chosen solution")
    print("   • Test on clean database before committing")
    print("   • Document the resolution approach in commit message")
    print()
    print(" WHAT WE DON'T DO:")
    print("   • Automatic merge migrations (can hide real conflicts)")
    print("   • Silent conflict resolution (team needs to understand changes)")
    print("   • Bypass team coordination (communication is essential)")
    
    return True

def analyze_conflict_complexity():
    """Analyze migration conflicts and provide manual resolution guidance."""
    print("Analyzing migration conflicts for manual resolution...")
    
    migrations = get_migration_files("api")
    migration_numbers = {}
    
    # Group migrations by number
    for migration in migrations:
        match = re.match(r'(\d+)_', migration)
        if match:
            num = int(match.group(1))
            if num not in migration_numbers:
                migration_numbers[num] = []
            migration_numbers[num].append(migration)
    
    conflicts = {num: files for num, files in migration_numbers.items() if len(files) > 1}
    
    if not conflicts:
        print(" No migration conflicts detected")
        return
    
    print(f" Found {len(conflicts)} conflict(s) requiring manual resolution:")
    
    for num, files in conflicts.items():
        print(f"\n Migration {num:04d} conflicts:")
        for file in files:
            print(f"   • {file}")
        
        # Analyze each conflicting migration
        complexity_factors = []
        
        for file in files:
            try:
                with open(f"api/migrations/{file}", 'r') as f:
                    content = f.read()
                    
                # Check for complexity factors
                if 'RunPython' in content:
                    complexity_factors.append("Data migration (RunPython)")
                if 'RunSQL' in content:
                    complexity_factors.append("Custom SQL (RunSQL)")
                if 'RenameField' in content:
                    complexity_factors.append("Field rename")
                if 'RenameModel' in content:
                    complexity_factors.append("Model rename")
                if 'AlterField' in content:
                    complexity_factors.append("Field modification")
                if 'DeleteField' in content:
                    complexity_factors.append("Field deletion")
                if 'DeleteModel' in content:
                    complexity_factors.append("Model deletion")
                if 'CreateModel' in content:
                    complexity_factors.append("Model creation")
                if 'AddField' in content:
                    complexity_factors.append("Field addition")
                    
            except Exception as e:
                print(f" Could not analyze {file}: {e}")
        
        # Provide manual resolution guidance
        print(f"\n Manual Resolution Guidance for migration {num:04d}:")
        
        if not complexity_factors:
            print("    COMPLEXITY: Low")
            print("    SUGGESTED APPROACH: Rename and sequence")
            print("      1. Keep first migration as is")
            print("      2. Rename second to next number (e.g., 0012)")
            print("      3. Update dependencies in renamed file")
        elif len(complexity_factors) <= 3 and not any('Data migration' in factor or 'Custom SQL' in factor for factor in complexity_factors):
            print("    COMPLEXITY: Medium")
            print("    SUGGESTED APPROACH: Delete and recreate OR manual combination")
            print("      1. Consider combining operations into single migration")
            print("      2. Test thoroughly due to schema complexity")
        else:
            print("    COMPLEXITY: High")
            print("    SUGGESTED APPROACH: Coordinate with team member")
            print("      1. Discuss with other developer before resolving")
            print("      2. Consider data migration order and dependencies")
            print("      3. Test extensively on development data")
        
        if complexity_factors:
            print("    Complexity factors detected:")
            for factor in set(complexity_factors):
                print(f"      • {factor}")
        
        print("   " + "="*50)

def explain_manual_resolution():
    """Explain manual migration conflict resolution approaches."""
    print(" Manual Migration Conflict Resolution")
    print("=" * 50)
    print()
    print(" PHILOSOPHY:")
    print("   Manual resolution gives full control over migration conflicts")
    print("   Team coordination ensures everyone understands changes")
    print("   Prevents unexpected automated merge issues")
    print()
    print(" RESOLUTION STRATEGIES:")
    print()
    print("1 RENAME AND SEQUENCE:")
    print("   • Best for: Independent schema changes")
    print("   • Process: Rename one migration, update dependencies")
    print("   • Result: Two sequential migrations")
    print("   • Example: 0011_add_profile.py → 0012_add_profile.py")
    print()
    print("2 DELETE AND RECREATE:")
    print("   • Best for: Simple, non-conflicting changes")
    print("   • Process: Delete both, remake as single migration")
    print("   • Result: One migration with all changes")
    print("   • Benefit: Cleaner migration history")
    print()
    print("3 MANUAL COMBINATION:")
    print("   • Best for: Complex changes needing specific order")
    print("   • Process: Create new migration, manually combine operations")
    print("   • Result: Carefully crafted single migration")
    print("   • Requires: Deep understanding of operations")
    print()
    print("  CRITICAL STEPS:")
    print("   1. Communicate with team member who created other migration")
    print("   2. Understand what each migration does")
    print("   3. Choose appropriate resolution strategy")
    print("   4. Test thoroughly on clean database")
    print("   5. Document resolution in commit message")
    print()
    print(" WHAT WE DON'T USE:")
    print("   • Automatic merge migrations (can hide conflicts)")
    print("   • Squash migrations (too risky for team environments)")
    print("   • Silent resolution (team communication is essential)")

def main():
    """Main function to handle different operations."""
    if len(sys.argv) < 2:
        print("Usage: python3 manage_migrations.py [check|guide|explain|analyze]")
        print("  check    - Check for migration conflicts")
        print("  guide    - Provide manual resolution guidance for conflicts")  
        print("  explain  - Explain manual resolution approaches")
        print("  analyze  - Analyze conflicts and provide specific guidance")
        return
    
    operation = sys.argv[1].lower()
    
    if operation == "check":
        check_migration_conflicts()
    elif operation == "guide":
        provide_manual_resolution_guidance()
    elif operation == "explain":
        explain_manual_resolution()
    elif operation == "analyze":
        analyze_conflict_complexity()
    else:
        print(f"Unknown operation: {operation}")
        print("Available operations: check, guide, explain, analyze")
        print("Run 'python3 manage_migrations.py explain' to understand manual resolution approaches")

if __name__ == "__main__":
    main()
