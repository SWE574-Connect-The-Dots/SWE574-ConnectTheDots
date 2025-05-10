from datetime import date
from django.urls import reverse
from django.contrib.auth.models import User
from rest_framework import status
from rest_framework.test import APITestCase, APIClient


class ProfileTests(APITestCase):
    def setUp(self):
        self.user1 = User.objects.create_user(
            username='testuser1',
            email='test1@example.com',
            password='testpass123'
        )
        self.user1.profile.profession = "Software Engineer"
        self.user1.profile.bio = "I'm a test user with a bio"
        self.user1.profile.dob = date(1990, 1, 15)
        self.user1.profile.save()
        
        self.user2 = User.objects.create_user(
            username='testuser2',
            email='test2@example.com',
            password='testpass123'
        )
        self.user2.profile.profession = "Data Scientist"
        self.user2.profile.bio = "Another user's bio"
        self.user2.profile.dob = date(1995, 6, 10)
        self.user2.profile.save()
        
        self.client = APIClient()
    
    def test_profile_created_with_user(self):
        """Test that a profile is automatically created when a user is created"""
        new_user = User.objects.create_user(
            username='newuser',
            email='new@example.com',
            password='newpass123'
        )
        
        self.assertTrue(hasattr(new_user, 'profile'))
        self.assertIsNotNone(new_user.profile)
        
        self.assertIsNone(new_user.profile.profession)
        self.assertIsNone(new_user.profile.bio)
        self.assertIsNone(new_user.profile.dob)
        
        self.assertIsNotNone(new_user.profile.created_at)
        self.assertIsNotNone(new_user.profile.updated_at)
    
    def test_get_own_profile(self):
        """Test that a user can retrieve their own profile"""
        self.client.force_authenticate(user=self.user1)
        response = self.client.get(reverse('profile-me'))
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['user']['username'], 'testuser1')
        self.assertEqual(response.data['profession'], 'Software Engineer')
        self.assertEqual(response.data['bio'], "I'm a test user with a bio")
        self.assertEqual(response.data['dob'], '1990-01-15')
    
    def test_get_another_user_profile(self):
        """Test that a user can retrieve another user's profile"""
        self.client.force_authenticate(user=self.user1)
        response = self.client.get(
            reverse('profile-user-profile', kwargs={'pk': 'testuser2'})
        )
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['user']['username'], 'testuser2')
        self.assertEqual(response.data['profession'], 'Data Scientist')
        self.assertEqual(response.data['bio'], "Another user's bio")
        self.assertEqual(response.data['dob'], '1995-06-10')
    
    def test_get_nonexistent_profile(self):
        """Test that requesting a non-existent profile returns 404"""
        self.client.force_authenticate(user=self.user1)
        response = self.client.get(
            reverse('profile-user-profile', kwargs={'pk': 'nonexistentuser'})
        )
        
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
    
    def test_update_own_profile(self):
        """Test that a user can update their own profile"""
        self.client.force_authenticate(user=self.user1)
        
        update_data = {
            'profession': 'DevOps Engineer',
            'bio': 'Updated bio',
            'dob': '1991-02-20'
        }
        
        response = self.client.patch(
            reverse('profile-detail', kwargs={'pk': self.user1.profile.id}),
            update_data,
            format='json'
        )
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        self.user1.profile.refresh_from_db()
        
        self.assertEqual(self.user1.profile.profession, 'DevOps Engineer')
        self.assertEqual(self.user1.profile.bio, 'Updated bio')
        self.assertEqual(str(self.user1.profile.dob), '1991-02-20')
    
    def test_cannot_update_another_user_profile(self):
        """Test that a user cannot update another user's profile"""
        self.client.force_authenticate(user=self.user1)
        
        update_data = {
            'profession': 'Hacked Profession',
            'bio': 'Hacked bio'
        }
        
        response = self.client.patch(
            reverse('profile-detail', kwargs={'pk': self.user2.profile.id}),
            update_data,
            format='json'
        )
        
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        
        self.user2.profile.refresh_from_db()
        
        self.assertEqual(self.user2.profile.profession, 'Data Scientist')
        self.assertEqual(self.user2.profile.bio, "Another user's bio")
    
    def test_profile_serialization(self):
        """Test that profile is serialized correctly"""
        self.client.force_authenticate(user=self.user1)
        response = self.client.get(reverse('profile-me'))
        
        self.assertIn('user', response.data)
        self.assertIn('profession', response.data)
        self.assertIn('bio', response.data)
        self.assertIn('dob', response.data)
        self.assertIn('created_at', response.data)
        self.assertIn('updated_at', response.data)
        self.assertIn('joined_spaces', response.data) 