from datetime import timedelta
import requests
from django.contrib.auth import authenticate
from django.contrib.auth.models import User
from django.db import models
from django.db.models import Q
from django.utils import timezone
from django.utils.dateparse import parse_datetime
from django.urls import reverse
from rest_framework import viewsets, permissions, status
from rest_framework.decorators import api_view, permission_classes, action
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework.exceptions import ValidationError
from rest_framework_simplejwt.tokens import RefreshToken
from .models import Space, Tag, Property, EdgeProperty, Profile, Node, Edge, GraphSnapshot, Discussion, DiscussionReaction, SpaceModerator, Report, Activity, Archive, record_activity
from .graph import SpaceGraph
from .neo4j_db import Neo4jConnection 
from .serializers import (RegisterSerializer, SpaceSerializer, TagSerializer, 
                          UserSerializer, ProfileSerializer, DiscussionSerializer, 
                          ReportSerializer, ActivityStreamSerializer, ArchiveSerializer,
                          NodeSerializer)
from .wikidata import get_wikidata_properties, extract_location_from_properties
from .permissions import IsCollaboratorOrReadOnly, IsProfileOwner, IsAdmin, IsAdminOrModerator, IsSpaceModerator, CanChangeUserType, IsNotArchivedUser
from .reporting import REASON_CODES, REASONS_VERSION
from django.core.cache import cache
from django.http import JsonResponse
from django.db.models import Count
import google.generativeai as genai

def _recompute_entity_reports(content_type, content_id):
    """Recalculate report_count and is_reported based on OPEN reports only."""
    open_count = Report.objects.filter(content_type=content_type, content_id=content_id, status=Report.STATUS_OPEN).count()
    is_reported = open_count > 0
    if content_type == Report.CONTENT_SPACE:
        try:
            obj = Space.objects.get(id=content_id)
            obj.report_count = open_count
            obj.is_reported = is_reported
            obj.save(update_fields=['report_count', 'is_reported'])
        except Space.DoesNotExist:
            pass
    elif content_type == Report.CONTENT_NODE:
        try:
            obj = Node.objects.get(id=content_id)
            obj.report_count = open_count
            obj.is_reported = is_reported
            obj.save(update_fields=['report_count', 'is_reported'])
        except Node.DoesNotExist:
            pass
    elif content_type == Report.CONTENT_DISCUSSION:
        try:
            obj = Discussion.objects.get(id=content_id)
            obj.report_count = open_count
            obj.is_reported = is_reported
            obj.save(update_fields=['report_count', 'is_reported'])
        except Discussion.DoesNotExist:
            pass
    elif content_type == Report.CONTENT_PROFILE:
        try:
            obj = Profile.objects.get(user__id=content_id)
            obj.report_count = open_count
            obj.is_reported = is_reported
            obj.save(update_fields=['report_count', 'is_reported'])
        except Profile.DoesNotExist:
            pass


