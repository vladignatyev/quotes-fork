from django.test import TestCase
from .models import DeviceSession

class ModelTest(TestCase):
    def test_device_session_creation(self):
        test_token = 'abcdabcdabcdabcdabcdabcdabcdabcd'

        obj = DeviceSession.create_from_token(token=test_token)

        self.assertIsNotNone(obj)
        self.assertEqual(obj.token, test_token)

    def test_device_token_validation(self):
        invalid_token = 'invalid-token'

        obj = DeviceSession.create_from_token(token=invalid_token)

        self.assertIsNone(obj)
