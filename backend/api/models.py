from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone
from django.db.models.signals import post_save
from django.dispatch import receiver

class Profile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    profession = models.CharField(max_length=100, blank=True, null=True)
    bio = models.TextField(max_length=500, blank=True, null=True)
    dob = models.DateField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.user.username}'s profile"

@receiver(post_save, sender=User)
def create_user_profile(sender, instance, created, **kwargs):
    if created:
        Profile.objects.create(user=instance)

@receiver(post_save, sender=User)
def save_user_profile(sender, instance, **kwargs):
    instance.profile.save()

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
        
class Property(models.Model):
    node = models.ForeignKey('Node', on_delete=models.CASCADE, related_name='node_properties')
    property_id = models.CharField(max_length=255)
    
class Node(models.Model):
    label = models.CharField(max_length=255)
    wikidata_id = models.CharField(max_length=50, blank=True, null=True)
    created_by = models.ForeignKey(User, on_delete=models.CASCADE)
    space = models.ForeignKey(Space, on_delete=models.CASCADE)

class Edge(models.Model):
    source = models.ForeignKey(Node, related_name='source_edges', on_delete=models.CASCADE)
    target = models.ForeignKey(Node, related_name='target_edges', on_delete=models.CASCADE)
    relation_property = models.CharField(max_length=255)
    
class GraphSnapshot(models.Model):
    space_id = models.IntegerField()
    created_at = models.DateTimeField(default=timezone.now)
    created_by = models.ForeignKey(User, on_delete=models.SET_NULL, null=True)
    snapshot_data = models.JSONField()

    def __str__(self):
        return f"Snapshot {self.id} at {self.created_at}"

