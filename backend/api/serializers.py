from django.contrib.auth.models import User
from .models import Profile, Space, Tag, Discussion, DiscussionReaction
from datetime import date
from rest_framework import serializers

class RegisterSerializer(serializers.ModelSerializer):
    profession = serializers.CharField(write_only=True, required=True)
    dob = serializers.DateField(write_only=True, required=True)
    
    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'password', 'profession', 'dob']
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
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            password=validated_data['password']
        )
        # Update the profile that was automatically created by the signal
        user.profile.profession = profession
        user.profile.dob = dob
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
    joined_spaces = serializers.SerializerMethodField()
    owned_spaces = serializers.SerializerMethodField()

    class Meta:
        model = Profile
        fields = ['user', 'profession', 'bio', 'dob', 'created_at', 'updated_at', 'joined_spaces', 'owned_spaces']

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
    
    class Meta:
        model = Space
        fields = ['id', 'title', 'description', 'created_at', 'creator_username', 'tags', 'tag_ids', 'collaborators']
        read_only_fields = ['creator_username', 'created_at']
    
    def get_collaborators(self, obj):
        return [user.username for user in obj.collaborators.all()]
    
    def create(self, validated_data):
        tag_ids = validated_data.pop('tag_ids', [])
        space = Space.objects.create(**validated_data)
        
        space.collaborators.add(validated_data['creator'])
        
        for tag in tag_ids:
            space.tags.add(tag)
            
        return space

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
