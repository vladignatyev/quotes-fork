from quoterank.core import *

from django.test import TestCase


# Create your tests here.
class CoreTest(TestCase):
    def test_gompertz_default(self):
        self.assertEqual(gompertz(0), 1.0)

    def test_gompertz_in_future(self):
        self.assertEqual(round(gompertz(30)), 3.0)
        self.assertEqual(round(gompertz(300)), 3.0)


class ModelTest(TestCase):
    def test_get_top(self):
        #
        pass
