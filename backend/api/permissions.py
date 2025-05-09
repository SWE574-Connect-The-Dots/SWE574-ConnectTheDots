from rest_framework import permissions

class IsCollaboratorOrReadOnly(permissions.BasePermission):
    """
    Custom permission to only allow collaborators of a space to edit it.
    """
    
    def has_permission(self, request, view):
        if request.method in permissions.SAFE_METHODS:
            return True
            
        # Allow join/leave/check actions
        if view.action in ['join_space', 'leave_space', 'check_collaborator']:
            return True
            
        return True  # Object-level permissions will handle the rest
    
    def has_object_permission(self, request, view, obj):
        if request.method in permissions.SAFE_METHODS:
            return True
            
        # Allow join/leave/check actions
        if view.action in ['join_space', 'leave_space', 'check_collaborator']:
            return True
        
        # Write permissions are only allowed to collaborators
        return request.user in obj.collaborators.all()

class IsSpaceCollaborator(permissions.BasePermission):
    """
    Custom permission to only allow actions for collaborators of a space.
    """
    
    def has_permission(self, request, view):
        # If space_pk is in the URL, check if user is a collaborator
        space_pk = view.kwargs.get('space_pk')
        if space_pk:
            from .models import Space
            try:
                space = Space.objects.get(pk=space_pk)
                return request.user in space.collaborators.all()
            except Space.DoesNotExist:
                return False
        return True 