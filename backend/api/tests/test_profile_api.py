from django.test import TestCase
from django.contrib.auth.models import User
from django.urls import reverse
from rest_framework.test import APIClient
from rest_framework import status

class ProfileAPITestCase(TestCase):
    def setUp(self):
        self.user1 = User.objects.create_user(
            username='testuser1',
            email='test1@example.com',
            password='testpass123'
        )
        self.user2 = User.objects.create_user(
            username='testuser2',
            email='test2@example.com',
            password='testpass123'
        )
        
        self.user1.profile.profession = "Software Engineer"
        self.user1.profile.bio = "Original bio"
        self.user1.profile.save()
        
        self.client = APIClient()
    
    def test_get_own_profile(self):
        """Test retrieving own profile"""
        self.client.force_authenticate(user=self.user1)
        response = self.client.get(reverse('profile-me'))
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['bio'], 'Original bio')
        self.assertEqual(response.data['profession'], 'Software Engineer')
    
    def test_get_user_profile(self):
        """Test retrieving another user's profile"""
        self.client.force_authenticate(user=self.user2)
        response = self.client.get(
            reverse('profile-user-profile', kwargs={'pk': self.user1.username})
        )
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['bio'], 'Original bio')
        self.assertEqual(response.data['profession'], 'Software Engineer')
    
    def test_update_own_profile(self):
        """Test updating own profile"""
        self.client.force_authenticate(user=self.user1)
        update_data = {
            'bio': 'Updated bio information',
            'profession': 'Data Scientist'
        }
        
        response = self.client.put(
            reverse('profile-update-profile'),
            update_data
        )
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['bio'], 'Updated bio information')
        self.assertEqual(response.data['profession'], 'Data Scientist')
        
        self.user1.refresh_from_db()
        self.assertEqual(self.user1.profile.bio, 'Updated bio information')
        self.assertEqual(self.user1.profile.profession, 'Data Scientist')
    
    def test_update_bio_only(self):
        """Test updating only the bio field"""
        self.client.force_authenticate(user=self.user1)
        update_data = {
            'bio': 'New bio text only'
        }
        
        response = self.client.put(
            reverse('profile-update-profile'),
            update_data
        )
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['bio'], 'New bio text only')
        self.assertEqual(response.data['profession'], 'Software Engineer')
    
    def test_add_profession(self):
        """Test adding profession when it wasn't set before"""
        self.user2.profile.profession = None
        self.user2.profile.save()
        
        self.client.force_authenticate(user=self.user2)
        update_data = {
            'profession': 'New Profession'
        }
        
        response = self.client.put(
            reverse('profile-update-profile'),
            update_data
        )
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['profession'], 'New Profession')
        
        self.user2.refresh_from_db()
        self.assertEqual(self.user2.profile.profession, 'New Profession')
    
    def test_unauthorized_access(self):
        """Test that unauthorized users can't update profiles"""
        update_data = {
            'bio': 'Should not update',
            'profession': 'Should not update'
        }
        
        response = self.client.put(
            reverse('profile-update-profile'),
            update_data
        )
        
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        
        self.user1.refresh_from_db()
        self.assertEqual(self.user1.profile.bio, 'Original bio')
        self.assertEqual(self.user1.profile.profession, 'Software Engineer')
    
    def test_can_access_admin_dashboard_field_in_profile_response(self):
        """Test that can_access_admin_dashboard field is included in profile response"""
        self.client.force_authenticate(user=self.user1)
        response = self.client.get(reverse('profile-me'))
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('can_access_admin_dashboard', response.data)
        # Regular user without spaces should have False
        self.assertFalse(response.data['can_access_admin_dashboard'])
    
    def test_can_access_admin_dashboard_for_space_creator(self):
        """Test that space creator has can_access_admin_dashboard set to True"""
        from api.models import Space
        
        # Create a space for user1
        Space.objects.create(
            title='Test Space',
            description='Test description',
            creator=self.user1
        )
        
        self.client.force_authenticate(user=self.user1)
        response = self.client.get(reverse('profile-me'))
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(response.data['can_access_admin_dashboard']) 