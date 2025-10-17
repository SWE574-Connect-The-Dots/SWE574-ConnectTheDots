import os

DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": os.getenv("POSTGRES_DB", "mydb"),
        "USER": os.getenv("POSTGRES_USER", "myuser"),
        "PASSWORD": os.getenv("POSTGRES_PASSWORD", "yy"),
        "HOST": os.getenv("POSTGRES_HOST", "db"),
        "PORT": os.getenv("POSTGRES_PORT", "5432"),
    }
}
