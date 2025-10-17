import os, sys, django
from django.conf import settings
from django.test.utils import get_runner

try:
    from xmlrunner.extra.djangotestrunner import XMLTestRunner
except ImportError:
    from xmlrunner import XMLTestRunner

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")
django.setup()

TestRunner = get_runner(settings)

# The key line: use Django's runner with xmlrunner integration
test_runner = XMLTestRunner(
    output='test-reports',  # this folder is relative to where this script runs
    verbosity=2,
)

failures = test_runner.run_tests(['api.tests'])
print("XMLTestRunner writing to:", os.path.abspath('test-reports'))
sys.exit(bool(failures))