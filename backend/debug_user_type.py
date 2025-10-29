#!/usr/bin/env python
import os
import sys
import django

# Add the backend directory to the Python path
sys.path.append('/app')

# Set the Django settings module
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'backend.settings')

# Setup Django
django.setup()

from django.contrib.auth.models import User
from api.models import Profile, Space, SpaceModerator
from django.test import Client
from rest_framework.test import APIClient
import json

# Create test users
admin_user = User.objects.create_user('testadmin', 'admin@test.com', 'pass123')
admin_user.profile.user_type = Profile.ADMIN
admin_user.profile.save()

regular_user = User.objects.create_user('testuser', 'user@test.com', 'pass123')

# Create test space
space = Space.objects.create(
    title='Test Space',
    description='Test description',
    creator=admin_user
)

print(f"Admin user type: {admin_user.profile.user_type}")
print(f"Regular user type: {regular_user.profile.user_type}")
print(f"Profile.MODERATOR constant: {Profile.MODERATOR}")

# Test the API endpoint
client = APIClient()
client.force_authenticate(user=admin_user)

data = {
    'user_id': regular_user.id,
    'user_type': Profile.MODERATOR,
    'space_id': space.id
}

print(f"Sending data: {data}")

response = client.post('/api/auth/change-user-type/', data, format='json')
print(f"Response status: {response.status_code}")
print(f"Response data: {response.data}")

# Check if user type changed
regular_user.profile.refresh_from_db()
print(f"Regular user type after request: {regular_user.profile.user_type}")