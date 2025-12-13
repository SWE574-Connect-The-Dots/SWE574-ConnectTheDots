from django.contrib.auth.models import User
from .models import Profile, Space, Tag, Discussion, DiscussionReaction, Node, Report, Activity, Archive
from .reporting import ALLOWED_REASON_CODES
from datetime import date
from rest_framework import serializers

class RegisterSerializer(serializers.ModelSerializer):
    profession = serializers.CharField(write_only=True, required=True)
    dob = serializers.DateField(write_only=True, required=True)
    latitude = serializers.FloatField(write_only=True, required=False, allow_null=True)
    longitude = serializers.FloatField(write_only=True, required=False, allow_null=True)
    location_name = serializers.CharField(write_only=True, required=False, allow_blank=True, allow_null=True)
    
    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'password', 'profession', 'dob', 'latitude', 'longitude', 'location_name']
        extra_kwargs = {
            'username': {'required': True},
            'password': {'write_only': True},
            'email': {'required': True},
        }
        
    
    def validate_dob(self, value):
        """
        Custom validation to check if the user is at least 18 years old.
        """
        today = date.today()
        age = today.year - value.year - ((today.month, today.day) < (value.month, value.day))

        if age < 18:
            raise serializers.ValidationError("You must be at least 18 years old to register.")

        return value

    def create(self, validated_data):
        profession = validated_data.pop('profession', '')
        dob = validated_data.pop('dob', None)
        latitude = validated_data.pop('latitude', None)
        longitude = validated_data.pop('longitude', None)
        location_name = validated_data.pop('location_name', None)

        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            password=validated_data['password']
        )
        # Update the profile that was automatically created by the signal
        user.profile.profession = profession
        user.profile.dob = dob
        user.profile.latitude = latitude
        user.profile.longitude = longitude
        user.profile.location_name = location_name
        user.profile.save()
        return user
    
class UserSerializer(serializers.ModelSerializer):
    is_staff = serializers.BooleanField(read_only=True)
    is_superuser = serializers.BooleanField(read_only=True)
    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'first_name', 'last_name', 'is_staff', 'is_superuser']

