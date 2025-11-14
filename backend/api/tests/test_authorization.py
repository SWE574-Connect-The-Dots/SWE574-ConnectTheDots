from django.test import TestCase
from django.contrib.auth.models import User
from rest_framework.test import APITestCase, APIClient
from rest_framework import status
from rest_framework_simplejwt.tokens import RefreshToken
from api.models import Profile, Space, SpaceModerator, Tag
from api.permissions import IsAdmin, IsAdminOrModerator, IsSpaceModerator, CanChangeUserType


class ProfileModelTest(TestCase):
    """Test Profile model and user type functionality"""
    
    def setUp(self):
        self.admin_user = User.objects.create_user(
            username='testadmin',
            email='admin@test.com',
            password='testpass123'
        )
        self.moderator_user = User.objects.create_user(
            username='testmoderator', 
            email='moderator@test.com',
            password='testpass123'
        )
        self.regular_user = User.objects.create_user(
            username='testuser',
            email='user@test.com', 
            password='testpass123'
        )
        
    def test_profile_creation_with_default_user_type(self):
        """Test that profiles are created with default user type (USER)"""
        profile = self.regular_user.profile
        self.assertEqual(profile.user_type, Profile.USER)
        self.assertTrue(profile.is_regular_user())
        self.assertFalse(profile.is_admin())
        self.assertFalse(profile.is_moderator())
        
    def test_admin_user_type_methods(self):
        """Test admin user type methods"""
        profile = self.admin_user.profile
        profile.user_type = Profile.ADMIN
        profile.save()
        
        self.assertTrue(profile.is_admin())
        self.assertFalse(profile.is_moderator())
        self.assertFalse(profile.is_regular_user())
        
    def test_moderator_user_type_methods(self):
        """Test moderator user type methods"""
        profile = self.moderator_user.profile
        profile.user_type = Profile.MODERATOR
        profile.save()
        
        self.assertFalse(profile.is_admin())
        self.assertTrue(profile.is_moderator())
        self.assertFalse(profile.is_regular_user())
        
    def test_user_type_display(self):
        """Test user type display names"""
        admin_profile = self.admin_user.profile
        admin_profile.user_type = Profile.ADMIN
        admin_profile.save()
        
        moderator_profile = self.moderator_user.profile  
        moderator_profile.user_type = Profile.MODERATOR
        moderator_profile.save()
        
        regular_profile = self.regular_user.profile
        
        self.assertEqual(admin_profile.get_user_type_display(), 'Admin')
        self.assertEqual(moderator_profile.get_user_type_display(), 'Moderator')
        self.assertEqual(regular_profile.get_user_type_display(), 'User')


class SpaceModeratorModelTest(TestCase):
    """Test SpaceModerator model functionality"""
    
    def setUp(self):
        self.admin_user = User.objects.create_user(
            username='admin',
            email='admin@test.com',
            password='testpass123'
        )
        self.admin_user.profile.user_type = Profile.ADMIN
        self.admin_user.profile.save()
        
        self.moderator_user = User.objects.create_user(
            username='moderator',
            email='moderator@test.com', 
            password='testpass123'
        )
        self.moderator_user.profile.user_type = Profile.MODERATOR
        self.moderator_user.profile.save()
        
        self.space = Space.objects.create(
            title='Test Space',
            description='A test space',
            creator=self.admin_user
        )
        
    def test_space_moderator_creation(self):
        """Test creating space moderator assignment"""
        space_moderator = SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
        
        self.assertEqual(space_moderator.user, self.moderator_user)
        self.assertEqual(space_moderator.space, self.space)
        self.assertEqual(space_moderator.assigned_by, self.admin_user)
        
    def test_can_moderate_space_admin(self):
        """Test that admin can moderate any space"""
        profile = self.admin_user.profile
        self.assertTrue(profile.can_moderate_space(self.space))
        
    def test_can_moderate_space_assigned_moderator(self):
        """Test that assigned moderator can moderate specific space"""
        SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
        
        profile = self.moderator_user.profile
        self.assertTrue(profile.can_moderate_space(self.space))
        
    def test_cannot_moderate_unassigned_space(self):
        """Test that moderator cannot moderate unassigned space"""
        other_space = Space.objects.create(
            title='Other Space',
            description='Another space',
            creator=self.admin_user
        )
        
        profile = self.moderator_user.profile
        self.assertFalse(profile.can_moderate_space(other_space))
        
    def test_space_get_moderators(self):
        """Test getting moderators for a space"""
        SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
        
        moderators = self.space.get_moderators()
        self.assertIn(self.moderator_user, moderators)
        
    def test_space_is_moderator(self):
        """Test checking if user is moderator of space"""
        SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
        
        self.assertTrue(self.space.is_moderator(self.moderator_user))
        
        regular_user = User.objects.create_user(
            username='regular',
            email='regular@test.com',
            password='testpass123'
        )
        self.assertFalse(self.space.is_moderator(regular_user))


