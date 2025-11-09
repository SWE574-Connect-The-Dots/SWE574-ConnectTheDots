from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    register, 
    login, 
    search, 
    SpaceViewSet, 
    TagViewSet, 
    ProfileViewSet,
    assign_moderator,
    change_user_type,
    remove_moderator,
    get_user_permissions,
    list_users_by_type,
    dashboard_stats,
)

router = DefaultRouter()
router.register(r'spaces', SpaceViewSet, basename='space')
router.register(r'tags', TagViewSet, basename='tag')
router.register(r'profiles', ProfileViewSet, basename='profile')

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
]
