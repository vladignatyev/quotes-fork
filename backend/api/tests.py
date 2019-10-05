import json
from unittest import skip

from django.test import TestCase
from .models import *
from django.urls import reverse
from django.utils import timezone

from django.test import RequestFactory

from .views import AuthenticateView


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
    FAKE_URL_PATH = '/someauthpath/'

    def setUp(self):
        self.factory = RequestFactory()

    def post_data(self, payload):
        data_dict = {
            'data': json.dumps(payload, ensure_ascii=False)
        }

        request = self.factory.post(self.FAKE_URL_PATH, data_dict)
        view = AuthenticateView()
        response = view.post(request)
        return response

    def test_post_allowed(self):
        request = self.factory.post(self.FAKE_URL_PATH)
        view = AuthenticateView()
        response = view.post(request)
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
        signature = generate_signature(device_token, timestamp)

        very_big_object = {
            'device_token': device_token,
            'timestamp': timestamp,
            'signature': signature,
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
            'signature': signature
        }

        # When
        response = self.post_data(payload)

        # Then
        self.assertEqual(200, response.status_code)
        self.assertIsNotNone(response.content)
        self.assertIsNotNone(DeviceSession.objects.get(token=device_token))

    def test_auth_token_generation_symmetry(self):
        # Given
        auth_token = generate_auth_token()
        # When
        check_result = check_auth_token(auth_token)
        # Then
        self.assertTrue(check_result)

    @skip('should more stable modification')
    def test_auth_token_generation_modification_doesnt_pass_validation(self):
        # Given
        auth_token = generate_auth_token()

        # When
        modified_token1 = auth_token[1] + auth_token[0] + auth_token[2:]
        modified_token2 = auth_token[:-2] + auth_token[-1] + auth_token[-2]

        # Then
        self.assertTrue(check_auth_token(auth_token))
        self.assertFalse(check_auth_token(modified_token1))
        self.assertFalse(check_auth_token(modified_token2))

    def test_auth_token_generation_zero_padding_attack_doesnt_expose_server_secret(self):
        # Given
        random_value = ''
        server_secret = get_server_secret()
        crafted_auth_token = sign_auth_token(random_value)

        # When
        check_result = check_auth_token(crafted_auth_token)

        # Then
        self.assertTrue(check_result)
        self.assertNotIn(crafted_auth_token, get_server_secret())
