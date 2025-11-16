from django.test import TestCase
from django.contrib.auth.models import User
from rest_framework.test import APIClient
from rest_framework import status
from django.urls import reverse
from ..models import Space, Discussion, Profile, SpaceModerator, DiscussionReaction, Report


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

    def test_admin_can_delete_discussion(self):
        """Test that admin can delete any discussion"""
        admin = User.objects.create_user(
            username='admin', email='admin@example.com', password='password123'
        )
        admin.profile.user_type = Profile.ADMIN
        admin.profile.save()
        
        self.client.force_authenticate(user=admin)
        url = reverse('space-delete-discussion', kwargs={'pk': self.space.id, 'discussion_id': self.discussion1.id})
        response = self.client.delete(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('message', response.data)
        self.assertEqual(Discussion.objects.count(), 1)
        self.assertFalse(Discussion.objects.filter(id=self.discussion1.id).exists())

    def test_moderator_can_delete_discussion_in_their_space(self):
        """Test that moderator can delete discussions in spaces they moderate"""
        moderator = User.objects.create_user(
            username='moderator', email='moderator@example.com', password='password123'
        )
        moderator.profile.user_type = Profile.MODERATOR
        moderator.profile.save()
        
        # Assign moderator to the space
        SpaceModerator.objects.create(user=moderator, space=self.space, assigned_by=self.creator)
        
        self.client.force_authenticate(user=moderator)
        url = reverse('space-delete-discussion', kwargs={'pk': self.space.id, 'discussion_id': self.discussion1.id})
        response = self.client.delete(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(Discussion.objects.count(), 1)
        self.assertFalse(Discussion.objects.filter(id=self.discussion1.id).exists())

    def test_space_moderator_without_profile_flag_can_delete(self):
        """Test that a user assigned as SpaceModerator (without profile flag) can delete discussions"""
        space_moderator = User.objects.create_user(
            username='space_mod', email='space_mod@example.com', password='password123'
        )
        # Note: user_type remains USER (default), not MODERATOR
        self.assertEqual(space_moderator.profile.user_type, Profile.USER)
        
        # Assign as SpaceModerator for the space
        SpaceModerator.objects.create(user=space_moderator, space=self.space, assigned_by=self.creator)
        
        self.client.force_authenticate(user=space_moderator)
        url = reverse('space-delete-discussion', kwargs={'pk': self.space.id, 'discussion_id': self.discussion1.id})
        response = self.client.delete(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(Discussion.objects.count(), 1)
        self.assertFalse(Discussion.objects.filter(id=self.discussion1.id).exists())

    def test_moderator_cannot_delete_discussion_outside_their_space(self):
        """Test that moderator cannot delete discussions in spaces they don't moderate"""
        moderator = User.objects.create_user(
            username='moderator', email='moderator@example.com', password='password123'
        )
        moderator.profile.user_type = Profile.MODERATOR
        moderator.profile.save()
        
        # Create another space (moderator is not assigned to it)
        other_space = Space.objects.create(
            title='Other Space',
            description='Other Description',
            creator=self.creator
        )
        other_discussion = Discussion.objects.create(
            space=other_space,
            user=self.creator,
            text='Other discussion'
        )
        
        self.client.force_authenticate(user=moderator)
        url = reverse('space-delete-discussion', kwargs={'pk': other_space.id, 'discussion_id': other_discussion.id})
        response = self.client.delete(url)
        
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        # Check for either 'error' (our custom message) or 'detail' (DRF default)
        self.assertTrue('error' in response.data or 'detail' in response.data)
        self.assertEqual(Discussion.objects.count(), 3)  # All discussions still exist

    def test_regular_user_cannot_delete_discussion(self):
        """Test that regular users cannot delete discussions"""
        self.client.force_authenticate(user=self.collaborator)
        url = reverse('space-delete-discussion', kwargs={'pk': self.space.id, 'discussion_id': self.discussion1.id})
        response = self.client.delete(url)
        
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)
        # Check for either 'error' (our custom message) or 'detail' (DRF default)
        self.assertTrue('error' in response.data or 'detail' in response.data)
        self.assertEqual(Discussion.objects.count(), 2)  # Discussion still exists

    def test_delete_nonexistent_discussion(self):
        """Test that deleting a non-existent discussion returns 404"""
        admin = User.objects.create_user(
            username='admin', email='admin@example.com', password='password123'
        )
        admin.profile.user_type = Profile.ADMIN
        admin.profile.save()
        
        self.client.force_authenticate(user=admin)
        url = reverse('space-delete-discussion', kwargs={'pk': self.space.id, 'discussion_id': 99999})
        response = self.client.delete(url)
        
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
        self.assertIn('error', response.data)

    def test_delete_discussion_deletes_related_reactions(self):
        """Test that deleting a discussion also deletes related reactions (CASCADE)"""
        admin = User.objects.create_user(
            username='admin', email='admin@example.com', password='password123'
        )
        admin.profile.user_type = Profile.ADMIN
        admin.profile.save()
        
        # Add reactions to the discussion
        DiscussionReaction.objects.create(
            discussion=self.discussion1,
            user=self.creator,
            value=DiscussionReaction.UPVOTE
        )
        DiscussionReaction.objects.create(
            discussion=self.discussion1,
            user=self.collaborator,
            value=DiscussionReaction.DOWNVOTE
        )
        
        self.assertEqual(DiscussionReaction.objects.filter(discussion=self.discussion1).count(), 2)
        
        self.client.force_authenticate(user=admin)
        url = reverse('space-delete-discussion', kwargs={'pk': self.space.id, 'discussion_id': self.discussion1.id})
        response = self.client.delete(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        # Verify discussion is deleted
        self.assertFalse(Discussion.objects.filter(id=self.discussion1.id).exists())
        # Verify reactions are also deleted (CASCADE)
        self.assertEqual(DiscussionReaction.objects.filter(discussion_id=self.discussion1.id).count(), 0)

    def test_delete_discussion_deletes_related_reports(self):
        """Test that deleting a discussion also deletes related reports"""
        admin = User.objects.create_user(
            username='admin', email='admin@example.com', password='password123'
        )
        admin.profile.user_type = Profile.ADMIN
        admin.profile.save()
        
        # Create reports for the discussion
        report1 = Report.objects.create(
            content_type=Report.CONTENT_DISCUSSION,
            content_id=self.discussion1.id,
            reason='SPAM',
            reporter=self.creator,
            space=self.space
        )
        report2 = Report.objects.create(
            content_type=Report.CONTENT_DISCUSSION,
            content_id=self.discussion1.id,
            reason='HARASSMENT',
            reporter=self.collaborator,
            space=self.space
        )
        
        self.assertEqual(Report.objects.filter(content_type=Report.CONTENT_DISCUSSION, content_id=self.discussion1.id).count(), 2)
        
        self.client.force_authenticate(user=admin)
        url = reverse('space-delete-discussion', kwargs={'pk': self.space.id, 'discussion_id': self.discussion1.id})
        response = self.client.delete(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        # Verify discussion is deleted
        self.assertFalse(Discussion.objects.filter(id=self.discussion1.id).exists())
        # Verify reports are also deleted
        self.assertEqual(Report.objects.filter(content_type=Report.CONTENT_DISCUSSION, content_id=self.discussion1.id).count(), 0)
        # Verify the report objects are actually gone
        self.assertFalse(Report.objects.filter(id=report1.id).exists())
        self.assertFalse(Report.objects.filter(id=report2.id).exists()) 