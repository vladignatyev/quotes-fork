import json

from django.conf import settings
from django.test import TestCase
from django.apps import apps


from ..models import *
from ..models import _truncate

from api.models import *


from .common import GameBalanceMixin, ContentMixin, TimeAssert



class ProfileTest(GameBalanceMixin, TimeAssert):
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

    def test_profile_last_active(self):
        # Given
        from datetime import datetime
        now1 = datetime.now()

        self.assertEqual(0, len(Profile.objects.all()))
        session = DeviceSession.objects.create()

        # When
        session.save()
        profile = Profile.objects.get_by_session(session)
        profile_last_active = profile.last_active
        profile.balance = 1000
        profile.save()

        now2 = datetime.now()
        # Then
        self.assertTime(profile_last_active, now1)
        self.assertTime(profile.last_active, now2)


    def test_profile_last_active2(self):
        # Given
        from datetime import datetime
        import time

        self.assertEqual(0, len(Profile.objects.all()))
        session = DeviceSession.objects.create()

        # When
        session.save()
        profile = Profile.objects.get_by_session(session)
        now1 = datetime.now()
        profile_last_active1 = profile.last_active
        profile.save()

        time.sleep(0.1)

        now2 = datetime.now()
        profile.save()
        profile_last_active2 = Profile.objects.get(pk=profile.pk).last_active

        time.sleep(0.1)

        now3 = datetime.now()
        profile.save()
        profile_last_active3 = Profile.objects.get(pk=profile.pk).last_active

        # Then
        self.assertTime(profile_last_active1, now1)
        self.assertTime(profile_last_active2, now2)
        self.assertTime(profile_last_active3, now3)

        self.assertTime(now2, now3, f=self.assertFalse)
        self.assertTime(now1, now2, f=self.assertFalse)


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
        game_settings = GameBalance.objects.get_actual()

        # Then
        self.assertEqual(30, game_settings.initial_profile_balance)


class ProfileCategoryUnlocking(GameBalanceMixin):
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


class ProfileCategoryPurchaseUnlock(GameBalanceMixin):
    def test_should_unlock_after_purchase_validated(self):
        # Given
        PRICE_TO_UNLOCK = 10E8 # ANY

        session = DeviceSession.objects.create()
        session.save()

        profile = Profile.objects.get_by_session(device_session=session)

        category = QuoteCategory.objects.create(title='Тестовая платная категория',
                                                is_payable=True,
                                                price_to_unlock=PRICE_TO_UNLOCK)


        google_play_product = GooglePlayProduct.objects.create()
        google_play_purchase = GooglePlayIAPPurchase.objects.create(product=google_play_product,
                                                                    device_session=session)
        google_play_purchase.save()

        purchase = CategoryUnlockPurchase.objects.create(profile=profile,
                                              type=CategoryUnlockTypes.UNLOCK_BY_PURCHASE,
                                              category_to_unlock=category,
                                              google_play_purchase=google_play_purchase)
        purchase.save()

        # When
        google_play_purchase.status = PurchaseStatus.VALID  # manually 'validated' purchase
        google_play_purchase.save()

        # Then
        self.assertIn(profile, category.available_to_users.all())


