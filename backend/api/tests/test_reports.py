from django.urls import reverse
from rest_framework.test import APITestCase, APIClient
from django.contrib.auth.models import User
from api.models import Space, Node, Discussion, Profile, Report, SpaceModerator, Archive

class ReportFeatureTestsUpdated(APITestCase):
    def setUp(self):
        self.admin = User.objects.create_user(username='admin', password='pass', email='a@a.com')
        self.admin.profile.user_type = Profile.ADMIN
        self.admin.profile.save()

        self.moderator = User.objects.create_user(username='mod', password='pass', email='m@m.com')
        self.moderator.profile.user_type = Profile.MODERATOR
        self.moderator.profile.save()

        self.user = User.objects.create_user(username='user', password='pass', email='u@u.com')
        self.user2 = User.objects.create_user(username='user2', password='pass', email='u2@u.com')

        self.space = Space.objects.create(title='Test Space', description='D', creator=self.admin)
        self.space.collaborators.add(self.admin, self.moderator, self.user, self.user2)
        SpaceModerator.objects.create(user=self.moderator, space=self.space, assigned_by=self.admin)

        self.node = Node.objects.create(label='Test Node', created_by=self.admin, space=self.space)
        self.discussion = Discussion.objects.create(space=self.space, user=self.admin, text='Hello World, this is a test discussion.')

        self.client = APIClient()

    def auth(self, who):
        self.client.force_authenticate(user=who)

    def test_report_list_endpoints_by_status(self):
        # Create reports with different statuses
        report_open = Report.objects.create(reporter=self.user, content_type='space', content_id=self.space.id, reason='SPAM', status=Report.STATUS_OPEN, space=self.space)
        report_dismissed = Report.objects.create(reporter=self.user, content_type='node', content_id=self.node.id, reason='SPAM', status=Report.STATUS_DISMISSED, space=self.space)
        report_archived = Report.objects.create(reporter=self.user, content_type='discussion', content_id=self.discussion.id, reason='SPAM', status=Report.STATUS_ARCHIVED, space=self.space)
        
        self.auth(self.admin)
        
        # Test /reports/open/
        res_open = self.client.get(reverse('report-open'))
        self.assertEqual(res_open.status_code, 200)
        self.assertEqual(len(res_open.data), 1)
        self.assertEqual(res_open.data[0]['content_id'], report_open.content_id)
        self.assertEqual(res_open.data[0]['reports'][0]['id'], report_open.id)

        # Test /reports/dismissed/
        res_dismissed = self.client.get(reverse('report-dismissed'))
        self.assertEqual(res_dismissed.status_code, 200)
        self.assertEqual(len(res_dismissed.data), 1)
        self.assertEqual(res_dismissed.data[0]['content_id'], report_dismissed.content_id)
        self.assertEqual(res_dismissed.data[0]['reports'][0]['id'], report_dismissed.id)

        # Test /reports/archived/
        res_archived = self.client.get(reverse('report-archived'))
        self.assertEqual(res_archived.status_code, 200)
        self.assertEqual(len(res_archived.data), 1)
        self.assertEqual(res_archived.data[0]['content_id'], report_archived.content_id)
        self.assertEqual(res_archived.data[0]['reports'][0]['id'], report_archived.id)

    def test_dismiss_action_dismisses_all_open_reports(self):
        # User 1 reports the node
        self.auth(self.user)
        self.client.post(reverse('report-list'), {'content_type': 'node', 'content_id': self.node.id, 'reason': 'SPAM'}, format='json')
        
        # User 2 also reports the same node
        self.auth(self.user2)
        res = self.client.post(reverse('report-list'), {'content_type': 'node', 'content_id': self.node.id, 'reason': 'INAPPROPRIATE'}, format='json')
        report_id_to_dismiss = res.data['id']

        self.node.refresh_from_db()
        self.assertEqual(self.node.report_count, 2)
        
        # Admin dismisses one of the reports
        self.auth(self.admin)
        self.client.post(reverse('report-dismiss', args=[report_id_to_dismiss]))
        
        # Check that all reports for the node are dismissed and count is zero
        self.node.refresh_from_db()
        self.assertEqual(self.node.report_count, 0)
        self.assertFalse(self.node.is_reported)
        self.assertEqual(Report.objects.filter(content_type='node', content_id=self.node.id, status=Report.STATUS_DISMISSED).count(), 2)

    def test_archiving_item_archives_open_reports(self):
        # Create a few reports on a space via API to trigger report_count update
        self.auth(self.user)
        self.client.post(reverse('report-list'), {'content_type': 'space', 'content_id': self.space.id, 'reason': 'SPAM'}, format='json')
        self.auth(self.user2)
        self.client.post(reverse('report-list'), {'content_type': 'space', 'content_id': self.space.id, 'reason': 'INAPPROPRIATE'}, format='json')
        
        self.space.refresh_from_db()
        self.assertEqual(self.space.report_count, 2)

        # Admin archives the space
        self.auth(self.admin)
        self.client.post(reverse('archive_item'), {'content_type': 'space', 'content_id': self.space.id, 'reason': 'Archived'})
        
        self.space.refresh_from_db()
        self.assertTrue(self.space.is_archived)
        self.assertEqual(self.space.report_count, 0) # report count should be recomputed to 0
        self.assertEqual(Report.objects.filter(content_type='space', content_id=self.space.id, status=Report.STATUS_ARCHIVED).count(), 2)

    def test_report_serializer_includes_content_object_label(self):
        # Create a report for each content type
        report_space = Report.objects.create(reporter=self.user, content_type='space', content_id=self.space.id, reason='SPAM', space=self.space)
        report_node = Report.objects.create(reporter=self.user, content_type='node', content_id=self.node.id, reason='SPAM', space=self.space)
        report_discussion = Report.objects.create(reporter=self.user, content_type='discussion', content_id=self.discussion.id, reason='SPAM', space=self.space)
        report_profile = Report.objects.create(reporter=self.user, content_type='profile', content_id=self.user2.id, reason='FAKE_ACCOUNT')
        
        self.auth(self.admin)
        
        # Check space report label
        res_space = self.client.get(reverse('report-detail', args=[report_space.id]))
        self.assertEqual(res_space.data['content_object_label'], self.space.title)
        self.assertIn('content_object', res_space.data)
        self.assertEqual(res_space.data['content_object']['title'], self.space.title)
        
        # Check node report label
        res_node = self.client.get(reverse('report-detail', args=[report_node.id]))
        self.assertEqual(res_node.data['content_object_label'], self.node.label)
        self.assertIn('content_object', res_node.data)
        self.assertEqual(res_node.data['content_object']['label'], self.node.label)

        # Check discussion report label
        res_discussion = self.client.get(reverse('report-detail', args=[report_discussion.id]))
        expected_label = (self.discussion.text[:47] + '...') if len(self.discussion.text) > 50 else self.discussion.text
        self.assertEqual(res_discussion.data['content_object_label'], expected_label)
        self.assertIn('content_object', res_discussion.data)
        self.assertIn('text', res_discussion.data['content_object'])

        # Check profile report label
        res_profile = self.client.get(reverse('report-detail', args=[report_profile.id]))
        self.assertEqual(res_profile.data['content_object_label'], self.user2.username)
        self.assertIn('content_object', res_profile.data)
        self.assertEqual(res_profile.data['content_object']['user']['username'], self.user2.username)

    def test_default_report_list_shows_open_reports(self):
        report_open = Report.objects.create(reporter=self.user, content_type='space', content_id=self.space.id, reason='SPAM', status=Report.STATUS_OPEN, space=self.space)
        Report.objects.create(reporter=self.user, content_type='node', content_id=self.node.id, reason='SPAM', status=Report.STATUS_DISMISSED, space=self.space)
        
        self.auth(self.admin)
        res = self.client.get(reverse('report-list'))
        
        self.assertEqual(res.status_code, 200)
        self.assertEqual(len(res.data), 1)
        self.assertEqual(res.data[0]['content_id'], report_open.content_id)
        self.assertEqual(res.data[0]['reports'][0]['status'], Report.STATUS_OPEN)
