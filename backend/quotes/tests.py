from django.conf import settings
from django.test import TestCase

from .models import Profile, Product, ProfileFactory
from api.models import DeviceSession, GooglePlayProduct, GooglePlayIAPPurchase, PurchaseStatus


class UtilsTest(TestCase):
    def test_truncate(self):
        from .models import _truncate

        self.assertEqual('Карл у Кла˚˚˚', _truncate('Карл у Клары украл коралы', length=10, suffix='˚˚˚'))
        self.assertEqual('Карл у Клары украл коралы', _truncate('Карл у Клары украл коралы', length=100, suffix='˚˚˚'))


class ProfileTest(TestCase):
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
        factory = ProfileFactory()

        # When
        profile = factory.create_new_profile()

        # Then
        self.assertEqual(settings.QUOTES_INITIAL_PROFILE_BALANCE, profile.balance)


    def test_should_increase_balance_after_valid_purchase(self):
        # Given
        BALANCE_RECHARGE = 10

        google_play_product = GooglePlayProduct.objects.create()
        app_product = Product.objects.create(admin_title='10 монет',
                                             app_title='test_product',
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

        self.assertEqual(settings.QUOTES_INITIAL_PROFILE_BALANCE + BALANCE_RECHARGE, profile.balance)

    def test_shouldnt_increase_balance_when_purchase_has_default_status(self):
        # Given
        BALANCE_RECHARGE = 10

        google_play_product = GooglePlayProduct.objects.create()
        app_product = Product.objects.create(admin_title='10 монет',
                                             app_title='test_product',
                                             balance_recharge=BALANCE_RECHARGE,
                                             google_play_product=google_play_product)
        session = DeviceSession.objects.create()

        # When
        purchase = GooglePlayIAPPurchase.objects.create(product=google_play_product,
                                                        device_session=session)
        purchase.save()

        # Then
        profile = Profile.objects.filter(device_sessions__pk__contains=session)[0]  # auto-generated profile

        self.assertEqual(settings.QUOTES_INITIAL_PROFILE_BALANCE, profile.balance)
