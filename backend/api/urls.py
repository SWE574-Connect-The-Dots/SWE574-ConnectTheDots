from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import (
    register, 
    login, 
    search, 
    SpaceViewSet, 
    TagViewSet, 
    ProfileViewSet,
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
]
