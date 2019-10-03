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


from rest_framework import reverse
class ViewsTestCase(TestCase):


    def test_should_return_list_of_topics(self):
        response = self.client.get(reverse('quotes-api:topics'))

        self.assertEqual(response.status_code, 200)
