from django.contrib.auth.models import User
from .models import Profile, Space, Tag
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
        Profile.objects.create(user=user, profession=profession, dob=dob)
        return user
    
class TagSerializer(serializers.ModelSerializer):
    class Meta:
        model = Tag
        fields = ['id', 'name']

class SpaceSerializer(serializers.ModelSerializer):
    creator_username = serializers.ReadOnlyField(source='creator.username')
    tags = TagSerializer(many=True, read_only=True)
    tag_ids = serializers.PrimaryKeyRelatedField(
        queryset=Tag.objects.all(),
        many=True, 
        write_only=True,
        required=False
    )
    
    class Meta:
        model = Space
        fields = ['id', 'title', 'description', 'created_at', 'creator_username', 'tags', 'tag_ids']
        read_only_fields = ['creator_username', 'created_at']
    
    def create(self, validated_data):
        tag_ids = validated_data.pop('tag_ids', [])
        space = Space.objects.create(**validated_data)
        
        space.collaborators.add(validated_data['creator'])
        
        for tag in tag_ids:
            space.tags.add(tag)
            
        return space