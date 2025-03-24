from django.test import TestCase
from django.contrib.auth.models import User
from api.models import Tag, Space
from django.utils import timezone

class SpaceModelTest(TestCase):
    def setUp(self):
        self.user = User.objects.create_user(username='testuser', password='testpass')
        self.tag1 = Tag.objects.create(name='Avokado')
        self.tag2 = Tag.objects.create(name='DumTagTag')

        self.space = Space.objects.create(
            title='Test Space',
            description='Description of test space',
            creator=self.user
        )
        self.space.tags.add(self.tag1, self.tag2)
        self.space.collaborators.add(self.user)

    def test_space_creation(self):
        self.assertEqual(self.space.title, 'Test Space')
        self.assertEqual(self.space.creator.username, 'testuser')
        self.assertEqual(self.space.description, 'Description of test space')
        self.assertTrue(self.space.created_at <= timezone.now())
        self.assertIn(self.user, self.space.collaborators.all())

    def test_space_tag_relationship(self):
        tags = self.space.tags.all()
        self.assertEqual(tags.count(), 2)
        self.assertIn(self.tag1, tags)
        self.assertIn(self.tag2, tags)

    def test_space_ordering(self):
        space2 = Space.objects.create(
            title='Second Space',
            description='Another space description',
            creator=self.user
        )
        spaces = Space.objects.all()
        self.assertEqual(spaces.first(), space2)