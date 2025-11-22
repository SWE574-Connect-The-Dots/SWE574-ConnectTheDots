import json
from unittest.mock import patch, MagicMock
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from django.contrib.auth.models import User
from ..models import Space, Node, Property
from ..wikidata import get_wikidata_properties

class PropertyAPITests(APITestCase):
    def setUp(self):
        self.user = User.objects.create_user(username='testuser', password='testpassword')
        self.space = Space.objects.create(title='Test Space', creator=self.user)
        self.space.collaborators.add(self.user)
        self.node = Node.objects.create(label='Test Node', wikidata_id='Q123', space=self.space, created_by=self.user)
        self.client.login(username='testuser', password='testpassword')

    def test_add_node_with_properties(self):
        url = reverse('space-add-node', kwargs={'pk': self.space.pk})
        data = {
            'wikidata_entity': {'id': 'Q456', 'label': 'New Test Node'},
            'selected_properties': [
                {'property': 'P1', 'statement_id': 'Q456$statement1'},
                {'property': 'P2', 'statement_id': 'Q456$statement2'}
            ]
        }
        response = self.client.post(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(Node.objects.count(), 2)
        new_node = Node.objects.get(wikidata_id='Q456')
        self.assertEqual(Property.objects.filter(node=new_node).count(), 2)
        self.assertTrue(Property.objects.filter(node=new_node, property_id='P1', statement_id='Q456$statement1').exists())

    @patch('api.views.get_wikidata_properties')
    def test_get_node_properties(self, mock_get_wikidata_properties):
        Property.objects.create(node=self.node, property_id='P31', statement_id='Q123$statement3')
        mock_get_wikidata_properties.return_value = [
            {
                "statement_id": "Q123$statement3",
                "property": "P31",
                "property_label": "instance of",
                "value": {'type': 'entity', 'id': 'Q42', 'text': 'Douglas Adams'},
                "display": "instance of: Douglas Adams"
            }
        ]
        
        url = reverse('space-node-properties', kwargs={'pk': self.space.pk, 'node_id': self.node.pk})
        response = self.client.get(url, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['statement_id'], 'Q123$statement3')
        self.assertEqual(response.data[0]['property_label'], 'instance of')

    def test_update_node_properties(self):
        Property.objects.create(node=self.node, property_id='P18', statement_id='Q123$oldstatement')
        url = reverse('space-update-node-properties', kwargs={'pk': self.space.pk, 'node_id': self.node.pk})
        data = {
            'selected_properties': [
                {'property': 'P569', 'statement_id': 'Q123$newstatement1'},
                {'property': 'P19', 'statement_id': 'Q123$newstatement2'}
            ]
        }
        response = self.client.put(url, data, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(Property.objects.filter(node=self.node).count(), 2)
        self.assertFalse(Property.objects.filter(node=self.node, statement_id='Q123$oldstatement').exists())
        self.assertTrue(Property.objects.filter(node=self.node, statement_id='Q123$newstatement1').exists())

    def test_delete_node_property(self):
        prop_to_delete = Property.objects.create(node=self.node, property_id='P27', statement_id='Q123$statement-to-delete')
        url = reverse('space-delete-node-property', kwargs={'pk': self.space.pk, 'node_id': self.node.pk, 'statement_id': prop_to_delete.statement_id})
        response = self.client.delete(url, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertFalse(Property.objects.filter(id=prop_to_delete.id).exists())

class WikidataUtilTests(APITestCase):

    @patch('api.wikidata.requests.post')
    def test_get_wikidata_properties_sparql(self, mock_post):
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {
            "results": {
                "bindings": [
                    {
                        "statement": {"type": "uri", "value": "http://www.wikidata.org/entity/statement/Q111-a1b2c3d4"},
                        "property": {"type": "uri", "value": "http://www.wikidata.org/entity/P31"},
                        "propertyLabel": {"xml:lang": "en", "type": "literal", "value": "instance of"},
                        "value": {"type": "uri", "value": "http://www.wikidata.org/entity/Q30"},
                        "valueLabel": {"xml:lang": "en", "type": "literal", "value": "planet"}
                    }
                ]
            }
        }
        mock_post.return_value = mock_response

        entity_id = 'Q111'
        properties = get_wikidata_properties(entity_id)

        self.assertEqual(len(properties), 1)
        prop = properties[0]
        self.assertEqual(prop['statement_id'], 'Q111-a1b2c3d4')
        self.assertEqual(prop['property'], 'P31')
        self.assertEqual(prop['property_label'], 'instance of')
        self.assertIsInstance(prop['value'], dict)
        self.assertEqual(prop['value']['id'], 'Q30')
        self.assertEqual(prop['value']['text'], 'planet')
        self.assertEqual(prop['display'], 'instance of: planet')
        mock_post.assert_called_once()
