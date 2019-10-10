import json

from django.test import TestCase
from django.urls import reverse
from django.utils import timezone

from api.models import *
from ..models import Profile, GameBalance


class GameBalanceMixin(TestCase):
    TEST_INITIAL_PROFILE_BALANCE = 20

    def setUp(self):
        super(GameBalanceMixin, self).setUp()
        self._gamebalance = GameBalance.objects.create(initial_profile_balance=self.TEST_INITIAL_PROFILE_BALANCE)

    def tearDown(self):
        super(GameBalanceMixin, self).tearDown()
        self._gamebalance.delete()



class AuthenticatedTestCase(TestCase):
    INITIAL_USER_BALANCE = 1000

    def setUp(self):
        '''
        Receiving auth token during normal HTTP flow.
        '''
        url = reverse('quote-auth')

        device_token = 'sometesttoken'
        timestamp = timezone.now().strftime('%Y-%m-%dT%H:%M:%S%z')
        signature = generate_signature(device_token, timestamp)
        nickname = 'Tester'

        payload = {
            'device_token': device_token,
            'timestamp': timestamp,
            'signature': signature,
            'nickname': nickname
        }

        response = self.client.post(url, json.dumps(payload, ensure_ascii=False), content_type='application/json')
        self.assertEqual(200, response.status_code)
        self.auth_token = json.loads(response.content)['auth_token']

        self.profile = Profile.objects.get_by_auth_token(self.auth_token)
        self.profile.balance = self.INITIAL_USER_BALANCE
        self.profile.save()
        self.device_session = self.profile.device_sessions.latest('pk')


    def tearDown(self):
        pass

    def auth(self):
        ''' Helper for passing auth token to the view '''
        return {'HTTP_X-Client-Auth': self.auth_token}
