from django.contrib.auth.models import User
from .models import Profile, Space, Tag, Discussion, DiscussionReaction
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
                 'joined_spaces', 'owned_spaces', 'moderated_spaces']

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
            'tags', 'tag_ids', 'collaborators'
        ]
        read_only_fields = ['creator_username', 'created_at']
    
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

class DiscussionSerializer(serializers.ModelSerializer):
    username = serializers.ReadOnlyField(source='user.username')
    upvotes = serializers.SerializerMethodField()
    downvotes = serializers.SerializerMethodField()
    user_reaction = serializers.SerializerMethodField()

    class Meta:
        model = Discussion
        fields = ['id', 'text', 'created_at', 'username', 'upvotes', 'downvotes', 'user_reaction']
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
