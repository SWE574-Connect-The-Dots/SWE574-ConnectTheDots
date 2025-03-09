from django.urls import path
from . import views

urlpatterns = [
    path('', views.hello_world),
    path('register/', views.register, name='register'),
    path('login/', views.login, name='login'),
]