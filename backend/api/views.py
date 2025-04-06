import requests
from django.shortcuts import render
from rest_framework.response import Response
from rest_framework.decorators import api_view, permission_classes, action
from rest_framework.permissions import AllowAny
from django.contrib.auth import authenticate
from rest_framework_simplejwt.tokens import RefreshToken
from .serializers import RegisterSerializer, SpaceSerializer, TagSerializer
from rest_framework import viewsets, permissions
from .models import Space, Tag
from django.utils import timezone
from datetime import timedelta
from django.db import models
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from .graph import SpaceGraph, Node, Edge, GraphSnapshot
from .wikidata import get_wikidata_properties

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

class IsCreatorOrReadOnly(permissions.BasePermission):
    def has_object_permission(self, request, view, obj):
        if request.method in permissions.SAFE_METHODS:
            return True
        return obj.creator == request.user

class TagViewSet(viewsets.ModelViewSet):
    queryset = Tag.objects.all()
    serializer_class = TagSerializer
    permission_classes = [permissions.IsAuthenticated]

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
            response = requests.get(url, params=params)
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
    permission_classes = [permissions.IsAuthenticated, IsCreatorOrReadOnly]
    
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
        data = request.data
        source_node_id = data.get('source_node_id')
        wikidata_entity = data['wikidata_entity']
        selected_properties = data.get('selected_properties', [])            
        edge_label = data.get('edge_label', '')

        new_node = Node.objects.create(
            label=wikidata_entity['label'],
            wikidata_id=wikidata_entity['id'],
            created_by=request.user
        )

        # Only create an edge if there's a source node
        if source_node_id:
            source = Node.objects.get(id=source_node_id)
            Edge.objects.create(source=source, target=new_node, relation_property=edge_label)

        return Response({'node_id': new_node.id}, status=201)

    @action(detail=True, methods=['get'], url_path='nodes')
    def nodes(self, request, pk=None):
        nodes = Node.objects.filter(created_by__joined_spaces__id=pk)
        data = [{'id': node.id, 'label': node.label} for node in nodes]
        return Response(data)
    
    @action(detail=True, methods=['get'], url_path='snapshots')
    def snapshots(self, request, pk=None):
        snapshots = GraphSnapshot.objects.filter(space_id=pk).order_by('-created_at')
        data = [{'id': s.id, 'created_at': s.created_at} for s in snapshots]
        return Response(data)
    
    @action(detail=True, methods=['post'], url_path='snapshots/create')
    def create_snapshot(self, request, pk=None):
        graph = SpaceGraph(pk)
        graph.load_from_db()
        snapshot = graph.create_snapshot(request.user)
        return Response({'snapshot_id': snapshot.id, 'created_at': snapshot.created_at})
    
    @action(detail=True, methods=['post'], url_path='snapshots/revert')
    def revert_snapshot(self, request, pk=None):
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
            'limit': 50
        }

        try:
            response = requests.get(url, params=params)
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
        """Fetch entity properties from Wikidata"""
        if not entity_id:
            return Response({"error": "Missing entity_id"}, status=400)

        properties = get_wikidata_properties(entity_id)
        return Response(properties)