class AuthorizationPermissionsTest(APITestCase):
    """Test custom permission classes"""
    
    def setUp(self):
        self.admin_user = User.objects.create_user(
            username='admin',
            email='admin@test.com',
            password='testpass123'
        )
        self.admin_user.profile.user_type = Profile.ADMIN
        self.admin_user.profile.save()
        
        self.moderator_user = User.objects.create_user(
            username='moderator',
            email='moderator@test.com',
            password='testpass123'
        )
        self.moderator_user.profile.user_type = Profile.MODERATOR
        self.moderator_user.profile.save()
        
        self.regular_user = User.objects.create_user(
            username='regular',
            email='regular@test.com',
            password='testpass123'
        )
        
        self.space = Space.objects.create(
            title='Test Space',
            description='A test space',
            creator=self.admin_user
        )
        
        SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
        
    def get_token(self, user):
        """Helper method to get JWT token for user"""
        refresh = RefreshToken.for_user(user)
        return str(refresh.access_token)
        
    def test_is_admin_permission(self):
        """Test IsAdmin permission class"""
        # Admin should have permission
        self.client.force_authenticate(user=self.admin_user)
        response = self.client.get('/api/auth/users/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        # Moderator should not have permission for admin-only endpoints
        self.client.force_authenticate(user=self.moderator_user)
        response = self.client.post('/api/auth/assign-moderator/', {
            'user_id': self.regular_user.id,
            'space_id': self.space.id
        })
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        
        # Regular user should not have permission
        self.client.force_authenticate(user=self.regular_user)
        response = self.client.post('/api/auth/assign-moderator/', {
            'user_id': self.regular_user.id,
            'space_id': self.space.id
        })
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        
    def test_is_admin_or_moderator_permission(self):
        """Test IsAdminOrModerator permission class"""
        # Admin should have permission
        self.client.force_authenticate(user=self.admin_user)
        response = self.client.get(f'/api/auth/users/?space_id={self.space.id}')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        # Moderator should have permission
        self.client.force_authenticate(user=self.moderator_user)
        response = self.client.get(f'/api/auth/users/?space_id={self.space.id}')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        # Regular user should not have permission
        self.client.force_authenticate(user=self.regular_user)
        response = self.client.get(f'/api/auth/users/?space_id={self.space.id}')
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)


