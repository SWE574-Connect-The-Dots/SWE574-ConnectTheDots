from unittest.mock import patch
from rest_framework.test import APITestCase
from rest_framework import status
from django.contrib.auth.models import User


class WikidataAPITests(APITestCase):
    def setUp(self):
        # Authenticate to satisfy default IsAuthenticated requirement
        self.user = User.objects.create_user(username='tester', password='pw')
        self.client.login(username='tester', password='pw')
    @patch('api.views.requests.get')
    def test_wikidata_search_success(self, mock_get):
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = {
            'search': [
                {
                    'id': 'Q937',
                    'label': 'Albert Einstein',
                    'description': 'German-born theoretical physicist',
                    'url': 'https://www.wikidata.org/wiki/Q937',
                },
                {
                    'id': 'Q42',
                    'label': 'Douglas Adams',
                    'description': 'English writer',
                    'url': 'https://www.wikidata.org/wiki/Q42',
                },
            ]
        }

        resp = self.client.get('/api/spaces/wikidata-search/', {'q': 'einstein'})
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        self.assertEqual(len(resp.data), 2)
        self.assertEqual(resp.data[0]['id'], 'Q937')
        self.assertEqual(resp.data[0]['label'], 'Albert Einstein')
        self.assertEqual(resp.data[0]['description'], 'German-born theoretical physicist')
        self.assertEqual(resp.data[0]['url'], 'https://www.wikidata.org/wiki/Q937')

    @patch('api.views.get_wikidata_properties')
    def test_wikidata_entity_properties_success_with_cache_header(self, mock_get_props):
        entity_id = 'Q937'
        mock_get_props.return_value = [
            {
                'statement_id': 'Q937$abc',
                'property': 'P31',
                'property_label': 'instance of',
                'value': {'type': 'entity', 'id': 'Q5', 'text': 'human'},
                'display': 'instance of: human',
            }
        ]

        resp = self.client.get(f'/api/spaces/wikidata-entity-properties/{entity_id}/')
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        self.assertIsInstance(resp.data, list)
        self.assertEqual(resp.data[0]['property'], 'P31')
        # Should include an X-Cache header; initial call should be a MISS
        self.assertIn('X-Cache', resp)
        self.assertEqual(resp['X-Cache'], 'MISS')
