import json

from django.test import TestCase
from .models import DeviceSession, Credentials, get_shared_secret, get_server_secret, generate_signature
from django.urls import reverse
from django.utils import timezone



class ModelTest(TestCase):
    def test_device_session_creation(self):
        test_token = 'abcdabcdabcdabcdabcdabcdabcdabcd'

        obj = DeviceSession.objects.create_from_token(token=test_token)

        self.assertIsNotNone(obj)
        self.assertEqual(obj.token, test_token)

    # todo:
    def test_device_token_validation(self):
        invalid_token = 'invalid-token'

        obj = DeviceSession.objects.create_from_token(token=invalid_token)

        self.assertIsNone(obj)

    def test_credentials(self):
        Credentials.objects.create(google_play_bundle_id='com.google.test1')
        Credentials.objects.create(google_play_bundle_id='com.google.test2')
        Credentials.objects.create(google_play_bundle_id='com.google.test3')

        credentials = Credentials.objects.get()

        self.assertEqual('com.google.test3', credentials.google_play_bundle_id)

    def test_credentials_didnt_exist_should_provide_secrets(self):
        # Given
        self.assertEqual(0, len(Credentials.objects.all()))

        # When
        credentials = Credentials.objects.get()

        # Then
        self.assertIsNotNone(credentials.server_secret)
        self.assertIsNotNone(credentials.shared_secret)
        self.assertNotEqual(credentials.shared_secret, credentials.server_secret)

    def test_credentials_cleans_lrucache_for_tokens(self):
        # Given
        self.assertEqual(0, len(Credentials.objects.all()))
        credentials = Credentials.objects.get()
        self.assertEqual(str(credentials.shared_secret), get_shared_secret())
        self.assertEqual(str(credentials.server_secret), get_server_secret())

        # When
        new_credentials = Credentials.objects.create()
        new_credentials.save()
        self.assertEqual(str(new_credentials.shared_secret), get_shared_secret())
        self.assertEqual(str(new_credentials.server_secret), get_server_secret())


class AuthenticationTest(TestCase):
    def post_data(self, payload, url=reverse('api-auth')):
        # return self.client.generic("POST", url, json.dumps(payload))
        return self.client.post(url, {'data': json.dumps(payload, ensure_ascii=False)})


    def test_get_not_allowed(self):
        response = self.client.get(reverse('api-auth'))
        self.assertEqual(405, response.status_code)

    def test_put_not_allowed(self):
        response = self.client.put(reverse('api-auth'))
        self.assertEqual(405, response.status_code)

    def test_post_allowed(self):
        response = self.client.post(reverse('api-auth'))
        self.assertNotEqual(405, response.status_code)

    def test_invalid_form(self):
        # Given
        payload = {}

        # When
        response = self.post_data(payload)

        # Then
        self.assertEqual(401, response.status_code)

    def test_giant_form_shouldnt_even_deserialize(self):
        # Given
        device_token = 'cafe' * 10000
        timestamp = timezone.now().strftime('%Y-%m-%dT%H:%M:%S%z')
        nickname = 'Тестировщик'
        signature = generate_signature(device_token, timestamp)

        very_big_object = {
            'device_token': device_token,
            'timestamp': timestamp,
            'signature': signature,
            'nickname': nickname
        }

        # When
        response = self.post_data(very_big_object)

        # Then
        self.assertEqual(401, response.status_code)



    def test_authentication_new_user(self):
        # Given
        device_token = 'sometesttoken'
        timestamp = timezone.now().strftime('%Y-%m-%dT%H:%M:%S%z')
        signature = generate_signature(device_token, timestamp)

        payload = {
            'device_token': device_token,
            'timestamp': timestamp,
            'nickname': 'Тестировщик',
            'signature': signature
        }

        # When
        response = self.post_data(payload)

        # Then
        self.assertEqual(200, response.status_code)
        self.assertIsNotNone(response.content)
