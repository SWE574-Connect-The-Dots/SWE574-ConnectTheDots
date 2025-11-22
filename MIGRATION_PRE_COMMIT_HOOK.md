# Git Hooks Usage Guide

This guide explains how to use the Git hooks provided for Django migration conflict prevention in the SWE574-ConnectTheDots project.

## Overview

We provide two Git hook scripts to help prevent migration conflicts before they happen:

- **`install-hooks.sh`** - Easy installation script for Git hooks
- **`pre-commit-hook.sh`** - Pre-commit hook that validates migrations

## Quick Start

### Install Git Hooks (Recommended for All Team Members)

```bash
# From the project root directory
bash infra/install-hooks.sh
```

That's it! The hook is now active and will check your commits automatically.

## Files Explained

### `infra/install-hooks.sh`

**Purpose:** Automates the installation of Git hooks for the project.

**What it does:**
- Creates `.git/hooks/` directory if it doesn't exist
- Copies `pre-commit-hook.sh` to `.git/hooks/pre-commit`
- Makes the hook executable
- Provides installation confirmation and usage instructions

**Usage:**
```bash
bash infra/install-hooks.sh
```

**Output example:**
```
Installing Git hooks for SWE574-ConnectTheDots...
Pre-commit hook installed successfully

The hook will now:
  • Check for migration conflicts before each commit
  • Block commits with conflicting migrations
  • Provide guidance on resolving conflicts

To disable the hook temporarily:
  git commit --no-verify

To uninstall:
  rm .git/hooks/pre-commit
```

### `infra/pre-commit-hook.sh`

**Purpose:** Validates Django migrations before allowing commits to proceed.

**What it does:**
1. Checks if migration files are being committed
2. Runs migration conflict detection using `manage_migrations.py`
3. Performs basic duplicate number checks as fallback
4. Blocks commits if conflicts are detected
5. Provides clear instructions for resolution

**Automatic triggers:**
- Runs on every `git commit`
- Only activates when migration files are being committed
- Skips check if no migration files are in the commit

## Usage Scenarios

### Scenario 1: Normal Development (No Migration Files)

```bash
git add src/components/NewComponent.jsx
git commit -m "Add new component"
```

**Result:**
```
Checking for Django migration conflicts...
No migration files in this commit
Pre-commit checks completed successfully
```

### Scenario 2: Committing New Migrations (No Conflicts)

```bash
# After making model changes
python manage.py makemigrations
git add backend/api/migrations/0011_add_user_profile.py
git commit -m "Add user profile migration"
```

**Result:**
```
Checking for Django migration conflicts...
Found migration files in commit:
backend/api/migrations/0011_add_user_profile.py
Migration checks passed
Pre-commit checks completed successfully
```

### Scenario 3: Conflict Detected (Commit Blocked)

```bash
git add backend/api/migrations/0011_duplicate_number.py
git commit -m "Add conflicting migration"
```

**Result:**
```
Checking for Django migration conflicts...
Found migration files in commit:
backend/api/migrations/0011_duplicate_number.py
Duplicate migration numbers detected: 0011
Manual resolution required:
   1. Run: cd backend && python3 manage_migrations.py analyze
   2. Follow the manual resolution guidance
   3. Coordinate with team member who created conflicting migration

Commit blocked until conflicts are manually resolved
```

## Management Commands

### Temporarily Disable Hook

If you need to commit despite conflicts (not recommended):

```bash
git commit --no-verify -m "Emergency commit bypassing hooks"
```

### Check Hook Status

```bash
# Check if hook is installed
ls -la .git/hooks/pre-commit

# View hook content
cat .git/hooks/pre-commit
```

### Uninstall Hook

```bash
rm .git/hooks/pre-commit
```

### Reinstall Hook

```bash
bash infra/install-hooks.sh
```

## Conflict Resolution Workflow

When the hook detects conflicts:

### Step 1: Understand the Problem
```bash
# Check what migrations exist
ls backend/api/migrations/

# Use our management script
cd backend/
python3 manage_migrations.py check
```

### Step 2: Resolve Conflicts Manually
```bash
# Get detailed analysis and guidance
python3 manage_migrations.py analyze

# Get general guidance
python3 manage_migrations.py guide

# Choose resolution approach:
# Option 1: Rename and sequence
# Option 2: Delete and recreate  
# Option 3: Manual combination
```

### Step 3: Test the Resolution
```bash
# Test migration application
python3 manage.py migrate --check
python3 manage.py migrate --plan
```

### Step 4: Commit the Resolution
```bash
git add backend/api/migrations/
git commit -m "Resolve migration conflicts"
```

## Best Practices

### For Individual Developers

1. **Always install hooks when joining the project:**
   ```bash
   bash infra/install-hooks.sh
   ```

2. **Pull latest changes before creating migrations:**
   ```bash
   git pull origin develop
   python3 manage.py makemigrations
   ```

3. **Test migrations before committing:**
   ```bash
   python3 manage.py migrate
   python3 manage.py migrate api XXXX  # rollback test
   python3 manage.py migrate  # reapply
   ```

## Troubleshooting

### Hook Not Running

**Problem:** Commits go through without migration checks.

**Solutions:**
```bash
# Check if hook exists and is executable
ls -la .git/hooks/pre-commit

# Reinstall if missing
bash infra/install-hooks.sh

# Check for typos in hook file
cat .git/hooks/pre-commit | head -5
```

### Hook Failing Unexpectedly

**Problem:** Hook blocks commits even when no conflicts exist.

**Solutions:**
```bash
# Check for Python/Django issues
cd backend/
python3 manage_migrations.py check

# Bypass temporarily if urgent
git commit --no-verify -m "Bypass hook for urgent fix"

# Debug the issue afterward
```

### False Positives

**Problem:** Hook detects conflicts that don't exist.

**Solutions:**
```bash
# Run manual check
cd backend/
python3 manage.py migrate --plan

# Update management script if needed
# Report issue to team
```

## Integration with CI/CD

The Git hooks work alongside your CI/CD pipeline:

1. **Local Development:** Git hooks catch conflicts before commits
2. **CI Pipeline:** GitHub Actions validate migrations during builds
3. **Double Protection:** Both local and remote validation ensure quality

### Relationship with CI Workflows

```
Local Development → Git Hooks → Push → CI Workflows
     ↓                ↓           ↓         ↓
   Develop         Commit      GitHub    Pipeline
   Changes         Check       Actions    Validation
```

---

## Quick Reference

```bash
# Install hooks
bash infra/install-hooks.sh

# Check for conflicts manually
cd backend/ && python3 manage_migrations.py check

# Resolve conflicts
cd backend/ && python3 manage_migrations.py resolve

# Bypass hook (emergency only)
git commit --no-verify

# Uninstall hook
rm .git/hooks/pre-commit
```