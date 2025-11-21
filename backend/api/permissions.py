from rest_framework import permissions
from rest_framework.exceptions import PermissionDenied
from .models import Profile, SpaceModerator

class IsCollaboratorOrReadOnly(permissions.BasePermission):
    """
    Custom permission to only allow collaborators of a space to edit it.
    """
    
    def has_permission(self, request, view):
        if request.method in permissions.SAFE_METHODS:
            return True
            
        if view.action in ['join_space', 'leave_space', 'check_collaborator']:
            return True
            
        return True
    
    def has_object_permission(self, request, view, obj):
        if request.method in permissions.SAFE_METHODS:
            return True
            
        # Allow admins (staff or superuser) the same permissions as creators
        if request.user.is_staff or request.user.is_superuser:
            return True
            
        return request.user in obj.collaborators.all() or request.user == obj.creator

class IsSpaceCollaborator(permissions.BasePermission):
    """
    Custom permission to only allow actions for collaborators of a space.
    """
    
    def has_permission(self, request, view):
        space_pk = view.kwargs.get('space_pk')
        if space_pk:
            from .models import Space
            try:
                space = Space.objects.get(pk=space_pk)
                return request.user in space.collaborators.all()
            except Space.DoesNotExist:
                return False
        return True 

class IsProfileOwner(permissions.BasePermission):
    """
    Custom permission to only allow owners of a profile to edit it.
    """
    def has_object_permission(self, request, view, obj):
        if request.method in permissions.SAFE_METHODS:
            return True
            
        return obj.user == request.user

class IsAdmin(permissions.BasePermission):
    """
    Custom permission to only allow admins.
    """
    def has_permission(self, request, view):
        if not request.user.is_authenticated:
            return False
        try:
            profile = request.user.profile
            return profile.is_admin()
        except Profile.DoesNotExist:
            return False

class IsAdminOrModerator(permissions.BasePermission):
    """
    Custom permission to allow admins and moderators.
    """
    def has_permission(self, request, view):
        if not request.user.is_authenticated:
            return False
        try:
            profile = request.user.profile
            return profile.is_admin() or profile.is_moderator()
        except Profile.DoesNotExist:
            return False

class IsSpaceModerator(permissions.BasePermission):
    """
    Custom permission to check if user is a moderator of a specific space.
    """
    def has_permission(self, request, view):
        if not request.user.is_authenticated:
            return False
        
        space_pk = view.kwargs.get('space_pk') or view.kwargs.get('pk')
        if not space_pk:
            return False
            
        try:
            profile = request.user.profile
            if profile.is_admin():
                return True
                
            from .models import Space
            space = Space.objects.get(pk=space_pk)
            return profile.can_moderate_space(space)
        except (Profile.DoesNotExist, Space.DoesNotExist):
            return False

class CanChangeUserType(permissions.BasePermission):
    """
    Custom permission for changing user types.
    Admins can change any user type.
    Moderators can only change regular users to moderators within their spaces.
    """
    def has_permission(self, request, view):
        if not request.user.is_authenticated:
            return False
        
        try:
            profile = request.user.profile
            # Admins can always change user types
            if profile.is_admin():
                return True
            
            # Moderators can only change user types in specific contexts
            if profile.is_moderator():
                # This permission will be further checked in the view
                return True
                
        except Profile.DoesNotExist:
            pass
        
        return False


class IsNotArchivedUser(permissions.BasePermission):
    """
    Custom permission to block archived users from performing any actions.
    """
    message = "Your account has been archived and cannot perform any actions."
    
    def has_permission(self, request, view):
        if not request.user.is_authenticated:
            return True
        
        try:
            profile = request.user.profile
            if profile.is_archived:
                return False
            return True
        except Profile.DoesNotExist:
            return True