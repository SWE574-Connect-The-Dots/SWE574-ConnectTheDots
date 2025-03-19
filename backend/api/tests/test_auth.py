from django.contrib.auth.models import User
from rest_framework.test import APITestCase
from rest_framework import status

class AuthTestCase(APITestCase):

    def setUp(self):
        """
        Create a test user before running tests.
        """
        self.user = User.objects.create_user(username="testuser", password="testpassword")
        self.register_url = "/api/register/"
        self.login_url = "/api/login/"

    def test_register_user(self):
        """
        Ensure we can register a new user successfully with profession and date of birth.
        """
        data = {
            "username": "newuser",
            "email": "newuser@example.com",
            "password": "newpassword",
            "profession": "Software Engineer",
            "dob": "2000-01-01"
        }
        response = self.client.post(self.register_url, data)

        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertIn("message", response.data)
        self.assertEqual(response.data["message"], "User registered successfully")

        self.assertTrue(User.objects.filter(username="newuser").exists())

        user = User.objects.get(username="newuser")
        self.assertEqual(user.profile.profession, "Software Engineer")
        self.assertEqual(str(user.profile.dob), "2000-01-01")

    def test_register_user_no_profession(self):
        """
        Ensure registering new user fails if profession is empty.
        """
        data = {
            "username": "newuser",
            "email": "newuser@example.com",
            "password": "newpassword",
            "profession": "",
            "dob": "2000-01-01"
        }
        response = self.client.post(self.register_url, data)

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_register_user_below_age_eighteen(self):
        """
        Ensure registering a new user fails when date of birth is below 18.
        """
        data = {
            "username": "newuser",
            "email": "newuser@example.com",
            "password": "newpassword",
            "profession": "Software Engineer",
            "dob": "2010-01-01"
        }
        response = self.client.post(self.register_url, data)

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_register_existing_user(self):
        """
        Ensure registering an existing user fails.
        """
        data = {
            "username": "testuser",
            "email": "testuser@example.com",
            "password": "testpassword"
        }
        response = self.client.post(self.register_url, data)
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_login_valid_user(self):
        """
        Ensure login works for a valid user.
        """
        data = {"username": "testuser", "password": "testpassword"}
        response = self.client.post(self.login_url, data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn("token", response.data)

    def test_login_invalid_user(self):
        """
        Ensure login fails for invalid username credentials.
        """
        data = {"username": "nosuchuser", "password": "testpassword"}
        response = self.client.post(self.login_url, data)
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_login_invalid_password(self):
        """
        Ensure login fails for incorrect password.
        """
        data = {"username": "testuser", "password": "wrongpassword"}
        response = self.client.post(self.login_url, data)
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