class AuthorizationAPITest(APITestCase):
    """Test authorization API endpoints"""
    
    def setUp(self):
        self.admin_user = User.objects.create_user(
            username='admin',
            email='admin@test.com',
            password='testpass123'
        )
        self.admin_user.profile.user_type = Profile.ADMIN
        self.admin_user.profile.save()
        
        self.moderator_user = User.objects.create_user(
            username='moderator',
            email='moderator@test.com',
            password='testpass123'
        )
        self.moderator_user.profile.user_type = Profile.MODERATOR
        self.moderator_user.profile.save()
        
        self.regular_user = User.objects.create_user(
            username='regular',
            email='regular@test.com',
            password='testpass123'
        )
        
        self.space = Space.objects.create(
            title='Test Space',
            description='A test space',
            creator=self.admin_user
        )
        
    def test_assign_moderator_as_admin(self):
        """Test admin can assign moderator to space"""
        self.client.force_authenticate(user=self.admin_user)
        
        data = {
            'user_id': self.regular_user.id,
            'space_id': self.space.id
        }
        
        response = self.client.post('/api/auth/assign-moderator/', data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        
        # Check that user type changed to moderator
        self.regular_user.profile.refresh_from_db()
        self.assertEqual(self.regular_user.profile.user_type, Profile.MODERATOR)
        
        # Check that space moderator assignment was created
        self.assertTrue(
            SpaceModerator.objects.filter(
                user=self.regular_user,
                space=self.space
            ).exists()
        )
        
    def test_assign_moderator_unauthorized(self):
        """Test non-admin cannot assign moderator"""
        self.client.force_authenticate(user=self.regular_user)
        
        data = {
            'user_id': self.moderator_user.id,
            'space_id': self.space.id
        }
        
        response = self.client.post('/api/auth/assign-moderator/', data)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        
    def test_change_user_type_as_admin(self):
        """Test admin can change any user type"""
        self.client.force_authenticate(user=self.admin_user)
        
        data = {
            'user_id': self.regular_user.id,
            'user_type': Profile.MODERATOR,
            'space_id': self.space.id
        }
        
        response = self.client.post('/api/auth/change-user-type/', data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        # Check that user type changed
        self.regular_user.profile.refresh_from_db()
        self.assertEqual(self.regular_user.profile.user_type, Profile.MODERATOR)
        
    def test_change_user_type_as_moderator_valid(self):
        """Test moderator can change regular user to moderator in their space"""
        # Assign moderator to space first
        SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
        
        self.client.force_authenticate(user=self.moderator_user)
        
        data = {
            'user_id': self.regular_user.id,
            'user_type': Profile.MODERATOR,
            'space_id': self.space.id
        }
        
        response = self.client.post('/api/auth/change-user-type/', data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
    def test_change_user_type_as_moderator_invalid(self):
        """Test moderator cannot change user type in space they don't moderate"""
        other_space = Space.objects.create(
            title='Other Space',
            description='Another space',
            creator=self.admin_user
        )
        
        self.client.force_authenticate(user=self.moderator_user)
        
        data = {
            'user_id': self.regular_user.id,
            'user_type': Profile.MODERATOR,
            'space_id': other_space.id
        }
        
        response = self.client.post('/api/auth/change-user-type/', data)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        
    def test_remove_moderator_as_admin(self):
        """Test admin can remove moderator from space"""
        # First assign moderator
        SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
        
        self.client.force_authenticate(user=self.admin_user)
        
        data = {
            'user_id': self.moderator_user.id,
            'space_id': self.space.id
        }
        
        response = self.client.delete('/api/auth/remove-moderator/', data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        # Check that space moderator assignment was removed
        self.assertFalse(
            SpaceModerator.objects.filter(
                user=self.moderator_user,
                space=self.space
            ).exists()
        )
        
    def test_get_user_permissions_admin(self):
        """Test getting admin user permissions"""
        self.client.force_authenticate(user=self.admin_user)
        
        response = self.client.get('/api/auth/permissions/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        data = response.json()
        self.assertEqual(data['user_type'], Profile.ADMIN)
        self.assertEqual(data['user_type_display'], 'Admin')
        self.assertTrue(data['is_admin'])
        self.assertFalse(data['is_moderator'])
        self.assertFalse(data['is_regular_user'])
        
    def test_get_user_permissions_moderator(self):
        """Test getting moderator user permissions"""
        SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
        
        self.client.force_authenticate(user=self.moderator_user)
        
        response = self.client.get('/api/auth/permissions/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        data = response.json()
        self.assertEqual(data['user_type'], Profile.MODERATOR)
        self.assertEqual(data['user_type_display'], 'Moderator')
        self.assertFalse(data['is_admin'])
        self.assertTrue(data['is_moderator'])
        self.assertFalse(data['is_regular_user'])
        self.assertEqual(len(data['moderated_spaces']), 1)
        
    def test_list_users_as_admin(self):
        """Test admin can list all users"""
        self.client.force_authenticate(user=self.admin_user)
        
        response = self.client.get('/api/auth/users/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        data = response.json()
        self.assertIn('users', data)
        self.assertGreaterEqual(len(data['users']), 3)  # At least our 3 test users
        
    def test_list_users_as_moderator_with_space(self):
        """Test moderator can list users in their space"""
        SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
        
        self.client.force_authenticate(user=self.moderator_user)
        
        response = self.client.get(f'/api/auth/users/?space_id={self.space.id}')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
    def test_list_users_as_moderator_without_space(self):
        """Test moderator cannot list users without specifying space"""
        self.client.force_authenticate(user=self.moderator_user)
        
        response = self.client.get('/api/auth/users/')
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        
    def test_unauthenticated_access_denied(self):
        """Test that unauthenticated users cannot access authorization endpoints"""
        endpoints = [
            '/api/auth/assign-moderator/',
            '/api/auth/change-user-type/',
            '/api/auth/remove-moderator/',
            '/api/auth/permissions/',
            '/api/auth/users/'
        ]
        
        for endpoint in endpoints:
            response = self.client.get(endpoint)
            # DRF returns 403 Forbidden for permission denied, not 401 Unauthorized
            self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)


class AuthorizationWorkflowTest(APITestCase):
    """Test complete authorization workflows"""
    
    def setUp(self):
        self.admin_user = User.objects.create_user(
            username='admin',
            email='admin@test.com',
            password='testpass123'
        )
        self.admin_user.profile.user_type = Profile.ADMIN
        self.admin_user.profile.save()
        
        self.regular_user1 = User.objects.create_user(
            username='user1',
            email='user1@test.com',
            password='testpass123'
        )
        
        self.regular_user2 = User.objects.create_user(
            username='user2',
            email='user2@test.com',
            password='testpass123'
        )
        
        self.space1 = Space.objects.create(
            title='Space 1',
            description='First test space',
            creator=self.admin_user
        )
        
        self.space2 = Space.objects.create(
            title='Space 2',
            description='Second test space',
            creator=self.admin_user
        )
        
    def test_complete_moderator_workflow(self):
        """Test complete workflow of assigning and managing moderators"""
        self.client.force_authenticate(user=self.admin_user)
        
        # Step 1: Assign user1 as moderator of space1
        response = self.client.post('/api/auth/assign-moderator/', {
            'user_id': self.regular_user1.id,
            'space_id': self.space1.id
        })
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        
        # Verify user1 is now a moderator
        self.regular_user1.profile.refresh_from_db()
        self.assertEqual(self.regular_user1.profile.user_type, Profile.MODERATOR)
        
        # Step 2: Login as user1 (now moderator) and check permissions
        self.client.force_authenticate(user=self.regular_user1)
        response = self.client.get('/api/auth/permissions/')
        data = response.json()
        self.assertTrue(data['is_moderator'])
        self.assertEqual(len(data['moderated_spaces']), 1)
        
        # Step 3: Moderator tries to assign another moderator in their space
        response = self.client.post('/api/auth/change-user-type/', {
            'user_id': self.regular_user2.id,
            'user_type': Profile.MODERATOR,
            'space_id': self.space1.id
        })
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        # Step 4: Moderator tries to assign moderator in space they don't moderate
        response = self.client.post('/api/auth/change-user-type/', {
            'user_id': self.regular_user2.id,
            'user_type': Profile.MODERATOR,
            'space_id': self.space2.id
        })
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        
        # Step 5: Admin removes moderator from space
        self.client.force_authenticate(user=self.admin_user)
        response = self.client.delete('/api/auth/remove-moderator/', {
            'user_id': self.regular_user1.id,
            'space_id': self.space1.id
        })
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        
        # Verify moderator assignment was removed
        self.assertFalse(
            SpaceModerator.objects.filter(
                user=self.regular_user1,
                space=self.space1
            ).exists()
        )
        
    def test_multiple_space_moderation(self):
        """Test moderator assigned to multiple spaces"""
        self.client.force_authenticate(user=self.admin_user)
        
        # Assign user1 as moderator of both spaces
        self.client.post('/api/auth/assign-moderator/', {
            'user_id': self.regular_user1.id,
            'space_id': self.space1.id
        })
        
        self.client.post('/api/auth/assign-moderator/', {
            'user_id': self.regular_user1.id,
            'space_id': self.space2.id
        })
        
        # Refresh profile from database
        self.regular_user1.profile.refresh_from_db()
        
        # Check that user1 can moderate both spaces
        profile = self.regular_user1.profile
        self.assertTrue(profile.can_moderate_space(self.space1))
        self.assertTrue(profile.can_moderate_space(self.space2))
        
        # Check permissions endpoint shows both spaces
        self.client.force_authenticate(user=self.regular_user1)
        response = self.client.get('/api/auth/permissions/')
        data = response.json()
        self.assertEqual(len(data['moderated_spaces']), 2)
        
        # Remove from one space - should still be moderator
        self.client.force_authenticate(user=self.admin_user)
        self.client.delete('/api/auth/remove-moderator/', {
            'user_id': self.regular_user1.id,
            'space_id': self.space1.id
        })
        
        # User should still be moderator type since they moderate space2
        self.regular_user1.profile.refresh_from_db()
        self.assertEqual(self.regular_user1.profile.user_type, Profile.MODERATOR)
        
        # Remove from last space - should become regular user
        self.client.delete('/api/auth/remove-moderator/', {
            'user_id': self.regular_user1.id,
            'space_id': self.space2.id
        })
        
        self.regular_user1.profile.refresh_from_db()
        self.assertEqual(self.regular_user1.profile.user_type, Profile.USER)

class AdminDashboardAccessTest(APITestCase):
    """Test admin dashboard access for space creators and moderators"""
    
    def setUp(self):
        self.admin_user = User.objects.create_user(
            username='admin',
            email='admin@test.com',
            password='testpass123'
        )
        self.admin_user.profile.user_type = Profile.ADMIN
        self.admin_user.profile.save()
        
        self.moderator_user = User.objects.create_user(
            username='moderator',
            email='moderator@test.com',
            password='testpass123'
        )
        self.moderator_user.profile.user_type = Profile.MODERATOR
        self.moderator_user.profile.save()
        
        self.space_creator = User.objects.create_user(
            username='creator',
            email='creator@test.com',
            password='testpass123'
        )
        
        self.regular_user = User.objects.create_user(
            username='regular',
            email='regular@test.com',
            password='testpass123'
        )
        
        self.space = Space.objects.create(
            title='Test Space',
            description='A test space',
            creator=self.space_creator
        )
        
        # Assign moderator to space
        SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
    
    def test_can_access_admin_dashboard_field_admin(self):
        """Test can_access_admin_dashboard field returns True for admin"""
        self.client.force_authenticate(user=self.admin_user)
        response = self.client.get('/api/profiles/me/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(response.data['can_access_admin_dashboard'])
    
    def test_can_access_admin_dashboard_field_space_creator(self):
        """Test can_access_admin_dashboard field returns True for space creator"""
        self.client.force_authenticate(user=self.space_creator)
        response = self.client.get('/api/profiles/me/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(response.data['can_access_admin_dashboard'])
    
    def test_can_access_admin_dashboard_field_moderator_with_spaces(self):
        """Test can_access_admin_dashboard field returns True for moderator with spaces"""
        self.client.force_authenticate(user=self.moderator_user)
        response = self.client.get('/api/profiles/me/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(response.data['can_access_admin_dashboard'])
    
    def test_can_access_admin_dashboard_field_moderator_without_spaces(self):
        """Test can_access_admin_dashboard field returns False for moderator without spaces"""
        # Create a moderator without any moderated spaces
        moderator_no_spaces = User.objects.create_user(
            username='moderator_no_spaces',
            email='mod2@test.com',
            password='testpass123'
        )
        moderator_no_spaces.profile.user_type = Profile.MODERATOR
        moderator_no_spaces.profile.save()
        
        self.client.force_authenticate(user=moderator_no_spaces)
        response = self.client.get('/api/profiles/me/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertFalse(response.data['can_access_admin_dashboard'])
    
    def test_can_access_admin_dashboard_field_regular_user(self):
        """Test can_access_admin_dashboard field returns False for regular user"""
        self.client.force_authenticate(user=self.regular_user)
        response = self.client.get('/api/profiles/me/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertFalse(response.data['can_access_admin_dashboard'])
    
    def test_moderator_loses_access_after_removal_from_all_spaces(self):
        """Test moderator loses admin dashboard access after being removed from all spaces"""
        # Moderator has access while moderating a space
        self.client.force_authenticate(user=self.moderator_user)
        response = self.client.get('/api/profiles/me/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertTrue(response.data['can_access_admin_dashboard'])
        
        # Remove moderator from space
        self.client.force_authenticate(user=self.admin_user)
        self.client.delete('/api/auth/remove-moderator/', {
            'user_id': self.moderator_user.id,
            'space_id': self.space.id
        })
        
        # Check can_access_admin_dashboard field - should now be False
        self.client.force_authenticate(user=self.moderator_user)
        response = self.client.get('/api/profiles/me/')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertFalse(response.data['can_access_admin_dashboard'])