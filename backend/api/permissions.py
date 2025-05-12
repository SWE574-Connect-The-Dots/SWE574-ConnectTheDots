from rest_framework import permissions

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