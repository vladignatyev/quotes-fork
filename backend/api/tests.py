from django.test import TestCase
from .models import DeviceSession

class ModelTest(TestCase):
    def test_device_session_creation(self):
        test_token = 'some-device-generated-token'

        obj = DeviceSession.create_from_token(token=test_token)

        self.assertIsNotNone(obj)
        self.assertEqual(obj.token, test_token)
