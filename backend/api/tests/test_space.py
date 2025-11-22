from django.test import TestCase
from django.contrib.auth.models import User
from api.models import Tag, Space, Node, Edge
from django.utils import timezone
from rest_framework.test import APITestCase
from rest_framework import status
from unittest.mock import patch

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
    
    def test_space_creator_gets_admin_dashboard_access(self):
        """Test that creating a space grants admin dashboard access"""
        from api.serializers import ProfileSerializer
        
        # Create a new user without any spaces
        new_user = User.objects.create_user(username='newuser', password='testpass')
        
        # User initially has no spaces
        profile_serializer = ProfileSerializer(new_user.profile)
        self.assertFalse(profile_serializer.data['can_access_admin_dashboard'])
        
        # Create a space
        new_space = Space.objects.create(
            title='New Space',
            description='New space description',
            creator=new_user
        )
        
        # Refresh profile and check access
        new_user.profile.refresh_from_db()
        profile_serializer = ProfileSerializer(new_user.profile)
        self.assertTrue(profile_serializer.data['can_access_admin_dashboard'])

class SpaceAPITest(APITestCase):
    def setUp(self):
        self.user = User.objects.create_user(username='testuser', password='testpassword')
        self.client.login(username='testuser', password='testpassword')
        self.space = Space.objects.create(title='API Test Space', description='Description', creator=self.user)
        self.node1 = Node.objects.create(label='Node 1', created_by=self.user, space=self.space)
        self.node2 = Node.objects.create(label='Node 2', created_by=self.user, space=self.space)

    @patch('requests.get')
    def test_wikidata_property_search(self, mock_get):
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = {
            'search': [
                {'id': 'P31', 'label': 'instance of', 'description': 'that class of which this subject is a particular example'},
            ]
        }
        
        response = self.client.get('/api/spaces/wikidata-property-search/', {'q': 'instance'})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['id'], 'P31')

    def test_add_edge_with_wikidata_id(self):
        self.space.collaborators.add(self.user)
        data = {
            'source_id': self.node1.id,
            'target_id': self.node2.id,
            'label': 'connects to',
            'wikidata_property_id': 'P123'
        }
        response = self.client.post(f'/api/spaces/{self.space.id}/edges/add/', data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        edge = Edge.objects.get(id=response.data['edge_id'])
        self.assertEqual(edge.wikidata_property_id, 'P123')

    def test_update_edge_with_wikidata_id(self):
        self.space.collaborators.add(self.user)
        edge = Edge.objects.create(source=self.node1, target=self.node2, relation_property='old label')
        data = {
            'label': 'new label',
            'wikidata_property_id': 'P456'
        }
        response = self.client.put(f'/api/spaces/{self.space.id}/edges/{edge.id}/update/', data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        edge.refresh_from_db()
        self.assertEqual(edge.relation_property, 'new label')
        self.assertEqual(edge.wikidata_property_id, 'P456')

    def test_add_node_with_edge_and_wikidata_property_id(self):
        self.space.collaborators.add(self.user)
        data = {
            'related_node_id': self.node1.id,
            'wikidata_entity': {
                'id': 'Q42',
                'label': 'Douglas Adams'
            },
            'selected_properties': [],
            'edge_label': 'author of',
            'wikidata_property_id': 'P50',
            'is_new_node_source': False
        }
        response = self.client.post(f'/api/spaces/{self.space.id}/add-node/', data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        
        # Verify node was created
        new_node = Node.objects.get(id=response.data['node_id'])
        self.assertEqual(new_node.label, 'Douglas Adams')
        self.assertEqual(new_node.wikidata_id, 'Q42')
        
        # Verify edge was created with wikidata_property_id
        edge = Edge.objects.get(source=self.node1, target=new_node)
        self.assertEqual(edge.relation_property, 'author of')
        self.assertEqual(edge.wikidata_property_id, 'P50')