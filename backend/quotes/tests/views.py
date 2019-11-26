from unittest import skip
import json
import uuid


from django.urls import reverse
from django.test import TestCase
from django.utils import timezone
from django.apps import apps


from api.models import *

from ..models import *
from ..views import BaseView

from .common import AuthenticatedTestCase, ContentMixin


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
        self.assertEqual(GameBalance.objects.get_actual(), profile.settings)

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


class LevelsListTest(AuthenticatedTestCase, ContentMixin):
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

        self.assertEqual(len(content['objects']), len(self.quotes))
        self.assertEqual(self.author.name, content['objects'][0]['author'])

        # only check that all keys present
        for o in content['objects']:
            fields = ('id', 'text', 'author',
                      'reward', 'beautiful',
                      'order', 'complete',
                      'splitted')
            self.assertEqual(set(fields), set(o.keys()))

    def test_get_levels_list_for_payable_category_returns_402_if_category_locked_for_user(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        self.category.is_payable = True
        self.category.save()

        # When
        url = reverse('levels-list', kwargs={'category_pk': self.category.pk})
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(402, response.status_code)

    def test_nonexisting_category_cause_404(self):
        # When
        url = reverse('levels-list', kwargs={'category_pk': 123456})
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(404, response.status_code)

    def test_get_levels_list_for_payable_category_returns_list_when_unlocked_for_coins(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        self.category.is_payable = True
        self.category.save()

        # When
        # Unlocking for coins
        # unlocked, _ = Profile.objects.unlock_category(self.profile, self.category)
        # self.assertTrue(unlocked)

        unlock_purchase = CategoryUnlockPurchase.objects.create(profile=self.profile,
                                              type=CategoryUnlockTypes.UNLOCK_FOR_COINS,
                                              category_to_unlock=self.category)

        unlock_purchase.do_unlock()

        # tbd


        url = reverse('levels-list', kwargs={'category_pk': self.category.pk})
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(200, response.status_code)

    def test_get_levels_list_for_payable_category_returns_list_when_unlocked_for_cash(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        self.category.is_payable = True
        self.category.save()

        # When
        # Unlocking for purchase
        google_play_product = GooglePlayProduct.objects.create()
        purchase = GooglePlayIAPPurchase.objects.create(product=google_play_product,
                                                        device_session=self.device_session)

        unlock_purchase = CategoryUnlockPurchase.objects.create(profile=self.profile,
                                              type=CategoryUnlockTypes.UNLOCK_BY_PURCHASE,
                                              category_to_unlock=self.category,
                                              google_play_purchase=purchase)

        purchase.status = PurchaseStatus.PURCHASED  # manually 'validated' purchase
        purchase.save()  # do_unlock will be called on save see `signals.py`


        url = reverse('levels-list', kwargs={'category_pk': self.category.pk})
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(200, response.status_code)


class AllAchievementListViewTest(AuthenticatedTestCase):
    def test_achievements_view_all_present(self):
        # Given
        url = reverse('achievements-list-all')
        achievements = []
        for i in range(0,10):
            achievements += [Achievement.objects.create(icon='default',
                                                        title=f'some test achievement #{i}',
                                                        received_text=f'you received {i}',
                                                        description_text=f'you leaerned {i}')]
        # When
        response = self.client.get(url, **self.auth())
        self.assertEqual(200, response.status_code)
        self.assertEqual(10, len(json.loads(response.content)['objects']))


class AchievementListViewTest(AuthenticatedTestCase):
    def test_achievements_view_present(self):
        # Given
        url = reverse('achievements-list')
        achievements = []
        for i in range(0,10):
            achievements += [Achievement.objects.create(icon='default',
                                                        title=f'some test achievement #{i}',
                                                        received_text=f'you received {i}',
                                                        description_text=f'you leaerned {i}')]
        # When
        response = self.client.get(url, **self.auth())
        self.assertEqual(200, response.status_code)
        self.assertEqual(0, len(json.loads(response.content)['objects']))

        # When
        AchievementReceiving.objects.create(achievement=achievements[0], profile=self.profile)

        response = self.client.get(url, **self.auth())
        self.assertEqual(200, response.status_code)
        self.assertEqual(1, len(json.loads(response.content)['objects']))



class CategoryUnlockViewTest(AuthenticatedTestCase, ContentMixin):
    def test_should_unlock_locked_category(self):
        # Given
        price_coins = 10
        initial_balance = 1000

        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)
        self.category.is_payable = True
        self.category.price_to_unlock = price_coins
        self.category.save()

        self.assertFalse(self.category.is_available_to_user(self.profile))
        self.assertEqual(initial_balance, self.profile.balance)

        url = reverse('category-unlock', kwargs={'category_pk': self.category.pk})

        # When
        response = self.client.post(url, **self.auth())

        # Then
        self.assertEqual(200, response.status_code)
        updated_profile = Profile.objects.get(pk=self.profile.pk)
        updated_category = QuoteCategory.objects.get(pk=self.category.pk)

        self.assertTrue(updated_category.is_available_to_user(updated_profile))
        self.assertEqual(initial_balance - price_coins, updated_profile.balance)

    def test_shouldnt_unlock_locked_category_if_not_enough_coins(self):
        # Given
        price_coins = 10
        initial_balance = 1

        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)
        self.category.is_payable = True
        self.category.price_to_unlock = price_coins
        self.category.save()
        self.profile.balance = initial_balance
        self.profile.save()

        self.assertFalse(self.category.is_available_to_user(self.profile))
        self.assertEqual(initial_balance, self.profile.balance)

        url = reverse('category-unlock', kwargs={'category_pk': self.category.pk})

        # When
        response = self.client.post(url, **self.auth())
        self.assertEqual(402, response.status_code)

        # Then
        updated_profile = Profile.objects.get(pk=self.profile.pk)
        updated_category = QuoteCategory.objects.get(pk=self.category.pk)

        self.assertFalse(updated_category.is_available_to_user(updated_profile))
        self.assertEqual(initial_balance, updated_profile.balance)

    def test_shouldnt_500_if_performed_unlock_multiple_times_issue_16(self):
        # Given
        price_coins = 10
        initial_balance = 100

        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)
        self.category.is_payable = True
        self.category.price_to_unlock = price_coins
        self.category.save()
        self.profile.balance = initial_balance
        self.profile.save()

        self.assertFalse(self.category.is_available_to_user(self.profile))
        self.assertEqual(initial_balance, self.profile.balance)

        url = reverse('category-unlock', kwargs={'category_pk': self.category.pk})

        # When
        for i in range(0,10):
            response = self.client.post(url, **self.auth())
            self.assertEqual(200, response.status_code)
            self.assertTrue(self.category.is_available_to_user(self.profile))

        response = self.client.post(url, **self.auth())
        self.assertEqual(200, response.status_code)

        # Then
        self.assertTrue(self.category.is_available_to_user(self.profile))


