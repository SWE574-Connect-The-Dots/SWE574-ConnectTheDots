from datetime import timedelta
from django.contrib.auth.models import User
from django.urls import reverse
from django.utils import timezone
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


class ActivityStreamViewTests(APITestCase):
    def setUp(self):
        self.url = reverse('activity_stream')
        now = timezone.now()
        self.activities = [
            Activity.objects.create(
                as2_id='urn:uuid:111',
                type='Create',
                actor='alice',
                object='Space:1',
                summary='Space created',
                published=now - timedelta(minutes=10),
            ),
            Activity.objects.create(
                as2_id='urn:uuid:222',
                type='Join',
                actor='bob',
                object='Space:1',
                summary='Bob joined',
                published=now - timedelta(minutes=5),
            ),
            Activity.objects.create(
                as2_id='urn:uuid:333',
                type='Add',
                actor='alice',
                object='Edge:99',
                summary='Edge added',
                published=now - timedelta(minutes=1),
            ),
        ]

    def test_returns_ordered_collection_page(self):
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        self.assertEqual(body['type'], 'OrderedCollectionPage')
        self.assertEqual(body['@context'], 'https://www.w3.org/ns/activitystreams')
        self.assertEqual(body['totalItems'], 3)
        self.assertEqual(len(body['orderedItems']), 3)
        self.assertEqual(body['orderedItems'][0]['summary'], 'Edge added')

    def test_filters_by_type_and_since(self):
        since_value = (self.activities[0].published + timedelta(minutes=1)).isoformat()
        response = self.client.get(self.url, {'type': 'Join', 'since': since_value})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        self.assertEqual(body['totalItems'], 1)
        self.assertEqual(body['orderedItems'][0]['type'], 'Join')
        self.assertEqual(body['orderedItems'][0]['actor']['name'], 'bob')

    def test_paginates_results(self):
        response = self.client.get(self.url, {'limit': 1})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        self.assertEqual(len(body['orderedItems']), 1)
        self.assertIn('next', body)

        response_page_2 = self.client.get(self.url, {'limit': 1, 'page': 2})
        self.assertEqual(response_page_2.status_code, status.HTTP_200_OK)
        body_page_2 = response_page_2.json()
        self.assertEqual(len(body_page_2['orderedItems']), 1)
        self.assertIn('prev', body_page_2)

    def test_filters_by_since_last_day(self):
        """Test filtering activities from the last 1 day"""
        now = timezone.now()
        
        activity_2_days_ago = Activity.objects.create(
            as2_id='urn:uuid:444',
            type='Create',
            actor='charlie',
            object='Space:2',
            summary='Old space created',
            published=now - timedelta(days=2),
        )
        activity_1_day_ago = Activity.objects.create(
            as2_id='urn:uuid:555',
            type='Join',
            actor='dave',
            object='Space:2',
            summary='Dave joined yesterday',
            published=now - timedelta(days=1, minutes=-5), 
        )
        activity_12_hours_ago = Activity.objects.create(
            as2_id='urn:uuid:666',
            type='Add',
            actor='eve',
            object='Edge:100',
            summary='Edge added 12h ago',
            published=now - timedelta(hours=12),
        )
        activity_1_hour_ago = Activity.objects.create(
            as2_id='urn:uuid:777',
            type='Create',
            actor='frank',
            object='Node:50',
            summary='Node created 1h ago',
            published=now - timedelta(hours=1),
        )
        
        one_day_ago = (now - timedelta(days=1)).isoformat()
        response = self.client.get(self.url, {'since': one_day_ago})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        
        returned_ids = [item['id'] for item in body['orderedItems']]
        self.assertIn('urn:uuid:555', returned_ids)  
        self.assertIn('urn:uuid:666', returned_ids) 
        self.assertIn('urn:uuid:777', returned_ids)  
        self.assertNotIn('urn:uuid:444', returned_ids)  
        self.assertEqual(body['totalItems'], 6)  

    def test_filters_by_since_exact_boundary(self):
        """Test filtering with exact 1 day boundary"""
        now = timezone.now()
        exactly_one_day_ago = now - timedelta(days=1)
        
        activity_exact = Activity.objects.create(
            as2_id='urn:uuid:888',
            type='Create',
            actor='grace',
            object='Space:3',
            summary='Exactly 1 day ago',
            published=exactly_one_day_ago,
        )
        
        activity_just_before = Activity.objects.create(
            as2_id='urn:uuid:999',
            type='Join',
            actor='henry',
            object='Space:3',
            summary='Just before 1 day',
            published=exactly_one_day_ago - timedelta(seconds=1),
        )
        
        since_value = exactly_one_day_ago.isoformat()
        response = self.client.get(self.url, {'since': since_value})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        
        returned_ids = [item['id'] for item in body['orderedItems']]
        self.assertIn('urn:uuid:888', returned_ids) 
        self.assertNotIn('urn:uuid:999', returned_ids)  

    def test_filters_by_object_parameter(self):
        """Test filtering by object parameter (space-specific filtering)"""
        now = timezone.now()
        
        activity_space1 = Activity.objects.create(
            as2_id='urn:uuid:aaa',
            type='Create',
            actor='alice',
            object='Space:1',
            summary='Space 1 activity',
            published=now - timedelta(minutes=10),
        )
        activity_space2 = Activity.objects.create(
            as2_id='urn:uuid:bbb',
            type='Join',
            actor='bob',
            object='Space:2',
            summary='Space 2 activity',
            published=now - timedelta(minutes=5),
        )
        activity_space1_2 = Activity.objects.create(
            as2_id='urn:uuid:ccc',
            type='Add',
            actor='alice',
            object='Space:1',
            summary='Another Space 1 activity',
            published=now - timedelta(minutes=1),
        )
        
        response = self.client.get(self.url, {'object': 'Space:1'})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        
        returned_ids = [item['id'] for item in body['orderedItems']]
        self.assertIn('urn:uuid:aaa', returned_ids)
        self.assertIn('urn:uuid:ccc', returned_ids)
        self.assertNotIn('urn:uuid:bbb', returned_ids)
        self.assertEqual(body['totalItems'], 4)

    def test_filters_by_since_and_object_combined(self):
        """Test combining since and object filters"""
        now = timezone.now()
        
        old_activity = Activity.objects.create(
            as2_id='urn:uuid:ddd',
            type='Create',
            actor='alice',
            object='Space:1',
            summary='Old Space 1 activity',
            published=now - timedelta(days=2),
        )
        
        recent_activity = Activity.objects.create(
            as2_id='urn:uuid:eee',
            type='Join',
            actor='bob',
            object='Space:1',
            summary='Recent Space 1 activity',
            published=now - timedelta(hours=12),
        )
        
        other_space_activity = Activity.objects.create(
            as2_id='urn:uuid:fff',
            type='Add',
            actor='charlie',
            object='Space:2',
            summary='Recent Space 2 activity',
            published=now - timedelta(hours=6),
        )
        
        one_day_ago = (now - timedelta(days=1)).isoformat()
        response = self.client.get(self.url, {
            'since': one_day_ago,
            'object': 'Space:1'
        })
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        
        returned_ids = [item['id'] for item in body['orderedItems']]
        self.assertIn('urn:uuid:eee', returned_ids)  
        self.assertNotIn('urn:uuid:ddd', returned_ids)  
        self.assertNotIn('urn:uuid:fff', returned_ids) 

    def test_filters_by_actor_parameter(self):
        """Test filtering by actor parameter"""
        now = timezone.now()
        
        activity_alice = Activity.objects.create(
            as2_id='urn:uuid:ggg',
            type='Create',
            actor='alice',
            object='Space:4',
            summary='Alice activity',
            published=now - timedelta(minutes=10),
        )
        activity_bob = Activity.objects.create(
            as2_id='urn:uuid:hhh',
            type='Join',
            actor='bob',
            object='Space:4',
            summary='Bob activity',
            published=now - timedelta(minutes=5),
        )
        
        response = self.client.get(self.url, {'actor': 'alice'})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        
        returned_ids = [item['id'] for item in body['orderedItems']]
        self.assertIn('urn:uuid:ggg', returned_ids)
        self.assertIn('urn:uuid:111', returned_ids) 
        self.assertNotIn('urn:uuid:hhh', returned_ids)
        self.assertNotIn('urn:uuid:222', returned_ids)  

    def test_invalid_since_parameter(self):
        """Test error handling for invalid since parameter"""
        response = self.client.get(self.url, {'since': 'invalid-date'})
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn('since', response.json())

    def test_invalid_until_parameter(self):
        """Test error handling for invalid until parameter"""
        response = self.client.get(self.url, {'until': 'not-a-date'})
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn('until', response.json())

    def test_filters_by_until_parameter(self):
        """Test filtering by until parameter"""
        now = timezone.now()
        
        activity_old = Activity.objects.create(
            as2_id='urn:uuid:iii',
            type='Create',
            actor='alice',
            object='Space:5',
            summary='Old activity',
            published=now - timedelta(days=3),
        )
        activity_recent = Activity.objects.create(
            as2_id='urn:uuid:jjj',
            type='Join',
            actor='bob',
            object='Space:5',
            summary='Recent activity',
            published=now - timedelta(hours=1),
        )
        
        two_days_ago = (now - timedelta(days=2)).isoformat()
        response = self.client.get(self.url, {'until': two_days_ago})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        
        returned_ids = [item['id'] for item in body['orderedItems']]
        self.assertIn('urn:uuid:iii', returned_ids) 
        self.assertNotIn('urn:uuid:jjj', returned_ids)  

    def test_filters_by_since_and_until_combined(self):
        """Test combining since and until filters for date range"""
        now = timezone.now()
        
        activity_old = Activity.objects.create(
            as2_id='urn:uuid:kkk',
            type='Create',
            actor='alice',
            object='Space:6',
            summary='Too old',
            published=now - timedelta(days=5),
        )
        activity_in_range = Activity.objects.create(
            as2_id='urn:uuid:lll',
            type='Join',
            actor='bob',
            object='Space:6',
            summary='In range',
            published=now - timedelta(days=2),
        )
        activity_recent = Activity.objects.create(
            as2_id='urn:uuid:mmm',
            type='Add',
            actor='charlie',
            object='Space:6',
            summary='Too recent',
            published=now - timedelta(hours=1),
        )
        
        four_days_ago = (now - timedelta(days=4)).isoformat()
        one_day_ago = (now - timedelta(days=1)).isoformat()
        response = self.client.get(self.url, {
            'since': four_days_ago,
            'until': one_day_ago
        })
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        
        returned_ids = [item['id'] for item in body['orderedItems']]
        self.assertNotIn('urn:uuid:kkk', returned_ids) 
        self.assertIn('urn:uuid:lll', returned_ids) 
        self.assertNotIn('urn:uuid:mmm', returned_ids)  

    def test_case_insensitive_filters(self):
        """Test that type, actor, and object filters are case insensitive"""
        now = timezone.now()
        
        activity = Activity.objects.create(
            as2_id='urn:uuid:nnn',
            type='Create',
            actor='Alice',
            object='Space:7',
            summary='Case test',
            published=now - timedelta(minutes=5),
        )
        
        response = self.client.get(self.url, {'type': 'create'})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        returned_ids = [item['id'] for item in body['orderedItems']]
        self.assertIn('urn:uuid:nnn', returned_ids)
        
        response = self.client.get(self.url, {'actor': 'alice'})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        returned_ids = [item['id'] for item in body['orderedItems']]
        self.assertIn('urn:uuid:nnn', returned_ids)
        
        response = self.client.get(self.url, {'object': 'space:7'})
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        body = response.json()
        returned_ids = [item['id'] for item in body['orderedItems']]
        self.assertIn('urn:uuid:nnn', returned_ids)

