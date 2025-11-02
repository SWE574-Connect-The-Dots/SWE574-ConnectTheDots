from django.contrib.auth.models import User
from rest_framework.test import APITestCase
from rest_framework import status

from api.models import Space, Discussion


class DiscussionReactionAPITests(APITestCase):
    def setUp(self):
        self.u1 = User.objects.create_user(username='u1', password='pw')
        self.u2 = User.objects.create_user(username='u2', password='pw')
        self.space = Space.objects.create(title='S', description='D', creator=self.u1)
        # Current permissions require collaborators for write actions
        self.space.collaborators.add(self.u1)
        self.space.collaborators.add(self.u2)
        # Create an initial discussion owned by u1
        self.discussion = Discussion.objects.create(space=self.space, user=self.u1, text='Hello')

    def test_upvote_then_toggle_off(self):
        self.client.login(username='u2', password='pw')
        url = f'/api/spaces/{self.space.id}/discussions/{self.discussion.id}/react/'

        # First upvote
        r1 = self.client.post(url, {'value': 'up'}, format='json')
        self.assertEqual(r1.status_code, status.HTTP_200_OK)
        data1 = r1.data['discussion']
        self.assertEqual(data1['upvotes'], 1)
        self.assertEqual(data1['downvotes'], 0)
        self.assertEqual(data1['user_reaction'], 'up')

        # Same upvote again toggles off
        r2 = self.client.post(url, {'value': 'up'}, format='json')
        self.assertEqual(r2.status_code, status.HTTP_200_OK)
        self.assertTrue(r2.data.get('toggled_off'))
        data2 = r2.data['discussion']
        self.assertEqual(data2['upvotes'], 0)
        self.assertEqual(data2['downvotes'], 0)
        # Serializer computes user_reaction for current user; after delete, should be None
        self.assertIsNone(data2['user_reaction'])

    def test_switch_upvote_to_downvote(self):
        self.client.login(username='u2', password='pw')
        url = f'/api/spaces/{self.space.id}/discussions/{self.discussion.id}/react/'

        # Upvote first
        r1 = self.client.post(url, {'value': 'up'}, format='json')
        self.assertEqual(r1.status_code, status.HTTP_200_OK)
        self.assertEqual(r1.data['discussion']['upvotes'], 1)
        self.assertEqual(r1.data['discussion']['downvotes'], 0)

        # Then switch to downvote
        r2 = self.client.post(url, {'value': 'down'}, format='json')
        self.assertEqual(r2.status_code, status.HTTP_200_OK)
        self.assertFalse(r2.data.get('toggled_off'))
        data2 = r2.data['discussion']
        self.assertEqual(data2['upvotes'], 0)
        self.assertEqual(data2['downvotes'], 1)
        self.assertEqual(data2['user_reaction'], 'down')
