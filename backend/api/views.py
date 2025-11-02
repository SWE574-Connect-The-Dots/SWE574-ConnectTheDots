from datetime import timedelta
import requests
from django.contrib.auth import authenticate
from django.contrib.auth.models import User
from django.db import models
from django.db.models import Q
from django.utils import timezone
from rest_framework import viewsets, permissions, status
from rest_framework.decorators import api_view, permission_classes, action
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework_simplejwt.tokens import RefreshToken
from .models import Space, Tag, Property, Profile, Node, Edge, GraphSnapshot, Discussion, DiscussionReaction, SpaceModerator
from .graph import SpaceGraph
from .serializers import RegisterSerializer, SpaceSerializer, TagSerializer, UserSerializer, ProfileSerializer, DiscussionSerializer
from .wikidata import get_wikidata_properties, extract_location_from_properties
from .permissions import IsCollaboratorOrReadOnly, IsProfileOwner, IsAdmin, IsAdminOrModerator, IsSpaceModerator, CanChangeUserType
from django.core.cache import cache
from django.http import JsonResponse

@api_view(['POST'])
@permission_classes([AllowAny])
def register(request):
    serializer = RegisterSerializer(data=request.data)
    if serializer.is_valid():
        serializer.save()
        return Response({"message": "User registered successfully"}, status=201)
    return Response(serializer.errors, status=400)

@api_view(['POST'])
@permission_classes([AllowAny])
def login(request):
    username = request.data.get('username')
    password = request.data.get('password')
    user = authenticate(username=username, password=password)
    if user:
        refresh = RefreshToken.for_user(user)
        return Response({
            "message": "Login successful",
            "token": str(refresh.access_token)
        })
    return Response({"message": "Invalid credentials"}, status=400)

@api_view(['GET'])
@permission_classes([IsAuthenticated])
def search(request):
    query = request.query_params.get('q', '').strip()
    if not query:
        return Response({
            'spaces': [],
            'users': []
        })
    
    spaces = Space.objects.filter(
        Q(title__icontains=query) | 
        Q(description__icontains=query) |
        Q(tags__name__icontains=query)
    ).distinct().order_by('-created_at')
    
    users = User.objects.filter(
        username__icontains=query
    ).order_by('username')
    

    user_data = []
    for user in users:
        user_serializer = UserSerializer(user).data
        try:
            user_serializer['profession'] = user.profile.profession
        except:
            user_serializer['profession'] = None
        user_data.append(user_serializer)
    
    space_serializer = SpaceSerializer(spaces, many=True)
    
    return Response({
        'spaces': space_serializer.data,
        'users': user_data
    })

class IsCreatorOrReadOnly(permissions.BasePermission):
    def has_object_permission(self, request, view, obj):
        if request.method in permissions.SAFE_METHODS:
            return True
        return obj.creator == request.user

class TagViewSet(viewsets.ModelViewSet):
    queryset = Tag.objects.all()
    serializer_class = TagSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    @action(detail=False, methods=['get'])
    def search_wikidata(self, request):
        """Endpoint to search Wikidata by query term"""
        query = request.query_params.get('query', '')
        if not query:
            return Response({"error": "Query parameter is required"}, status=400)
        
        url = 'https://www.wikidata.org/w/api.php'
        params = {
            'action': 'wbsearchentities',
            'format': 'json',
            'search': query,
            'language': 'en',
            'limit': 50
        }
        
        try:
            headers = {
                'User-Agent': 'ConnectTheDots/1.0 (https://github.com/repo/connectthedots)'
            }
            response = requests.get(url, params=params, headers=headers)
            data = response.json()
            
            results = []
            for item in data.get('search', []):
                results.append({
                    'id': item.get('id'),
                    'label': item.get('label'),
                    'description': item.get('description', ''),
                    'url': item.get('url', '')
                })
                
            return Response(results)
        except Exception as e:
            return Response({"error": str(e)}, status=500)
    
    def create(self, request, *args, **kwargs):
        """Create a tag with Wikidata information"""
        name = request.data.get('name')
        wikidata_id = request.data.get('wikidata_id')
        wikidata_label = request.data.get('wikidata_label')
        
        try:
            existing_tag = Tag.objects.get(name=name)
            if wikidata_id and not existing_tag.wikidata_id:
                existing_tag.wikidata_id = wikidata_id
            if wikidata_label and not existing_tag.wikidata_label:
                existing_tag.wikidata_label = wikidata_label
            existing_tag.save()
            serializer = self.get_serializer(existing_tag)
            return Response(serializer.data, status=200)
        except Tag.DoesNotExist:
            data = {
                'name': name,
                'wikidata_id': wikidata_id,
                'wikidata_label': wikidata_label
            }
            
            serializer = self.get_serializer(data=data)
            serializer.is_valid(raise_exception=True)
            self.perform_create(serializer)
            headers = self.get_success_headers(serializer.data)
            return Response(serializer.data, status=201, headers=headers)

