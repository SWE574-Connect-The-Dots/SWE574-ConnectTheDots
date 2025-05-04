from django.urls import path, include
from rest_framework.routers import DefaultRouter
from . import views

router = DefaultRouter()
router.register(r'spaces', views.SpaceViewSet)
router.register(r'tags', views.TagViewSet)

urlpatterns = [
    path('register/', views.register, name='register'),
    path('login/', views.login, name='login'),
    path('search/', views.search, name='search'),
    path('', include(router.urls)),
]
