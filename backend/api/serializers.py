from django.contrib.auth.models import User
from .models import Profile
from datetime import date
from rest_framework import serializers

class RegisterSerializer(serializers.ModelSerializer):
    # Add your custom field here, example: profession
    profession = serializers.CharField(write_only=True, required=True)
    dob = serializers.DateField(write_only=True, required=True)
    
    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'password', 'profession', 'dob']
        extra_kwargs = {
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