class ProfileSerializer(serializers.ModelSerializer):
    user = UserSerializer(read_only=True)
    user_type_display = serializers.CharField(source='get_user_type_display', read_only=True)
    joined_spaces = serializers.SerializerMethodField()
    owned_spaces = serializers.SerializerMethodField()
    moderated_spaces = serializers.SerializerMethodField()
    can_access_admin_dashboard = serializers.SerializerMethodField()

    def geocode_location(self, country, city):
        """Convert location text to coordinates using Nominatim API"""
        import requests
        import time
        
        # Build address string
        address_parts = []
        if city:
            address_parts.append(city)
        if country:
            address_parts.append(country)
            
        if not address_parts:
            return None, None
            
        address = ", ".join(address_parts)
        
        try:
            # Use Nominatim API for geocoding
            url = "https://nominatim.openstreetmap.org/search"
            params = {
                'q': address,
                'format': 'json',
                'limit': 1
            }
            headers = {
                'User-Agent': 'ConnectTheDots/1.0'
            }
            
            response = requests.get(url, params=params, headers=headers, timeout=10)
            time.sleep(1)  # Be respectful to the API
            
            if response.status_code == 200:
                data = response.json()
                if data:
                    return float(data[0]['lat']), float(data[0]['lon'])
        except Exception as e:
            print(f"Profile geocoding error: {e}")
            
        return None, None

    class Meta:
        model = Profile
        fields = ['user', 'user_type', 'user_type_display', 'profession', 'bio', 'dob', 
                 'created_at', 'updated_at', 'country', 'city', 'latitude', 'longitude', 'location_name', 
                 'joined_spaces', 'owned_spaces', 'moderated_spaces', 'can_access_admin_dashboard']

    def get_joined_spaces(self, obj):
        joined_spaces = Space.objects.filter(collaborators=obj.user)
        return [{
            'id': space.id,
            'title': space.title,
            'description': space.description
        } for space in joined_spaces]
        
    def get_owned_spaces(self, obj):
        owned_spaces = Space.objects.filter(creator=obj.user)
        return [{
            'id': space.id,
            'title': space.title,
            'description': space.description
        } for space in owned_spaces]
    
    def get_moderated_spaces(self, obj):
        from .models import SpaceModerator
        moderated_spaces = SpaceModerator.objects.filter(user=obj.user).select_related('space')
        return [{
            'id': moderator.space.id,
            'title': moderator.space.title,
            'description': moderator.space.description,
            'assigned_at': moderator.assigned_at
        } for moderator in moderated_spaces]
    
    def get_can_access_admin_dashboard(self, obj):
        """
        Check if user can access admin dashboard.
        Returns True if:
        - User is admin
        - User is moderator AND has at least one moderated space
        - User has created at least one space
        """
        # Admin always has access
        if obj.is_admin():
            return True
        
        # Moderator has access if they moderate at least one space
        if obj.is_moderator():
            from .models import SpaceModerator
            has_moderated_spaces = SpaceModerator.objects.filter(user=obj.user).exists()
            if has_moderated_spaces:
                return True
        
        # Space creator has access if they created at least one space
        has_owned_spaces = Space.objects.filter(creator=obj.user).exists()
        if has_owned_spaces:
            return True
        
        return False
    
    def update(self, instance, validated_data):
        # Handle different location update scenarios
        location_fields = ['country', 'city']
        location_changed = any(field in validated_data for field in location_fields)
        coordinates_provided = 'latitude' in validated_data and 'longitude' in validated_data
        location_name_provided = 'location_name' in validated_data
        
        # Scenario 1: Country/City changed and coordinates NOT manually provided
        if location_changed and not coordinates_provided:
            country = validated_data.get('country', instance.country)
            city = validated_data.get('city', instance.city)
            
            if country or city:  # Geocode if we have location info
                lat, lon = self.geocode_location(country, city)
                if lat and lon:
                    validated_data['latitude'] = lat
                    validated_data['longitude'] = lon
                    # Auto-generate location_name if not manually provided
                    if not location_name_provided:
                        if country and city:
                            validated_data['location_name'] = f"{city}, {country}"
                        elif city:
                            validated_data['location_name'] = city
                        elif country:
                            validated_data['location_name'] = country
            else:
                # Clear coordinates if no location info provided
                validated_data['latitude'] = None
                validated_data['longitude'] = None
                if not location_name_provided:
                    validated_data['location_name'] = None
        
        # Scenario 2: Coordinates manually provided - clear derived location_name unless manually set
        elif coordinates_provided and not location_name_provided:
            # Keep the coordinates but update location_name based on country/city if available
            country = validated_data.get('country', instance.country)  
            city = validated_data.get('city', instance.city)
            if country and city:
                validated_data['location_name'] = f"{city}, {country}"
            elif city:
                validated_data['location_name'] = city
            elif country:
                validated_data['location_name'] = country
        
        # Scenario 3: Only location_name provided - keep existing coordinates and country/city
        # (This is handled automatically by the parent update method)
        
        return super().update(instance, validated_data)
    
class TagSerializer(serializers.ModelSerializer):
    class Meta:
        model = Tag
        fields = ['id', 'name', 'wikidata_id', 'wikidata_label'] 

