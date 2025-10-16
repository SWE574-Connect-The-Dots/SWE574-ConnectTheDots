import os, sys, django
from django.conf import settings
from django.test.utils import get_runner
import xmlrunner

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")
django.setup()

TestRunner = get_runner(settings)
test_runner = xmlrunner.extra.djangotestrunner.XMLTestRunner(
    output='test-reports',  # directory for XML files
    verbosity=2
)

failures = test_runner.run_tests(['api.tests'])
sys.exit(bool(failures))