def _normalize_property_value_for_storage(raw_value):
    """
    Convert property value payload to text/id for storage and search.
    """
    if raw_value is None:
        return None, None
    if isinstance(raw_value, dict):
        value_id = raw_value.get('id') or raw_value.get('value')
        text = raw_value.get('text') or raw_value.get('label') or value_id
        if text is None and value_id is None:
            text = str(raw_value)
        return text, value_id
    return str(raw_value), None

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
        username__icontains=query,
        profile__is_archived=False
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
    
    def get_permissions(self):
        """
        Read operations need IsAuthenticated.
        Create/Update operations need IsAuthenticated + IsNotArchivedUser.
        """
        if self.action in ['list', 'retrieve', 'search_wikidata']:
            permission_classes = [permissions.IsAuthenticated]
        else:
            permission_classes = [permissions.IsAuthenticated, IsNotArchivedUser]
        return [permission() for permission in permission_classes]
    
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
    
    def _format_node_label(self, node):
        if not node:
            return "Unknown node"
        label = getattr(node, 'label', None)
        if label:
            return f"'{label}'"
        node_id = getattr(node, 'id', None)
        return f"Node #{node_id}" if node_id is not None else "Unknown node"

    def _format_edge_summary(self, username, source_node, relation_label, target_node):
        relation = relation_label or "connection"
        source_label = self._format_node_label(source_node)
        target_label = self._format_node_label(target_node)
        return f"{username} created edge {source_label} -[{relation}]-> {target_label}"

    def get_permissions(self):
        """
        Custom permissions based on action:
        - discussions and wikidata_search endpoints are open to all (no permission required)
        - join/leave/check-collaborator/add_discussion/delete_discussion endpoints need IsAuthenticated + IsNotArchivedUser
        - other write operations require IsAuthenticated + IsCollaboratorOrReadOnly + IsNotArchivedUser
        - read operations (list, retrieve) only need IsAuthenticated
        """
        if self.action in ['discussions', 'wikidata_search']:
            permission_classes = [permissions.AllowAny]
        elif self.action in ['list', 'retrieve', 'trending', 'new', 'top_scored', 'collaborators', 'top_collaborators', 
                             'nodes', 'edges', 'snapshots', 'wikidata_entity_properties', 'node_properties', 'graph_search', 'summarize_space']:
            permission_classes = [permissions.IsAuthenticated]
        elif self.action in ['join_space', 'leave_space', 'check_collaborator', 'add_discussion', 'delete_discussion',
                             'react_discussion', 'add_node', 'delete_node', 'update_node_properties', 'delete_node_property',
                             'update_node_location', 'update_edge', 'delete_edge', 'add_edge', 'create_snapshot', 'revert_snapshot']:
            permission_classes = [permissions.IsAuthenticated, IsNotArchivedUser]
        else:
            permission_classes = [permissions.IsAuthenticated, IsCollaboratorOrReadOnly, IsNotArchivedUser]
        return [permission() for permission in permission_classes]

    @action(detail=True, methods=['get'], url_path='graph-search')
    def graph_search(self, request, pk=None):
        """
        Search the graph using Neo4j with support for multiple node, edge, and property queries.
        Supports depth parameter for N-hop neighbor search.
        """
        node_query = request.query_params.get('node_q', '').strip()
        edge_query = request.query_params.get('edge_q', '').strip()
        property_query = request.query_params.get('property_q', '').strip()
        depth = request.query_params.get('depth', '1')
        
        # Parse depth parameter
        try:
            depth = int(depth)
            if depth < 1:
                depth = 1
            elif depth > 5:  # Max depth limit for performance
                depth = 5
        except ValueError:
            depth = 1
        
        # Fallback for backward compatibility or single search box
        general_query = request.query_params.get('q', '').strip()
        if general_query and not node_query and not edge_query and not property_query:
            node_query = general_query
            edge_query = general_query
            
        if not node_query and not edge_query and not property_query:
            return Response({'nodes': [], 'edges': []})
            
        results = Neo4jConnection.search_graph(int(pk), node_queries=node_query, edge_queries=edge_query, property_queries=property_query, depth=depth)
        return Response(results)
        
    def perform_create(self, serializer):
        space = serializer.save(creator=self.request.user)
        # Ensure the creator is also recorded as a moderator of this space
        try:
            SpaceModerator.objects.get_or_create(
                user=self.request.user,
                space=space,
                defaults={'assigned_by': self.request.user}
            )
        except Exception:
            pass
        try:
            record_activity(
                actor_user=self.request.user,
                type='Create',
                object=f'Space:{space.id}',
                summary=f"{self.request.user.username} created space '{space.title}'",
                payload={'space_id': space.id}
            )
        except Exception:
            pass
        
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
        
        if space.is_archived:
            return Response({'message': 'Cannot join an archived space'}, status=403)
        
        if user in space.collaborators.all():
            return Response({'message': 'You are already a collaborator of this space'}, status=400)
            
        space.collaborators.add(user)
        try:
            record_activity(
                actor_user=user,
                type='Join',
                object=f'Space:{space.id}',
                summary=f"{user.username} joined space '{space.title}'",
                payload={'space_id': space.id}
            )
        except Exception:
            pass
        return Response({'message': 'Successfully joined the space'}, status=200)
    
    @action(detail=True, methods=['post'], url_path='leave')
    def leave_space(self, request, pk=None):
        space = self.get_object()
        user = request.user
        
        if space.is_archived:
            return Response({'message': 'Cannot leave an archived space'}, status=403)
        
        if user == space.creator:
            return Response({'message': 'Creator cannot leave the space'}, status=400)
            
        if user not in space.collaborators.all():
            return Response({'message': 'You are not a collaborator of this space'}, status=400)
            
        space.collaborators.remove(user)
        try:
            record_activity(
                actor_user=user,
                type='Leave',
                object=f'Space:{space.id}',
                summary=f"{user.username} left space '{space.title}'",
                payload={'space_id': space.id}
            )
        except Exception:
            pass
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
        
        if space.is_archived:
            return Response({'message': 'Cannot add discussions to an archived space'}, status=403)
        
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
        try:
            record_activity(
                actor_user=user,
                type='Create',
                object=f'Discussion:{discussion.id}',
                target=f'Space:{space.id}',
                summary=f"{user.username} commented in '{space.title}'",
                payload={'space_id': space.id, 'discussion_id': discussion.id}
            )
        except Exception:
            pass
        return Response(serializer.data, status=201)

    @action(detail=True, methods=['post', 'delete'], url_path='discussions/(?P<discussion_id>[^/.]+)/react', permission_classes=[IsAuthenticated])
    def react_discussion(self, request, pk=None, discussion_id=None):
        """Upvote or downvote a discussion. Auth required, anyone can react."""
        space = self.get_object()
        
        if space.is_archived:
            return Response({'message': 'Cannot react to discussions in an archived space'}, status=403)
        
        try:
            discussion = Discussion.objects.get(id=discussion_id, space=space)
        except Discussion.DoesNotExist:
            return Response({'message': 'Discussion not found'}, status=404)
        
        if request.method == 'DELETE':
            DiscussionReaction.objects.filter(discussion=discussion, user=request.user).delete()
            try:
                record_activity(
                    actor_user=request.user,
                    type='Remove',
                    object=f'Reaction:{discussion.id}',
                    target=f'Discussion:{discussion.id}',
                    summary=f"{request.user.username} removed reaction",
                    payload={'discussion_id': discussion.id}
                )
            except Exception:
                pass
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
                try:
                    record_activity(
                        actor_user=request.user,
                        type='Remove',
                        object=f'Reaction:{discussion.id}',
                        target=f'Discussion:{discussion.id}',
                        summary=f"{request.user.username} removed reaction",
                        payload={'discussion_id': discussion.id}
                    )
                except Exception:
                    pass
            else:
                reaction.value = value
                reaction.save()
                toggled = False
                try:
                    record_activity(
                        actor_user=request.user,
                        type=('Like' if value == DiscussionReaction.UPVOTE else 'Dislike'),
                        object=f'Discussion:{discussion.id}',
                        summary=f"{request.user.username} reacted",
                        payload={'discussion_id': discussion.id, 'value': int(value)}
                    )
                except Exception:
                    pass
        else:
            toggled = False
            try:
                record_activity(
                    actor_user=request.user,
                    type=('Like' if value == DiscussionReaction.UPVOTE else 'Dislike'),
                    object=f'Discussion:{discussion.id}',
                    summary=f"{request.user.username} reacted",
                    payload={'discussion_id': discussion.id, 'value': int(value)}
                )
            except Exception:
                pass

        serializer = DiscussionSerializer(discussion, context={'request': request})
        return Response({'toggled_off': toggled, 'discussion': serializer.data}, status=200)

    @action(detail=True, methods=['delete'], url_path='discussions/(?P<discussion_id>[^/.]+)/delete')
    def delete_discussion(self, request, pk=None, discussion_id=None):
        """Delete a discussion. Only admins or moderators can delete discussions."""
        space = self.get_object()
        user = request.user
        
        if space.is_archived:
            return Response({'error': 'Cannot delete discussions in an archived space'}, status=403)
        
        try:
            discussion = Discussion.objects.get(id=discussion_id, space=space)
        except Discussion.DoesNotExist:
            return Response({'error': 'Discussion not found'}, status=404)
        
        if not (user.is_staff or user.is_superuser or user.profile.is_admin()):
            if not SpaceModerator.objects.filter(user=user, space=space).exists():
                return Response({'error': 'You are not a moderator of this space'}, status=403)
        
        Report.objects.filter(content_type=Report.CONTENT_DISCUSSION, content_id=discussion_id).delete()
        
        discussion.delete()
        return Response({'message': 'Discussion deleted successfully'}, status=200)

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
        
        if space.is_archived:
            return Response({'message': 'Cannot add nodes to an archived space'}, status=403)
        
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
            description=wikidata_entity.get('description', ''),
            created_by=request.user,
            space = space
        )

        # --- NEO4J INTEGRATION START ---
        # Prepare properties for Neo4j
        neo4j_props = {
            'wikidata_id': new_node.wikidata_id,
            'description': new_node.description,
            'created_by': request.user.username,
            'created_at': new_node.created_at.isoformat()
        }
        
        for prop in selected_properties:
            key = prop.get('property_label') or prop.get('property')
            value = prop.get('value')
            if key and value:
                # Sanitize key for Neo4j property
                safe_key = "".join(x for x in key if x.isalnum() or x == "_")
                if safe_key:
                    neo4j_props[safe_key] = str(value)

        # Save to Neo4j
        Neo4jConnection.create_node(
            node_id=new_node.id,
            label=new_node.label,
            space_id=space.id,
            properties=neo4j_props
        )
        # --- NEO4J INTEGRATION END ---

        try:
            record_activity(
                actor_user=request.user,
                type='Create',
                object=f'Node:{new_node.id}',
                target=f'Space:{space.id}',
                summary=f"{request.user.username} added node '{new_node.label}'",
                payload={'space_id': space.id, 'node_id': new_node.id}
            )
        except Exception:
            pass
        
        for prop in selected_properties:
            value_text, value_id = _normalize_property_value_for_storage(prop.get('value'))
            Property.objects.create(
                node=new_node, 
                property_id=prop.get('property'),
                statement_id=prop.get('statement_id'),
                property_label=prop.get('property_label'),
                value=prop.get('value'),
                value_text=value_text,
                value_id=value_id
            )
        
        # Auto-fetch and store ALL P31 values if none were selected
        has_p31 = any(prop.get('property') == 'P31' for prop in selected_properties)
        
        if not has_p31 and wikidata_entity and wikidata_entity.get('id'):
            try:
                from .wikidata import get_wikidata_properties
                all_properties = get_wikidata_properties(wikidata_entity.get('id'))
                
                # Find ALL P31 properties (note: key is 'property', not 'property_id')
                p31_props = [p for p in all_properties if p.get('property') == 'P31']
                
                # Store ALL P31 values
                for p31 in p31_props:
                    value_text, value_id = _normalize_property_value_for_storage(p31.get('value'))
                    Property.objects.create(
                        node=new_node,
                        property_id='P31',
                        statement_id=p31.get('statement_id'),
                        property_label=p31.get('property_label', 'instance of'),
                        value=p31.get('value'),
                        value_text=value_text,
                        value_id=value_id
                    )
                    
                if p31_props:
                    print(f"Auto-fetched {len(p31_props)} P31 properties for node {new_node.id}")
                    
            except Exception as e:
                # Don't fail node creation if P31 fetch fails
                print(f"Failed to auto-fetch P31 for {wikidata_entity.get('id')}: {e}")
        
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
                e = Edge.objects.create(
                    source=new_node, 
                    target=related_node, 
                    relation_property=edge_label,
                    wikidata_property_id=wikidata_property_id
                )
                # --- NEO4J INTEGRATION START ---
                Neo4jConnection.create_edge(
                    edge_id=e.id,
                    source_node_id=new_node.id,
                    target_node_id=related_node.id,
                    relation_label=edge_label,
                    properties={'wikidata_property_id': wikidata_property_id}
                )
                # --- NEO4J INTEGRATION END ---
                try:
                    edge_summary = self._format_edge_summary(
                        request.user.username,
                        new_node,
                        edge_label or e.relation_property,
                        related_node,
                    )
                    record_activity(
                        actor_user=request.user,
                        type='Add',
                        object=f'Edge:{e.id}',
                        target=f'Space:{space.id}',
                        summary=edge_summary,
                        payload={'edge_id': e.id, 'source_id': new_node.id, 'target_id': related_node.id}
                    )
                except Exception:
                    pass
            else:
                e = Edge.objects.create(
                    source=related_node, 
                    target=new_node, 
                    relation_property=edge_label,
                    wikidata_property_id=wikidata_property_id
                )
                # --- NEO4J INTEGRATION START ---
                Neo4jConnection.create_edge(
                    edge_id=e.id,
                    source_node_id=related_node.id,
                    target_node_id=new_node.id,
                    relation_label=edge_label,
                    properties={'wikidata_property_id': wikidata_property_id}
                )
                # --- NEO4J INTEGRATION END ---
                try:
                    edge_summary = self._format_edge_summary(
                        request.user.username,
                        related_node,
                        edge_label or e.relation_property,
                        new_node,
                    )
                    record_activity(
                        actor_user=request.user,
                        type='Add',
                        object=f'Edge:{e.id}',
                        target=f'Space:{space.id}',
                        summary=edge_summary,
                        payload={'edge_id': e.id, 'source_id': related_node.id, 'target_id': new_node.id}
                    )
                except Exception:
                    pass

        return Response({'node_id': new_node.id}, status=201)

    @action(detail=True, methods=['get'], url_path='nodes')
    def nodes(self, request, pk=None):
        nodes = Node.objects.filter(space_id=pk, is_archived=False)
        serializer = NodeSerializer(nodes, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['get'], url_path='edges')
    def edges(self, request, pk=None):
        """Get all edges for a specific space"""
        space_nodes = Node.objects.filter(space_id=pk).values_list('id', flat=True)
        
        edges = Edge.objects.filter(
            source_id__in=space_nodes,
            target_id__in=space_nodes
        ).prefetch_related('edge_properties')
        
        data = []
        for edge in edges:
            props = [
                {
                    'statement_id': ep.statement_id,
                    'property_id': ep.property_id,
                    'property_label': ep.property_label or ep.property_id,
                    'value': ep.value,
                    'value_text': ep.value_text,
                    'value_id': ep.value_id
                }
                for ep in edge.edge_properties.all()
            ]
            data.append({
                'id': edge.id,
                'source': edge.source.id,
                'target': edge.target.id,
                'label': edge.relation_property,
                'wikidata_property_id': edge.wikidata_property_id,
                'created_at': edge.created_at,
                'properties': props
            })
        
        return Response(data)

    @action(detail=True, methods=['get'], url_path='search/text')
    def search_text(self, request, pk=None):
        """Text search on node labels/wikidata ids and edge relation labels within a space."""
        query = request.query_params.get('q', '').strip()
        if not query:
            return Response({'nodes': [], 'edges': []})

        nodes = Node.objects.filter(
            space_id=pk,
            is_archived=False
        ).filter(
            Q(label__icontains=query) | Q(wikidata_id__icontains=query)
        )

        edges = Edge.objects.filter(
            source__space_id=pk,
            relation_property__icontains=query
        ).prefetch_related('edge_properties')

        node_data = [
            {
                'id': n.id,
                'label': n.label,
                'wikidata_id': n.wikidata_id,
                'created_at': n.created_at,
                'created_by': n.created_by.id if n.created_by else None,
                'created_by_username': n.created_by.username if n.created_by else None
            }
            for n in nodes
        ]

        edge_data = []
        for edge in edges:
            edge_data.append({
                'id': edge.id,
                'source': edge.source_id,
                'target': edge.target_id,
                'label': edge.relation_property,
                'wikidata_property_id': edge.wikidata_property_id,
                'properties': [
                    {
                        'statement_id': ep.statement_id,
                        'property_id': ep.property_id,
                        'property_label': ep.property_label or ep.property_id,
                        'value': ep.value,
                        'value_text': ep.value_text,
                        'value_id': ep.value_id
                    } for ep in edge.edge_properties.all()
                ]
            })

        return Response({'nodes': node_data, 'edges': edge_data})

    @action(detail=True, methods=['get'], url_path='search/properties')
    def search_properties(self, request, pk=None):
        """List available properties (from nodes and edges) within a space with counts."""
        space = self.get_object()
        node_props = (
            Property.objects
            .filter(node__space=space)
            .values('property_id', 'property_label')
            .annotate(count=Count('id'))
        )
        edge_props = (
            EdgeProperty.objects
            .filter(edge__source__space=space)
            .values('property_id', 'property_label')
            .annotate(count=Count('id'))
        )

        results = []
        for item in node_props:
            results.append({
                'property_id': item['property_id'],
                'property_label': item['property_label'] or item['property_id'],
                'count': item['count'],
                'source': 'node'
            })
        for item in edge_props:
            results.append({
                'property_id': item['property_id'],
                'property_label': item['property_label'] or item['property_id'],
                'count': item['count'],
                'source': 'edge'
            })
        return Response(results)

    @action(detail=True, methods=['get'], url_path='search/properties/(?P<property_id>[^/.]+)/values')
    def search_property_values(self, request, pk=None, property_id=None):
        """List available values for a given property within a space."""
        space = self.get_object()
        query = request.query_params.get('q', '').strip()

        def _values_qs(model_cls, rel_field):
            qs = model_cls.objects.filter(**{f"{rel_field}__space": space, "property_id": property_id})
            if query:
                qs = qs.filter(Q(value_text__icontains=query) | Q(property_label__icontains=query))
            return qs

        node_values = (
            _values_qs(Property, 'node')
            .values('value_id', 'value_text')
            .annotate(count=Count('id'))
        )
        edge_values = (
            _values_qs(EdgeProperty, 'edge__source')
            .values('value_id', 'value_text')
            .annotate(count=Count('id'))
        )

        results = []
        for item in node_values:
            results.append({
                'value_id': item['value_id'],
                'value_text': item['value_text'],
                'count': item['count'],
                'source': 'node'
            })
        for item in edge_values:
            results.append({
                'value_id': item['value_id'],
                'value_text': item['value_text'],
                'count': item['count'],
                'source': 'edge'
            })
        return Response(results)

    def _apply_rule_to_queryset(self, qs, rule):
        property_id = rule.get('property_id') or rule.get('property')
        value_id = rule.get('value_id')
        value_text = rule.get('value_text') or rule.get('value')

        q_obj = Q(property_id=property_id)
        if value_id:
            q_obj &= (Q(value_id=value_id) | Q(value__id=value_id))
        if value_text:
            q_obj &= Q(value_text__icontains=value_text)
        return qs.filter(q_obj)

    @action(detail=True, methods=['post'], url_path='search/query')
    def search_query(self, request, pk=None):
        space = self.get_object()
        rules = request.data.get('rules', [])
        legacy_logic = request.data.get('logic')
        
        if not rules:
            return Response({'nodes': [], 'edges': []})

        node_results = []
        base_node_qs = Property.objects.filter(node__space=space)
        for i, rule in enumerate(rules):
            qs = self._apply_rule_to_queryset(base_node_qs, rule)
            ids = set(qs.values_list('node_id', flat=True))
            if ids:
                node_results.append((ids, i))

        edge_results = []
        base_edge_qs = EdgeProperty.objects.filter(edge__source__space=space)
        for i, rule in enumerate(rules):
            qs = self._apply_rule_to_queryset(base_edge_qs, rule)
            ids = set(qs.values_list('edge_id', flat=True))
            if ids:
                edge_results.append((ids, i))

        def combine_results_sequential(results, rules, legacy_logic):
            if not results:
                return set()
            if len(results) == 1:
                return results[0][0]
            
            use_sequential = any('operator' in rule for rule in rules)
            
            if use_sequential:
                result = results[0][0]
                for i in range(1, len(results)):
                    current_set, current_rule_idx = results[i]
                    prev_rule_idx = results[i-1][1]
                    
                    operator = (rules[prev_rule_idx].get('operator') or 'AND').upper()
                    if operator not in ['AND', 'OR']:
                        operator = 'AND'
                    
                    if operator == 'AND':
                        result = result & current_set
                    else:
                        result = result | current_set
                return result
            else:
                logic = (legacy_logic or 'AND').upper()
                if logic not in ['AND', 'OR']:
                    logic = 'AND'
                
                result = results[0][0]
                for s, _ in results[1:]:
                    result = result & s if logic == 'AND' else result | s
                return result

        matching_node_ids = combine_results_sequential(node_results, rules, legacy_logic)
        matching_edge_ids = combine_results_sequential(edge_results, rules, legacy_logic)

        nodes = Node.objects.filter(id__in=matching_node_ids, space=space)
        edges = Edge.objects.filter(id__in=matching_edge_ids, source__space=space).prefetch_related('edge_properties')

        node_data = NodeSerializer(nodes, many=True).data
        edge_data = []
        for edge in edges:
            edge_data.append({
                'id': edge.id,
                'source': edge.source_id,
                'target': edge.target_id,
                'label': edge.relation_property,
                'wikidata_property_id': edge.wikidata_property_id,
                'properties': [
                    {
                        'statement_id': ep.statement_id,
                        'property_id': ep.property_id,
                        'property_label': ep.property_label or ep.property_id,
                        'value': ep.value,
                        'value_text': ep.value_text,
                        'value_id': ep.value_id
                    } for ep in edge.edge_properties.all()
                ]
            })

        return Response({'nodes': node_data, 'edges': edge_data})
    
    @action(detail=True, methods=['get'], url_path='collaborators')
    def collaborators(self, request, pk=None):
        """Get collaborator statistics for a space"""
        space = self.get_object()
        
        
        collaborators = space.collaborators.all()
        
        
        timeline_data = []
        
        
        if collaborators.exists():
            
            today = timezone.now().date().isoformat()
            timeline_data.append({
                'date': today,
                'collaborator_count': collaborators.count(),
                'collaborator_ids': list(collaborators.values_list('id', flat=True))
            })
        
        return Response({
            'total_collaborators': collaborators.count(),
            'timeline': timeline_data,
            'collaborators': [
                {
                    'id': user.id,
                    'username': user.username,
                    'email': user.email
                }
                for user in collaborators
            ]
        })
    
    @action(detail=True, methods=['get'], url_path='top-collaborators')
    def top_collaborators(self, request, pk=None):
        """Get top collaborators based on activity scoring"""
        from collections import defaultdict
        
        space = self.get_object()
        
        
        collaborators = space.collaborators.all()
        
        
        collaborator_scores = defaultdict(lambda: {
            'user': None,
            'node_count': 0,
            'edge_count': 0,
            'discussion_count': 0,
            'total_score': 0
        })
        
        
        for user in collaborators:
            collaborator_scores[user.id]['user'] = user
        
        
        nodes = Node.objects.filter(space=space, created_by__in=collaborators, is_archived=False)
        for node in nodes:
            if node.created_by:
                collaborator_scores[node.created_by.id]['node_count'] += 1
        
        
        edges = Edge.objects.filter(
            source__space=space, 
            source__created_by__in=collaborators,
            source__is_archived=False,
            target__is_archived=False
        )
        for edge in edges:
            if edge.source and edge.source.created_by:
                collaborator_scores[edge.source.created_by.id]['edge_count'] += 1
        
        
        discussions = Discussion.objects.filter(space=space, user__in=collaborators)
        for discussion in discussions:
            if discussion.user:
                collaborator_scores[discussion.user.id]['discussion_count'] += 1
        
       
        scored_collaborators = []
        for user_id, data in collaborator_scores.items():
            if data['user']:  
                
                total_score = (data['node_count'] * 4) + (data['edge_count'] * 2) + (data['discussion_count'] * 1)
                
                scored_collaborators.append({
                    'id': data['user'].id,
                    'username': data['user'].username,
                    'email': data['user'].email,
                    'node_count': data['node_count'],
                    'edge_count': data['edge_count'],
                    'discussion_count': data['discussion_count'],
                    'total_score': total_score
                })
        
        
        scored_collaborators.sort(key=lambda x: x['total_score'], reverse=True)
        top_collaborators = scored_collaborators[:10]
        
        return Response({
            'top_collaborators': top_collaborators,
            'total_collaborators': len(scored_collaborators)
        })
    
    @action(detail=True, methods=['get'], url_path='snapshots')
    def snapshots(self, request, pk=None):
        snapshots = GraphSnapshot.objects.filter(space_id=pk).order_by('-created_at')
        data = [{'id': s.id, 'created_at': s.created_at} for s in snapshots]
        return Response(data)
    
    @action(detail=True, methods=['post'], url_path='snapshots/create')
    def create_snapshot(self, request, pk=None):
        space = self.get_object()
        
        if space.is_archived:
            return Response({'message': 'Cannot create snapshots for an archived space'}, status=403)
        
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can create snapshots'}, status=403)
            
        graph = SpaceGraph(pk)
        graph.load_from_db()
        snapshot = graph.create_snapshot(request.user)
        return Response({'snapshot_id': snapshot.id, 'created_at': snapshot.created_at})
    
    @action(detail=True, methods=['post'], url_path='snapshots/revert')
    def revert_snapshot(self, request, pk=None):
        space = self.get_object()
        
        if space.is_archived:
            return Response({'message': 'Cannot revert snapshots for an archived space'}, status=403)
        
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can revert snapshots'}, status=403)
            
        snapshot_id = request.data.get('snapshot_id')
        if not snapshot_id: 
            return Response({'error': 'snapshot_id is required'}, status=400)
        
        graph = SpaceGraph(pk)
        graph.revert_to_snapshot(snapshot_id)
        try:
            record_activity(
                actor_user=request.user,
                type='Update',
                object=f'Space:{space.id}',
                target=f'Snapshot:{snapshot_id}',
                summary=f"{request.user.username} reverted graph to snapshot {snapshot_id}",
                payload={'snapshot_id': snapshot_id, 'space_id': space.id}
            )
        except Exception:
            pass
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

    @action(detail=True, methods=['get'], url_path='all-properties')
    def all_properties(self, request, pk=None):
        """Get all unique properties across all nodes in a space"""
        try:
            space = self.get_object()
            
            # Get all unique properties in this space
            properties = Property.objects.filter(
                node__space_id=pk
            ).values(
                'id', 'property_id', 'property_label'
            ).distinct()
            
            # Convert to list and remove duplicates
            props_list = []
            seen_ids = set()
            
            for prop in properties:
                prop_id = prop['property_id']
                if prop_id not in seen_ids:
                    seen_ids.add(prop_id)
                    props_list.append({
                        'id': prop['id'],
                        'property': prop['property_id'],
                        'property_label': prop['property_label'] or prop['property_id']
                    })
            
            # Sort by label for better UX
            props_list.sort(key=lambda x: x['property_label'])
            
            return Response(props_list)
        except Exception as e:
            print(f"Error fetching all properties: {str(e)}")
            return Response(
                {"error": str(e)},
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
        
        if space.is_archived:
            return Response({'message': 'Cannot delete nodes in an archived space'}, status=403)
        
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can delete nodes'}, status=403)
            
        try:
            node = Node.objects.get(id=node_id, space_id=pk)
            deleted_node_label = node.label
            Edge.objects.filter(source=node).delete()
            Edge.objects.filter(target=node).delete()
            Property.objects.filter(node=node).delete()
            deleted_node_id = node.id
            node.delete()

            # --- NEO4J INTEGRATION START ---
            Neo4jConnection.delete_node(deleted_node_id)
            # --- NEO4J INTEGRATION END ---

            try:
                record_activity(
                    actor_user=request.user,
                    type='Delete',
                    object=f'Node:{deleted_node_id}',
                    target=f'Space:{space.id}',
                    summary=f"{request.user.username} deleted node '{deleted_node_label}'",
                    payload={'node_id': deleted_node_id, 'node_label': deleted_node_label, 'space_id': space.id}
                )
            except Exception:
                pass
            return Response({'message': 'Node successfully deleted'}, status=200)
        except Node.DoesNotExist:
            return Response({'error': 'Node not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)
    
    @action(detail=True, methods=['get'], url_path='instance-types')
    def get_instance_types(self, request, pk=None):
        """
        Get instance type GROUPS for nodes in this space.
        
        Returns groups (not individual types) with counts.
        
        Returns:
            {
                'instance_groups': [
                    {'group_id': 'CITY', 'group_label': 'City', 'count': 10},
                    {'group_id': 'HUMAN', 'group_label': 'Human', 'count': 5},
                    ...
                ],
                'nodes_by_group': {
                    'CITY': [1, 2, 3, ...],
                    'HUMAN': [4, 5, ...],
                    ...
                }
            }
        """
        try:
            space = self.get_object()
            
            # Get all nodes with their instance types
            from .serializers import NodeSerializer
            nodes = space.node_set.all()
            
            # Count nodes per group
            group_counts = {}
            nodes_by_group = {}
            
            for node in nodes:
                serializer = NodeSerializer(node)
                instance_type = serializer.data.get('instance_type')
                
                if instance_type and instance_type.get('group_id'):
                    group_id = instance_type['group_id']
                    
                    if group_id not in group_counts:
                        group_counts[group_id] = {
                            'group_id': group_id,
                            'group_label': instance_type['group_label'],
                            'count': 0
                        }
                        nodes_by_group[group_id] = []
                    
                    group_counts[group_id]['count'] += 1
                    nodes_by_group[group_id].append(node.id)
            
            # Sort by count
            instance_groups = sorted(
                group_counts.values(),
                key=lambda x: x['count'],
                reverse=True
            )
            
            return Response({
                'instance_groups': instance_groups,
                'nodes_by_group': nodes_by_group
            })
        except Exception as e:
            return Response({'error': str(e)}, status=500)
    
    @action(detail=True, methods=['put'], url_path='nodes/(?P<node_id>[^/.]+)/update-properties')
    def update_node_properties(self, request, pk=None, node_id=None):
        """Update the properties of a node"""
        space = self.get_object()
        
        if space.is_archived:
            return Response({'message': 'Cannot update nodes in an archived space'}, status=403)
        
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can update nodes'}, status=403)
            
        try:
            node = Node.objects.get(id=node_id, space_id=pk)
            selected_properties = request.data.get('selected_properties', [])
            Property.objects.filter(node=node).delete()

            for prop in selected_properties:
                if not prop:
                    continue
                value_text, value_id = _normalize_property_value_for_storage(prop.get('value'))
                Property.objects.create(
                    node=node,
                    property_id=prop.get('property'),
                    statement_id=prop.get('statement_id'),
                    property_label=prop.get('property_label'),
                    value=prop.get('value'),
                    value_text=value_text,
                    value_id=value_id
                )
            
            # Auto-fetch and store ALL P31 values if none were selected
            has_p31 = any(prop.get('property') == 'P31' for prop in selected_properties if prop)
            
            if not has_p31 and node.wikidata_id:
                try:
                    from .wikidata import get_wikidata_properties
                    all_properties = get_wikidata_properties(node.wikidata_id)
                    
                    # Find ALL P31 properties
                    p31_props = [p for p in all_properties if p.get('property_id') == 'P31']
                    
                    # Store ALL P31 values
                    for p31 in p31_props:
                        value_text, value_id = _normalize_property_value_for_storage(p31.get('value'))
                        Property.objects.create(
                            node=node,
                            property_id='P31',
                            statement_id=p31.get('statement_id'),
                            property_label=p31.get('property_label', 'instance of'),
                            value=p31.get('value'),
                            value_text=value_text,
                            value_id=value_id
                        )
                        
                    if p31_props:
                        print(f"Auto-fetched {len(p31_props)} P31 properties for node {node.id}")
                        
                except Exception as e:
                    # Don't fail node update if P31 fetch fails
                    print(f"Failed to auto-fetch P31 for {node.wikidata_id}: {e}")
            
            if selected_properties:
                location_data = extract_location_from_properties(selected_properties)
                
                if any(location_data.values()):
                    for field, value in location_data.items():
                        if value is not None:
                            setattr(node, field, value)
                    node.save()
                
                # --- NEO4J INTEGRATION START ---
                # Update Neo4j properties
                neo4j_update_props = {}
                if location_data:
                    neo4j_update_props.update(location_data)
                
                for prop in selected_properties:
                    key = prop.get('property_label') or prop.get('property')
                    value = prop.get('value')
                    if key and value:
                        safe_key = "".join(x for x in key if x.isalnum() or x == "_")
                        if safe_key:
                            neo4j_update_props[safe_key] = str(value)
                
                if neo4j_update_props:
                    Neo4jConnection.update_node(
                        node_id=node.id,
                        properties=neo4j_update_props
                    )
                # --- NEO4J INTEGRATION END ---
                
            try:
                record_activity(
                    actor_user=request.user,
                    type='Update',
                    object=f'Node:{node.id}',
                    target=f'Space:{space.id}',
                    summary=f"{request.user.username} updated node properties",
                    payload={'node_id': node.id, 'space_id': space.id}
                )
            except Exception:
                pass
            return Response({'message': 'Node properties updated'}, status=200)
        except Node.DoesNotExist:
            return Response({'error': 'Node not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    @action(detail=True, methods=['delete'], url_path='nodes/(?P<node_id>[^/.]+)/properties/(?P<statement_id>[^/.]+)')
    def delete_node_property(self, request, pk=None, node_id=None, statement_id=None):
        """Delete a single property from a node by its statement_id"""
        space = self.get_object()
        
        if space.is_archived:
            return Response({'message': 'Cannot delete node properties in an archived space'}, status=403)
        
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can update nodes'}, status=403)
        
        try:
            node = Node.objects.get(id=node_id, space_id=pk)
            property_to_delete = Property.objects.get(node=node, statement_id=statement_id)
            
            # --- NEO4J INTEGRATION START ---
            prop_key = property_to_delete.property_label or property_to_delete.property_id
            if prop_key:
                safe_key = "".join(x for x in prop_key if x.isalnum() or x == "_")
                if safe_key:
                    Neo4jConnection.delete_node_property(node.id, safe_key)
            # --- NEO4J INTEGRATION END ---

            property_to_delete.delete()
            try:
                record_activity(
                    actor_user=request.user,
                    type='Remove',
                    object=f'Property:{statement_id}',
                    target=f'Node:{node.id}',
                    summary=f"{request.user.username} removed a node property",
                    payload={'node_id': node.id, 'statement_id': statement_id}
                )
            except Exception:
                pass
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
        
        if space.is_archived:
            return Response({'message': 'Cannot update nodes in an archived space'}, status=403)
        
        if request.user != space.creator and request.user not in space.collaborators.all():
            return Response({'message': 'Only space members can update node location'}, status=403)
            
        try:
            node = Node.objects.get(id=node_id, space_id=pk)
            
            
            location_data = request.data.get('location', request.data)
            print(f"Received location data: {location_data}")
            
            
            node.country = location_data.get('country', '') or None
            node.city = location_data.get('city', '') or None
            node.district = location_data.get('district', '') or None
            node.street = location_data.get('street', '') or None
            node.latitude = location_data.get('latitude') if location_data.get('latitude') is not None else None
            node.longitude = location_data.get('longitude') if location_data.get('longitude') is not None else None
            node.location_name = location_data.get('location_name', '') or None
            
            print(f"Updated node location - Country: {node.country}, City: {node.city}, Lat: {node.latitude}, Lng: {node.longitude}")
            
            node.save()

            # --- NEO4J INTEGRATION START ---
            neo4j_loc_props = {
                'country': node.country,
                'city': node.city,
                'district': node.district,
                'street': node.street,
                'latitude': node.latitude,
                'longitude': node.longitude,
                'location_name': node.location_name
            }
            Neo4jConnection.update_node(
                node_id=node.id,
                properties=neo4j_loc_props
            )
            # --- NEO4J INTEGRATION END ---

            try:
                record_activity(
                    actor_user=request.user,
                    type='Update',
                    object=f'Node:{node.id}',
                    target=f'Space:{space.id}',
                    summary=f"{request.user.username} updated node location",
                    payload={'node_id': node.id, 'space_id': space.id}
                )
            except Exception:
                pass
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
        
        if space.is_archived:
            return Response({'message': 'Cannot update edges in an archived space'}, status=403)
        
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can update edges'}, status=403)
        try:
            edge = Edge.objects.get(id=edge_id, source__space=space)
            new_label = request.data.get('label', '').strip()
            new_source_id = request.data.get('source_id')
            new_target_id = request.data.get('target_id')
            wikidata_property_id = request.data.get('wikidata_property_id', None)
            edge_properties = request.data.get('edge_properties', None)

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

            # --- NEO4J INTEGRATION START ---
            # If label or endpoints changed, we must recreate the edge in Neo4j
            # because relationship types are immutable and endpoints define the relationship identity.
            # For simplicity, we'll delete and recreate if there's any structural change.
            # If only properties changed, we could just update properties, but recreating is safer for consistency here.
            
            neo4j_edge_props = {'wikidata_property_id': wikidata_property_id}
            if edge_properties:
                for prop in edge_properties:
                    key = prop.get('property_label') or prop.get('property')
                    value = prop.get('value')
                    if key and value:
                        safe_key = "".join(x for x in key if x.isalnum() or x == "_")
                        if safe_key:
                            neo4j_edge_props[safe_key] = str(value)

            Neo4jConnection.delete_edge(edge.id)
            Neo4jConnection.create_edge(
                edge_id=edge.id,
                source_node_id=edge.source.id,
                target_node_id=edge.target.id,
                relation_label=edge.relation_property,
                properties=neo4j_edge_props
            )
            # --- NEO4J INTEGRATION END ---

            if edge_properties is not None:
                EdgeProperty.objects.filter(edge=edge).delete()
                for prop in edge_properties:
                    if not prop:
                        continue
                    value_text, value_id = _normalize_property_value_for_storage(prop.get('value'))
                    EdgeProperty.objects.create(
                        edge=edge,
                        property_id=prop.get('property'),
                        statement_id=prop.get('statement_id'),
                        property_label=prop.get('property_label'),
                        value=prop.get('value'),
                        value_text=value_text,
                        value_id=value_id
                    )
            try:
                record_activity(
                    actor_user=request.user,
                    type='Update',
                    object=f'Edge:{edge.id}',
                    target=f'Space:{space.id}',
                    summary=f"{request.user.username} updated an edge",
                    payload={'edge_id': edge.id}
                )
            except Exception:
                pass
            return Response({'message': 'Edge updated successfully'}, status=200)
        except Edge.DoesNotExist:
            return Response({'error': 'Edge not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    @action(detail=True, methods=['delete'], url_path='edges/(?P<edge_id>[^/.]+)/delete')
    def delete_edge(self, request, pk=None, edge_id=None):
        """Delete an edge from the graph"""
        space = self.get_object()
        
        if space.is_archived:
            return Response({'message': 'Cannot delete edges in an archived space'}, status=403)
        
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can delete edges'}, status=403)
        try:
            edge = Edge.objects.get(id=edge_id, source__space=space)
            eid = edge.id
            sid = edge.source_id
            tid = edge.target_id
            edge.delete()

            # --- NEO4J INTEGRATION START ---
            Neo4jConnection.delete_edge(eid)
            # --- NEO4J INTEGRATION END ---

            try:
                record_activity(
                    actor_user=request.user,
                    type='Delete',
                    object=f'Edge:{eid}',
                    target=f'Space:{space.id}',
                    summary=f"{request.user.username} deleted edge {sid}->{tid}",
                    payload={'edge_id': eid, 'source_id': sid, 'target_id': tid}
                )
            except Exception:
                pass
            return Response({'message': 'Edge deleted successfully'}, status=200)
        except Edge.DoesNotExist:
            return Response({'error': 'Edge not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    @action(detail=True, methods=['post'], url_path='edges/add')
    def add_edge(self, request, pk=None):
        """Add an edge between two existing nodes in the space"""
        space = self.get_object()
        
        if space.is_archived:
            return Response({'message': 'Cannot add edges to an archived space'}, status=403)
        
        if request.user not in space.collaborators.all():
            return Response({'message': 'Only collaborators can add edges'}, status=403)
        source_id = request.data.get('source_id')
        target_id = request.data.get('target_id')
        label = request.data.get('label', '').strip()
        wikidata_property_id = request.data.get('wikidata_property_id', None)
        edge_properties = request.data.get('edge_properties', [])
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

            # --- NEO4J INTEGRATION START ---
            neo4j_edge_props = {'wikidata_property_id': wikidata_property_id}
            for prop in edge_properties:
                key = prop.get('property_label') or prop.get('property')
                value = prop.get('value')
                if key and value:
                    safe_key = "".join(x for x in key if x.isalnum() or x == "_")
                    if safe_key:
                        neo4j_edge_props[safe_key] = str(value)

            Neo4jConnection.create_edge(
                edge_id=edge.id,
                source_node_id=source.id,
                target_node_id=target.id,
                relation_label=label,
                properties=neo4j_edge_props
            )
            # --- NEO4J INTEGRATION END ---

            for prop in edge_properties:
                value_text, value_id = _normalize_property_value_for_storage(prop.get('value'))
                EdgeProperty.objects.create(
                    edge=edge,
                    property_id=prop.get('property'),
                    statement_id=prop.get('statement_id'),
                    property_label=prop.get('property_label'),
                    value=prop.get('value'),
                    value_text=value_text,
                    value_id=value_id
                )
            try:
                edge_summary = self._format_edge_summary(
                    request.user.username,
                    source,
                    label,
                    target,
                )
                record_activity(
                    actor_user=request.user,
                    type='Add',
                    object=f'Edge:{edge.id}',
                    target=f'Space:{space.id}',
                    summary=edge_summary,
                    payload={'edge_id': edge.id, 'source_id': source.id, 'target_id': target.id}
                )
            except Exception:
                pass
            return Response({'message': 'Edge created', 'edge_id': edge.id}, status=201)
        except Node.DoesNotExist:
            return Response({'error': 'Node not found'}, status=404)
        except Exception as e:
            return Response({'error': str(e)}, status=500)

    def update(self, request, *args, **kwargs):
        space = self.get_object()
        
        if space.is_archived:
            return Response({'detail': 'Cannot update an archived space'}, status=403)
        
        return super().update(request, *args, **kwargs)
    
    def destroy(self, request, *args, **kwargs):
        space = self.get_object()
        user = request.user
        
        if space.is_archived:
            return Response({'detail': 'Cannot delete an archived space'}, status=403)
    
        # Permission check
        if user != space.creator and not (user.is_staff or user.is_superuser):
            return Response({'detail': 'You do not have permission to delete this space.'}, status=403)
    
        try:
            response = super().destroy(request, *args, **kwargs)
        except Exception as e:
            return Response({'detail': str(e)}, status=400)
    
        try:
            record_activity(
                actor_user=user,
                type='Delete',
                object=f"Space:{space.id}",
                summary=f"{user.username} deleted space '{space.title}'",
                payload={'space_id': space.id}
            )
        except Exception:
            pass
    
        return response

    @action(detail=True, methods=['post'], url_path='summarize')
    def summarize_space(self, request, pk=None):
        import os
        
        try:
            space = self.get_object()
            
            nodes = Node.objects.filter(space=space, is_archived=False)
            edges = Edge.objects.filter(source__space=space, source__is_archived=False, target__is_archived=False)
            discussions = Discussion.objects.filter(space=space).order_by('-created_at')[:10]  # Latest 10 discussions
            
            nodes_with_connections = nodes.annotate(
                outgoing_count=Count('source_edges', distinct=True),
                incoming_count=Count('target_edges', distinct=True)
            )
            
            total_nodes = nodes_with_connections.count()
            
            if total_nodes > 0:
                nodes_sorted_by_connections = sorted(
                    nodes_with_connections,
                    key=lambda n: n.outgoing_count + n.incoming_count,
                    reverse=True
                )
            else:
                nodes_sorted_by_connections = []
            
            space_data = {
                'title': space.title,
                'description': space.description or 'No description',
                'creator': space.creator.username,
                'collaborators': [user.username for user in space.collaborators.all()],
                'tags': [tag.name for tag in space.tags.all()],
                'location': {
                    'country': space.country or 'Not specified',
                    'city': space.city or 'Not specified',
                }
            }
            
            # nodes with connection counts
            nodes_data = []
            for node in nodes_with_connections[:30]:
                try:
                    node_info = {
                        'label': node.label or 'Unlabeled',
                        'wikidata_id': node.wikidata_id or 'N/A',
                        'connections': node.outgoing_count + node.incoming_count,
                        'properties': []
                    }
                    for prop in node.node_properties.all()[:5]:
                        node_info['properties'].append({
                            'property': prop.property_label or prop.property_id or 'Unknown',
                            'value': prop.value_text or prop.value_id or 'N/A'
                        })
                    nodes_data.append(node_info)
                except Exception as node_error:
                    continue
            
            # edges
            edges_data = []
            
            top_node_ids = {node.id for node in nodes_sorted_by_connections[:30]}
            filtered_edges = [
                e for e in edges 
                if e.source_id in top_node_ids and e.target_id in top_node_ids
            ]
            
            if len(filtered_edges) < 30:
                remaining_edges = [e for e in edges if e not in filtered_edges]
                filtered_edges.extend(remaining_edges[:30 - len(filtered_edges)])
            
            for edge in filtered_edges[:30]:
                try:
                    edge_info = {
                        'source': edge.source.label if (edge.source and edge.source.label) else 'Unknown',
                        'target': edge.target.label if (edge.target and edge.target.label) else 'Unknown',
                        'relation': edge.relation_property or 'connected to'
                    }
                    edges_data.append(edge_info)
                except Exception as edge_error:
                    continue
            
            # COMMENTS
            discussions_data = []
            for discussion in discussions:
                try:
                    discussions_data.append({
                        'user': discussion.user.username,
                        'text': (discussion.text[:200] if discussion.text else 'No text'),
                        'created_at': discussion.created_at.strftime('%Y-%m-%d')
                    })
                except Exception as disc_error:
                    continue
            
            # NODE with connection counts
            nodes_text = []
            for n in nodes_data:  # Use all 30 sorted nodes
                try:
                    connection_info = f"[{n['connections']} connections]"
                    if n['properties']:
                        props_text = ', '.join([f"{p['property']}: {p['value']}" for p in n['properties']])
                        nodes_text.append(f"- {n['label']} ({n['wikidata_id']}) {connection_info}: {props_text}")
                    else:
                        nodes_text.append(f"- {n['label']} ({n['wikidata_id']}) {connection_info}: No properties")
                except Exception as e:
                    continue
            nodes_section = '\n'.join(nodes_text) if nodes_text else 'No nodes yet'
            
            # EDGE
            edges_text = []
            for e in edges_data[:20]:
                try:
                    edges_text.append(f"- {e['source']} → [{e['relation']}] → {e['target']}")
                except Exception as e_error:
                    continue
            edges_section = '\n'.join(edges_text) if edges_text else 'No edges yet'
            
            discussions_text = []
            for d in discussions_data[:5]:
                try:
                    discussions_text.append(f"- {d['user']} ({d['created_at']}): {d['text']}")
                except Exception as d_error:
                    continue
            discussions_section = '\n'.join(discussions_text) if discussions_text else 'No discussions yet'
            
            ##### PROMPT #######
            prompt = f"""Analyze and summarize the following knowledge graph space:

**Space Information:**
- Title: {space_data['title']}
- Description: {space_data['description']}
- Creator: {space_data['creator']}
- Collaborators: {', '.join(space_data['collaborators']) if space_data['collaborators'] else 'None'}
- Tags: {', '.join(space_data['tags']) if space_data['tags'] else 'None'}
- Location: {space_data['location']['city']}, {space_data['location']['country']}

**Graph Structure:**
- Total Nodes: {nodes.count()}
- Total Edges: {edges.count()}

**Sample Nodes (top 30 by connections):**
{nodes_section}

**Sample Connections:**
{edges_section}

**Recent Discussions:**
{discussions_section}

Please provide a comprehensive summary using markdown formatting:
- Use **bold** for important concepts, entities, and key insights
- Use ### for section headings
- Use bullet points (-) for lists
- Keep it organized and easy to scan
- Make it engaging and informative

Include these sections:
### Overview
Briefly describe the main theme and purpose of this knowledge graph (2-3 sentences with key terms in **bold**)

### Key Entities & Relationships
List the most important entities and their connections using **bold** for entity names

### Insights & Patterns
Highlight interesting patterns or notable findings with **bold** emphasis on key points

### Activity & Collaboration
Describe the collaboration level and discussion activity with important metrics in **bold**

Keep the summary concise (2-3 paragraphs total) but well-formatted for easy reading."""

            api_key = os.getenv('GEMINI_API_KEY')
            if not api_key:
                return Response({
                    'error': 'Gemini API key not configured. Please set GEMINI_API_KEY environment variable.'
                }, status=500)
            
            genai.configure(api_key=api_key)
            
            model_name = 'gemini-2.5-flash'
            model_used = model_name
            
            try:
                model = genai.GenerativeModel(model_name)
                response = model.generate_content(prompt)
                summary = response.text
            except Exception as flash_error:
                error_str = str(flash_error).lower()
                if 'quota' in error_str or 'resource' in error_str or '429' in error_str:
                    model_name = 'gemini-2.5-flash-lite'
                    model_used = model_name
                    try:
                        model = genai.GenerativeModel(model_name)
                        response = model.generate_content(prompt)
                        summary = response.text
                    except Exception as lite_error:
                        raise Exception(f"Both models failed. Flash error: {flash_error}, Lite error: {lite_error}")
                else:
                    raise flash_error
            
            return Response({
                'summary': summary,
                'model_used': model_used,
                'metadata': {
                    'node_count': nodes.count(),
                    'edge_count': edges.count(),
                    'discussion_count': discussions.count(),
                    'collaborator_count': space.collaborators.count()
                }
            })
            
        except Exception as e:
            return Response({
                'error': f'Failed to generate summary: {str(e)}'
            }, status=500)

    @action(detail=False, methods=['get'], url_path='top-scored', permission_classes=[IsAuthenticated])
    def top_scored(self, request):
        """Get top scored spaces based on: Node (4pts) + Edge (2pts) + Contributor (4pts) + Discussion (1pt)"""
        limit = int(request.query_params.get('limit', 10))
        
        
        spaces = Space.objects.prefetch_related('collaborators', 'tags').all()
        
        spaces_with_scores = []
        for space in spaces:
            # Count nodes and edges (excluding archived nodes)
            node_count = Node.objects.filter(space=space, is_archived=False).count()
            edge_count = Edge.objects.filter(source__space=space, source__is_archived=False, target__is_archived=False).count()
            
            # Count collaborators 
            collaborator_count = space.collaborators.count()
            
            # Count discussions
            discussion_count = Discussion.objects.filter(space=space).count()
            
            # Calculate score: Node (4pts) + Edge (2pts) + Contributor (4pts) + Discussion (1pt)
            score = (node_count * 4) + (edge_count * 2) + (collaborator_count * 4) + (discussion_count * 1)
            
            spaces_with_scores.append({
                'id': space.id,
                'title': space.title,
                'description': space.description,
                'creator_username': space.creator.username,
                'created_at': space.created_at,
                'country': space.country,
                'city': space.city,
                'district': space.district,
                'street': space.street,
                'latitude': space.latitude,
                'longitude': space.longitude,
                'tags': [{'id': tag.id, 'name': tag.name} for tag in space.tags.all()],
                'collaborators': [user.username for user in space.collaborators.all()],
                'node_count': node_count,
                'edge_count': edge_count,
                'collaborator_count': collaborator_count,
                'discussion_count': discussion_count,
                'score': score
            })
        
        # Sort by score (highest first) and limit results
        spaces_with_scores.sort(key=lambda x: x['score'], reverse=True)
        top_spaces = spaces_with_scores[:limit]
        
        return Response(top_spaces)

class ProfileViewSet(viewsets.ModelViewSet):
    queryset = Profile.objects.all()
    serializer_class = ProfileSerializer
    
    def get_permissions(self):
        """
        Read operations need IsAuthenticated + IsProfileOwner.
        Update operations need IsAuthenticated + IsProfileOwner + IsNotArchivedUser.
        """
        if self.action in ['list', 'retrieve', 'me', 'user_profile']:
            permission_classes = [permissions.IsAuthenticated, IsProfileOwner]
        elif self.action in ['update', 'partial_update', 'update_profile']:
            permission_classes = [permissions.IsAuthenticated, IsProfileOwner, IsNotArchivedUser]
        else:
            permission_classes = [permissions.IsAuthenticated, IsProfileOwner]
        return [permission() for permission in permission_classes]

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

class ReportViewSet(viewsets.ModelViewSet):
    queryset = Report.objects.all()
    serializer_class = ReportSerializer
    
    def get_permissions(self):
        """
        Read operations only need IsAuthenticated.
        Create operations need IsAuthenticated + IsNotArchivedUser.
        Update operations are for admins/moderators only (handled separately).
        """
        if self.action in ['list', 'retrieve', 'reasons']:
            permission_classes = [permissions.IsAuthenticated]
        elif self.action == 'create':
            permission_classes = [permissions.IsAuthenticated, IsNotArchivedUser]
        else:
            # Other actions (update, partial_update, dismiss) have their own permission checks in the methods
            permission_classes = [permissions.IsAuthenticated]
        return [permission() for permission in permission_classes]

    def _get_report_subject_label(self, report):
        try:
            if report.content_type == Report.CONTENT_SPACE:
                space = report.space or Space.objects.filter(id=report.content_id).first()
                return space.title if space else f"Space #{report.content_id}"
            if report.content_type == Report.CONTENT_NODE:
                node = Node.objects.filter(id=report.content_id).first()
                if node and hasattr(node, 'label'):
                    return f"Node: {node.label}"
                return f"Node #{report.content_id}"
            if report.content_type == Report.CONTENT_DISCUSSION:
                discussion = Discussion.objects.filter(id=report.content_id).first()
                if discussion and discussion.text:
                    snippet = (discussion.text[:30] + '…') if len(discussion.text) > 30 else discussion.text
                    return f"Discussion \"{snippet}\""
                return f"Discussion #{report.content_id}"
            if report.content_type == Report.CONTENT_PROFILE:
                profile = Profile.objects.filter(user__id=report.content_id).first()
                if profile and profile.user:
                    return profile.user.username
                return f"Profile #{report.content_id}"
        except Exception:
            pass
        return f"{report.content_type.title()} #{report.content_id}"

    def get_queryset(self):
        user = self.request.user
        qs = super().get_queryset()
        # Admins can see all
        if user.is_staff or user.is_superuser or user.profile.is_admin():
            return qs
        # Moderators (either profile flag OR having SpaceModerator rows) can only see reports
        # in spaces they moderate, and not profile reports (space is null)
        moderated_space_ids = list(SpaceModerator.objects.filter(user=user).values_list('space_id', flat=True))
        if user.profile.is_moderator() or len(moderated_space_ids) > 0:
            return qs.filter(space_id__in=moderated_space_ids).exclude(content_type=Report.CONTENT_PROFILE)
        # Regular users: no listing
        return qs.none()

    def retrieve(self, request, *args, **kwargs):
        instance = self.get_object()
        serializer = self.get_serializer(instance)
        data = serializer.data

        content_type = instance.content_type
        content_id = instance.content_id
        content_object_data = None
        ModelClass, SerializerClass = None, None

        if content_type == 'space':
            ModelClass, SerializerClass = Space, SpaceSerializer
        elif content_type == 'node':
            ModelClass, SerializerClass = Node, NodeSerializer
        elif content_type == 'discussion':
            ModelClass, SerializerClass = Discussion, DiscussionSerializer
        elif content_type == 'profile':
            ModelClass, SerializerClass = Profile, ProfileSerializer

        if ModelClass:
            try:
                instance_qs = ModelClass.objects
                if content_type == 'profile':
                    content_instance = instance_qs.get(user__id=content_id)
                else:
                    content_instance = instance_qs.get(id=content_id)
                
                context = {'request': self.request} if SerializerClass == DiscussionSerializer else {}
                content_object_data = SerializerClass(content_instance, context=context).data
            except ModelClass.DoesNotExist:
                content_object_data = {'error': f'{content_type} with id {content_id} not found.'}
        
        data['content_object'] = content_object_data
        return Response(data)

    def _get_grouped_reports(self, status):
        queryset = self.get_queryset().filter(status=status).order_by('-created_at')

        grouped_reports = {}
        for report in queryset:
            key = (report.content_type, report.content_id)
            if key not in grouped_reports:
                grouped_reports[key] = {
                    'content_type': report.content_type,
                    'content_id': report.content_id,
                    'content_object': None,
                    'reports': []
                }
            grouped_reports[key]['reports'].append(report)

        content_keys = list(grouped_reports.keys())
        for content_type, content_id in content_keys:
            key = (content_type, content_id)
            ModelClass, SerializerClass = None, None
            if content_type == 'space':
                ModelClass, SerializerClass = Space, SpaceSerializer
            elif content_type == 'node':
                ModelClass, SerializerClass = Node, NodeSerializer
            elif content_type == 'discussion':
                ModelClass, SerializerClass = Discussion, DiscussionSerializer
            elif content_type == 'profile':
                ModelClass, SerializerClass = Profile, ProfileSerializer
            
            if ModelClass:
                try:
                    instance_qs = ModelClass.objects
                    if content_type == 'profile':
                        instance = instance_qs.get(user__id=content_id)
                    else:
                        instance = instance_qs.get(id=content_id)
                    
                    context = {'request': self.request} if SerializerClass == DiscussionSerializer else {}
                    grouped_reports[key]['content_object'] = SerializerClass(instance, context=context).data
                except ModelClass.DoesNotExist:
                    grouped_reports[key]['content_object'] = {'error': f'{content_type} with id {content_id} not found.'}
                    
        ReportSerializerClass = self.get_serializer_class()
        for key, group in grouped_reports.items():
            group['reports'] = ReportSerializerClass(group['reports'], many=True).data

        return list(grouped_reports.values())

    def list(self, request, *args, **kwargs):
        """
        By default, list only OPEN reports, grouped by content.
        For other statuses, use /reports/dismissed/ or /reports/archived/.
        """
        grouped_data = self._get_grouped_reports(Report.STATUS_OPEN)
        page = self.paginate_queryset(grouped_data)
        if page is not None:
            return self.get_paginated_response(page)
        return Response(grouped_data)

    @action(detail=False, methods=['get'], url_path='open')
    def open(self, request):
        """List all reports with OPEN status, grouped by content."""
        grouped_data = self._get_grouped_reports(Report.STATUS_OPEN)
        page = self.paginate_queryset(grouped_data)
        if page is not None:
            return self.get_paginated_response(page)
        return Response(grouped_data)

    @action(detail=False, methods=['get'], url_path='dismissed')
    def dismissed(self, request):
        """List all reports with DISMISSED status, grouped by content."""
        grouped_data = self._get_grouped_reports(Report.STATUS_DISMISSED)
        page = self.paginate_queryset(grouped_data)
        if page is not None:
            return self.get_paginated_response(page)
        return Response(grouped_data)

    @action(detail=False, methods=['get'], url_path='archived')
    def archived(self, request):
        """List all reports with ARCHIVED status, grouped by content."""
        grouped_data = self._get_grouped_reports(Report.STATUS_ARCHIVED)
        page = self.paginate_queryset(grouped_data)
        if page is not None:
            return self.get_paginated_response(page)
        return Response(grouped_data)

    def perform_create(self, serializer):
        report = serializer.save()
        _recompute_entity_reports(report.content_type, report.content_id)
        try:
            space_reference = f"Space:{report.space_id}" if report.space_id else None
            subject_label = self._get_report_subject_label(report)
            record_activity(
                actor_user=self.request.user,
                type='Report',
                object=f"{report.content_type}:{report.content_id}",
                target=space_reference,
                summary=f"{self.request.user.username} reported {subject_label}",
                payload={
                    'report_id': report.id,
                    'space_id': report.space_id,
                    'status': report.status,
                    'reason': report.reason,
                },
            )
        except Exception:
            pass

    def partial_update(self, request, *args, **kwargs):
        pk = kwargs.get('pk')
        try:
            report = Report.objects.get(pk=pk)
        except Report.DoesNotExist:
            return Response({'error': 'Report not found'}, status=404)
        
        new_status = request.data.get('status')
        if new_status not in [Report.STATUS_OPEN, Report.STATUS_DISMISSED, Report.STATUS_ARCHIVED]:
            return Response({'error': 'Invalid status'}, status=400)

        user = request.user
        # Admins can update any; moderators only within their spaces and not profile reports
        if not (user.is_staff or user.is_superuser or user.profile.is_admin()):
            # Check if user is a moderator (either by profile flag OR by SpaceModerator assignment)
            moderated_space_ids = list(SpaceModerator.objects.filter(user=user).values_list('space_id', flat=True))
            is_moderator = user.profile.is_moderator() or len(moderated_space_ids) > 0
            
            if not is_moderator:
                return Response({'error': 'Insufficient permissions'}, status=403)
            if report.content_type == Report.CONTENT_PROFILE:
                return Response({'error': 'Profile reports can only be updated by admins'}, status=403)
            if not SpaceModerator.objects.filter(user=user, space=report.space).exists():
                return Response({'error': 'You are not a moderator of this space'}, status=403)

        report.status = new_status
        report.save(update_fields=['status', 'updated_at'])
        _recompute_entity_reports(report.content_type, report.content_id)
        serializer = self.get_serializer(report)
        return Response(serializer.data)

    @action(detail=True, methods=['post'], url_path='dismiss')
    def dismiss(self, request, pk=None):
        """Dismiss a report. Only admins and moderators can dismiss reports."""
        # Get report directly from database to check permissions before filtering
        try:
            report = Report.objects.get(pk=pk)
        except Report.DoesNotExist:
            return Response({'error': 'Report not found'}, status=404)
        
        user = request.user

        # Check permissions: Admins can dismiss any; moderators only within their spaces and not profile reports
        if not (user.is_staff or user.is_superuser or user.profile.is_admin()):
            # Check if user is a moderator (either by profile flag OR by SpaceModerator assignment)
            moderated_space_ids = list(SpaceModerator.objects.filter(user=user).values_list('space_id', flat=True))
            is_moderator = user.profile.is_moderator() or len(moderated_space_ids) > 0
            
            if not is_moderator:
                return Response({'error': 'Insufficient permissions'}, status=403)
            if report.content_type == Report.CONTENT_PROFILE:
                return Response({'error': 'Profile reports can only be dismissed by admins'}, status=403)
            if not SpaceModerator.objects.filter(user=user, space=report.space).exists():
                return Response({'error': 'You are not a moderator of this space'}, status=403)

        open_reports = Report.objects.filter(
            content_type=report.content_type,
            content_id=report.content_id,
            status=Report.STATUS_OPEN
        )

        if not open_reports.exists():
            return Response({'error': 'There are no open reports for this item to dismiss'}, status=400)

        open_reports.update(status=Report.STATUS_DISMISSED, updated_at=timezone.now())

        _recompute_entity_reports(report.content_type, report.content_id)
        
        return Response({'message': 'All open reports for this item have been dismissed.'})

    @action(detail=False, methods=['get'], url_path='reasons')
    def reasons(self, request):
        """Return all reasons for all content types, including common ones per type."""
        from .reporting import REASON_CODES
        def combine(content_type):
            return REASON_CODES['common'] + REASON_CODES[content_type]
        payload = {
            'version': REASONS_VERSION,
            'reasons': {
                'space': combine('space'),
                'node': combine('node'),
                'discussion': combine('discussion'),
                'profile': combine('profile'),
            }
        }
        return Response(payload)

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

@api_view(['GET'])
@permission_classes([IsAuthenticated])
def dashboard_stats(request):
    """Get dashboard statistics for admin overview"""
    try:
        # Check if user has admin permissions
        profile = Profile.objects.get(user=request.user)
        if not (profile.is_admin() or profile.is_moderator()):
            return Response({'error': 'Permission denied'}, status=status.HTTP_403_FORBIDDEN)
        
        # Get total counts (excluding archived items)
        total_users = User.objects.filter(profile__is_archived=False).count()
        total_spaces = Space.objects.filter(is_archived=False).count()
        total_graph_nodes = Node.objects.filter(is_archived=False).count()
        
        # Simplified active discussions count - just count all discussions for now
        total_discussions = Discussion.objects.count()
        
        # For now, use total discussions as active discussions
        active_discussions = total_discussions
        
        # Get additional useful stats (excluding archived items)
        total_edges = Edge.objects.filter(
            source__is_archived=False,
            target__is_archived=False
        ).count()
        
        return Response({
            'totalUsers': total_users,
            'totalSpaces': total_spaces,
            'totalGraphNodes': total_graph_nodes,
            'activeDiscussions': active_discussions,
            'totalEdges': total_edges,
            'totalDiscussions': total_discussions
        })
        
    except Profile.DoesNotExist:
        return Response({'error': 'Profile not found'}, status=status.HTTP_404_NOT_FOUND)
    except Exception as e:
        print(f"Dashboard stats error: {str(e)}")  # Add logging
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class ActivityStreamView(APIView):
    """
    Serve a unified ActivityStreams OrderedCollectionPage built from stored Activity rows.
    """
    permission_classes = [AllowAny]
    serializer_class = ActivityStreamSerializer
    DEFAULT_LIMIT = 25
    MAX_LIMIT = 100

    def get(self, request):
        queryset = Activity.objects.all().order_by('-published')
        queryset = self._apply_filters(request, queryset)

        limit = self._get_limit(request)
        page = self._get_page(request)

        total_items = queryset.count()
        start = (page - 1) * limit
        end = start + limit
        activities = queryset[start:end]

        serializer = self.serializer_class(activities, many=True, context={'request': request})

        collection_url = self._build_collection_url(request)
        page_url = self._build_page_url(request, page)

        data = {
            '@context': 'https://www.w3.org/ns/activitystreams',
            'id': page_url,
            'type': 'OrderedCollectionPage',
            'partOf': collection_url,
            'totalItems': total_items,
            'orderedItems': serializer.data,
        }

        if start > 0:
            data['prev'] = self._build_page_url(request, page - 1)
        if end < total_items:
            data['next'] = self._build_page_url(request, page + 1)

        return Response(data)

    def _apply_filters(self, request, queryset):
        activity_type = request.query_params.get('type')
        actor = request.query_params.get('actor')
        obj = request.query_params.get('object')
        since = request.query_params.get('since')
        until = request.query_params.get('until')

        if activity_type:
            queryset = queryset.filter(type__iexact=activity_type)
        if actor:
            queryset = queryset.filter(actor__iexact=actor)
        if obj:
            queryset = queryset.filter(object__iexact=obj)
        if since:
            queryset = queryset.filter(published__gte=self._parse_datetime(since, 'since'))
        if until:
            queryset = queryset.filter(published__lte=self._parse_datetime(until, 'until'))

        return queryset

    def _parse_datetime(self, value, field_name):
        parsed = parse_datetime(value)
        if not parsed:
            raise ValidationError({field_name: 'Invalid ISO 8601 datetime value'})
        if timezone.is_naive(parsed):
            parsed = timezone.make_aware(parsed, timezone.get_current_timezone())
        return parsed

    def _get_limit(self, request):
        raw_limit = request.query_params.get('limit', self.DEFAULT_LIMIT)
        try:
            limit = int(raw_limit)
        except (TypeError, ValueError):
            raise ValidationError({'limit': 'limit must be an integer'})
        if limit < 1 or limit > self.MAX_LIMIT:
            raise ValidationError({'limit': f'limit must be between 1 and {self.MAX_LIMIT}'})
        return limit

    def _get_page(self, request):
        raw_page = request.query_params.get('page', 1)
        try:
            page = int(raw_page)
        except (TypeError, ValueError):
            raise ValidationError({'page': 'page must be an integer'})
        if page < 1:
            raise ValidationError({'page': 'page must be at least 1'})
        return page

    def _build_collection_url(self, request):
        base_url = request.build_absolute_uri(reverse('activity_stream'))
        params = request.query_params.copy()
        params.pop('page', None)
        if params:
            return f"{base_url}?{params.urlencode()}"
        return base_url

    def _build_page_url(self, request, page_number):
        base_url = request.build_absolute_uri(reverse('activity_stream'))
        params = request.query_params.copy()
        params['page'] = page_number
        encoded = params.urlencode()
        return f"{base_url}?{encoded}" if encoded else base_url

@api_view(['POST'])
@permission_classes([IsAuthenticated, IsAdminOrModerator])
def archive_item(request):
    """Archive a Space, Node, or Profile. Admins can archive anything, moderators can archive items in their spaces."""
    try:
        user = request.user
        content_type = request.data.get('content_type')
        content_id = request.data.get('content_id')
        reason = request.data.get('reason', '')
        
        if not content_type or not content_id:
            return Response({'error': 'content_type and content_id are required'}, status=status.HTTP_400_BAD_REQUEST)
        
        if content_type not in [Archive.CONTENT_SPACE, Archive.CONTENT_NODE, Archive.CONTENT_PROFILE]:
            return Response({'error': 'Invalid content_type'}, status=status.HTTP_400_BAD_REQUEST)
        
        if not (user.is_staff or user.is_superuser or user.profile.is_admin()):
            moderated_space_ids = list(SpaceModerator.objects.filter(user=user).values_list('space_id', flat=True))
            is_moderator = user.profile.is_moderator() or len(moderated_space_ids) > 0
            
            if not is_moderator:
                return Response({'error': 'Insufficient permissions'}, status=403)
            if content_type == Archive.CONTENT_PROFILE:
                return Response({'error': 'Profile archives can only be managed by admins'}, status=403)
            
            if content_type == Archive.CONTENT_SPACE:
                try:
                    space = Space.objects.get(id=content_id)
                    if space.id not in moderated_space_ids:
                        return Response({'error': 'You are not a moderator of this space'}, status=403)
                except Space.DoesNotExist:
                    return Response({'error': 'Space not found'}, status=status.HTTP_404_NOT_FOUND)
            
            elif content_type == Archive.CONTENT_NODE:
                try:
                    node = Node.objects.get(id=content_id)
                    if node.space_id not in moderated_space_ids:
                        return Response({'error': 'You are not a moderator of this space'}, status=403)
                except Node.DoesNotExist:
                    return Response({'error': 'Node not found'}, status=status.HTTP_404_NOT_FOUND)
        
        Report.objects.filter(
            content_type=content_type,
            content_id=content_id,
            status=Report.STATUS_OPEN
        ).update(status=Report.STATUS_ARCHIVED, updated_at=timezone.now())

        _recompute_entity_reports(content_type, content_id)
        
        if content_type == Archive.CONTENT_SPACE:
            try:
                space = Space.objects.get(id=content_id)
                if space.is_archived:
                    return Response({'error': 'Space is already archived'}, status=status.HTTP_400_BAD_REQUEST)
                space.is_archived = True
                space.save(update_fields=['is_archived'])
            except Space.DoesNotExist:
                return Response({'error': 'Space not found'}, status=status.HTTP_404_NOT_FOUND)
                
        elif content_type == Archive.CONTENT_NODE:
            try:
                node = Node.objects.get(id=content_id)
                if node.is_archived:
                    return Response({'error': 'Node is already archived'}, status=status.HTTP_400_BAD_REQUEST)
                node.is_archived = True
                node.save(update_fields=['is_archived'])
            except Node.DoesNotExist:
                return Response({'error': 'Node not found'}, status=status.HTTP_404_NOT_FOUND)
                
        elif content_type == Archive.CONTENT_PROFILE:
            try:
                profile = Profile.objects.get(user__id=content_id)
                if profile.is_archived:
                    return Response({'error': 'Profile is already archived'}, status=status.HTTP_400_BAD_REQUEST)
                profile.is_archived = True
                profile.save(update_fields=['is_archived'])
            except Profile.DoesNotExist:
                return Response({'error': 'Profile not found'}, status=status.HTTP_404_NOT_FOUND)
        
        archive = Archive.objects.create(
            content_type=content_type,
            content_id=content_id,
            archived_by=request.user,
            reason=reason
        )
        
        serializer = ArchiveSerializer(archive)
        return Response(serializer.data, status=status.HTTP_201_CREATED)
        
    except Exception as e:
        print(f"Archive error: {str(e)}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['GET'])
@permission_classes([IsAuthenticated, IsAdminOrModerator])
def list_archived_items(request):
    """List archived items. Admins see all, moderators see only items from their spaces."""
    try:
        user = request.user
        
        if user.is_staff or user.is_superuser or user.profile.is_admin():
            archives = Archive.objects.all().order_by('-archived_at')
        else:
            moderated_space_ids = list(SpaceModerator.objects.filter(user=user).values_list('space_id', flat=True))
            if user.profile.is_moderator() or len(moderated_space_ids) > 0:
                space_archives = Archive.objects.filter(
                    content_type=Archive.CONTENT_SPACE,
                    content_id__in=moderated_space_ids
                )
                
                node_ids_in_moderated_spaces = Node.objects.filter(
                    space_id__in=moderated_space_ids
                ).values_list('id', flat=True)
                node_archives = Archive.objects.filter(
                    content_type=Archive.CONTENT_NODE,
                    content_id__in=list(node_ids_in_moderated_spaces)
                )
                
                archives = (space_archives | node_archives).order_by('-archived_at')
            else:
                archives = Archive.objects.none()
        
        serializer = ArchiveSerializer(archives, many=True)
        return Response(serializer.data)
        
    except Exception as e:
        print(f"List archives error: {str(e)}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@permission_classes([IsAuthenticated, IsAdminOrModerator])
def restore_archived_item(request, archive_id):
    """Restore an archived item. Admins can restore anything, moderators can restore items from their spaces."""
    try:
        user = request.user
        archive = Archive.objects.get(id=archive_id)
        
        if not (user.is_staff or user.is_superuser or user.profile.is_admin()):
            moderated_space_ids = list(SpaceModerator.objects.filter(user=user).values_list('space_id', flat=True))
            is_moderator = user.profile.is_moderator() or len(moderated_space_ids) > 0
            
            if not is_moderator:
                return Response({'error': 'Insufficient permissions'}, status=403)
            if archive.content_type == Archive.CONTENT_PROFILE:
                return Response({'error': 'Profile archives can only be restored by admins'}, status=403)
            
            if archive.content_type == Archive.CONTENT_SPACE:
                try:
                    space = Space.objects.get(id=archive.content_id)
                    if space.id not in moderated_space_ids:
                        return Response({'error': 'You are not a moderator of this space'}, status=403)
                except Space.DoesNotExist:
                    return Response({'error': 'Space not found'}, status=status.HTTP_404_NOT_FOUND)
            
            elif archive.content_type == Archive.CONTENT_NODE:
                try:
                    node = Node.objects.get(id=archive.content_id)
                    if node.space_id not in moderated_space_ids:
                        return Response({'error': 'You are not a moderator of this space'}, status=403)
                except Node.DoesNotExist:
                    return Response({'error': 'Node not found'}, status=status.HTTP_404_NOT_FOUND)
        
        if archive.content_type == Archive.CONTENT_SPACE:
            try:
                space = Space.objects.get(id=archive.content_id)
                space.is_archived = False
                space.save(update_fields=['is_archived'])
            except Space.DoesNotExist:
                return Response({'error': 'Space not found'}, status=status.HTTP_404_NOT_FOUND)
                
        elif archive.content_type == Archive.CONTENT_NODE:
            try:
                node = Node.objects.get(id=archive.content_id)
                node.is_archived = False
                node.save(update_fields=['is_archived'])
            except Node.DoesNotExist:
                return Response({'error': 'Node not found'}, status=status.HTTP_404_NOT_FOUND)
                
        elif archive.content_type == Archive.CONTENT_PROFILE:
            try:
                profile = Profile.objects.get(user__id=archive.content_id)
                profile.is_archived = False
                profile.save(update_fields=['is_archived'])
            except Profile.DoesNotExist:
                return Response({'error': 'Profile not found'}, status=status.HTTP_404_NOT_FOUND)
        
        archive.delete()
        
        return Response({'message': 'Item restored successfully'}, status=status.HTTP_200_OK)
        
    except Archive.DoesNotExist:
        return Response({'error': 'Archive not found'}, status=status.HTTP_404_NOT_FOUND)
    except Exception as e:
        print(f"Restore error: {str(e)}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

