from django.urls import reverse
from django.contrib.auth.models import User
from rest_framework import status
from rest_framework.test import APITestCase, APIClient
from api.models import Space, Tag, Profile
from datetime import date

class SearchTests(APITestCase):
    def setUp(self):
        # Create test users
        self.user1 = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            password='testpass123'
        )
        Profile.objects.create(user=self.user1, profession="Developer", dob=date(2000, 1, 1))
        
        self.user2 = User.objects.create_user(
            username='searchableuser',
            email='search@example.com',
            password='testpass123'
        )
        Profile.objects.create(user=self.user2, profession="Designer", dob=date(2000, 1, 1))
        
        # Create tags
        self.tag1 = Tag.objects.create(name='python')
        self.tag2 = Tag.objects.create(name='django')
        self.tag3 = Tag.objects.create(name='react')
        self.tag4 = Tag.objects.create(name='javascript')
        
        # Create spaces
        self.space1 = Space.objects.create(
            title='Python Programming',
            description='Learn Python programming language',
            creator=self.user1
        )
        self.space1.tags.add(self.tag1)
        
        self.space2 = Space.objects.create(
            title='React Basics',
            description='Learn React.js framework',
            creator=self.user1
        )
        self.space2.tags.add(self.tag3)
        
        self.space3 = Space.objects.create(
            title='Web Development',
            description='Full-stack web development with Django and React',
            creator=self.user2
        )
        self.space3.tags.add(self.tag2, self.tag3)
        
        self.space4 = Space.objects.create(
            title='JavaScript Fundamentals',
            description='Learn JavaScript basics',
            creator=self.user2
        )
        self.space4.tags.add(self.tag4)
        
        # Set up the API client and authenticate
        self.client = APIClient()
        self.client.force_authenticate(user=self.user1)
    
    def test_search_spaces_by_title(self):
        """Test searching spaces by title"""
        response = self.client.get(reverse('search'), {'q': 'python'})
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data['spaces']), 1)
        self.assertEqual(response.data['spaces'][0]['title'], 'Python Programming')
    
    def test_search_spaces_by_description(self):
        """Test searching spaces by description"""
        response = self.client.get(reverse('search'), {'q': 'django'})
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(len(response.data['spaces']) >= 1)
        
        # Check if space3 is in the results
        space_titles = [space['title'] for space in response.data['spaces']]
        self.assertIn('Web Development', space_titles)
    
    def test_search_spaces_by_tag(self):
        """Test searching spaces by tag"""
        response = self.client.get(reverse('search'), {'q': 'react'})
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data['spaces']), 2)
        
        # Check if both spaces with React tag are returned
        space_titles = [space['title'] for space in response.data['spaces']]
        self.assertIn('React Basics', space_titles)
        self.assertIn('Web Development', space_titles)
    
    def test_search_users_by_username(self):
        """Test searching users by username"""
        response = self.client.get(reverse('search'), {'q': 'searchable'})
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data['users']), 1)
        self.assertEqual(response.data['users'][0]['username'], 'searchableuser')
    
    def test_search_with_no_results(self):
        """Test search with query that returns no results"""
        response = self.client.get(reverse('search'), {'q': 'nonexistent'})
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data['spaces']), 0)
        self.assertEqual(len(response.data['users']), 0)
    
    def test_search_with_empty_query(self):
        """Test search with empty query"""
        response = self.client.get(reverse('search'), {'q': ''})
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data['spaces']), 0)
        self.assertEqual(len(response.data['users']), 0)
    
    def test_search_requires_authentication(self):
        """Test that search requires authentication"""
        # Create a new client without authentication
        client = APIClient()
        response = client.get(reverse('search'), {'q': 'python'})
        
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN) 