class CategoryModelUnlock(GameBalanceMixin):
    def test_should_unlock_locked(self):
        # Given
        PRICE_TO_UNLOCK = 10

        session = DeviceSession.objects.create()
        session.save()

        profile = Profile.objects.get_by_session(device_session=session)
        self.assertTrue(profile.balance > PRICE_TO_UNLOCK)

        category = QuoteCategory.objects.create(title='Тестовая платная категория',
                                                is_payable=True,
                                                price_to_unlock=PRICE_TO_UNLOCK)

        unlock = CategoryUnlockPurchase.objects.create(profile=profile,
                                              type=CategoryUnlockTypes.UNLOCK_FOR_COINS,
                                              category_to_unlock=category)

        # When
        unlock.do_unlock()

        # Then
        self.assertIn(profile, category.available_to_users.all())

    def test_should_raise_insufficient_funds_exception(self):
        # Given
        PRICE_TO_UNLOCK = 10E8 # huge amount

        session = DeviceSession.objects.create()
        session.save()

        profile = Profile.objects.get_by_session(device_session=session)

        category = QuoteCategory.objects.create(title='Тестовая платная категория',
                                                is_payable=True,
                                                price_to_unlock=PRICE_TO_UNLOCK)

        unlock = CategoryUnlockPurchase.objects.create(profile=profile,
                                              type=CategoryUnlockTypes.UNLOCK_FOR_COINS,
                                              category_to_unlock=category)

        # When
        with self.assertRaises(CategoryUnlockPurchase.InsufficientFunds):
            unlock.do_unlock()


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

    def test_split_should_merge_multiple_spaces(self):
        self.assertEqual(['а', 'что', 'если?'], quote_split('а что  если?'))

    def test_split_should_merge_space_followed_by_special_char(self):
        self.assertEqual(['а', 'что', 'если?'], quote_split('а что  если ?'))

    def test_split_should_avoid_empty_chunks(self):
        self.assertEqual(['а', 'что', 'если'], quote_split('а что  если '))

    def test_beautiful_text_should_capitalize_first(self):
        q1 = 'and what is the use of a book without pictures or conversations?'
        q2 = 'а что если?'

        self.assertEqual('And what is the use of a book without pictures or conversations?',
                         beautiful_text(q1))

        self.assertEqual('А что если?',
                         beautiful_text(q2))

    def test_beautiful_text_should_remove_multiple_spaces(self):
        q1 = 'and what is the   use of a book without pictures or conversations?'
        q2 = 'а что  если ?'

        self.assertEqual('And what is the use of a book without pictures or conversations?',
                         beautiful_text(q1))

        self.assertEqual('А что если?',
                         beautiful_text(q2))




