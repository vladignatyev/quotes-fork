from django.test import TestCase
from .models import DeviceSession, Credentials


class ModelTest(TestCase):
    def test_device_session_creation(self):
        test_token = 'abcdabcdabcdabcdabcdabcdabcdabcd'

        obj = DeviceSession.create_from_token(token=test_token)

        self.assertIsNotNone(obj)
        self.assertEqual(obj.token, test_token)

    # todo:
    def test_device_token_validation(self):
        invalid_token = 'invalid-token'

        obj = DeviceSession.create_from_token(token=invalid_token)

        self.assertIsNone(obj)

    def test_credentials(self):
        Credentials.objects.create(google_play_bundle_id='com.google.test1')
        Credentials.objects.create(google_play_bundle_id='com.google.test2')
        Credentials.objects.create(google_play_bundle_id='com.google.test3')

        credentials = Credentials.objects.get()

        self.assertEqual('com.google.test3', credentials.google_play_bundle_id)
