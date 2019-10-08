import json

from django.conf import settings
from django.test import TestCase

from .models import *
from api.models import *


class UtilsTest(TestCase):
    def test_truncate(self):
        from .models import _truncate

        self.assertEqual('Карл у Кла˚˚˚', _truncate('Карл у Клары украл коралы', length=10, suffix='˚˚˚'))
        self.assertEqual('Карл у Клары украл коралы', _truncate('Карл у Клары украл коралы', length=100, suffix='˚˚˚'))


class GameBalanceTestCase(TestCase):
    TEST_INITIAL_PROFILE_BALANCE = 20

    def setUp(self):
        super(GameBalanceTestCase, self).setUp()
        self._gamebalance = GameBalance.objects.create(initial_profile_balance=self.TEST_INITIAL_PROFILE_BALANCE)

    def tearDown(self):
        super(GameBalanceTestCase, self).tearDown()
        self._gamebalance.delete()


class ProfileTest(GameBalanceTestCase):
    def test_autocreate_profile_when_new_device_session_created(self):
        # Given
        self.assertEqual(0, len(Profile.objects.all()))

        # When
        session = DeviceSession.objects.create()

        # Then
        self.assertEqual(1, len(Profile.objects.filter(device_sessions__pk__contains=session).all()))

    def test_shouldnt_create_dupes(self):
        # Given
        self.assertEqual(0, len(Profile.objects.all()))
        session = DeviceSession.objects.create()

        # When
        session.save()

        # Then
        self.assertEqual(1, len(Profile.objects.filter(device_sessions__pk__contains=session).all()))

    def test_should_have_default_balance(self):
        # Given
        profile = Profile.objects.create()

        # When
        pass

        # Then
        self.assertEqual(self.TEST_INITIAL_PROFILE_BALANCE, profile.balance)

    def test_should_have_default_balance_equal_to_latest_game_balance(self):
        # Given
        GameBalance.objects.create(initial_profile_balance=10)
        GameBalance.objects.create(initial_profile_balance=20)
        GameBalance.objects.create(initial_profile_balance=30)

        # When
        profile = Profile.objects.create()

        # Then
        self.assertEqual(30, profile.balance)


    def test_should_increase_balance_after_valid_purchase(self):
        # Given
        BALANCE_RECHARGE = 10

        google_play_product = GooglePlayProduct.objects.create()
        app_product = BalanceRechargeProduct.objects.create(admin_title='10 монет',
                                             balance_recharge=BALANCE_RECHARGE,
                                             google_play_product=google_play_product)
        session = DeviceSession.objects.create()

        # When
        purchase = GooglePlayIAPPurchase.objects.create(product=google_play_product,
                                                        device_session=session)
        purchase.status = PurchaseStatus.VALID  # manually 'validated' purchase
        purchase.save()

        # Then
        profile = Profile.objects.filter(device_sessions__pk__contains=session)[0]  # auto-generated profile

        self.assertEqual(self.TEST_INITIAL_PROFILE_BALANCE + BALANCE_RECHARGE, profile.balance)

    def test_shouldnt_increase_balance_when_purchase_has_default_status(self):
        # Given
        BALANCE_RECHARGE = 10

        google_play_product = GooglePlayProduct.objects.create()
        app_product = BalanceRechargeProduct.objects.create(admin_title='10 монет',
                                             balance_recharge=BALANCE_RECHARGE,
                                             google_play_product=google_play_product)
        session = DeviceSession.objects.create()

        # When
        purchase = GooglePlayIAPPurchase.objects.create(product=google_play_product,
                                                        device_session=session)
        purchase.save()

        # Then
        profile = Profile.objects.filter(device_sessions__pk__contains=session)[0]  # auto-generated profile

        self.assertEqual(self.TEST_INITIAL_PROFILE_BALANCE, profile.balance)


class GameBalanceTest(TestCase):
    def test_should_always_return_the_latest(self):
        # Given
        GameBalance.objects.create(initial_profile_balance=10)
        GameBalance.objects.create(initial_profile_balance=20)
        GameBalance.objects.create(initial_profile_balance=30)

        # When
        game_settings = GameBalance.objects.get_actual_game_settings()

        # Then
        self.assertEqual(30, game_settings.initial_profile_balance)