class SpaceViewSet(viewsets.ModelViewSet):
    queryset = Space.objects.all()
    serializer_class = SpaceSerializer
    
    def get_permissions(self):
        """
        Custom permissions based on action:
        - discussions and wikidata_search endpoints are open to all (no permission required)
        - join/leave/check-collaborator/add_discussion endpoints need only IsAuthenticated
        - other write operations require IsCollaboratorOrReadOnly
        """
        if self.action in ['discussions', 'wikidata_search']:
            permission_classes = [permissions.AllowAny]
        elif self.action in ['join_space', 'leave_space', 'check_collaborator', 'add_discussion']:
            permission_classes = [permissions.IsAuthenticated]
        else:
            permission_classes = [permissions.IsAuthenticated, IsCollaboratorOrReadOnly]
        return [permission() for permission in permission_classes]
        
    def perform_create(self, serializer):
        serializer.save(creator=self.request.user)
        
    def create(self, request, *args, **kwargs):
        if 'tags' in request.data and isinstance(request.data['tags'], list):
            tag_ids = []
            for tag_name in request.data['tags']:
                tag, created = Tag.objects.get_or_create(name=tag_name)
                tag_ids.append(tag.id)
            request.data['tag_ids'] = tag_ids
            
        return super().create(request, *args, **kwargs)
    
    @action(detail=True, methods=['post'], url_path='join')
    def join_space(self, request, pk=None):
        space = self.get_object()
        user = request.user
        
        if user in space.collaborators.all():
            return Response({'message': 'You are already a collaborator of this space'}, status=400)
            
        space.collaborators.add(user)
        return Response({'message': 'Successfully joined the space'}, status=200)
    
    @action(detail=True, methods=['post'], url_path='leave')
    def leave_space(self, request, pk=None):
        space = self.get_object()
        user = request.user
        
        if user == space.creator:
            return Response({'message': 'Creator cannot leave the space'}, status=400)
            
        if user not in space.collaborators.all():
            return Response({'message': 'You are not a collaborator of this space'}, status=400)
            
        space.collaborators.remove(user)
        return Response({'message': 'Successfully left the space'}, status=200)
    
    @action(detail=True, methods=['get'], url_path='check-collaborator')
    def check_collaborator(self, request, pk=None):
        space = self.get_object()
        user = request.user
        is_collaborator = user in space.collaborators.all()
        return Response({'is_collaborator': is_collaborator})

    @action(detail=True, methods=['get'], url_path='discussions')
    def discussions(self, request, pk=None):
        """Get all discussions for a space"""
        space = self.get_object()
        user = request.user
        
        # Anyone can view discussions, no collaborator check needed
        discussions = Discussion.objects.filter(space=space)
        serializer = DiscussionSerializer(discussions, many=True, context={'request': request})
        return Response(serializer.data)
        
    @action(detail=True, methods=['post'], url_path='discussions/add')
    def add_discussion(self, request, pk=None):
        """Add a new discussion comment to a space"""
        space = self.get_object()
        user = request.user
        
        # Check if user is a collaborator
        if user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can add discussions'}, status=403)
            
        text = request.data.get('text')
        if not text:
            return Response({'message': 'Text is required'}, status=400)
            
        discussion = Discussion.objects.create(
            space=space,
            user=user,
            text=text
        )
        
        serializer = DiscussionSerializer(discussion, context={'request': request})
        return Response(serializer.data, status=201)

    @action(detail=True, methods=['post', 'delete'], url_path='discussions/(?P<discussion_id>[^/.]+)/react', permission_classes=[IsAuthenticated])
    def react_discussion(self, request, pk=None, discussion_id=None):
        """Upvote or downvote a discussion. Auth required, anyone can react."""
        space = self.get_object()
        try:
            discussion = Discussion.objects.get(id=discussion_id, space=space)
        except Discussion.DoesNotExist:
            return Response({'message': 'Discussion not found'}, status=404)
        
        if request.method == 'DELETE':
            DiscussionReaction.objects.filter(discussion=discussion, user=request.user).delete()
            serializer = DiscussionSerializer(discussion, context={'request': request})
            return Response({'discussion': serializer.data}, status=200)

        raw_value = str(request.data.get('value', '')).strip().lower()
        if raw_value in ('up', '1', 'true', 'upvote', '👍'):
            value = DiscussionReaction.UPVOTE
        elif raw_value in ('down', '-1', 'false', 'downvote', '👎'):
            value = DiscussionReaction.DOWNVOTE
        else:
            return Response({'message': "Invalid value. Use 'up' or 'down'."}, status=400)

        reaction, created = DiscussionReaction.objects.get_or_create(
            discussion=discussion,
            user=request.user,
            defaults={'value': value}
        )
        if not created:
            if reaction.value == value:
                reaction.delete()
                toggled = True
            else:
                reaction.value = value
                reaction.save()
                toggled = False
        else:
            toggled = False

        serializer = DiscussionSerializer(discussion, context={'request': request})
        return Response({'toggled_off': toggled, 'discussion': serializer.data}, status=200)

    @action(detail=False, methods=['get'])
    def trending(self, request):
        spaces = Space.objects.annotate(
            num_collaborators=models.Count('collaborators')
        ).order_by('-num_collaborators')

        serializer = self.get_serializer(spaces, many=True)
        return Response(serializer.data)

    @action(detail=False, methods=['get'])
    def new(self, request):
        recent_days = timezone.now() - timedelta(days=7)
        spaces = Space.objects.filter(
            created_at__gte=recent_days
        ).order_by('-created_at')

        serializer = self.get_serializer(spaces, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['post'], url_path='add-node')
    def add_node(self, request, pk=None):
        space = self.get_object()
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can add nodes'}, status=403)
            
        data = request.data
        related_node_id = data.get('related_node_id')
        wikidata_entity = data['wikidata_entity']
        selected_properties = data.get('selected_properties', [])            
        edge_label = data.get('edge_label', '')
        wikidata_property_id = data.get('wikidata_property_id', None)
        is_new_node_source = data.get('is_new_node_source', False)

        new_node = Node.objects.create(
            label=wikidata_entity['label'],
            wikidata_id=wikidata_entity['id'],
            created_by=request.user,
            space = space
        )
        
        for prop in selected_properties:
            Property.objects.create(
                node=new_node, 
                property_id=prop['property'],
                statement_id=prop['statement_id']
            )
        
        # Extract location information from selected properties if they exist
        if selected_properties:
            location_data = extract_location_from_properties(selected_properties)
            # Update node with location information if any was found
            if any(location_data.values()):
                for field, value in location_data.items():
                    if value is not None:
                        setattr(new_node, field, value)
                new_node.save()

        if related_node_id:
            related_node = Node.objects.get(id=related_node_id)
            if is_new_node_source:
                Edge.objects.create(
                    source=new_node, 
                    target=related_node, 
                    relation_property=edge_label,
                    wikidata_property_id=wikidata_property_id
                )
            else:
                Edge.objects.create(
                    source=related_node, 
                    target=new_node, 
                    relation_property=edge_label,
                    wikidata_property_id=wikidata_property_id
                )

        return Response({'node_id': new_node.id}, status=201)

    @action(detail=True, methods=['get'], url_path='nodes')
    def nodes(self, request, pk=None):
        nodes = Node.objects.filter(space_id=pk)
        data = []
        for node in nodes:
            node_data = {
                'id': node.id, 
                'label': node.label, 
                'wikidata_id': node.wikidata_id,
                'country': node.country,
                'city': node.city,
                'district': node.district,
                'street': node.street,
                'latitude': node.latitude,
                'longitude': node.longitude,
                'location_name': node.location_name
            }
            data.append(node_data)
        return Response(data)
    
    @action(detail=True, methods=['get'], url_path='edges')
    def edges(self, request, pk=None):
        """Get all edges for a specific space"""
        space_nodes = Node.objects.filter(space_id=pk).values_list('id', flat=True)
        
        edges = Edge.objects.filter(
            source_id__in=space_nodes,
            target_id__in=space_nodes
        )
        
        data = [
            {
                'id': edge.id,
                'source': edge.source.id,
                'target': edge.target.id,
                'label': edge.relation_property,
                'wikidata_property_id': edge.wikidata_property_id
            } 
            for edge in edges
        ]
        
        return Response(data)
    
    @action(detail=True, methods=['get'], url_path='snapshots')
    def snapshots(self, request, pk=None):
        snapshots = GraphSnapshot.objects.filter(space_id=pk).order_by('-created_at')
        data = [{'id': s.id, 'created_at': s.created_at} for s in snapshots]
        return Response(data)
    
    @action(detail=True, methods=['post'], url_path='snapshots/create')
    def create_snapshot(self, request, pk=None):
        space = self.get_object()
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can create snapshots'}, status=403)
            
        graph = SpaceGraph(pk)
        graph.load_from_db()
        snapshot = graph.create_snapshot(request.user)
        return Response({'snapshot_id': snapshot.id, 'created_at': snapshot.created_at})
    
    @action(detail=True, methods=['post'], url_path='snapshots/revert')
    def revert_snapshot(self, request, pk=None):
        space = self.get_object()
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can revert snapshots'}, status=403)
            
        snapshot_id = request.data.get('snapshot_id')
        if not snapshot_id: 
            return Response({'error': 'snapshot_id is required'}, status=400)
        
        graph = SpaceGraph(pk)
        graph.revert_to_snapshot(snapshot_id)
        return Response({'message': 'Graph reverted successfully'})
    
    @action(detail=False, methods=['get'], url_path='wikidata-search')
    def wikidata_search(self, request):
        """Search Wikidata entities by query"""
        query = request.query_params.get('q', '')
        if not query:
            return Response({"error": "Query parameter is required"}, status=400)

        url = 'https://www.wikidata.org/w/api.php'
        params = {
            'action': 'wbsearchentities',
            'format': 'json',
            'search': query,
            'language': 'en',
            'limit': 100
        }

        try:
            headers = {
                'User-Agent': 'ConnectTheDots/1.0 (https://github.com/repo/connectthedots)'
            }
            response = requests.get(url, params=params, headers=headers)
            data = response.json()

            results = [{
                'id': item.get('id'),
                'label': item.get('label'),
                'description': item.get('description', ''),
                'url': item.get('url', '')
            } for item in data.get('search', [])]

            return Response(results)

        except Exception as e:
            return Response({"error": str(e)}, status=500)

    @action(detail=False, methods=['get'], url_path='wikidata-property-search')
    def wikidata_property_search(self, request):
        """Search Wikidata properties by query"""
        query = request.query_params.get('q', '')
        if not query:
            return Response({"error": "Query parameter is required"}, status=400)

        url = 'https://www.wikidata.org/w/api.php'
        params = {
            'action': 'wbsearchentities',
            'format': 'json',
            'search': query,
            'type': 'property',
            'language': 'en',
            'limit': 100
        }

        try:
            headers = {
                'User-Agent': 'ConnectTheDots/1.0 (https://github.com/repo/connectthedots)'
            }
            response = requests.get(url, params=params, headers=headers)
            data = response.json()

            results = [{
                'id': item.get('id'),
                'label': item.get('label'),
                'description': item.get('description', ''),
                'url': item.get('url', '')
            } for item in data.get('search', [])]

            return Response(results)
        except Exception as e:
            return Response({"error": str(e)}, status=500)

    @action(detail=False, methods=['get'], url_path='wikidata-entity-properties/(?P<entity_id>[^/.]+)')
    def wikidata_entity_properties(self, request, entity_id=None):
        """Fetch entity properties from Wikidata with enhanced labels and formatting"""
        if not entity_id:
            return Response({"error": "Missing entity_id"}, status=400)
        
        cache_key = f"wikidata_props_{entity_id}"
        is_cached = cache.get(cache_key) is not None
        
        try:
            properties = get_wikidata_properties(entity_id)
            
            headers = {}
            if is_cached:
                headers['X-Cache'] = 'HIT'
            else:
                headers['X-Cache'] = 'MISS'
            
            return Response(
                properties, 
                headers=headers
            )
        except Exception as e:
            print(f"Error in wikidata_entity_properties for {entity_id}: {str(e)}")
            return Response(
                {"error": f"Failed to fetch properties for {entity_id}"},
                status=500
            )

    @action(detail=True, methods=['get'], url_path='nodes/(?P<node_id>[^/.]+)/properties')
    def node_properties(self, request, pk=None, node_id=None):
        """Get properties of a specific node with human-readable labels"""
        try:
            node = Node.objects.get(id=node_id, space_id=pk)
            properties = Property.objects.filter(node=node)
            
            if not properties:
                return Response([])
            
            wikidata_props = {}
            if node.wikidata_id:
                try:
                    wikidata_props_list = get_wikidata_properties(node.wikidata_id)
                    wikidata_props = {prop["statement_id"]: prop for prop in wikidata_props_list}
                except Exception as e:
                    print(f"Error fetching Wikidata properties: {str(e)}")
            
            result = []
            for prop in properties:
                prop_data = wikidata_props.get(prop.statement_id, {})
                result.append({
                    'statement_id': prop.statement_id,
                    'property_id': prop.property_id,
                    'property_label': prop_data.get('property_label', prop.property_id.replace('P', 'Property ')),
                    'property_value': prop_data.get('value', None),
                    'display': prop_data.get('display', f"{prop.property_id}: No value available")
                })
                
            return Response(result)
        except Node.DoesNotExist:
            return Response({'error': 'Node not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)
    
    @action(detail=True, methods=['delete'], url_path='nodes/(?P<node_id>[^/.]+)')
    def delete_node(self, request, pk=None, node_id=None):
        """Delete a node and its associated edges and properties"""
        space = self.get_object()
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can delete nodes'}, status=403)
            
        try:
            node = Node.objects.get(id=node_id, space_id=pk)
            Edge.objects.filter(source=node).delete()
            Edge.objects.filter(target=node).delete()
            Property.objects.filter(node=node).delete()
            node.delete()
            return Response({'message': 'Node successfully deleted'}, status=200)
        except Node.DoesNotExist:
            return Response({'error': 'Node not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)
    
    @action(detail=True, methods=['put'], url_path='nodes/(?P<node_id>[^/.]+)/update-properties')
    def update_node_properties(self, request, pk=None, node_id=None):
        """Update the properties of a node"""
        space = self.get_object()
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can update nodes'}, status=403)
            
        try:
            node = Node.objects.get(id=node_id, space_id=pk)
            selected_properties = request.data.get('selected_properties', [])
            Property.objects.filter(node=node).delete()

            for prop in selected_properties:
                if not prop:
                    continue
                Property.objects.create(
                    node=node,
                    property_id=prop['property'],
                    statement_id=prop['statement_id']
                )
            
            # Extract location information from updated properties if they exist
            if selected_properties:
                location_data = extract_location_from_properties(selected_properties)
                # Update node with location information if any was found
                if any(location_data.values()):
                    for field, value in location_data.items():
                        if value is not None:
                            setattr(node, field, value)
                    node.save()
                
            return Response({'message': 'Node properties updated'}, status=200)
        except Node.DoesNotExist:
            return Response({'error': 'Node not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    @action(detail=True, methods=['delete'], url_path='nodes/(?P<node_id>[^/.]+)/properties/(?P<statement_id>[^/.]+)')
    def delete_node_property(self, request, pk=None, node_id=None, statement_id=None):
        """Delete a single property from a node by its statement_id"""
        space = self.get_object()
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can update nodes'}, status=403)
        
        try:
            node = Node.objects.get(id=node_id, space_id=pk)
            property_to_delete = Property.objects.get(node=node, statement_id=statement_id)
            property_to_delete.delete()
            
            return Response({'message': 'Property deleted successfully'}, status=200)
        except Node.DoesNotExist:
            return Response({'error': 'Node not found'}, status=404)
        except Property.DoesNotExist:
            return Response({'error': 'Property not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    @action(detail=True, methods=['put'], url_path='nodes/(?P<node_id>[^/.]+)/update-location')
    def update_node_location(self, request, pk=None, node_id=None):
        """Update the location information of a node"""
        space = self.get_object()
        # Allow both space creator and collaborators to update location
        if request.user != space.creator and request.user not in space.collaborators.all():
            return Response({'message': 'Only space members can update node location'}, status=403)
            
        try:
            node = Node.objects.get(id=node_id, space_id=pk)
            
            # Get location data from request (frontend sends it wrapped in 'location')
            location_data = request.data.get('location', request.data)
            print(f"Received location data: {location_data}")
            
            # Update node location fields
            node.country = location_data.get('country', '') or None
            node.city = location_data.get('city', '') or None
            node.district = location_data.get('district', '') or None
            node.street = location_data.get('street', '') or None
            node.latitude = location_data.get('latitude') if location_data.get('latitude') is not None else None
            node.longitude = location_data.get('longitude') if location_data.get('longitude') is not None else None
            node.location_name = location_data.get('location_name', '') or None
            
            print(f"Updated node location - Country: {node.country}, City: {node.city}, Lat: {node.latitude}, Lng: {node.longitude}")
            
            node.save()
            
            return Response({
                'message': 'Node location updated successfully',
                'location': {
                    'country': node.country,
                    'city': node.city,
                    'district': node.district,
                    'street': node.street,
                    'latitude': node.latitude,
                    'longitude': node.longitude,
                    'location_name': node.location_name
                }
            }, status=200)
            
        except Node.DoesNotExist:
            return Response({'error': 'Node not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    @action(detail=True, methods=['put'], url_path='edges/(?P<edge_id>[^/.]+)/update')
    def update_edge(self, request, pk=None, edge_id=None):
        """Update the label and/or direction of an edge"""
        space = self.get_object()
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can update edges'}, status=403)
        try:
            edge = Edge.objects.get(id=edge_id, source__space=space)
            new_label = request.data.get('label', '').strip()
            new_source_id = request.data.get('source_id')
            new_target_id = request.data.get('target_id')
            wikidata_property_id = request.data.get('wikidata_property_id', None)

            if new_source_id and new_target_id:
                if (str(edge.source.id) != str(new_source_id)) or (str(edge.target.id) != str(new_target_id)):
                    if Edge.objects.filter(source_id=new_source_id, target_id=new_target_id).exclude(id=edge.id).exists() or \
                       Edge.objects.filter(source_id=new_target_id, target_id=new_source_id).exclude(id=edge.id).exists():
                        return Response({'error': 'Edge already exists between these nodes'}, status=400)
                    edge.source_id = new_source_id
                    edge.target_id = new_target_id

            if new_label:
                edge.relation_property = new_label
            
            edge.wikidata_property_id = wikidata_property_id
            edge.save()
            return Response({'message': 'Edge updated successfully'}, status=200)
        except Edge.DoesNotExist:
            return Response({'error': 'Edge not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    @action(detail=True, methods=['delete'], url_path='edges/(?P<edge_id>[^/.]+)/delete')
    def delete_edge(self, request, pk=None, edge_id=None):
        """Delete an edge from the graph"""
        space = self.get_object()
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can delete edges'}, status=403)
        try:
            edge = Edge.objects.get(id=edge_id, source__space=space)
            edge.delete()
            return Response({'message': 'Edge deleted successfully'}, status=200)
        except Edge.DoesNotExist:
            return Response({'error': 'Edge not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    @action(detail=True, methods=['post'], url_path='edges/add')
    def add_edge(self, request, pk=None):
        """Add an edge between two existing nodes in the space"""
        space = self.get_object()
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can add edges'}, status=403)
        source_id = request.data.get('source_id')
        target_id = request.data.get('target_id')
        label = request.data.get('label', '').strip()
        wikidata_property_id = request.data.get('wikidata_property_id', None)
        if not source_id or not target_id or not label:
            return Response({'error': 'source_id, target_id, and label are required'}, status=400)
        try:
            source = Node.objects.get(id=source_id, space=space)
            target = Node.objects.get(id=target_id, space=space)
            if Edge.objects.filter(source=source, target=target).exists() or Edge.objects.filter(source=target, target=source).exists():
                return Response({'error': 'Edge already exists between these nodes'}, status=400)
            edge = Edge.objects.create(
                source=source, 
                target=target, 
                relation_property=label,
                wikidata_property_id=wikidata_property_id
            )
            return Response({'message': 'Edge created', 'edge_id': edge.id}, status=201)
        except Node.DoesNotExist:
            return Response({'error': 'Node not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    def destroy(self, request, *args, **kwargs):
        space = self.get_object()
        user = request.user
        # Allow only creator or admin (is_staff or is_superuser)
        if user != space.creator and not (user.is_staff or user.is_superuser):
            return Response({'detail': 'You do not have permission to delete this space.'}, status=403)
        return super().destroy(request, *args, **kwargs)

class ProfileViewSet(viewsets.ModelViewSet):
    queryset = Profile.objects.all()
    serializer_class = ProfileSerializer
    permission_classes = [permissions.IsAuthenticated, IsProfileOwner]

    @action(detail=False, methods=['get'])
    def me(self, request):
        profile = request.user.profile
        serializer = self.get_serializer(profile)
        return Response(serializer.data)

    @action(detail=False, methods=['put', 'patch'])
    def update_profile(self, request):
        profile = request.user.profile
        serializer = self.get_serializer(profile, data=request.data, partial=True)
        
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        else:
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    @action(detail=True, methods=['get'])
    def user_profile(self, request, pk=None):
        try:
            user = User.objects.get(username=pk)
            profile = user.profile
            serializer = self.get_serializer(profile)
            return Response(serializer.data)
        except User.DoesNotExist:
            return Response({'error': 'User not found'}, status=status.HTTP_404_NOT_FOUND)

    def get_queryset(self):
        return Profile.objects.all()

# Authorization Management Views

@api_view(['POST'])
@permission_classes([IsAuthenticated, IsAdmin])
def assign_moderator(request):
    """
    Admin can assign a moderator to a specific space.
    """
    user_id = request.data.get('user_id')
    space_id = request.data.get('space_id')
    
    if not user_id or not space_id:
        return Response({'error': 'user_id and space_id are required'}, status=status.HTTP_400_BAD_REQUEST)
    
    try:
        user = User.objects.get(id=user_id)
        space = Space.objects.get(id=space_id)
        
        # Update user type to moderator
        profile = user.profile
        profile.user_type = Profile.MODERATOR
        profile.save()
        
        # Create space moderator assignment
        space_moderator, created = SpaceModerator.objects.get_or_create(
            user=user,
            space=space,
            defaults={'assigned_by': request.user}
        )
        
        if created:
            return Response({
                'message': f'{user.username} has been assigned as moderator for {space.title}',
                'user_type': profile.user_type
            }, status=status.HTTP_201_CREATED)
        else:
            return Response({
                'message': f'{user.username} is already a moderator for {space.title}',
                'user_type': profile.user_type
            })
    
    except User.DoesNotExist:
        return Response({'error': 'User not found'}, status=status.HTTP_404_NOT_FOUND)
    except Space.DoesNotExist:
        return Response({'error': 'Space not found'}, status=status.HTTP_404_NOT_FOUND)

@api_view(['POST'])
@permission_classes([IsAuthenticated, CanChangeUserType])
def change_user_type(request):
    """
    Change user type. Admins can change any user type.
    Moderators can only change regular users to moderators within their spaces.
    """
    user_id = request.data.get('user_id')
    new_user_type = request.data.get('user_type')
    space_id = request.data.get('space_id')  # Required for moderator actions
    
    if not user_id or not new_user_type:
        return Response({'error': 'user_id and user_type are required'}, status=status.HTTP_400_BAD_REQUEST)
    
    try:
        target_user = User.objects.get(id=user_id)
        target_profile = target_user.profile
        requesting_profile = request.user.profile
        
        # Validate new_user_type - convert to int if string (DRF converts integers to strings)
        valid_types = [Profile.ADMIN, Profile.MODERATOR, Profile.USER]
        if isinstance(new_user_type, str):
            try:
                new_user_type = int(new_user_type)
            except ValueError:
                return Response({'error': 'Invalid user type format'}, status=status.HTTP_400_BAD_REQUEST)
        
        if new_user_type not in valid_types:
            return Response({'error': 'Invalid user type'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Admin can change any user type
        if requesting_profile.is_admin():
            target_profile.user_type = new_user_type
            target_profile.save()
            
            # If changing to moderator and space_id provided, assign to space
            if new_user_type == Profile.MODERATOR and space_id:
                try:
                    space = Space.objects.get(id=space_id)
                    SpaceModerator.objects.get_or_create(
                        user=target_user,
                        space=space,
                        defaults={'assigned_by': request.user}
                    )
                except Space.DoesNotExist:
                    pass  # Continue without space assignment
            
            return Response({
                'message': f'{target_user.username} user type changed to {target_profile.get_user_type_display()}',
                'user_type': new_user_type
            })
        
        # Moderator can only change regular users to moderators in their spaces
        elif requesting_profile.is_moderator():
            if not space_id:
                return Response({'error': 'space_id is required for moderator actions'}, status=status.HTTP_400_BAD_REQUEST)
            
            try:
                space = Space.objects.get(id=space_id)
                
                # Check if requesting user can moderate this space
                if not requesting_profile.can_moderate_space(space):
                    return Response({'error': 'You are not a moderator of this space'}, status=status.HTTP_403_FORBIDDEN)
                
                # Can only change regular users to moderators
                if target_profile.user_type != Profile.USER or new_user_type != Profile.MODERATOR:
                    return Response({'error': 'You can only change regular users to moderators'}, status=status.HTTP_403_FORBIDDEN)
                
                target_profile.user_type = Profile.MODERATOR
                target_profile.save()
                
                SpaceModerator.objects.get_or_create(
                    user=target_user,
                    space=space,
                    defaults={'assigned_by': request.user}
                )
                
                return Response({
                    'message': f'{target_user.username} has been assigned as moderator for {space.title}',
                    'user_type': new_user_type
                })
                
            except Space.DoesNotExist:
                return Response({'error': 'Space not found'}, status=status.HTTP_404_NOT_FOUND)
        
        else:
            return Response({'error': 'Insufficient permissions'}, status=status.HTTP_403_FORBIDDEN)
    
    except User.DoesNotExist:
        return Response({'error': 'User not found'}, status=status.HTTP_404_NOT_FOUND)

@api_view(['DELETE'])
@permission_classes([IsAuthenticated, IsAdmin])
def remove_moderator(request):
    """
    Admin can remove a moderator from a specific space.
    """
    user_id = request.data.get('user_id')
    space_id = request.data.get('space_id')
    
    if not user_id or not space_id:
        return Response({'error': 'user_id and space_id are required'}, status=status.HTTP_400_BAD_REQUEST)
    
    try:
        user = User.objects.get(id=user_id)
        space = Space.objects.get(id=space_id)
        
        # Remove space moderator assignment
        space_moderator = SpaceModerator.objects.filter(user=user, space=space).first()
        if space_moderator:
            space_moderator.delete()
            
            # Check if user is moderator of any other spaces
            remaining_assignments = SpaceModerator.objects.filter(user=user).exists()
            if not remaining_assignments:
                # If no other moderator assignments, change user type back to regular user
                profile = user.profile
                profile.user_type = Profile.USER
                profile.save()
            
            return Response({
                'message': f'{user.username} has been removed as moderator from {space.title}'
            })
        else:
            return Response({'error': 'User is not a moderator of this space'}, status=status.HTTP_400_BAD_REQUEST)
    
    except User.DoesNotExist:
        return Response({'error': 'User not found'}, status=status.HTTP_404_NOT_FOUND)
    except Space.DoesNotExist:
        return Response({'error': 'Space not found'}, status=status.HTTP_404_NOT_FOUND)

@api_view(['GET'])
@permission_classes([IsAuthenticated])
def get_user_permissions(request):
    """
    Get current user's permissions and type.
    """
    try:
        profile = request.user.profile
        moderated_spaces = []
        
        if profile.is_moderator():
            moderated_spaces = list(SpaceModerator.objects.filter(user=request.user).values(
                'space__id', 'space__title', 'assigned_at'
            ))
        
        return Response({
            'user_type': profile.user_type,
            'user_type_display': profile.get_user_type_display(),
            'is_admin': profile.is_admin(),
            'is_moderator': profile.is_moderator(),
            'is_regular_user': profile.is_regular_user(),
            'moderated_spaces': moderated_spaces
        })
    
    except Profile.DoesNotExist:
        return Response({'error': 'Profile not found'}, status=status.HTTP_404_NOT_FOUND)

@api_view(['GET'])
@permission_classes([IsAuthenticated, IsAdminOrModerator])
def list_users_by_type(request):
    """
    List users by type. Admins can see all users, moderators can see users in their spaces.
    """
    user_type = request.query_params.get('user_type')
    space_id = request.query_params.get('space_id')
    
    try:
        requesting_profile = request.user.profile
        
        if requesting_profile.is_admin():
            # Admin can see all users
            queryset = Profile.objects.all()
            if user_type:
                queryset = queryset.filter(user_type=int(user_type))
                
        elif requesting_profile.is_moderator():
            # Moderator can only see users in spaces they moderate
            if not space_id:
                return Response({'error': 'space_id is required for moderators'}, status=status.HTTP_400_BAD_REQUEST)
            
            try:
                space = Space.objects.get(id=space_id)
                if not requesting_profile.can_moderate_space(space):
                    return Response({'error': 'You are not a moderator of this space'}, status=status.HTTP_403_FORBIDDEN)
                
                # Get users who are collaborators of the space
                collaborator_ids = space.collaborators.values_list('id', flat=True)
                queryset = Profile.objects.filter(user__id__in=collaborator_ids)
                
                if user_type:
                    queryset = queryset.filter(user_type=int(user_type))
                    
            except Space.DoesNotExist:
                return Response({'error': 'Space not found'}, status=status.HTTP_404_NOT_FOUND)
        
        users_data = []
        for profile in queryset:
            users_data.append({
                'id': profile.user.id,
                'username': profile.user.username,
                'email': profile.user.email,
                'user_type': profile.user_type,
                'user_type_display': profile.get_user_type_display(),
                'profession': profile.profession,
                'created_at': profile.created_at
            })
        
        return Response({'users': users_data})
    
    except Profile.DoesNotExist:
        return Response({'error': 'Profile not found'}, status=status.HTTP_404_NOT_FOUND)