class SpaceSerializer(serializers.ModelSerializer):
    creator_username = serializers.ReadOnlyField(source='creator.username')
    tags = TagSerializer(many=True, read_only=True)
    tag_ids = serializers.PrimaryKeyRelatedField(
        queryset=Tag.objects.all(),
        many=True, 
        write_only=True,
        required=False
    )
    collaborators = serializers.SerializerMethodField()
    
    def geocode_location(self, country, city, district=None, street=None):
        """Convert location text to coordinates using Nominatim API"""
        import requests
        import time
        
        # Build address string
        address_parts = []
        if street:
            address_parts.append(street)
        if district:
            address_parts.append(district)
        if city:
            address_parts.append(city)
        if country:
            address_parts.append(country)
            
        if not address_parts:
            return None, None
            
        address = ", ".join(address_parts)
        
        try:
            # Use Nominatim API for geocoding
            url = "https://nominatim.openstreetmap.org/search"
            params = {
                'q': address,
                'format': 'json',
                'limit': 1
            }
            headers = {
                'User-Agent': 'ConnectTheDots/1.0'
            }
            
            response = requests.get(url, params=params, headers=headers, timeout=10)
            time.sleep(1)  # Be respectful to the API
            
            if response.status_code == 200:
                data = response.json()
                if data:
                    return float(data[0]['lat']), float(data[0]['lon'])
        except Exception as e:
            print(f"Geocoding error: {e}")
            
        return None, None
    
    class Meta:
        model = Space
        fields = [
            'id', 'title', 'description', 'created_at', 'creator_username',
            'country', 'city', 'district', 'street', 'latitude', 'longitude',
            'tags', 'tag_ids', 'collaborators', 'is_archived'
        ]
        read_only_fields = ['creator_username', 'created_at', 'is_archived']
    
    def get_collaborators(self, obj):
        return [user.username for user in obj.collaborators.all()]
    
    def create(self, validated_data):
        tag_ids = validated_data.pop('tag_ids', [])
        
        # Geocode location if coordinates are not provided
        if not validated_data.get('latitude') or not validated_data.get('longitude'):
            country = validated_data.get('country')
            city = validated_data.get('city')
            district = validated_data.get('district')
            street = validated_data.get('street')
            
            if country or city:  # Only geocode if we have at least country or city
                lat, lon = self.geocode_location(country, city, district, street)
                if lat and lon:
                    validated_data['latitude'] = lat
                    validated_data['longitude'] = lon
        
        space = Space.objects.create(**validated_data)
        
        space.collaborators.add(validated_data['creator'])
        
        for tag in tag_ids:
            space.tags.add(tag)
            
        return space
    
    def update(self, instance, validated_data):
        # If location fields are being updated and coordinates are not provided,
        # try to geocode the new location
        location_fields = ['country', 'city', 'district', 'street']
        location_changed = any(field in validated_data for field in location_fields)
        coordinates_provided = 'latitude' in validated_data and 'longitude' in validated_data
        
        if location_changed and not coordinates_provided:
            country = validated_data.get('country', instance.country)
            city = validated_data.get('city', instance.city)
            district = validated_data.get('district', instance.district)
            street = validated_data.get('street', instance.street)
            
            if country or city:  # Only geocode if we have at least country or city
                lat, lon = self.geocode_location(country, city, district, street)
                if lat and lon:
                    validated_data['latitude'] = lat
                    validated_data['longitude'] = lon
        
        return super().update(instance, validated_data)

