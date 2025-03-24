from django.test import TestCase
from api.models import Tag


class TagModelTest(TestCase):
    def setUp(self):
        self.tag = Tag.objects.create(name='Avokado')

    def test_tag_creation(self):
        self.assertEqual(self.tag.name, 'Avokado')
        self.assertTrue(isinstance(self.tag, Tag))
        self.assertEqual(str(self.tag), 'Avokado')

    def test_tag_unique_constraint(self):
        with self.assertRaises(Exception):
            Tag.objects.create(name='Avokado')