class PurchaseStatusViewTest(AuthenticatedTestCase, ContentMixin):
    def test_present(self):
        # Given
        url = reverse('purchase-status-view', kwargs={'purchase_id': uuid.uuid4()})

        # When
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(404, response.status_code)

    def test_should_return_actual_status(self):
        # Given
        IAPPurchase = apps.get_model('api.GooglePlayIAPPurchase')
        Product = apps.get_model('api.GooglePlayProduct')

        product = Product.objects.create(sku='some-test-sku')

        purchase = IAPPurchase.objects.create(
            type=PurchaseTypes.PURCHASE,
            device_session=self.device_session,
            product=product,
            purchase_token='some-test-puchase-token-from-android',
            order_id='some-order-id-from-android'
        )

        url = reverse('purchase-status-view', kwargs={'purchase_id': purchase.id})

        # When
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(200, response.status_code)
        self.assertNotEqual(PurchaseStatus.PURCHASED, json.loads(response.content)['objects'][0]['status'])
        self.assertEqual(PurchaseStatus.DEFAULT, json.loads(response.content)['objects'][0]['status'])

    def test_should_return_actual_status2(self):
        # Given
        IAPPurchase = apps.get_model('api.GooglePlayIAPPurchase')
        Product = apps.get_model('api.GooglePlayProduct')

        product = Product.objects.create(sku='some-test-sku')

        purchase = IAPPurchase.objects.create(
            type=PurchaseTypes.PURCHASE,
            device_session=self.device_session,
            product=product,
            purchase_token='some-test-puchase-token-from-android',
            order_id='some-order-id-from-android'
        )

        purchase.status = PurchaseStatus.INVALID
        purchase.save()

        url = reverse('purchase-status-view', kwargs={'purchase_id': purchase.id})

        # When
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(200, response.status_code)
        self.assertEqual(PurchaseStatus.INVALID, json.loads(response.content)['objects'][0]['status'])

