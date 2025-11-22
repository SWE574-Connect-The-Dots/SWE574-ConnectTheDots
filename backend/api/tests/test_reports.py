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

    def test_admin_can_dismiss_report(self):
        """Test that admin can dismiss a report using the dismiss endpoint"""
        # Create a report
        self.auth(self.user)
        url = reverse('report-list')
        res = self.client.post(url, {'content_type': 'space', 'content_id': self.space.id, 'reason': 'SPAM'}, format='json')
        self.assertEqual(res.status_code, 201)
        report_id = res.data['id']
        
        # Admin dismisses the report
        self.auth(self.admin)
        dismiss_url = reverse('report-dismiss', args=[report_id])
        res = self.client.post(dismiss_url, format='json')
        self.assertEqual(res.status_code, 200)
        self.assertEqual(res.data['status'], Report.STATUS_DISMISSED)
        
        # Verify report is dismissed
        report = Report.objects.get(id=report_id)
        self.assertEqual(report.status, Report.STATUS_DISMISSED)
        
        # Verify aggregates are updated
        self.space.refresh_from_db()
        self.assertEqual(self.space.report_count, 0)
        self.assertFalse(self.space.is_reported)

    def test_moderator_can_dismiss_report_in_their_space(self):
        """Test that moderator can dismiss reports in spaces they moderate"""
        # Create a report
        self.auth(self.user)
        url = reverse('report-list')
        res = self.client.post(url, {'content_type': 'node', 'content_id': self.node.id, 'reason': 'INAPPROPRIATE'}, format='json')
        self.assertEqual(res.status_code, 201)
        report_id = res.data['id']
        
        # Moderator dismisses the report
        self.auth(self.moderator)
        dismiss_url = reverse('report-dismiss', args=[report_id])
        res = self.client.post(dismiss_url, format='json')
        self.assertEqual(res.status_code, 200)
        self.assertEqual(res.data['status'], Report.STATUS_DISMISSED)
        
        # Verify aggregates are updated
        self.node.refresh_from_db()
        self.assertEqual(self.node.report_count, 0)
        self.assertFalse(self.node.is_reported)

    def test_moderator_cannot_dismiss_report_outside_their_space(self):
        """Test that moderator cannot dismiss reports in spaces they don't moderate"""
        # Create another space and report
        other_space = Space.objects.create(title='Other Space', description='D', creator=self.admin)
        other_node = Node.objects.create(label='Other Node', created_by=self.admin, space=other_space)
        
        # Create a report in the other space
        self.auth(self.user)
        url = reverse('report-list')
        res = self.client.post(url, {'content_type': 'node', 'content_id': other_node.id, 'reason': 'SPAM'}, format='json')
        self.assertEqual(res.status_code, 201)
        report_id = res.data['id']
        
        # Moderator tries to dismiss (should fail)
        self.auth(self.moderator)
        dismiss_url = reverse('report-dismiss', args=[report_id])
        res = self.client.post(dismiss_url, format='json')
        self.assertEqual(res.status_code, 403)
        self.assertIn('error', res.data)
        
        # Verify report is still open
        report = Report.objects.get(id=report_id)
        self.assertEqual(report.status, Report.STATUS_OPEN)

    def test_regular_user_cannot_dismiss_report(self):
        """Test that regular users cannot dismiss reports"""
        # Create a report
        self.auth(self.user)
        url = reverse('report-list')
        res = self.client.post(url, {'content_type': 'discussion', 'content_id': self.discussion.id, 'reason': 'HARASSMENT'}, format='json')
        self.assertEqual(res.status_code, 201)
        report_id = res.data['id']
        
        # Regular user tries to dismiss (should fail)
        dismiss_url = reverse('report-dismiss', args=[report_id])
        res = self.client.post(dismiss_url, format='json')
        self.assertEqual(res.status_code, 403)
        self.assertIn('error', res.data)
        
        # Verify report is still open
        report = Report.objects.get(id=report_id)
        self.assertEqual(report.status, Report.STATUS_OPEN)

    def test_cannot_dismiss_already_dismissed_report(self):
        """Test that already dismissed reports cannot be dismissed again"""
        # Create and dismiss a report
        self.auth(self.user)
        url = reverse('report-list')
        res = self.client.post(url, {'content_type': 'space', 'content_id': self.space.id, 'reason': 'SPAM'}, format='json')
        self.assertEqual(res.status_code, 201)
        report_id = res.data['id']
        
        # Admin dismisses the report
        self.auth(self.admin)
        dismiss_url = reverse('report-dismiss', args=[report_id])
        res = self.client.post(dismiss_url, format='json')
        self.assertEqual(res.status_code, 200)
        
        # Try to dismiss again (should fail)
        res = self.client.post(dismiss_url, format='json')
        self.assertEqual(res.status_code, 400)
        self.assertIn('error', res.data)
        self.assertIn('already', res.data['error'].lower())

    def test_dismiss_updates_aggregates_correctly(self):
        """Test that dismissing reports correctly updates entity aggregates"""
        # Create multiple reports on the same entity
        self.auth(self.user)
        url = reverse('report-list')
        self.client.post(url, {'content_type': 'discussion', 'content_id': self.discussion.id, 'reason': 'SPAM'}, format='json')
        self.client.post(url, {'content_type': 'discussion', 'content_id': self.discussion.id, 'reason': 'HARASSMENT'}, format='json')
        self.client.post(url, {'content_type': 'discussion', 'content_id': self.discussion.id, 'reason': 'OFF_TOPIC'}, format='json')
        
        self.discussion.refresh_from_db()
        self.assertEqual(self.discussion.report_count, 3)
        self.assertTrue(self.discussion.is_reported)
        
        # Admin dismisses all reports
        self.auth(self.admin)
        reports = Report.objects.filter(content_type='discussion', content_id=self.discussion.id)
        for report in reports:
            dismiss_url = reverse('report-dismiss', args=[report.id])
            res = self.client.post(dismiss_url, format='json')
            self.assertEqual(res.status_code, 200)
        
        # Verify aggregates are updated
        self.discussion.refresh_from_db()
        self.assertEqual(self.discussion.report_count, 0)
        self.assertFalse(self.discussion.is_reported)

    def test_moderator_cannot_dismiss_profile_reports(self):
        """Test that moderators cannot dismiss profile reports"""
        # Create a profile report
        self.auth(self.user)
        url = reverse('report-list')
        target_user = User.objects.create_user(username='target', password='pass', email='t@t.com')
        res = self.client.post(url, {'content_type': 'profile', 'content_id': target_user.id, 'reason': 'FAKE_ACCOUNT'}, format='json')
        self.assertEqual(res.status_code, 201)
        report_id = res.data['id']
        
        # Moderator tries to dismiss (should fail)
        self.auth(self.moderator)
        dismiss_url = reverse('report-dismiss', args=[report_id])
        res = self.client.post(dismiss_url, format='json')
        self.assertEqual(res.status_code, 403)
        self.assertIn('error', res.data)
        self.assertIn('admin', res.data['error'].lower())
        
        # Verify report is still open
        report = Report.objects.get(id=report_id)
        self.assertEqual(report.status, Report.STATUS_OPEN)
        
        # Admin can dismiss profile reports
        self.auth(self.admin)
        res = self.client.post(dismiss_url, format='json')
        self.assertEqual(res.status_code, 200)
        self.assertEqual(res.data['status'], Report.STATUS_DISMISSED)

    def test_space_moderator_without_profile_flag_can_dismiss(self):
        """Test that a user assigned as SpaceModerator (without profile flag) can dismiss reports"""
        # Create a user who is a SpaceModerator but doesn't have the profile flag set
        space_moderator_user = User.objects.create_user(username='space_mod', password='pass', email='sm@sm.com')
        # Note: user_type remains USER (default), not MODERATOR
        self.assertEqual(space_moderator_user.profile.user_type, Profile.USER)
        
        # Assign as SpaceModerator for the space
        SpaceModerator.objects.create(user=space_moderator_user, space=self.space, assigned_by=self.admin)
        
        # Create a report
        self.auth(self.user)
        url = reverse('report-list')
        res = self.client.post(url, {'content_type': 'node', 'content_id': self.node.id, 'reason': 'SPAM'}, format='json')
        self.assertEqual(res.status_code, 201)
        report_id = res.data['id']
        
        # SpaceModerator (without profile flag) should be able to dismiss
        self.auth(space_moderator_user)
        dismiss_url = reverse('report-dismiss', args=[report_id])
        res = self.client.post(dismiss_url, format='json')
        self.assertEqual(res.status_code, 200)
        self.assertEqual(res.data['status'], Report.STATUS_DISMISSED)
        
        # Verify aggregates are updated
        self.node.refresh_from_db()
        self.assertEqual(self.node.report_count, 0)
        self.assertFalse(self.node.is_reported)
