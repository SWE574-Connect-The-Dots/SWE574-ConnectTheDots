from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone
from django.db.models.signals import post_save
from django.dispatch import receiver

class Profile(models.Model):
    # User types
    ADMIN = 1
    MODERATOR = 2
    USER = 3
    
    USER_TYPE_CHOICES = [
        (ADMIN, 'Admin'),
        (MODERATOR, 'Moderator'),
        (USER, 'User'),
    ]
    
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    user_type = models.IntegerField(choices=USER_TYPE_CHOICES, default=USER)
    profession = models.CharField(max_length=100, blank=True, null=True)
    bio = models.TextField(max_length=500, blank=True, null=True)
    dob = models.DateField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.user.username}'s profile"
    
    def is_admin(self):
        """Check if user is an admin"""
        return self.user_type == self.ADMIN
    
    def is_moderator(self):
        """Check if user is a moderator"""
        return self.user_type == self.MODERATOR
    
    def is_regular_user(self):
        """Check if user is a regular user"""
        return self.user_type == self.USER
    
    def can_moderate_space(self, space):
        """Check if user can moderate a specific space"""
        if self.is_admin():
            return True
        if self.is_moderator():
            return SpaceModerator.objects.filter(user=self.user, space=space).exists()
        return False

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
    
    def get_moderators(self):
        """Get all moderators for this space"""
        return User.objects.filter(spacemoderator__space=self)
    
    def is_moderator(self, user):
        """Check if a user is a moderator of this space"""
        return SpaceModerator.objects.filter(user=user, space=self).exists()
    
    class Meta:
        ordering = ['-created_at']

class SpaceModerator(models.Model):
    """Model to assign moderators to specific spaces"""
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    space = models.ForeignKey(Space, on_delete=models.CASCADE)
    assigned_by = models.ForeignKey(User, on_delete=models.CASCADE, related_name='assigned_moderators')
    assigned_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        unique_together = ('user', 'space')
        
    def __str__(self):
        return f"{self.user.username} moderates {self.space.title}"
        
class Property(models.Model):
    node = models.ForeignKey('Node', on_delete=models.CASCADE, related_name='node_properties')
    property_id = models.CharField(max_length=255)
    statement_id = models.CharField(max_length=255, unique=True, null=True, default=None)
    
class Node(models.Model):
    label = models.CharField(max_length=255)
    wikidata_id = models.CharField(max_length=50, blank=True, null=True)
    created_by = models.ForeignKey(User, on_delete=models.CASCADE)
    space = models.ForeignKey(Space, on_delete=models.CASCADE)

class Edge(models.Model):
    source = models.ForeignKey(Node, related_name='source_edges', on_delete=models.CASCADE)
    target = models.ForeignKey(Node, related_name='target_edges', on_delete=models.CASCADE)
    relation_property = models.CharField(max_length=255)
    wikidata_property_id = models.CharField(max_length=50, blank=True, null=True)
    
class GraphSnapshot(models.Model):
    space_id = models.IntegerField()
    created_at = models.DateTimeField(default=timezone.now)
    created_by = models.ForeignKey(User, on_delete=models.SET_NULL, null=True)
    snapshot_data = models.JSONField()

    def __str__(self):
        return f"Snapshot {self.id} at {self.created_at}"

class Discussion(models.Model):
    space = models.ForeignKey(Space, on_delete=models.CASCADE, related_name='discussions')
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    text = models.TextField()
    created_at = models.DateTimeField(default=timezone.now)
    
    def __str__(self):
        return f"Comment by {self.user.username} in {self.space.title}"
    
    class Meta:
        ordering = ['-created_at']

class DiscussionReaction(models.Model):
    UPVOTE = 1
    DOWNVOTE = -1
    REACTION_CHOICES = (
        (UPVOTE, 'upvote'),
        (DOWNVOTE, 'downvote'),
    )

    discussion = models.ForeignKey(Discussion, on_delete=models.CASCADE, related_name='reactions')
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    value = models.SmallIntegerField(choices=REACTION_CHOICES)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        unique_together = ('discussion', 'user')

    def __str__(self):
        label = '👍' if self.value == self.UPVOTE else '👎'
        return f"{self.user.username} {label} discussion {self.discussion_id}"