class LevelProgressTestCase(TestCase, ContentMixin):
    def setUp(self):
        super(LevelProgressTestCase, self).setUp()
        self._create_content_hierarchy()
        self._create_multiple_quotes()

        session = DeviceSession.objects.create()
        session.save()

        self.profile = Profile.objects.get(device_sessions__pk__contains=session)

    def test_marking_user_complete_will_update_balance(self):
        # Given
        self.assertEqual(0, len(list(Quote.objects.filter(complete_by_users=self.profile))))

        # When
        self.quotes[0].mark_complete(self.profile)
        self.quotes[5].mark_complete(self.profile)

        # Then
        complete = Quote.objects.filter(complete_by_users=self.profile)
        self.assertIn(self.quotes[0], complete)
        self.assertIn(self.quotes[5], complete)
        self.assertNotIn(self.quotes[1], complete)
        self.assertNotIn(self.quotes[2], complete)
        self.assertNotIn(self.quotes[3], complete)

    def test_getter_for_levels_complete(self):
        # Given

        # When
        self.quotes[3].mark_complete(self.profile)

        # Then
        complete = get_levels_complete_by_profile_in_category(self.profile, self.category)
        self.assertIn(self.quotes[3], complete)

        self.assertNotIn(self.quotes[0], complete)
        self.assertNotIn(self.quotes[1], complete)
        self.assertNotIn(self.quotes[2], complete)
        self.assertNotIn(self.quotes[5], complete)

    def test_complete_should_reward_user(self):
        # Given
        game_settings = GameBalance.objects.get_actual()

        expected_reward = game_settings.reward_per_level_completion
        profile_balance = self.profile.balance

        # When
        events = self.quotes[0].mark_complete(self.profile)
        profile_updated = Profile.objects.get(pk=self.profile.pk)

        # Then
        self.assertEqual(profile_balance + expected_reward, profile_updated.balance)

        self.assertIn((UserEvents.RECEIVED_PER_LEVEL_REWARD, expected_reward), events)

    def test_complex_case(self):
        # Given
        category_achievement = Achievement.objects.create(
            icon='test',
            title='category test achievement',
        )
        self.category.bonus_reward = 10
        self.category.on_complete_achievement = category_achievement
        self.category.save()

        section_achievement = Achievement.objects.create(
            icon='test',
            title='section test achievement',
        )

        self.section.bonus_reward = 100
        self.section.on_complete_achievement = section_achievement
        self.section.save()

        topic_achievement = Achievement.objects.create(
            icon='test',
            title='topic test achievement',
        )
        self.topic.bonus_reward = 1000
        self.topic.on_complete_achievement = topic_achievement
        self.topic.save()

        # When
        events = []
        for i in range(0, len(self.quotes)):
            events += self.quotes[i].mark_complete(self.profile)

        # Then
        self.assertIn((UserEvents.CATEGORY_COMPLETE, self.category.pk), events)
        self.assertIn((UserEvents.SECTION_COMPLETE, self.section.pk), events)
        self.assertIn((UserEvents.TOPIC_COMPLETE, self.topic.pk), events)

        self.assertIn((UserEvents.RECEIVED_PER_TOPIC_REWARD, 1000), events)
        self.assertIn((UserEvents.RECEIVED_PER_SECTION_REWARD, 100), events)
        self.assertIn((UserEvents.RECEIVED_PER_CATEGORY_REWARD, 10), events)

        level_rewards = list(UserEvents.filter_by_name(UserEvents.RECEIVED_PER_LEVEL_REWARD, events))
        self.assertEqual(7, len(level_rewards))
        self.assertEqual(35, sum(list(zip(*level_rewards))[1]))

        section_rewards = list(UserEvents.filter_by_name(UserEvents.RECEIVED_PER_SECTION_REWARD, events))
        self.assertEqual(1, len(section_rewards))
        self.assertEqual(100, sum(list(zip(*section_rewards))[1]))

        topic_rewards = list(UserEvents.filter_by_name(UserEvents.RECEIVED_PER_TOPIC_REWARD, events))
        self.assertEqual(1, len(topic_rewards))
        self.assertEqual(1000, sum(list(zip(*topic_rewards))[1]))

        topic_achievements = list(UserEvents.filter_by_name(UserEvents.RECEIVED_TOPIC_ACHIEVEMENT, events))
        self.assertEqual(1, len(topic_achievements))
        achievement_pk = topic_achievements[0][1]
        self.assertEqual(achievement_pk, topic_achievement.pk)

        section_achievements = list(UserEvents.filter_by_name(UserEvents.RECEIVED_SECTION_ACHIEVEMENT, events))
        self.assertEqual(1, len(section_achievements))
        achievement_pk = section_achievements[0][1]
        self.assertEqual(achievement_pk, section_achievement.pk)

        category_achievements = list(UserEvents.filter_by_name(UserEvents.RECEIVED_CATEGORY_ACHIEVEMENT, events))
        self.assertEqual(1, len(category_achievements))
        achievement_pk = category_achievements[0][1]
        self.assertEqual(achievement_pk, category_achievement.pk)


    def test_complex_case_multiple_categories(self):
        # Given

        section2 = Section.objects.create(title='Test section #2',
                                         topic=self.topic)

        category2 = QuoteCategory.objects.create(
            section=section2,
            title='Test category2',
            is_payable=False
        )

        category3 = QuoteCategory.objects.create(
            section=section2,
            title='Test category3',
            is_payable=False
        )

        quotes2 = self._create_multiple_quotes(category=category2)
        quotes3 = self._create_multiple_quotes(category=category3)

        # When
        events = []
        for i in range(0,len(quotes2)):
            events += quotes2[i].mark_complete(self.profile)

        for i in range(0, len(quotes3)):
            events += quotes3[i].mark_complete(self.profile)

        self.assertIn((UserEvents.CATEGORY_COMPLETE, category2.pk), events)
        self.assertIn((UserEvents.CATEGORY_COMPLETE, category3.pk), events)
        self.assertIn((UserEvents.SECTION_COMPLETE, section2.pk), events)
        self.assertNotIn((UserEvents.TOPIC_COMPLETE, self.topic.pk), events)
