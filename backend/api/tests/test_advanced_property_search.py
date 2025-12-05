from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from django.contrib.auth.models import User

from ..models import Space, Node, Edge, Property, EdgeProperty


class AdvancedPropertySearchTests(APITestCase):
    def setUp(self):
        self.user = User.objects.create_user(username='searcher', password='pass1234')
        self.space = Space.objects.create(title='Test Space', description='d', creator=self.user)
        self.space.collaborators.add(self.user)
        self.client.login(username='searcher', password='pass1234')

        # Nodes
        self.n1 = Node.objects.create(label='N1', space=self.space, created_by=self.user)
        self.n2 = Node.objects.create(label='N2', space=self.space, created_by=self.user)

        # Node properties
        Property.objects.create(
            node=self.n1,
            property_id='P31',
            property_label='instance of',
            statement_id='S1',
            value={'type': 'entity', 'id': 'Q5', 'text': 'human'},
            value_text='human',
            value_id='Q5'
        )
        Property.objects.create(
            node=self.n2,
            property_id='P27',
            property_label='country of citizenship',
            statement_id='S2',
            value={'type': 'entity', 'id': 'Q30', 'text': 'United States'},
            value_text='United States',
            value_id='Q30'
        )

        # Edge + property
        self.edge = Edge.objects.create(
            source=self.n1,
            target=self.n2,
            relation_property='knows',
            wikidata_property_id='P50'
        )
        EdgeProperty.objects.create(
            edge=self.edge,
            property_id='P50',
            property_label='author',
            statement_id='S3',
            value={'type': 'entity', 'id': 'Q42', 'text': 'Douglas Adams'},
            value_text='Douglas Adams',
            value_id='Q42'
        )

    def test_search_properties_lists_node_and_edge_props(self):
        url = f'/api/spaces/{self.space.id}/search/properties/'
        resp = self.client.get(url, follow=True)
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        props = {item['property_id']: item for item in resp.data}
        self.assertIn('P31', props)
        self.assertEqual(props['P31']['source'], 'node')
        self.assertIn('P50', props)
        self.assertEqual(props['P50']['source'], 'edge')

    def test_search_property_values_returns_distinct_values(self):
        url = f'/api/spaces/{self.space.id}/search/properties/P31/values/'
        resp = self.client.get(url, follow=True)
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        values = [(item['value_id'], item['value_text']) for item in resp.data]
        self.assertIn(('Q5', 'human'), values)

        # Filter with query should still match
        resp_q = self.client.get(url, {'q': 'hum'}, follow=True)
        self.assertEqual(resp_q.status_code, status.HTTP_200_OK)
        filtered_values = [item['value_text'] for item in resp_q.data]
        self.assertIn('human', filtered_values)

    def test_search_query_and_logic(self):
        url = f'/api/spaces/{self.space.id}/search/query/'
        payload = {
            'rules': [
                {'property_id': 'P31', 'value_id': 'Q5'},
                {'property_id': 'P50', 'value_id': 'Q42'}
            ],
            'logic': 'AND'
        }
        resp = self.client.post(url, payload, format='json', follow=True)
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        node_ids = [n['id'] for n in resp.data['nodes']]
        edge_ids = [e['id'] for e in resp.data['edges']]
        self.assertIn(self.n1.id, node_ids)
        self.assertIn(self.edge.id, edge_ids)

    def test_search_query_or_logic(self):
        url = f'/api/spaces/{self.space.id}/search/query/'
        payload = {
            'rules': [
                {'property_id': 'P31', 'value_id': 'Q5'},
                {'property_id': 'P27', 'value_id': 'Q30'}
            ],
            'logic': 'OR'
        }
        resp = self.client.post(url, payload, format='json', follow=True)
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        node_ids = [n['id'] for n in resp.data['nodes']]
        self.assertIn(self.n1.id, node_ids)
        self.assertIn(self.n2.id, node_ids)
