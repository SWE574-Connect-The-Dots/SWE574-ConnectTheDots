import os, sys, django
from django.conf import settings
from django.test.utils import get_runner

try:
    from xmlrunner.extra.djangotestrunner import XMLTestRunner
except ImportError:
    # fallback for recent versions that moved it
    from xmlrunner import XMLTestRunner

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")
django.setup()

TestRunner = get_runner(settings)
test_runner = XMLTestRunner(output='test-reports', verbosity=2)

failures = test_runner.run_tests(['api.tests'])
sys.exit(bool(failures))