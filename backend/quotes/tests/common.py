import json

from django.test import TestCase
from django.urls import reverse
from django.utils import timezone

from api.models import *
from ..models import *


class TimeAssert:
    def assertTime(self, t1, t2, delta_microseconds=1E4, f=None):
        _t1 = t1.timestamp()
        _t2 = t2.timestamp()

        if _t2 > _t1:
            dt = _t2 - _t1
        else:
            dt = _t1 - _t2
        dm = dt * 1E6
        if f is None:
            self.assertTrue(dm < delta_microseconds, f'dt is {dm} microseconds for t2={t2} and t1={t1}, but should be lesser than {delta_microseconds} microseconds')
        else:
            f(dm < delta_microseconds, f'dt is {dm} microseconds for t2={t2} and t1={t1}, but should be lesser than {delta_microseconds} microseconds')


class GameBalanceMixin(TestCase):
    TEST_INITIAL_PROFILE_BALANCE = 20

    def setUp(self):
        super(GameBalanceMixin, self).setUp()
        self._gamebalance = GameBalance.objects.create(initial_profile_balance=self.TEST_INITIAL_PROFILE_BALANCE)

    def tearDown(self):
        super(GameBalanceMixin, self).tearDown()
        self._gamebalance.delete()


class ContentMixin:
    def _create_content_hierarchy(self):
        topic = Topic.objects.create(title='Test topic',
                                     is_published=True)
        section = Section.objects.create(title='Test section',
                                         topic=topic,
                                         is_published=True)

        category = QuoteCategory.objects.create(
            section=section,
            title='Test category',
            is_payable=False,
            is_published=True
        )

        authors_name = 'Lewis Carroll'
        author = QuoteAuthor.objects.create(name=authors_name)

        self.topic = topic
        self.section = section
        self.category = category
        self.author = author


    def _create_multiple_quotes(self, category=None, author=None):
        category = category or self.category
        author = author or self.author

        q = [
            'And what is the use of a book without pictures or conversations?',
            'How funny it’ll seem to come out among the people that walk with their heads downwards!',
            'Oh, how I wish I could shut up like a telescope!',
            'I suppose I ought to eat or drink something or other; but the great question is ‘What?’',
            'When I used to read fairy tales, I fancied that kind of thing never happened, and now here I am in the middle of one!',
            'I’m older than you, and must know better.',
            'The best way to explain it is to do it.'
        ]

        quotes = []

        for t in q:
            quotes += [Quote.objects.create(text=t,
                                 author=author,
                                 category=category)]

        if category == self.category:
            self.quotes = quotes

        return quotes



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