#
# class PurchaseCoinsViewTest(AuthenticatedTestCase, ContentMixin):
#     def test_present(self):
#         # Given
#         url = reverse('purchase-coins-view')
#
#         # When
#         response = self.client.post(url, **self.auth())
#
#         # Then
#         self.assertEqual(400, response.status_code)
#
#     def test_should_create_required_objects(self):
#         # Given
#         url = reverse('purchase-coins-view')
#
#         google_play_product = GooglePlayProduct.objects.create(sku='test sku')
#         app_product = BalanceRechargeProduct.objects.create(admin_title='10 монет',
#                                              balance_recharge=10,
#                                              google_play_product=google_play_product)
#
#
#
#         params = {
#             'balance_recharge': app_product.id,
#             'order_id': 'some-order-id-from-android',
#             'purchase_token': 'some-test-puchase-token-from-android'
#         }
#
#         # When
#         response = self.client.post(url, params, content_type='application/json', **self.auth())
#
#         # Then
#         self.assertEqual(200, response.status_code)
#         purchase_id = json.loads(response.content)['objects'][0]['purchase_id']
#         IAPPurchase = apps.get_model('api.GooglePlayIAPPurchase')
#
#         purchase = IAPPurchase.objects.get(id=purchase_id)
#         self.assertEqual('some-order-id-from-android', purchase.order_id)
#         self.assertEqual('some-test-puchase-token-from-android', purchase.purchase_token)
#
#
#     def test_should_avoid_dupes(self):
#         # Given
#         url = reverse('purchase-coins-view')
#
#         google_play_product = GooglePlayProduct.objects.create(sku='test sku')
#         app_product = BalanceRechargeProduct.objects.create(admin_title='10 монет',
#                                              balance_recharge=10,
#                                              google_play_product=google_play_product)
#
#
#
#         params = {
#             'balance_recharge': app_product.id,
#             'order_id': 'some-order-id-from-android',
#             'purchase_token': 'some-test-puchase-token-from-android'
#         }
#
#         response = self.client.post(url, params, content_type='application/json', **self.auth())
#         self.assertEqual(200, response.status_code)
#         purchase_id = json.loads(response.content)['objects'][0]['purchase_id']
#
#         # When
#         for i in range(3):
#             self.client.post(url, params, content_type='application/json', **self.auth())
#
#         # Then
#         IAPPurchase = apps.get_model('api.GooglePlayIAPPurchase')
#         self.assertEqual(1, IAPPurchase.objects.filter(product=google_play_product).count())
#

