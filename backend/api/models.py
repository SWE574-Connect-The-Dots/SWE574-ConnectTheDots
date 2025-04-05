from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone

class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    profession = models.CharField(max_length=100)
    dob = models.DateField(null=True, blank=True)

    def __str__(self):
        return f"{self.user.username} - {self.profession} - {self.dob}"

class Tag(models.Model):
    name = models.CharField(max_length=50, unique=True)
    wikidata_id = models.CharField(max_length=20, blank=True, null=True)
    wikidata_label = models.CharField(max_length=100, blank=True, null=True)
    
    def __str__(self):
        return self.name

class Space(models.Model):
    title = models.CharField(max_length=200)
    description = models.TextField()
    created_at = models.DateTimeField(default=timezone.now)
    creator = models.ForeignKey(User, on_delete=models.CASCADE, related_name='created_spaces')
    collaborators = models.ManyToManyField(User, related_name='joined_spaces', blank=True)
    tags = models.ManyToManyField(Tag, blank=True)
    
    def __str__(self):
        return self.title
    
    class Meta:
        ordering = ['-created_at']