class ProfileCategoryUnlocking(GameBalanceTestCase):
    def test_should_unlock_category_for_given_profile(self):
        # Given
        INITIAL_BALANCE = 30
        PRICE_TO_UNLOCK = 5

        GameBalance.objects.create(initial_profile_balance=INITIAL_BALANCE)

        profile = Profile.objects.create()
        profile.save()

        category = QuoteCategory.objects.create(title='Тестовая платная категория',
                                                is_payable=True,
                                                price_to_unlock=PRICE_TO_UNLOCK)

        # When
        result, explanation = Profile.objects.unlock_category(profile, category)
        category = QuoteCategory.objects.get(pk=category.pk)

        # Then
        self.assertTrue(result)
        self.assertEqual(INITIAL_BALANCE - PRICE_TO_UNLOCK, profile.balance)
        self.assertIn(profile, category.available_to_users.all())

    def test_shouldnt_unlock_category_if_no_funds_available(self):
        # Given
        INITIAL_BALANCE = 4
        PRICE_TO_UNLOCK = 5

        GameBalance.objects.create(initial_profile_balance=INITIAL_BALANCE)

        profile = Profile.objects.create()
        profile.save()

        category = QuoteCategory.objects.create(title='Тестовая платная категория',
                                                is_payable=True,
                                                price_to_unlock=PRICE_TO_UNLOCK)

        # When
        result, explanation = Profile.objects.unlock_category(profile, category)
        category = QuoteCategory.objects.get(pk=category.pk)

        # Then
        self.assertFalse(result)
        self.assertNotIn(profile, category.available_to_users.all())


class ProfileCategoryPurchaseUnlock(GameBalanceTestCase):
    def test_should_unlock_after_purchase_validated(self):
        # Given
        PRICE_TO_UNLOCK = 10E8 # ANY

        session = DeviceSession.objects.create()
        session.save()

        GameBalance.objects.create(initial_profile_balance=0)
        profile = Profile.objects.get_by_session(device_session=session)

        category = QuoteCategory.objects.create(title='Тестовая платная категория',
                                                is_payable=True,
                                                price_to_unlock=PRICE_TO_UNLOCK)


        google_play_product = GooglePlayProduct.objects.create()
        google_play_purchase = GooglePlayIAPPurchase.objects.create(product=google_play_product,
                                                                    device_session=session)
        google_play_purchase.save()

        purchase = CategoryUnlockPurchase.objects.create(profile=profile,
                                              category_to_unlock=category,
                                              google_play_purchase=google_play_purchase)
        purchase.save()

        # When
        google_play_purchase.status = PurchaseStatus.VALID  # manually 'validated' purchase
        google_play_purchase.save()

        # Then
        self.assertIn(profile, category.available_to_users.all())


class QuotesAuthenticateTest(TestCase):
    def test_should_create_profile(self):
        # Given
        url = reverse('quote-auth')

        device_token = 'sometesttoken'
        timestamp = timezone.now().strftime('%Y-%m-%dT%H:%M:%S%z')
        signature = generate_signature(device_token, timestamp)
        nickname = 'Тестировщик'

        payload = {
            'device_token': device_token,
            'timestamp': timestamp,
            'signature': signature,
            'nickname': nickname
        }

        # When
        response = self.client.post(url, json.dumps(payload, ensure_ascii=False), content_type='application/json')

        # Then
        self.assertEqual(200, response.status_code)

        payload = json.loads(response.content)
        auth_token = payload['auth_token']

        profile = Profile.objects.get_by_auth_token(auth_token)
        self.assertEqual(nickname, profile.nickname)
        self.assertEqual(GameBalance.objects.get_actual_game_settings(), profile.settings)

        self.assertEqual(1, len(Profile.objects.all()))

    def test_should_respond_401_for_broken_case(self):
        # Given
        url = reverse('quote-auth')

        device_token = '1'
        timestamp = '2019-10-06T12:37:16+0000'
        signature = '1'
        nickname = 'тестировщик-9b76..b814'

        payload = {
            'device_token': device_token,
            'timestamp': timestamp,
            'signature': signature,
            'nickname': nickname
        }

        # When
        response = self.client.post(url, json.dumps(payload, ensure_ascii=False), content_type='application/json')

        # Then
        self.assertEqual(401, response.status_code)

    def test_should_respond_401_not_500_to_make_vadim_confident_about_his_mobile_client(self):
        # Given
        url = reverse('quote-auth')

        device_token = 'sometesttoken'
        timestamp = timezone.now().strftime('%Y-%m-%dT%H:%M:%S%z')
        signature = generate_signature(device_token, timestamp)
        nickname = 'Тестировщик'

        payload = {
            'device_token': device_token,
            'timestamp': timestamp,
            'signature': signature,
            'nickname': nickname
        }

        # When
        response = self.client.post(url, json.dumps(payload, ensure_ascii=False), content_type='application/json')
        response = self.client.post(url, json.dumps(payload, ensure_ascii=False), content_type='application/json')

        # Then
        self.assertEqual(401, response.status_code)