class NodeSerializer(serializers.ModelSerializer):
    created_by_username = serializers.ReadOnlyField(source='created_by.username')
    connection_count = serializers.SerializerMethodField()
    instance_type = serializers.SerializerMethodField()
    
    class Meta:
        model = Node
        fields = [
            'id', 'label', 'wikidata_id', 'created_at', 'created_by', 'created_by_username', 
            'space', 'country', 'city', 'district', 'street', 
            'latitude', 'longitude', 'location_name', 'description', 'is_archived',
            'connection_count', 'instance_type'
        ]
    
    def get_connection_count(self, obj):
        """Get total number of connections (incoming + outgoing)"""
        return obj.get_connection_count()
    
    def get_instance_type(self, obj):
        """
        Get the highest priority instance type GROUP for this node.
        
        When a node has multiple P31 values, they're all mapped to groups,
        then the highest priority group is returned.
        
        Returns:
            dict or None: {
                'group_id': 'CITY',
                'group_label': 'City', 
                'specific_types': ['Q515', 'Q1637706']  # For debugging/logging
            } or None
        """
        try:
            GROUPS = {
                'HUMAN': {'types': ['Q5', 'Q215627'], 'priority': 100},
                'COUNTRY': {'types': ['Q6256', 'Q3024240', 'Q859563', 'Q1520223'], 'priority': 90},
                'CITY': {'types': ['Q515', 'Q1549591', 'Q1637706', 'Q5119', 'Q200250', 'Q1093829', 'Q7930989', 'Q2514025'], 'priority': 85},
                'ADMINISTRATIVE': {'types': ['Q15042037', 'Q10864048', 'Q56061', 'Q842112', 'Q192611', 'Q174844', 'Q82794'], 'priority': 80},
                'SETTLEMENT': {'types': ['Q486972', 'Q532', 'Q3957', 'Q5084', 'Q3191695', 'Q15221371'], 'priority': 75},
                'ORGANIZATION': {'types': ['Q43229', 'Q4830453', 'Q783794', 'Q891723', 'Q219577', 'Q7210356', 'Q31855', 'Q2085381', 'Q294163'], 'priority': 70},
                'BUILDING': {'types': ['Q41176', 'Q16560', 'Q44494', 'Q16970', 'Q34627', 'Q44539', 'Q33506', 'Q483110', 'Q5003624'], 'priority': 65},
                'GEOGRAPHIC_FEATURE': {'types': ['Q8502', 'Q23442', 'Q4022', 'Q23397', 'Q39594', 'Q185113', 'Q39816', 'Q54050', 'Q177634'], 'priority': 60},
                'WORK': {'types': ['Q11424', 'Q571', 'Q7725634', 'Q13442814', 'Q732577', 'Q2188189', 'Q386724', 'Q3305213', 'Q860861'], 'priority': 55},
                'SPECIES': {'types': ['Q16521', 'Q7432', 'Q34740', 'Q35409', 'Q36602', 'Q37517'], 'priority': 50},
            }
            
            TYPE_TO_GROUP = {}
            for group_id, group_data in GROUPS.items():
                for type_id in group_data['types']:
                    TYPE_TO_GROUP[type_id] = group_id
            
            p31_props = obj.node_properties.filter(property_id='P31')
            
            if not p31_props.exists():
                return None
            
            matched_groups = {}
            specific_types = []
            
            for prop in p31_props:
                # Get value_id from prop.value_id or extract from prop.value JSONField (for backward compatibility)
                value_id = prop.value_id
                if not value_id and prop.value:
                    # Fallback: extract ID from value JSONField for older properties
                    if isinstance(prop.value, dict):
                        value_id = prop.value.get('id') or prop.value.get('value')
                
                if value_id:
                    specific_types.append(value_id)
                    group_id = TYPE_TO_GROUP.get(value_id)
                    if group_id and group_id not in matched_groups:
                        matched_groups[group_id] = GROUPS[group_id]['priority']
            
            if not matched_groups:
                # Log for future addition
                import logging
                logger = logging.getLogger(__name__)
                logger.info(f"Unmapped instance types for node {obj.id}: {specific_types}")
                return None
            
            best_group_id = max(matched_groups.keys(), key=lambda g: matched_groups[g])
            
            return {
                'group_id': best_group_id,
                'group_label': best_group_id.replace('_', ' ').title(),
                'specific_types': specific_types 
            }
                
        except Exception as e:
            import logging
            logger = logging.getLogger(__name__)
            logger.error(f"Error getting instance type for node {obj.id}: {e}")
            pass
        
        return None