class PurchasePlayViewTest(AuthenticatedTestCase, ContentMixin):
    def test_present(self):
        # Given
        url = reverse('purchase-play-view')

        # When
        response = self.client.post(url, **self.auth())

        # Then
        self.assertEqual(400, response.status_code)

    def test_should_create_required_objects(self):
        # Given
        url = reverse('purchase-play-view')

        google_play_product = GooglePlayProduct.objects.create(sku='test sku')
        d = PurchaseProductDiscovery()

        app_product = d.product_types['balance_recharge'].objects.create(admin_title='10 монет',
                                             balance_recharge=10,
                                             google_play_product=google_play_product)



        # params = {
        #     'balance_recharge': app_product.id,
        #     'order_id': 'some-order-id-from-android',
        #     'purchase_token': 'some-test-puchase-token-from-android'
        # }


        params = {
            'order_id': 'some-order-id-from-android',
            'purchase_token': 'some-test-puchase-token-from-android',
            'app_product': str(d.get_product_id_by_product(app_product)),
            'payload': ''
        }

        # When
        response = self.client.post(url, params, content_type='application/json', **self.auth())

        # Then
        self.assertEqual(200, response.status_code)
        purchase_id = json.loads(response.content)['objects'][0]['purchase_id']
        IAPPurchase = apps.get_model('api.GooglePlayIAPPurchase')

        purchase = IAPPurchase.objects.get(id=purchase_id)
        self.assertEqual('some-order-id-from-android', purchase.order_id)
        self.assertEqual('some-test-puchase-token-from-android', purchase.purchase_token)

    def test_should_avoid_dupes(self):
        # Given
        url = reverse('purchase-play-view')

        google_play_product = GooglePlayProduct.objects.create(sku='test sku')
        d = PurchaseProductDiscovery()

        app_product = d.product_types['balance_recharge'].objects.create(admin_title='10 монет',
                                             balance_recharge=10,
                                             google_play_product=google_play_product)

        params = {
            'order_id': 'some-order-id-from-android',
            'purchase_token': 'some-test-puchase-token-from-android',
            'app_product': str(d.get_product_id_by_product(app_product)),
            'payload': ''
        }

        response = self.client.post(url, params, content_type='application/json', **self.auth())
        self.assertEqual(200, response.status_code)
        purchase_id = json.loads(response.content)['objects'][0]['purchase_id']

        # When
        for i in range(3):
            self.client.post(url, params, content_type='application/json', **self.auth())

        # Then
        IAPPurchase = apps.get_model('api.GooglePlayIAPPurchase')
        self.assertEqual(1, IAPPurchase.objects.filter(product=google_play_product).count())