class AuthenticatedTestCase(TestCase):
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

        # When
        response = self.client.post(url, json.dumps(payload, ensure_ascii=False), content_type='application/json')
        self.assertEqual(200, response.status_code)
        self.auth_token = json.loads(response.content)['auth_token']


    def tearDown(self):
        pass

    def auth(self):
        ''' Helper for passing auth token to the view '''
        return {'HTTP_X-Client-Auth': self.auth_token}


class QuoteSplit(TestCase):
    def test_split_normal(self):
        self.assertEqual(['And', 'what', 'is', 'the', 'use', 'of', 'a', 'book',
                          'without', 'pictures', 'or', 'conversations?'],
                          quote_split('And what is the use of a book without pictures or conversations?'))
        self.assertEqual(['I', 'suppose', 'I', 'ought', 'to', 'eat', 'or',
                          'drink', 'something', 'or', 'other;', 'but', 'the',
                          'great', 'question', 'is', '‘What?’'], quote_split('I suppose I ought to eat or drink something or other; but the great question is ‘What?’'))

    def test_split_special(self):
        self.assertEqual(['And what is the use of', 'a book without', 'pictures or conversations', '?'],
                          quote_split('And what is the use of ^a book without ^pictures or conversations^?'))


    def test_unicode_normal(self):
        self.assertEqual(['改善', 'means', 'improvement.'], quote_split('改善 means improvement.'))
        self.assertEqual(['Кайдзен', 'означает', 'совершенствование.'], quote_split('Кайдзен означает совершенствование.'))

    def test_unicode_special(self):
        self.assertEqual(['改善', 'means improvement.'], quote_split('改善^ means improvement.'))
        self.assertEqual(['Кайдзен', 'означает совершенствование.'], quote_split('Кайдзен ^означает совершенствование.^'))


class LevelsListTest(AuthenticatedTestCase):
    def _create_content_hierarchy(self):
        topic = Topic.objects.create(title='Test topic',
                                     hidden=False)
        section = Section.objects.create(title='Test section',
                                         topic=topic)

        category = QuoteCategory.objects.create(
            section=section,
            title='Test category',
            is_payable=False
        )

        authors_name = 'Lewis Carroll'
        author = QuoteAuthor.objects.create(name=authors_name)

        self.topic = topic
        self.category = category
        self.author = author


    def _create_multiple_quotes(self, category, author):
        q = [
            'And what is the use of a book without pictures or conversations?',
            'How funny it’ll seem to come out among the people that walk with their heads downwards!',
            'Oh, how I wish I could shut up like a telescope!',
            'I suppose I ought to eat or drink something or other; but the great question is ‘What?’',
            'When I used to read fairy tales, I fancied that kind of thing never happened, and now here I am in the middle of one!',
            'I’m older than you, and must know better.',
            'The best way to explain it is to do it.'
        ]

        for t in q:
            Quote.objects.create(text=t,
                                 author=author,
                                 category=category)
        self.quotes_count = len(q)

    def test_get_levels_list_for_free_category(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        # When
        url = reverse('levels-list', kwargs={'category_pk': self.category.pk})
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(200, response.status_code)
        content = json.loads(response.content)

        self.assertEqual(len(content['objects']), self.quotes_count)
        self.assertEqual(self.author.name, content['objects'][0]['author'])

        # only check that all keys present
        for o in content['objects']:
            fields = ('id', 'text', 'author',
                      'category_complete_reward',
                      'order', 'complete',
                      'splitted')
            self.assertEqual(set(fields), set(o.keys()))
