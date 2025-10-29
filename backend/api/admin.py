from django.contrib import admin
from .models import Profile, Space, Tag, SpaceModerator, Node, Edge, GraphSnapshot, Discussion, DiscussionReaction, Property

@admin.register(Profile)
class ProfileAdmin(admin.ModelAdmin):
    list_display = ['user', 'user_type', 'profession', 'created_at']
    list_filter = ['user_type', 'created_at']
    search_fields = ['user__username', 'user__email', 'profession']

@admin.register(SpaceModerator)
class SpaceModeratorAdmin(admin.ModelAdmin):
    list_display = ['user', 'space', 'assigned_by', 'assigned_at']
    list_filter = ['assigned_at', 'space']
    search_fields = ['user__username', 'space__title', 'assigned_by__username']

@admin.register(Space)
class SpaceAdmin(admin.ModelAdmin):
    list_display = ['title', 'creator', 'created_at']
    list_filter = ['created_at']
    search_fields = ['title', 'description', 'creator__username']

admin.site.register(Tag)
admin.site.register(Node)
admin.site.register(Edge)
admin.site.register(GraphSnapshot)
admin.site.register(Discussion)
admin.site.register(DiscussionReaction)
admin.site.register(Property)