@skip('obsolete')
class PurchaseUnlockViewTest(AuthenticatedTestCase, ContentMixin):
    def test_present(self):
        # Given
        url = reverse('purchase-unlock-view')

        # When
        response = self.client.post(url, **self.auth())

        # Then
        self.assertEqual(400, response.status_code)

    def test_should_create_required_objects(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        url = reverse('purchase-unlock-view')

        google_play_product = GooglePlayProduct.objects.create(sku='test sku')

        self.category.is_payable = True
        self.category.save()

        params = {
            'order_id': 'some-order-id-from-android',
            'purchase_token': 'some-test-puchase-token-from-android',
            'category_id': self.category.pk,
            'google_play_product_id': google_play_product.id
        }

        # When
        response = self.client.post(url, params, content_type='application/json', **self.auth())
        #
        # # Then
        self.assertEqual(200, response.status_code)
        purchase_id = json.loads(response.content)['objects'][0]['purchase_id']
        IAPPurchase = apps.get_model('api.GooglePlayIAPPurchase')

        purchase = IAPPurchase.objects.get(id=purchase_id)
        self.assertEqual('some-order-id-from-android', purchase.order_id)
        self.assertEqual('some-test-puchase-token-from-android', purchase.purchase_token)


    def test_should_avoid_dupes(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        url = reverse('purchase-unlock-view')

        google_play_product = GooglePlayProduct.objects.create(sku='test sku')

        self.category.is_payable = True
        self.category.save()

        params = {
            'order_id': 'some-order-id-from-android',
            'purchase_token': 'some-test-puchase-token-from-android',
            'category_id': self.category.pk,
            'google_play_product_id': google_play_product.id
        }

        # When
        response = self.client.post(url, params, content_type='application/json', **self.auth())
        self.assertEqual(200, response.status_code)
        purchase_id = json.loads(response.content)['objects'][0]['purchase_id']

        # When
        for i in range(3):
            self.client.post(url, params, content_type='application/json', **self.auth())

        # Then
        IAPPurchase = apps.get_model('api.GooglePlayIAPPurchase')
        self.assertEqual(1, IAPPurchase.objects.filter(product=google_play_product).count())


    def test_should_respond_422_if_category_is_free(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        url = reverse('purchase-unlock-view')

        google_play_product = GooglePlayProduct.objects.create(sku='test sku')

        params = {
            'order_id': 'some-order-id-from-android',
            'purchase_token': 'some-test-puchase-token-from-android',
            'category_id': self.category.pk,
            'google_play_product_id': google_play_product.id
        }

        # When
        response = self.client.post(url, params, content_type='application/json', **self.auth())
        #
        # # Then
        self.assertEqual(422, response.status_code)


    def test_should_respond_404_if_category_doesnt_exist(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        url = reverse('purchase-unlock-view')

        google_play_product = GooglePlayProduct.objects.create(sku='test sku')

        params = {
            'order_id': 'some-order-id-from-android',
            'purchase_token': 'some-test-puchase-token-from-android',
            'category_id': 9999,
            'google_play_product_id': google_play_product.id
        }

        # When
        response = self.client.post(url, params, content_type='application/json', **self.auth())
        #
        # # Then
        self.assertEqual(404, response.status_code)

    def test_should_respond_404_if_google_play_product_doesnt_exist(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        url = reverse('purchase-unlock-view')

        # google_play_product = GooglePlayProduct.objects.create(sku='test sku')

        params = {
            'order_id': 'some-order-id-from-android',
            'purchase_token': 'some-test-puchase-token-from-android',
            'category_id': 9999,
            'google_play_product_id': uuid.uuid4()
        }

        # When
        response = self.client.post(url, params, content_type='application/json', **self.auth())
        #
        # # Then
        self.assertEqual(404, response.status_code)


class PurchaseableProductsListViewTest(AuthenticatedTestCase, ContentMixin):
    def test_present(self):
        # Given
        url = reverse('purchase-products-list')

        # When
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(200, response.status_code)

    def test_should_return_relevant_info(self):
        # Given
        url = reverse('purchase-products-list')

        GooglePlayProduct = apps.get_model('api.GooglePlayProduct')
        generic_product1 = GooglePlayProduct.objects.create(sku='test sku1')
        generic_product2 = GooglePlayProduct.objects.create(sku='test sku2')
        generic_product3 = GooglePlayProduct.objects.create(sku='test sku3')

        recharge1 = BalanceRechargeProduct.objects.create(admin_title='10 coins',
                                              balance_recharge=10,
                                              google_play_product=GooglePlayProduct.objects.create(sku='test sku4'))

        recharge2 = BalanceRechargeProduct.objects.create(admin_title='1000 coins',
                                              balance_recharge=1000,
                                              google_play_product=GooglePlayProduct.objects.create(sku='test sku5'))


        # When
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(200, response.status_code)

        response_objects = json.loads(response.content)['objects'][0]
        recharge_products = response_objects['balance_recharge']
        # other_products = response_objects['other_products']

        self.assertEqual(2, len(recharge_products))
        # self.assertEqual(3, len(other_products))

        for o in recharge_products:
            fields = ('id', 'balance_recharge', 'admin_title', 'sku', 'is_featured', 'image_url')
            self.assertEqual(set(fields), set(o.keys()))

        # for o in other_products:
        #     fields = ('id', 'sku')
        #     self.assertEqual(set(fields), set(o.keys()))

        recharge2_item = list(filter(lambda o: o['balance_recharge'] == 1000, recharge_products))[0]
        self.assertEqual(recharge2_item['admin_title'], recharge2.admin_title)
        self.assertEqual(recharge2_item['sku'], recharge2.google_play_product.sku)


class AnyEndpointAccessUpdatesLastActive(AuthenticatedTestCase):
    FAKE_URL_PATH = '/some-fake-url/'

    def test_last_active_post_requests(self):
        # Given
        from django.test import RequestFactory
        factory = RequestFactory()

        post_request = factory.post(self.FAKE_URL_PATH, **self.auth(), content_type='application/json')

        view = BaseView()

        # When
        view.dispatch(post_request)
        last_active1 = Profile.objects.get(pk=self.profile.pk).last_active

        view.dispatch(post_request)
        last_active2 = Profile.objects.get(pk=self.profile.pk).last_active

        view.dispatch(post_request)
        last_active3 = Profile.objects.get(pk=self.profile.pk).last_active

        # Then
        self.assertNotEqual(last_active1, last_active2)
        self.assertNotEqual(last_active2, last_active3)
        self.assertNotEqual(last_active1, last_active3)

        self.assertTrue(last_active3 > last_active2)
        self.assertTrue(last_active2 > last_active1)


class ProfileViewTest(AuthenticatedTestCase):
    def test_should_return_profile_fields(self):
        # Given
        url = reverse('profile-view')

        expected_fields = ('id', 'balance', 'last_active', 'nickname', 'initial_profile_balance',
                           'reward_per_level_completion', 'reward_per_doubleup', 'top_position_change_since_last_update', 'is_banned')

        # When
        response = self.client.get(url, **self.auth())

        # Then
        profile_flat = json.loads(response.content)['objects'][0]
        self.assertEqual(set(profile_flat.keys()), set(expected_fields))

class ProfileUpdateViewTest(AuthenticatedTestCase):
    def test_should_update_username(self):
        # Given
        url = reverse('profile-update-view')
        profile = self.profile

        new_nickname = 'test nickname'

        # When
        params = {'nickname': new_nickname}
        response = self.client.post(url, params, content_type='application/json', **self.auth())

        # Then
        self.assertEqual(200, response.status_code)
        self.assertEqual(new_nickname, Profile.objects.get(pk=self.profile.pk).nickname)


class TopicDetailViewTest(AuthenticatedTestCase, ContentMixin):
    def test_should_present(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        url = reverse('topic-detail', kwargs={'pk': self.topic.pk})

        response = self.client.get(url, **self.auth())

        self.assertEqual(200, response.status_code)
        response_objs = json.loads(response.content)['objects']

        expected_fields = ['id', 'title', 'bonus_reward', 'on_complete_achievement', 'sections', 'order']
        self.assertEqual(set(expected_fields), set(response_objs[0].keys()))

        # objects = json.loads(response.content)['objects']

    def test_shouldnt_show_hidden_topic(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)
        self.topic.is_published = False
        self.topic.save()
        url = reverse('topic-detail', kwargs={'pk': self.topic.pk})

        response = self.client.get(url, **self.auth())

        self.assertEqual(404, response.status_code)


class TopicListView(AuthenticatedTestCase, ContentMixin):
    def test_should_present(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        url = reverse('topic-list')

        response = self.client.get(url, **self.auth())

        self.assertEqual(200, response.status_code)
        response_objs = json.loads(response.content)['objects']

        self.assertEqual(1, len(response_objs))
        expected_fields = ['id', 'title', 'bonus_reward', 'on_complete_achievement', 'uri', 'order']
        self.assertEqual(set(expected_fields), set(response_objs[0].keys()))


class LevelCompleteView(AuthenticatedTestCase, ContentMixin):
    def test_should_present(self):
        # Given
        self._create_content_hierarchy()
        self._create_multiple_quotes(category=self.category, author=self.author)

        quote_ut = self.quotes[1]

        url = reverse('level-complete', kwargs={'level_pk': quote_ut.pk })

        response = self.client.post(url, **self.auth())

        self.assertEqual(200, response.status_code)
        response_objs = json.loads(response.content)['objects']

        complete_levels = get_levels_complete_by_profile_in_category(self.profile.pk, self.category.pk)
        self.assertEqual(quote_ut.pk, complete_levels[0].pk)


class PushNotificationSubscriptionView(AuthenticatedTestCase, ContentMixin):
    def test_should_present(self):
        # Given
        url = reverse('notifications-subscribe')

        # When
        register_token = 'some-registration-token-of-the-device'
        params = {'token': register_token}
        response = self.client.post(url, params, content_type='application/json', **self.auth())
        pushsub = PushSubscription.objects.get()

        # Then
        self.assertEqual(200, response.status_code)
        self.assertEqual(pushsub.token, register_token)


class ProfileRankSimpleTest(AuthenticatedTestCase, ContentMixin):

    def test_should_present(self):
        self.profile.get_flat()
