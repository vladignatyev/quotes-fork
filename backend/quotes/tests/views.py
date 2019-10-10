import json

from django.urls import reverse
from django.test import TestCase
from django.utils import timezone

from api.models import *

from ..models import *

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
                      'reward',
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
        unlocked, _ = Profile.objects.unlock_category(self.profile, self.category)
        self.assertTrue(unlocked)

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
                                              category_to_unlock=self.category,
                                              google_play_purchase=purchase)

        purchase.status = PurchaseStatus.VALID  # manually 'validated' purchase
        purchase.save()


        url = reverse('levels-list', kwargs={'category_pk': self.category.pk})
        response = self.client.get(url, **self.auth())

        # Then
        self.assertEqual(200, response.status_code)


class ViewTest(AuthenticatedTestCase):


#     path('topic/<int:pk>/', TopicDetail.as_view(), name='topic-detail'),
#     path('topic/list/', TopicList.as_view(), name='topic-list'),
#
#     path('levels/category/<int:category_pk>/', LevelsList.as_view(), name='levels-list'),
#
#     path('level/<int:level_pk>/complete', LevelCompleteView.as_view(), name='level-complete'),
#
#     path('profile/', ProfileView.as_view(), name='profile-view'),
#
#     path('achievements/', AchievementList.as_view(), name='achievements-list'),
#     path('achievements/all/', AllAchievementList.as_view(), name='achievements-list-all'),
# ]
    def test_profile_view_present(self):
        # Given
        url = reverse('profile-view')
        # When
        response = self.client.get(url, **self.auth())
        # Then
        self.assertEqual(200, response.status_code)

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
