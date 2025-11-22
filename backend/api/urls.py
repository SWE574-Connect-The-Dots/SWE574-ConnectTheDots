from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    register, 
    login, 
    search, 
    SpaceViewSet, 
    TagViewSet, 
    ProfileViewSet,
    ReportViewSet,
    assign_moderator,
    change_user_type,
    remove_moderator,
    get_user_permissions,
    list_users_by_type,
    dashboard_stats,
    ActivityStreamView,
    archive_item,
    list_archived_items,
    restore_archived_item,
)

router = DefaultRouter()
router.register(r'spaces', SpaceViewSet, basename='space')
router.register(r'tags', TagViewSet, basename='tag')
router.register(r'profiles', ProfileViewSet, basename='profile')
router.register(r'reports', ReportViewSet, basename='report')

urlpatterns = [
    path('', include(router.urls)),
    path('register/', register, name='register'),
    path('login/', login, name='login'),
    path('search/', search, name='search'),
    
    # Authorization management endpoints
    path('auth/assign-moderator/', assign_moderator, name='assign_moderator'),
    path('auth/change-user-type/', change_user_type, name='change_user_type'),
    path('auth/remove-moderator/', remove_moderator, name='remove_moderator'),
    path('auth/permissions/', get_user_permissions, name='get_user_permissions'),
    path('auth/users/', list_users_by_type, name='list_users_by_type'),
    
    # Dashboard endpoint
    path('dashboard/stats/', dashboard_stats, name='dashboard_stats'),
    
    # Activity stream
    path('activity-stream/', ActivityStreamView.as_view(), name='activity_stream'),

    # Archive endpoints
    path('archive/', list_archived_items, name='list_archived_items'),
    path('archive/create/', archive_item, name='archive_item'),
    path('archive/<int:archive_id>/restore/', restore_archived_item, name='restore_archived_item'),
]
