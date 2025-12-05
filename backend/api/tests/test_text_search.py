from django.contrib.auth.models import User
from rest_framework import status
from rest_framework.test import APITestCase

from ..models import Space, Node, Edge


class TextSearchTests(APITestCase):
    def setUp(self):
        self.user = User.objects.create_user(username='searchuser', password='pass1234')
        self.space = Space.objects.create(title='T', description='d', creator=self.user)
        self.space.collaborators.add(self.user)
        self.client.login(username='searchuser', password='pass1234')

        self.n1 = Node.objects.create(label='Quantum Mechanics', space=self.space, created_by=self.user)
        self.n2 = Node.objects.create(label='Classical Physics', space=self.space, created_by=self.user)
        self.edge = Edge.objects.create(
            source=self.n1,
            target=self.n2,
            relation_property='influences',
            wikidata_property_id='P1'
        )

    def test_text_search_matches_nodes_and_edges(self):
        url = f'/api/spaces/{self.space.id}/search/text/'

        resp_nodes = self.client.get(url, {'q': 'quantum'}, follow=True)
        self.assertEqual(resp_nodes.status_code, status.HTTP_200_OK)
        node_labels = [n['label'] for n in resp_nodes.data['nodes']]
        self.assertIn('Quantum Mechanics', node_labels)

        resp_edges = self.client.get(url, {'q': 'influ'}, follow=True)
        self.assertEqual(resp_edges.status_code, status.HTTP_200_OK)
        edge_labels = [e['label'] for e in resp_edges.data['edges']]
        self.assertIn('influences', edge_labels)
