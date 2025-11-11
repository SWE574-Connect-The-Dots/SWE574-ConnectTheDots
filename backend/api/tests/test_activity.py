from django.contrib.auth.models import User
from rest_framework.test import APITestCase
from rest_framework import status

from api.models import Space, Node, Edge, Discussion, Activity


class ActivityOutboxTests(APITestCase):
    def setUp(self):
        self.u1 = User.objects.create_user(username='u1', password='pw')
        self.u2 = User.objects.create_user(username='u2', password='pw')

    def test_space_create_records_activity(self):
        self.client.login(username='u1', password='pw')
        r = self.client.post('/api/spaces/', {'title': 'A', 'description': 'D'})
        self.assertEqual(r.status_code, status.HTTP_201_CREATED)
        space_id = r.data['id']
        self.assertTrue(Activity.objects.filter(type='Create', object=f'Space:{space_id}', actor='u1').exists())

    def test_join_records_activity(self):
        space = Space.objects.create(title='S', description='D', creator=self.u1)
        space.collaborators.add(self.u1)
        self.client.login(username='u2', password='pw')
        r = self.client.post(f'/api/spaces/{space.id}/join/')
        self.assertEqual(r.status_code, status.HTTP_200_OK)
        self.assertTrue(Activity.objects.filter(type='Join', object=f'Space:{space.id}', actor='u2').exists())

    def test_add_node_and_edge_records_activity(self):
        space = Space.objects.create(title='S2', description='D', creator=self.u1)
        space.collaborators.add(self.u1)
        n1 = Node.objects.create(label='N1', created_by=self.u1, space=space)
        self.client.login(username='u1', password='pw')
        payload = {
            'related_node_id': n1.id,
            'wikidata_entity': {'id': 'Q1', 'label': 'Entity'},
            'selected_properties': [],
            'edge_label': 'rel',
            'wikidata_property_id': 'P1',
            'is_new_node_source': False,
        }
        r = self.client.post(f'/api/spaces/{space.id}/add-node/', payload, format='json')
        self.assertEqual(r.status_code, status.HTTP_201_CREATED)
        new_node_id = r.data['node_id']
        self.assertTrue(Activity.objects.filter(type='Create', object=f'Node:{new_node_id}', actor='u1').exists())
        edge = Edge.objects.filter(target_id=new_node_id, source_id=n1.id).first()
        self.assertIsNotNone(edge)
        self.assertTrue(Activity.objects.filter(type='Add', object=f'Edge:{edge.id}', actor='u1').exists())

    def test_discussion_react_records_activity(self):
        space = Space.objects.create(title='S3', description='D', creator=self.u1)
        space.collaborators.add(self.u1)
        space.collaborators.add(self.u2)
        disc = Discussion.objects.create(space=space, user=self.u1, text='Hello')
        self.client.login(username='u2', password='pw')
        url = f'/api/spaces/{space.id}/discussions/{disc.id}/react/'
        r1 = self.client.post(url, {'value': 'up'}, format='json')
        self.assertEqual(r1.status_code, status.HTTP_200_OK)
        self.assertTrue(Activity.objects.filter(type='Like', object=f'Discussion:{disc.id}', actor='u2').exists())
        r2 = self.client.post(url, {'value': 'up'}, format='json')
        self.assertEqual(r2.status_code, status.HTTP_200_OK)
        self.assertTrue(Activity.objects.filter(type='Remove', target=f'Discussion:{disc.id}', actor='u2').exists())

