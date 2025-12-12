from django.urls import reverse
from rest_framework.test import APITestCase, APIClient
from django.contrib.auth.models import User
from api.models import Space, Node, Profile, Report, SpaceModerator, Archive


class ArchiveAPIAdminTest(APITestCase):
    def setUp(self):
        self.admin = User.objects.create_user(username='admin', password='pass', email='a@a.com')
        self.admin.profile.user_type = Profile.ADMIN
        self.admin.profile.save()

        self.user = User.objects.create_user(username='user', password='pass', email='u@u.com')
        self.space = Space.objects.create(title='Test Space', description='D', creator=self.admin)
        self.node = Node.objects.create(label='Test Node', created_by=self.admin, space=self.space)

        self.client = APIClient()
        self.client.force_authenticate(user=self.admin)

    def test_admin_can_archive_space(self):
        response = self.client.post(
            reverse('archive_item'),
            {'content_type': 'space', 'content_id': self.space.id, 'reason': 'Test'},
            format='json'
        )
        self.assertEqual(response.status_code, 201)
        self.space.refresh_from_db()
        self.assertTrue(self.space.is_archived)

    def test_admin_can_archive_node(self):
        response = self.client.post(
            reverse('archive_item'),
            {'content_type': 'node', 'content_id': self.node.id, 'reason': 'Test'},
            format='json'
        )
        self.assertEqual(response.status_code, 201)
        self.node.refresh_from_db()
        self.assertTrue(self.node.is_archived)

    def test_admin_can_archive_profile(self):
        response = self.client.post(
            reverse('archive_item'),
            {'content_type': 'profile', 'content_id': self.user.id, 'reason': 'Test'},
            format='json'
        )
        self.assertEqual(response.status_code, 201)
        self.user.profile.refresh_from_db()
        self.assertTrue(self.user.profile.is_archived)

    def test_admin_can_list_archived_items(self):
        Archive.objects.create(
            content_type=Archive.CONTENT_SPACE,
            content_id=self.space.id,
            archived_by=self.admin
        )
        response = self.client.get(reverse('list_archived_items'))
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.data), 1)

    def test_admin_can_restore_archived_space(self):
        self.space.is_archived = True
        self.space.save()
        archive = Archive.objects.create(
            content_type=Archive.CONTENT_SPACE,
            content_id=self.space.id,
            archived_by=self.admin
        )

        response = self.client.post(reverse('restore_archived_item', args=[archive.id]))
        self.assertEqual(response.status_code, 200)
        self.space.refresh_from_db()
        self.assertFalse(self.space.is_archived)

    def test_admin_can_restore_archived_node(self):
        self.node.is_archived = True
        self.node.save()
        archive = Archive.objects.create(
            content_type=Archive.CONTENT_NODE,
            content_id=self.node.id,
            archived_by=self.admin
        )

        response = self.client.post(reverse('restore_archived_item', args=[archive.id]))
        self.assertEqual(response.status_code, 200)
        self.node.refresh_from_db()
        self.assertFalse(self.node.is_archived)

    def test_admin_can_restore_archived_profile(self):
        self.user.profile.is_archived = True
        self.user.profile.save()
        archive = Archive.objects.create(
            content_type=Archive.CONTENT_PROFILE,
            content_id=self.user.id,
            archived_by=self.admin
        )

        response = self.client.post(reverse('restore_archived_item', args=[archive.id]))
        self.assertEqual(response.status_code, 200)
        self.user.profile.refresh_from_db()
        self.assertFalse(self.user.profile.is_archived)


class ArchiveAPIModeratorTest(APITestCase):
    def setUp(self):
        self.admin = User.objects.create_user(username='admin', password='pass', email='a@a.com')
        self.admin.profile.user_type = Profile.ADMIN
        self.admin.profile.save()

        self.moderator = User.objects.create_user(username='mod', password='pass', email='m@m.com')
        self.moderator.profile.user_type = Profile.MODERATOR
        self.moderator.profile.save()

        self.space = Space.objects.create(title='Moderated Space', description='D', creator=self.admin)
        self.space2 = Space.objects.create(title='Other Space', description='D', creator=self.admin)
        
        SpaceModerator.objects.create(user=self.moderator, space=self.space, assigned_by=self.admin)

        self.node_in_space = Node.objects.create(label='Node in space', created_by=self.admin, space=self.space)

        self.client = APIClient()
        self.client.force_authenticate(user=self.moderator)

    def test_moderator_can_archive_space_they_moderate(self):
        response = self.client.post(
            reverse('archive_item'),
            {'content_type': 'space', 'content_id': self.space.id, 'reason': 'Test'},
            format='json'
        )
        self.assertEqual(response.status_code, 201)
        self.space.refresh_from_db()
        self.assertTrue(self.space.is_archived)

    def test_moderator_cannot_archive_space_outside_jurisdiction(self):
        response = self.client.post(
            reverse('archive_item'),
            {'content_type': 'space', 'content_id': self.space2.id, 'reason': 'Test'},
            format='json'
        )
        self.assertEqual(response.status_code, 403)


class ArchiveAPIRegularUserTest(APITestCase):
    def setUp(self):
        self.admin = User.objects.create_user(username='admin', password='pass', email='a@a.com')
        self.admin.profile.user_type = Profile.ADMIN
        self.admin.profile.save()

        self.user = User.objects.create_user(username='user', password='pass', email='u@u.com')
        self.space = Space.objects.create(title='Test Space', description='D', creator=self.admin)

        self.client = APIClient()
        self.client.force_authenticate(user=self.user)

    def test_regular_user_cannot_archive_items(self):
        response = self.client.post(
            reverse('archive_item'),
            {'content_type': 'space', 'content_id': self.space.id, 'reason': 'Test'},
            format='json'
        )
        self.assertEqual(response.status_code, 403)

    def test_regular_user_cannot_list_archives(self):
        response = self.client.get(reverse('list_archived_items'))
        self.assertEqual(response.status_code, 403)
