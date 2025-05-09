from django.contrib.auth.models import User
from rest_framework.test import APITestCase, APIClient
from rest_framework import status
from api.models import Space

class SpaceCollaboratorTests(APITestCase):
    def setUp(self):
        """
        Setup test data with users and spaces
        """
        # Create test users
        self.user1 = User.objects.create_user(
            username='owner_user',
            email='owner@example.com',
            password='password123'
        )
        
        self.user2 = User.objects.create_user(
            username='collaborator_user',
            email='collaborator@example.com',
            password='password123'
        )
        
        self.user3 = User.objects.create_user(
            username='regular_user',
            email='regular@example.com',
            password='password123'
        )
        
        # Create a space with user1 as creator
        self.space = Space.objects.create(
            title='Test Collaboration Space',
            description='Space for testing collaboration features',
            creator=self.user1
        )
        
        # Add user1 (creator) and user2 as collaborators
        self.space.collaborators.add(self.user1)
        self.space.collaborators.add(self.user2)
        
        # Setup API clients
        self.client_creator = APIClient()
        self.client_creator.force_authenticate(user=self.user1)
        
        self.client_collaborator = APIClient()
        self.client_collaborator.force_authenticate(user=self.user2)
        
        self.client_regular = APIClient()
        self.client_regular.force_authenticate(user=self.user3)
        
        self.client_unauthenticated = APIClient()
        
    def test_join_space(self):
        """Test a user can join a space as collaborator"""
        # Regular user attempts to join the space
        response = self.client_regular.post(f'/api/spaces/{self.space.id}/join/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['message'], 'Successfully joined the space')
        
        # Verify user3 is now a collaborator
        self.space.refresh_from_db()
        self.assertIn(self.user3, self.space.collaborators.all())
        
    def test_join_space_already_collaborator(self):
        """Test joining a space when already a collaborator"""
        # User2 is already a collaborator and tries to join again
        response = self.client_collaborator.post(f'/api/spaces/{self.space.id}/join/')
        
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertEqual(response.data['message'], 'You are already a collaborator of this space')
    
    def test_leave_space(self):
        """Test a collaborator can leave a space"""
        # User2 leaves the space
        response = self.client_collaborator.post(f'/api/spaces/{self.space.id}/leave/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['message'], 'Successfully left the space')
        
        # Verify user2 is no longer a collaborator
        self.space.refresh_from_db()
        self.assertNotIn(self.user2, self.space.collaborators.all())
    
    def test_leave_space_not_collaborator(self):
        """Test leaving a space when not a collaborator"""
        # User3 is not a collaborator and tries to leave
        response = self.client_regular.post(f'/api/spaces/{self.space.id}/leave/')
        
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertEqual(response.data['message'], 'You are not a collaborator of this space')
    
    def test_leave_space_as_creator(self):
        """Test that a creator cannot leave their own space"""
        # User1 (creator) tries to leave the space
        response = self.client_creator.post(f'/api/spaces/{self.space.id}/leave/')
        
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertEqual(response.data['message'], 'Creator cannot leave the space')
        
        # Verify creator is still a collaborator
        self.space.refresh_from_db()
        self.assertIn(self.user1, self.space.collaborators.all())
    
    def test_check_collaborator_status(self):
        """Test checking collaborator status"""
        # Check status for a collaborator (user2)
        response = self.client_collaborator.get(f'/api/spaces/{self.space.id}/check-collaborator/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(response.data['is_collaborator'])
        
        # Check status for a non-collaborator (user3)
        response = self.client_regular.get(f'/api/spaces/{self.space.id}/check-collaborator/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertFalse(response.data['is_collaborator'])
    
    def test_unauthenticated_join_attempt(self):
        """Test that unauthenticated users cannot join spaces"""
        response = self.client_unauthenticated.post(f'/api/spaces/{self.space.id}/join/')
        
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN) 