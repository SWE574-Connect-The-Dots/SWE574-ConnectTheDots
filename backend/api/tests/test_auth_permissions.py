from django.test import TestCase, RequestFactory
from django.contrib.auth.models import User, AnonymousUser
from rest_framework.test import APIRequestFactory
from api.models import Profile, Space, SpaceModerator
from api.permissions import IsAdmin, IsAdminOrModerator, IsSpaceModerator, CanChangeUserType


class MockView:
    """Mock view class for testing permissions"""
    def __init__(self, **kwargs):
        for key, value in kwargs.items():
            setattr(self, key, value)


class AuthorizationPermissionsTest(TestCase):
    """Test custom permission classes in isolation"""
    
    def setUp(self):
        self.factory = APIRequestFactory()
        
        # Create test users
        self.admin_user = User.objects.create_user('admin', 'admin@test.com', 'pass')
        self.admin_user.profile.user_type = Profile.ADMIN
        self.admin_user.profile.save()
        
        self.moderator_user = User.objects.create_user('mod', 'mod@test.com', 'pass')
        self.moderator_user.profile.user_type = Profile.MODERATOR
        self.moderator_user.profile.save()
        
        self.regular_user = User.objects.create_user('user', 'user@test.com', 'pass')
        
        # Create test space
        self.space = Space.objects.create(
            title='Test Space',
            description='Test description',
            creator=self.admin_user
        )
        
        # Assign moderator to space
        SpaceModerator.objects.create(
            user=self.moderator_user,
            space=self.space,
            assigned_by=self.admin_user
        )
    
    def test_is_admin_permission_with_admin(self):
        """Test IsAdmin permission allows admin users"""
        request = self.factory.get('/test/')
        request.user = self.admin_user
        
        permission = IsAdmin()
        view = MockView()
        
        self.assertTrue(permission.has_permission(request, view))
    
    def test_is_admin_permission_with_moderator(self):
        """Test IsAdmin permission denies moderator users"""
        request = self.factory.get('/test/')
        request.user = self.moderator_user
        
        permission = IsAdmin()
        view = MockView()
        
        self.assertFalse(permission.has_permission(request, view))
    
    def test_is_admin_permission_with_regular_user(self):
        """Test IsAdmin permission denies regular users"""
        request = self.factory.get('/test/')
        request.user = self.regular_user
        
        permission = IsAdmin()
        view = MockView()
        
        self.assertFalse(permission.has_permission(request, view))
    
    def test_is_admin_permission_with_unauthenticated(self):
        """Test IsAdmin permission denies unauthenticated users"""
        request = self.factory.get('/test/')
        request.user = AnonymousUser()
        
        permission = IsAdmin()
        view = MockView()
        
        self.assertFalse(permission.has_permission(request, view))
    
    def test_is_admin_permission_with_no_profile(self):
        """Test IsAdmin permission handles users without profiles"""
        user_no_profile = User.objects.create_user('noprofile', 'no@test.com', 'pass')
        # Delete the profile that was auto-created
        user_no_profile.profile.delete()
        
        request = self.factory.get('/test/')
        request.user = user_no_profile
        
        permission = IsAdmin()
        view = MockView()
        
        self.assertFalse(permission.has_permission(request, view))
    
    def test_is_admin_or_moderator_permission_with_admin(self):
        """Test IsAdminOrModerator permission allows admin users"""
        request = self.factory.get('/test/')
        request.user = self.admin_user
        
        permission = IsAdminOrModerator()
        view = MockView()
        
        self.assertTrue(permission.has_permission(request, view))
    
    def test_is_admin_or_moderator_permission_with_moderator(self):
        """Test IsAdminOrModerator permission allows moderator users"""
        request = self.factory.get('/test/')
        request.user = self.moderator_user
        
        permission = IsAdminOrModerator()
        view = MockView()
        
        self.assertTrue(permission.has_permission(request, view))
    
    def test_is_admin_or_moderator_permission_with_regular_user(self):
        """Test IsAdminOrModerator permission denies regular users"""
        request = self.factory.get('/test/')
        request.user = self.regular_user
        
        permission = IsAdminOrModerator()
        view = MockView()
        
        self.assertFalse(permission.has_permission(request, view))
    
    def test_is_space_moderator_permission_with_admin(self):
        """Test IsSpaceModerator permission allows admin for any space"""
        request = self.factory.get('/test/')
        request.user = self.admin_user
        
        permission = IsSpaceModerator()
        view = MockView(kwargs={'space_pk': self.space.id})
        
        self.assertTrue(permission.has_permission(request, view))
    
    def test_is_space_moderator_permission_with_assigned_moderator(self):
        """Test IsSpaceModerator permission allows assigned moderator"""
        request = self.factory.get('/test/')
        request.user = self.moderator_user
        
        permission = IsSpaceModerator()
        view = MockView(kwargs={'space_pk': self.space.id})
        
        self.assertTrue(permission.has_permission(request, view))
    
    def test_is_space_moderator_permission_with_unassigned_moderator(self):
        """Test IsSpaceModerator permission denies unassigned moderator"""
        other_space = Space.objects.create(
            title='Other Space',
            description='Other description',
            creator=self.admin_user
        )
        
        request = self.factory.get('/test/')
        request.user = self.moderator_user
        
        permission = IsSpaceModerator()
        view = MockView(kwargs={'space_pk': other_space.id})
        
        self.assertFalse(permission.has_permission(request, view))
    
    def test_is_space_moderator_permission_with_pk_parameter(self):
        """Test IsSpaceModerator permission works with 'pk' parameter"""
        request = self.factory.get('/test/')
        request.user = self.moderator_user
        
        permission = IsSpaceModerator()
        view = MockView(kwargs={'pk': self.space.id})
        
        self.assertTrue(permission.has_permission(request, view))
    
    def test_is_space_moderator_permission_no_space_id(self):
        """Test IsSpaceModerator permission denies when no space ID provided"""
        request = self.factory.get('/test/')
        request.user = self.moderator_user
        
        permission = IsSpaceModerator()
        view = MockView(kwargs={})
        
        self.assertFalse(permission.has_permission(request, view))
    
    def test_is_space_moderator_permission_nonexistent_space(self):
        """Test IsSpaceModerator permission handles nonexistent space"""
        request = self.factory.get('/test/')
        request.user = self.moderator_user
        
        permission = IsSpaceModerator()
        view = MockView(kwargs={'space_pk': 99999})  # Non-existent space ID
        
        self.assertFalse(permission.has_permission(request, view))
    
    def test_can_change_user_type_permission_with_admin(self):
        """Test CanChangeUserType permission allows admin users"""
        request = self.factory.post('/test/')
        request.user = self.admin_user
        
        permission = CanChangeUserType()
        view = MockView()
        
        self.assertTrue(permission.has_permission(request, view))
    
    def test_can_change_user_type_permission_with_moderator(self):
        """Test CanChangeUserType permission allows moderator users"""
        request = self.factory.post('/test/')
        request.user = self.moderator_user
        
        permission = CanChangeUserType()
        view = MockView()
        
        self.assertTrue(permission.has_permission(request, view))
    
    def test_can_change_user_type_permission_with_regular_user(self):
        """Test CanChangeUserType permission denies regular users"""
        request = self.factory.post('/test/')
        request.user = self.regular_user
        
        permission = CanChangeUserType()
        view = MockView()
        
        self.assertFalse(permission.has_permission(request, view))
    
    def test_can_change_user_type_permission_with_unauthenticated(self):
        """Test CanChangeUserType permission denies unauthenticated users"""
        request = self.factory.post('/test/')
        request.user = AnonymousUser()
        
        permission = CanChangeUserType()
        view = MockView()
        
        self.assertFalse(permission.has_permission(request, view))
    
    def test_permission_with_missing_profile(self):
        """Test permissions handle missing profile gracefully"""
        user_no_profile = User.objects.create_user('noprofile2', 'no2@test.com', 'pass')
        user_no_profile.profile.delete()
        
        request = self.factory.get('/test/')
        request.user = user_no_profile
        
        # Test all permission classes
        permissions = [
            IsAdmin(),
            IsAdminOrModerator(),
            IsSpaceModerator(),
            CanChangeUserType()
        ]
        
        for permission in permissions:
            view = MockView(kwargs={'space_pk': self.space.id})
            self.assertFalse(
                permission.has_permission(request, view),
                f"{permission.__class__.__name__} should deny users without profiles"
            )