class DiscussionSerializer(serializers.ModelSerializer):
    username = serializers.ReadOnlyField(source='user.username')
    upvotes = serializers.SerializerMethodField()
    downvotes = serializers.SerializerMethodField()
    user_reaction = serializers.SerializerMethodField()

    class Meta:
        model = Discussion
        fields = ['id', 'space', 'text', 'created_at', 'username', 'upvotes', 'downvotes', 'user_reaction']
        read_only_fields = ['created_at', 'username', 'upvotes', 'downvotes', 'user_reaction']

    def get_upvotes(self, obj):
        return obj.reactions.filter(value=DiscussionReaction.UPVOTE).count()

    def get_downvotes(self, obj):
        return obj.reactions.filter(value=DiscussionReaction.DOWNVOTE).count()

    def get_user_reaction(self, obj):
        request = self.context.get('request')
        if not request or not getattr(request, 'user', None) or request.user.is_anonymous:
            return None
        reaction = obj.reactions.filter(user=request.user).first()
        if not reaction:
            return None
        return 'up' if reaction.value == DiscussionReaction.UPVOTE else 'down'


class ReportSerializer(serializers.ModelSerializer):
    reporter_username = serializers.ReadOnlyField(source='reporter.username')
    entity_report_count = serializers.SerializerMethodField()
    entity_is_reported = serializers.SerializerMethodField()
    content_object_label = serializers.SerializerMethodField()

    class Meta:
        model = Report
        fields = [
            'id', 'content_type', 'content_id', 'reason', 'status',
            'space', 'reporter', 'reporter_username', 'created_at', 'updated_at',
            'entity_report_count', 'entity_is_reported', 'content_object_label'
        ]
        read_only_fields = ['status', 'space', 'reporter', 'reporter_username', 'created_at', 'updated_at']

    def get_content_object_label(self, obj):
        """Get the name/title of the reported item"""
        if obj.content_type == Report.CONTENT_SPACE:
            try:
                return Space.objects.get(id=obj.content_id).title
            except Space.DoesNotExist:
                return f"Space #{obj.content_id} (deleted)"
        elif obj.content_type == Report.CONTENT_NODE:
            try:
                return Node.objects.get(id=obj.content_id).label
            except Node.DoesNotExist:
                return f"Node #{obj.content_id} (deleted)"
        elif obj.content_type == Report.CONTENT_PROFILE:
            try:
                return Profile.objects.get(user__id=obj.content_id).user.username
            except Profile.DoesNotExist:
                return f"User #{obj.content_id} (deleted)"
        elif obj.content_type == Report.CONTENT_DISCUSSION:
            try:
                text = Discussion.objects.get(id=obj.content_id).text
                return (text[:47] + '...') if len(text) > 50 else text
            except Discussion.DoesNotExist:
                return f"Discussion #{obj.content_id} (deleted)"
        return "Unknown"

    def validate(self, attrs):
        content_type = attrs.get('content_type')
        content_id = attrs.get('content_id')
        reason = attrs.get('reason')

        if content_type not in ALLOWED_REASON_CODES:
            raise serializers.ValidationError({'content_type': 'Invalid content_type'})

        if reason not in ALLOWED_REASON_CODES[content_type]:
            raise serializers.ValidationError({'reason': 'Invalid reason for content_type'})

        space_obj = None
        if content_type == Report.CONTENT_SPACE:
            try:
                space_obj = Space.objects.get(id=content_id)
            except Space.DoesNotExist:
                raise serializers.ValidationError({'content_id': 'Space not found'})
        elif content_type == Report.CONTENT_NODE:
            try:
                node = Node.objects.get(id=content_id)
                space_obj = node.space
            except Node.DoesNotExist:
                raise serializers.ValidationError({'content_id': 'Node not found'})
        elif content_type == Report.CONTENT_DISCUSSION:
            try:
                discussion = Discussion.objects.get(id=content_id)
                space_obj = discussion.space
                attrs['_discussion_user_id'] = discussion.user_id
            except Discussion.DoesNotExist:
                raise serializers.ValidationError({'content_id': 'Discussion not found'})
        elif content_type == Report.CONTENT_PROFILE:
            try:
                Profile.objects.get(user__id=content_id)
            except Profile.DoesNotExist:
                raise serializers.ValidationError({'content_id': 'Profile not found'})

        attrs['_resolved_space'] = space_obj
        return attrs

    def create(self, validated_data):
        request = self.context.get('request')
        user = request.user if request and request.user and not request.user.is_anonymous else None
        if not user:
            raise serializers.ValidationError('Authentication required')

        space_obj = validated_data.pop('_resolved_space', None)
        discussion_owner_id = validated_data.pop('_discussion_user_id', None)
        if discussion_owner_id and int(discussion_owner_id) == int(user.id):
            raise serializers.ValidationError({'content_id': 'You cannot report your own discussion'})
        report = Report.objects.create(
            reporter=user,
            space=space_obj,
            **validated_data
        )
        return report

    def _get_target_entity(self, obj):
        if obj.content_type == Report.CONTENT_SPACE:
            return Space.objects.filter(id=obj.content_id).first()
        if obj.content_type == Report.CONTENT_NODE:
            return Node.objects.filter(id=obj.content_id).first()
        if obj.content_type == Report.CONTENT_DISCUSSION:
            return Discussion.objects.filter(id=obj.content_id).first()
        if obj.content_type == Report.CONTENT_PROFILE:
            return Profile.objects.filter(user__id=obj.content_id).first()
        return None

    def get_entity_report_count(self, obj):
        target = self._get_target_entity(obj)
        return getattr(target, 'report_count', None) if target else None

    def get_entity_is_reported(self, obj):
        target = self._get_target_entity(obj)
        return getattr(target, 'is_reported', None) if target else None
    
