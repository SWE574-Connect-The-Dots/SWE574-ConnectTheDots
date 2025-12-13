from django.test import TestCase
from django.contrib.auth.models import User
from rest_framework.test import APITestCase
from rest_framework import status
from api.models import Space, Node, Property


class InstanceTypeGroupEndpointTests(APITestCase):
    """Test the instance-types endpoint with grouping functionality"""
    
    def setUp(self):
        """Set up test data"""
        self.user = User.objects.create_user(username='testuser', password='testpass')
        self.client.force_authenticate(user=self.user)
        
        # Create a test space
        self.space = Space.objects.create(
            title='Test Space',
            description='Test Description',
            creator=self.user
        )
        
        # Create nodes with different instance types
        self.node_human = Node.objects.create(
            label='Albert Einstein',
            wikidata_id='Q937',
            space=self.space,
            created_by=self.user
        )
        Property.objects.create(
            node=self.node_human,
            property_id='P31',
            statement_id='Q937$statement1',
            value_id='Q5',
            value_text='human'
        )
        
        self.node_city = Node.objects.create(
            label='New York City',
            wikidata_id='Q60',
            space=self.space,
            created_by=self.user
        )
        Property.objects.create(
            node=self.node_city,
            property_id='P31',
            statement_id='Q60$statement1',
            value_id='Q515',
            value_text='city'
        )
        
        # Create another human node
        self.node_human2 = Node.objects.create(
            label='Marie Curie',
            wikidata_id='Q7186',
            space=self.space,
            created_by=self.user
        )
        Property.objects.create(
            node=self.node_human2,
            property_id='P31',
            statement_id='Q7186$statement1',
            value_id='Q5',
            value_text='human'
        )
        
        # Create node without instance type
        self.node_no_type = Node.objects.create(
            label='Generic Node',
            space=self.space,
            created_by=self.user
        )
    
    def test_get_instance_groups_success(self):
        """Test successful retrieval of instance type groups"""
        url = f'/api/spaces/{self.space.id}/instance-types/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('instance_groups', response.data)
        self.assertIn('nodes_by_group', response.data)
        
        # Check instance groups are present
        instance_groups = response.data['instance_groups']
        self.assertEqual(len(instance_groups), 2)  # HUMAN and CITY groups
        
        # Check HUMAN group has count of 2
        human_group = next((g for g in instance_groups if g['group_id'] == 'HUMAN'), None)
        self.assertIsNotNone(human_group)
        self.assertEqual(human_group['count'], 2)
        self.assertEqual(human_group['group_label'], 'Human')
        
        # Check CITY group has count of 1
        city_group = next((g for g in instance_groups if g['group_id'] == 'CITY'), None)
        self.assertIsNotNone(city_group)
        self.assertEqual(city_group['count'], 1)
        self.assertEqual(city_group['group_label'], 'City')
    
    def test_get_instance_groups_sorted_by_count(self):
        """Test that instance groups are sorted by count (descending)"""
        url = f'/api/spaces/{self.space.id}/instance-types/'
        response = self.client.get(url)
        
        instance_groups = response.data['instance_groups']
        
        # First should be HUMAN with count 2
        self.assertEqual(instance_groups[0]['group_id'], 'HUMAN')
        self.assertEqual(instance_groups[0]['count'], 2)
        
        # Second should be CITY with count 1
        self.assertEqual(instance_groups[1]['group_id'], 'CITY')
        self.assertEqual(instance_groups[1]['count'], 1)
    
    def test_get_instance_groups_nodes_mapping(self):
        """Test that nodes_by_group correctly maps groups to node IDs"""
        url = f'/api/spaces/{self.space.id}/instance-types/'
        response = self.client.get(url)
        
        nodes_by_group = response.data['nodes_by_group']
        
        # HUMAN group should map to 2 node IDs
        self.assertIn('HUMAN', nodes_by_group)
        self.assertEqual(len(nodes_by_group['HUMAN']), 2)
        self.assertIn(self.node_human.id, nodes_by_group['HUMAN'])
        self.assertIn(self.node_human2.id, nodes_by_group['HUMAN'])
        
        # CITY group should map to 1 node ID
        self.assertIn('CITY', nodes_by_group)
        self.assertEqual(len(nodes_by_group['CITY']), 1)
        self.assertIn(self.node_city.id, nodes_by_group['CITY'])
    
    def test_get_instance_groups_empty_space(self):
        """Test endpoint with space that has no nodes"""
        empty_space = Space.objects.create(
            title='Empty Space',
            description='No nodes',
            creator=self.user
        )
        
        url = f'/api/spaces/{empty_space.id}/instance-types/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(len(response.data['instance_groups']), 0)
        self.assertEqual(len(response.data['nodes_by_group']), 0)
    
    def test_get_instance_groups_unmapped_types_excluded(self):
        """Test that unmapped instance types are excluded from results"""
        # Create node with unmapped instance type
        node_unmapped = Node.objects.create(
            label='Unmapped Node',
            space=self.space,
            created_by=self.user
        )
        Property.objects.create(
            node=node_unmapped,
            property_id='P31',
            statement_id='unmapped$s1',
            value_id='Q999999',  # Not in any group
            value_text='unmapped type'
        )
        
        url = f'/api/spaces/{self.space.id}/instance-types/'
        response = self.client.get(url)
        
        # Should still only have 2 groups (unmapped excluded)
        self.assertEqual(len(response.data['instance_groups']), 2)


