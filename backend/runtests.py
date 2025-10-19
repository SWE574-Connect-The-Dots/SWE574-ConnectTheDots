import os
import sys
import django
from django.conf import settings
from django.test.utils import get_runner


os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")
django.setup()

import xmlrunner


TestRunner = get_runner(settings)
runner = TestRunner(verbosity=2)

print("Running Django tests with XML output...")
result = xmlrunner.XMLTestRunner(output='/app/test-reports', verbosity=2).run(runner.build_suite(['api.tests']))

print("XML reports written to /app/test-reports")
sys.exit(not result.wasSuccessful())
