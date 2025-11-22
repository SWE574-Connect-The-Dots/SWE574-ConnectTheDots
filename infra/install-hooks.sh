#!/bin/bash
# Install Git hooks for the project

echo " Installing Git hooks for SWE574-ConnectTheDots..."

# Create hooks directory if it doesn't exist
mkdir -p .git/hooks

# Install pre-commit hook
cp infra/pre-commit-hook.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit

echo " Pre-commit hook installed successfully"
echo ""
echo "The hook will now:"
echo "  • Check for migration conflicts before each commit"
echo "  • Block commits with conflicting migrations"
echo "  • Provide guidance on resolving conflicts"
echo ""
echo "To disable the hook temporarily:"
echo "  git commit --no-verify"
echo ""
echo "To uninstall:"
echo "  rm .git/hooks/pre-commit"