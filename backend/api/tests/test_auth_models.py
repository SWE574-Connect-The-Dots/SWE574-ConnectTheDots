from django.test import TestCase
from django.contrib.auth.models import User
from api.models import Profile, Space, SpaceModerator


class AuthorizationModelsTest(TestCase):
    """Focused tests for authorization-related model functionality"""
    
    def setUp(self):
        self.user1 = User.objects.create_user('user1', 'user1@test.com', 'pass')
        self.user2 = User.objects.create_user('user2', 'user2@test.com', 'pass')
        self.admin = User.objects.create_user('admin', 'admin@test.com', 'pass')
        
        # Set admin user type
        self.admin.profile.user_type = Profile.ADMIN
        self.admin.profile.save()
        
        self.space = Space.objects.create(
            title='Test Space',
            description='Test description',
            creator=self.admin
        )
    
    def test_profile_user_type_constants(self):
        """Test that user type constants are correct"""
        self.assertEqual(Profile.ADMIN, 1)
        self.assertEqual(Profile.MODERATOR, 2) 
        self.assertEqual(Profile.USER, 3)
    
    def test_profile_default_user_type(self):
        """Test that new profiles default to USER type"""
        profile = self.user1.profile
        self.assertEqual(profile.user_type, Profile.USER)
    
    def test_profile_user_type_choices(self):
        """Test user type choices are properly defined"""
        choices = dict(Profile.USER_TYPE_CHOICES)
        self.assertEqual(choices[Profile.ADMIN], 'Admin')
        self.assertEqual(choices[Profile.MODERATOR], 'Moderator')
        self.assertEqual(choices[Profile.USER], 'User')
    
    def test_space_moderator_unique_constraint(self):
        """Test that user can only be assigned once per space"""
        # Create first assignment
        SpaceModerator.objects.create(
            user=self.user1,
            space=self.space,
            assigned_by=self.admin
        )
        
        # Try to create duplicate - should raise IntegrityError
        with self.assertRaises(Exception):
            SpaceModerator.objects.create(
                user=self.user1,
                space=self.space,
                assigned_by=self.admin
            )
    
    def test_space_moderator_str_representation(self):
        """Test SpaceModerator string representation"""
        moderator_assignment = SpaceModerator.objects.create(
            user=self.user1,
            space=self.space,
            assigned_by=self.admin
        )
        
        expected = f"{self.user1.username} moderates {self.space.title}"
        self.assertEqual(str(moderator_assignment), expected)
    
    def test_profile_can_moderate_space_logic(self):
        """Test the can_moderate_space method logic"""
        # Admin should always be able to moderate
        self.assertTrue(self.admin.profile.can_moderate_space(self.space))
        
        # Regular user should not be able to moderate
        self.assertFalse(self.user1.profile.can_moderate_space(self.space))
        
        # Make user1 a moderator but not assign to space
        self.user1.profile.user_type = Profile.MODERATOR
        self.user1.profile.save()
        self.assertFalse(self.user1.profile.can_moderate_space(self.space))
        
        # Assign user1 to moderate the space
        SpaceModerator.objects.create(
            user=self.user1,
            space=self.space,
            assigned_by=self.admin
        )
        self.assertTrue(self.user1.profile.can_moderate_space(self.space))
    
    def test_space_get_moderators_method(self):
        """Test Space.get_moderators() method"""
        # Initially no moderators
        moderators = self.space.get_moderators()
        self.assertEqual(moderators.count(), 0)
        
        # Assign two moderators
        self.user1.profile.user_type = Profile.MODERATOR
        self.user1.profile.save()
        self.user2.profile.user_type = Profile.MODERATOR  
        self.user2.profile.save()
        
        SpaceModerator.objects.create(
            user=self.user1,
            space=self.space,
            assigned_by=self.admin
        )
        SpaceModerator.objects.create(
            user=self.user2,
            space=self.space,
            assigned_by=self.admin
        )
        
        moderators = self.space.get_moderators()
        self.assertEqual(moderators.count(), 2)
        self.assertIn(self.user1, moderators)
        self.assertIn(self.user2, moderators)
    
    def test_space_is_moderator_method(self):
        """Test Space.is_moderator() method"""
        # Initially user1 is not a moderator
        self.assertFalse(self.space.is_moderator(self.user1))
        
        # Assign user1 as moderator
        SpaceModerator.objects.create(
            user=self.user1,
            space=self.space,
            assigned_by=self.admin
        )
        
        self.assertTrue(self.space.is_moderator(self.user1))
        self.assertFalse(self.space.is_moderator(self.user2))
    
    def test_profile_user_type_helper_methods(self):
        """Test is_admin, is_moderator, is_regular_user methods"""
        # Test admin profile
        admin_profile = self.admin.profile
        self.assertTrue(admin_profile.is_admin())
        self.assertFalse(admin_profile.is_moderator())
        self.assertFalse(admin_profile.is_regular_user())
        
        # Test moderator profile
        self.user1.profile.user_type = Profile.MODERATOR
        self.user1.profile.save()
        moderator_profile = self.user1.profile
        self.assertFalse(moderator_profile.is_admin())
        self.assertTrue(moderator_profile.is_moderator())
        self.assertFalse(moderator_profile.is_regular_user())
        
        # Test regular user profile
        regular_profile = self.user2.profile
        self.assertFalse(regular_profile.is_admin())
        self.assertFalse(regular_profile.is_moderator())
        self.assertTrue(regular_profile.is_regular_user())
    
    def test_cascade_deletion(self):
        """Test that deleting related objects properly cascades"""
        # Create moderator assignment
        SpaceModerator.objects.create(
            user=self.user1,
            space=self.space,
            assigned_by=self.admin
        )
        
        # Delete space - should delete moderator assignment
        space_id = self.space.id
        self.space.delete()
        
        self.assertFalse(
            SpaceModerator.objects.filter(space_id=space_id).exists()
        )
        
    def test_profile_signal_creation(self):
        """Test that profile is automatically created for new users"""
        new_user = User.objects.create_user('newuser', 'new@test.com', 'pass')
        
        # Profile should be automatically created
        self.assertTrue(hasattr(new_user, 'profile'))
        self.assertEqual(new_user.profile.user_type, Profile.USER)