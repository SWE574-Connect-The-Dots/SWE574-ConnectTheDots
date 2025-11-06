from django.urls import reverse
from rest_framework.test import APITestCase, APIClient
from django.contrib.auth.models import User
from api.models import Space, Node, Discussion, Profile, Report, SpaceModerator

class ReportFeatureTests(APITestCase):
    def setUp(self):
        self.admin = User.objects.create_user(username='admin', password='pass', email='a@a.com')
        self.admin.profile.user_type = Profile.ADMIN
        self.admin.profile.save()

        self.moderator = User.objects.create_user(username='mod', password='pass', email='m@m.com')
        self.moderator.profile.user_type = Profile.MODERATOR
        self.moderator.profile.save()

        self.user = User.objects.create_user(username='user', password='pass', email='u@u.com')

        self.space = Space.objects.create(title='S', description='D', creator=self.admin)
        self.space.collaborators.add(self.admin)
        SpaceModerator.objects.create(user=self.moderator, space=self.space, assigned_by=self.admin)

        self.node = Node.objects.create(label='N', created_by=self.admin, space=self.space)
        self.discussion = Discussion.objects.create(space=self.space, user=self.admin, text='Hi')

        self.client = APIClient()

    def auth(self, who):
        self.client.force_authenticate(user=who)

    def test_reasons_endpoint(self):
        self.auth(self.user)
        url = reverse('report-reasons')
        res = self.client.get(url)
        self.assertEqual(res.status_code, 200)
        self.assertIn('reasons', res.data)
        self.assertIn('space', res.data['reasons'])
        self.assertTrue(any(r['code'] == 'INAPPROPRIATE' for r in res.data['reasons']['space']))

    def test_create_space_report_and_aggregates(self):
        self.auth(self.user)
        url = reverse('report-list')
        payload = {
            'content_type': 'space',
            'content_id': self.space.id,
            'reason': 'INAPPROPRIATE'
        }
        res = self.client.post(url, payload, format='json')
        self.assertEqual(res.status_code, 201)
        self.space.refresh_from_db()
        self.assertEqual(self.space.report_count, 1)
        self.assertTrue(self.space.is_reported)

    def test_moderator_visibility_scoped_to_space(self):
        # Create report in this space
        self.auth(self.user)
        url = reverse('report-list')
        self.client.post(url, {'content_type': 'node', 'content_id': self.node.id, 'reason': 'SPAM'}, format='json')
        # As moderator, can list and see this report
        self.auth(self.moderator)
        res = self.client.get(url)
        self.assertEqual(res.status_code, 200)
        self.assertGreaterEqual(len(res.data), 1)

    def test_status_change_updates_aggregates(self):
        # Create two reports on discussion
        self.auth(self.user)
        url = reverse('report-list')
        self.client.post(url, {'content_type': 'discussion', 'content_id': self.discussion.id, 'reason': 'SPAM'}, format='json')
        self.client.post(url, {'content_type': 'discussion', 'content_id': self.discussion.id, 'reason': 'HARASSMENT'}, format='json')
        self.discussion.refresh_from_db()
        self.assertEqual(self.discussion.report_count, 2)
        # Admin dismisses all
        self.auth(self.admin)
        reports = Report.objects.filter(content_type='discussion', content_id=self.discussion.id)
        for r in reports:
            res = self.client.patch(reverse('report-detail', args=[r.id]), {'status': 'DISMISSED'}, format='json')
            self.assertEqual(res.status_code, 200)
        self.discussion.refresh_from_db()
        self.assertEqual(self.discussion.report_count, 0)
        self.assertFalse(self.discussion.is_reported)

    def test_profile_reports_admin_only_visibility(self):
        # Report a profile (target = self.user.id)
        self.auth(self.admin)
        url = reverse('report-list')
        res = self.client.post(url, {'content_type': 'profile', 'content_id': self.user.id, 'reason': 'FAKE_ACCOUNT'}, format='json')
        self.assertEqual(res.status_code, 201)
        # Moderator should not see profile report
        self.auth(self.moderator)
        res = self.client.get(url)
        self.assertEqual(res.status_code, 200)
        self.assertTrue(all(item['content_type'] != 'profile' for item in res.data))