class ActivityStreamSerializer(serializers.ModelSerializer):
    """
    Serialize Activity rows into ActivityStreams 2.0 compatible entries.
    """
    id = serializers.SerializerMethodField()
    actor = serializers.SerializerMethodField()
    object = serializers.SerializerMethodField()
    target = serializers.SerializerMethodField()

    class Meta:
        model = Activity
        fields = [
            'id',
            'type',
            'actor',
            'object',
            'target',
            'summary',
            'published',
            'to',
            'cc',
            'payload',
        ]

    def get_id(self, obj):
        if obj.as2_id:
            return obj.as2_id
        return f"urn:activity:{obj.pk}"

    def _format_reference(self, ref):
        if not ref:
            return None
        if ref.startswith('http://') or ref.startswith('https://'):
            return ref
        if ':' in ref:
            entity_type, identifier = ref.split(':', 1)
            return {
                'type': entity_type,
                'id': identifier,
                'name': ref,
            }
        return {'type': 'Object', 'name': ref}

    def get_actor(self, obj):
        actor = obj.actor or ''
        if actor.startswith('http://') or actor.startswith('https://'):
            return actor
        actor_type = 'Application' if actor.lower() == 'system' else 'Person'
        return {
            'type': actor_type,
            'name': actor or 'system',
        }

    def get_object(self, obj):
        return self._format_reference(obj.object)

    def get_target(self, obj):
        return self._format_reference(obj.target)


class ArchiveSerializer(serializers.ModelSerializer):
    archived_by_username = serializers.ReadOnlyField(source='archived_by.username')
    name = serializers.SerializerMethodField()
    
    class Meta:
        model = Archive
        fields = ['id', 'content_type', 'content_id', 'archived_by', 'archived_by_username', 'archived_at', 'reason', 'name']
        read_only_fields = ['archived_by', 'archived_by_username', 'archived_at']
    
    def get_name(self, obj):
        """Get the name/title of the archived item"""
        if obj.content_type == Archive.CONTENT_SPACE:
            try:
                space = Space.objects.get(id=obj.content_id)
                return space.title
            except Space.DoesNotExist:
                return f"Space #{obj.content_id} (deleted)"
        elif obj.content_type == Archive.CONTENT_NODE:
            try:
                node = Node.objects.get(id=obj.content_id)
                return node.label
            except Node.DoesNotExist:
                return f"Node #{obj.content_id} (deleted)"
        elif obj.content_type == Archive.CONTENT_PROFILE:
            try:
                profile = Profile.objects.get(user__id=obj.content_id)
                return profile.user.username
            except Profile.DoesNotExist:
                return f"User #{obj.content_id} (deleted)"
        return "Unknown"
