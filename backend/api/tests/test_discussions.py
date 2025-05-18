from django.test import TestCase
from django.contrib.auth.models import User
from rest_framework.test import APIClient
from rest_framework import status
from django.urls import reverse
from ..models import Space, Discussion


class DiscussionTests(TestCase):
    def setUp(self):
        self.creator = User.objects.create_user(
            username='creator', email='creator@example.com', password='password123'
        )
        self.collaborator = User.objects.create_user(
            username='collaborator', email='collaborator@example.com', password='password123'
        )
        self.non_collaborator = User.objects.create_user(
            username='non_collaborator', email='non_collaborator@example.com', password='password123'
        )
        
        self.space = Space.objects.create(
            title='Test Space',
            description='Test Description',
            creator=self.creator
        )
        
        self.space.collaborators.add(self.creator)
        self.space.collaborators.add(self.collaborator)
        
        self.discussion1 = Discussion.objects.create(
            space=self.space,
            user=self.creator,
            text='Test discussion 1'
        )
        self.discussion2 = Discussion.objects.create(
            space=self.space,
            user=self.collaborator,
            text='Test discussion 2'
        )
        
        self.client = APIClient()
    
    def test_get_discussions_as_collaborator(self):
        """Test that collaborators can view discussions"""
        self.client.force_authenticate(user=self.collaborator)
        url = reverse('space-discussions', kwargs={'pk': self.space.id})
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)
        self.assertEqual(response.data[0]['text'], 'Test discussion 2')  # Newest first
        self.assertEqual(response.data[1]['text'], 'Test discussion 1')
    
    def test_get_discussions_as_non_collaborator(self):
        """Test that non-collaborators can view discussions"""
        self.client.force_authenticate(user=self.non_collaborator)
        url = reverse('space-discussions', kwargs={'pk': self.space.id})
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)
    
    def test_get_discussions_unauthenticated(self):
        """Test that unauthenticated users can view discussions"""
        url = reverse('space-discussions', kwargs={'pk': self.space.id})
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)
    
    def test_add_discussion_as_collaborator(self):
        """Test that collaborators can add discussions"""
        self.client.force_authenticate(user=self.collaborator)
        url = reverse('space-add-discussion', kwargs={'pk': self.space.id})
        data = {'text': 'New discussion from collaborator'}
        response = self.client.post(url, data)
        
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data['text'], 'New discussion from collaborator')
        self.assertEqual(response.data['username'], 'collaborator')
        
        self.assertEqual(Discussion.objects.count(), 3)
        
        url = reverse('space-discussions', kwargs={'pk': self.space.id})
        response = self.client.get(url)
        self.assertEqual(len(response.data), 3)
        self.assertEqual(response.data[0]['text'], 'New discussion from collaborator')
    
    def test_add_discussion_as_non_collaborator(self):
        """Test that non-collaborators cannot add discussions"""
        self.client.force_authenticate(user=self.non_collaborator)
        url = reverse('space-add-discussion', kwargs={'pk': self.space.id})
        data = {'text': 'New discussion from non-collaborator'}
        response = self.client.post(url, data)
        
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        
        self.assertEqual(Discussion.objects.count(), 2)
    
    def test_add_discussion_unauthenticated(self):
        """Test that unauthenticated users cannot add discussions"""
        url = reverse('space-add-discussion', kwargs={'pk': self.space.id})
        data = {'text': 'New discussion from unauthenticated'}
        response = self.client.post(url, data)
        
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        
        self.assertEqual(Discussion.objects.count(), 2)
    
    def test_discussions_ordering(self):
        """Test that discussions are ordered by created_at descending (newest first)"""
        self.client.force_authenticate(user=self.collaborator)
        url = reverse('space-discussions', kwargs={'pk': self.space.id})
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 2)
        
        # Verify the order (newest first)
        created_times = [discussion['created_at'] for discussion in response.data]
        self.assertTrue(created_times[0] >= created_times[1]) 