class NodeSerializerGroupingTests(APITestCase):
    """Test the instance_type grouping in NodeSerializer"""
    
    def setUp(self):
        """Set up test data"""
        self.user = User.objects.create_user(username='testuser', password='testpass')
        self.client.force_authenticate(user=self.user)
        
        self.space = Space.objects.create(
            title='Test Space',
            description='Test',
            creator=self.user
        )
    
    def test_node_with_single_instance_type(self):
        """Test that node with single P31 property returns correct group"""
        node = Node.objects.create(
            label='Test Human',
            wikidata_id='Q123',
            space=self.space,
            created_by=self.user
        )
        Property.objects.create(
            node=node,
            property_id='P31',
            statement_id='Q123$s1',
            value_id='Q5',
            value_text='human'
        )
        
        url = f'/api/spaces/{self.space.id}/nodes/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        node_data = response.data[0]
        
        # Check instance_type field returns group info
        self.assertIn('instance_type', node_data)
        self.assertIsNotNone(node_data['instance_type'])
        self.assertEqual(node_data['instance_type']['group_id'], 'HUMAN')
        self.assertEqual(node_data['instance_type']['group_label'], 'Human')
        self.assertIn('specific_types', node_data['instance_type'])
        self.assertIn('Q5', node_data['instance_type']['specific_types'])
    
    def test_node_without_instance_type(self):
        """Test that node without P31 property returns instance_type: null"""
        node = Node.objects.create(
            label='Test Node',
            space=self.space,
            created_by=self.user
        )
        
        url = f'/api/spaces/{self.space.id}/nodes/'
        response = self.client.get(url)
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        node_data = response.data[0]
        
        # Check instance_type field exists but is None
        self.assertIn('instance_type', node_data)
        self.assertIsNone(node_data['instance_type'])
    
    def test_multiple_p31_same_group_collapses(self):
        """Test that multiple P31 values in same group collapse to one group"""
        node = Node.objects.create(
            label='Istanbul',
            wikidata_id='Q406',
            space=self.space,
            created_by=self.user
        )
        
        # Add multiple city-related P31 properties
        Property.objects.create(
            node=node,
            property_id='P31',
            statement_id='s1',
            value_id='Q1637706',  # megacity
            value_text='megacity'
        )
        Property.objects.create(
            node=node,
            property_id='P31',
            statement_id='s2',
            value_id='Q1549591',  # big city
            value_text='big city'
        )
        Property.objects.create(
            node=node,
            property_id='P31',
            statement_id='s3',
            value_id='Q515',  # city
            value_text='city'
        )
        
        url = f'/api/spaces/{self.space.id}/nodes/'
        response = self.client.get(url)
        
        node_data = response.data[0]
        
        # Should collapse to CITY group
        self.assertEqual(node_data['instance_type']['group_id'], 'CITY')
        # Should include all specific types
        self.assertEqual(len(node_data['instance_type']['specific_types']), 3)
        self.assertIn('Q1637706', node_data['instance_type']['specific_types'])
        self.assertIn('Q1549591', node_data['instance_type']['specific_types'])
        self.assertIn('Q515', node_data['instance_type']['specific_types'])
    
    def test_multiple_p31_different_groups_uses_priority(self):
        """Test that when node has P31 from different groups, highest priority wins"""
        node = Node.objects.create(
            label='Test Node',
            space=self.space,
            created_by=self.user
        )
        
        # Add P31 from CITY (priority 85) and ADMINISTRATIVE (priority 80)
        Property.objects.create(
            node=node,
            property_id='P31',
            statement_id='s1',
            value_id='Q515',  # city
            value_text='city'
        )
        Property.objects.create(
            node=node,
            property_id='P31',
            statement_id='s2',
            value_id='Q15042037',  # administrative territorial entity
            value_text='administrative territorial entity'
        )
        
        url = f'/api/spaces/{self.space.id}/nodes/'
        response = self.client.get(url)
        
        node_data = response.data[0]
        
        # CITY has higher priority (85 > 80), so should return CITY
        self.assertEqual(node_data['instance_type']['group_id'], 'CITY')
    
    def test_unmapped_instance_type_returns_none(self):
        """Test that unmapped instance types return None (hidden from filter)"""
        node = Node.objects.create(
            label='Unmapped Node',
            space=self.space,
            created_by=self.user
        )
        Property.objects.create(
            node=node,
            property_id='P31',
            statement_id='s1',
            value_id='Q999999',  # Not in any group
            value_text='unmapped type'
        )
        
        url = f'/api/spaces/{self.space.id}/nodes/'
        response = self.client.get(url)
        
        node_data = response.data[0]
        
        # Should return None for unmapped types
        self.assertIsNone(node_data['instance_type'])
    
    def test_node_serializer_backward_compatible(self):
        """Test that all existing fields are still present (backward compatibility)"""
        node = Node.objects.create(
            label='Test Node',
            wikidata_id='Q999',
            space=self.space,
            created_by=self.user
        )
        
        url = f'/api/spaces/{self.space.id}/nodes/'
        response = self.client.get(url)
        
        node_data = response.data[0]
        
        # Check all existing fields are still present
        expected_fields = [
            'id', 'label', 'wikidata_id', 'created_at', 'created_by', 
            'created_by_username', 'space', 'country', 'city', 'district', 
            'street', 'latitude', 'longitude', 'location_name', 'description', 
            'is_archived', 'connection_count'
        ]
        
        for field in expected_fields:
            self.assertIn(field, node_data, f"Field '{field}' missing - breaks backward compatibility!")
        
        # Check new field is present
        self.assertIn('instance_type', node_data)
