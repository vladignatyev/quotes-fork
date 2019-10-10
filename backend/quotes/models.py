import re

from django.utils import timezone

from django.db import models

from django.conf import settings
from django.db.models.signals import post_save

from django.forms.models import model_to_dict
from django.urls import reverse

from api.models import DeviceSession, PurchaseStatus, GooglePlayProduct, AppStoreProduct

from .events import UserEvents
from .utils import _truncate
from .rewardable import RewardableEntity

from .managers import *


class QuoteAuthor(models.Model):
    name = models.CharField("Автор цитаты", max_length=256)

    def __str__(self):
        return _truncate(self.name)

    class Meta:
        verbose_name = 'автор цитат'
        verbose_name_plural = 'авторы цитат'


class Achievement(models.Model):
    icon = models.CharField("Имя иконки в приложении", max_length=256)
    title = models.CharField(max_length=256)
    received_text = models.TextField(default='')
    description_text = models.TextField(default='')

    opened_by_users = models.ManyToManyField('Profile', verbose_name='Профили пользователей которые открыли ачивку', blank=True, through='AchievementReceiving')

    def __str__(self):
        return _truncate(f'{self.title}')

    class Meta:
        verbose_name = 'достижение'
        verbose_name_plural = 'достижения'


class AchievementReceiving(models.Model):
    achievement = models.ForeignKey('Achievement', on_delete=models.CASCADE)
    profile = models.ForeignKey('Profile', on_delete=models.CASCADE)
    received_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'достижения vs юзеры'
        verbose_name_plural = 'достижения vs юзеры'

    def __str__(self):
        return f'{self.profile} - {self.achievement}'


class Topic(RewardableEntity):
    title = models.CharField("Название Темы", max_length=256)
    hidden = models.BooleanField(default=False)

    def __str__(self):
        return _truncate(f'{self.title}')

    class Meta:
        verbose_name = 'тема'
        verbose_name_plural = 'темы'

    objects = TopicManager()

    complete_event_name = UserEvents.TOPIC_COMPLETE
    achievement_event_name = UserEvents.RECEIVED_TOPIC_ACHIEVEMENT
    reward_event_name = UserEvents.RECEIVED_PER_TOPIC_REWARD

    def is_completion_condition_met_by(self, profile):
        pass # todo


class Section(RewardableEntity):
    title = models.CharField("Название Раздела", max_length=256)
    topic = models.ForeignKey(Topic, on_delete=models.CASCADE)

    def __str__(self):
        return _truncate(f'{self.title}')

    class Meta:
        verbose_name = 'раздел'
        verbose_name_plural = 'разделы'

    complete_event_name = UserEvents.SECTION_COMPLETE
    achievement_event_name = UserEvents.RECEIVED_SECTION_ACHIEVEMENT
    reward_event_name = UserEvents.RECEIVED_PER_SECTION_REWARD

    def is_completion_condition_met_by(self, profile):
        pass # todo

    def handle_complete(self, profile):
        user_events = super(Section, self).handle_complete(profile)
        if self.topic.is_completion_condition_met_by(profile):
            user_events += self.topic.handle_complete(profile)
        return user_events



def quote_split(quote_item_text,
                special_case_re=r'\s*\^\s*',
                normal_case_re=r'\s+'):
    if '^' in quote_item_text:
        case = special_case_re
    else:
        case = normal_case_re
    return list(filter(None, re.split(case, quote_item_text)))


def get_levels(category_pk, profile):
    try:
        category = QuoteCategory.objects.get(pk=category_pk)
        levels = Quote.objects.filter(category=category)
        complete_levels = get_levels_complete_by_profile(profile, category)
    except QuoteCategory.DoesNotExist:
        return False

    if category.is_unlocked_by(profile) or not category.is_payable:
        result = []
        for item in levels:
            result += [{
                'id': item.id,
                'text': item.text,
                'author': item.author.name,
                'category_complete_reward': item.category.bonus_reward,
                'order': item.order_in_category,
                'complete': item in complete_levels,
                'splitted': quote_split(item.text)
            }]
        return result


class QuoteCategory(RewardableEntity):
    section = models.ForeignKey(Section, on_delete=models.CASCADE, default=None, null=True)
    title = models.CharField("Название Категории", max_length=256)
    icon = models.CharField(max_length=256, default='', blank=True)

    # Events
    is_event = models.BooleanField(default=False)
    event_due_date = models.DateTimeField(default=timezone.now, blank=True)
    event_title = models.CharField(max_length=256, default='', blank=True)
    event_icon = models.CharField(max_length=256, default='', blank=True)
    event_description = models.TextField(default='', blank=True)
    event_win_achievement = models.ForeignKey(Achievement, on_delete=models.SET_NULL, null=True, blank=True,
                                              related_name='on_event_complete_achievement')

    # Locked category
    is_payable = models.BooleanField('Категория платная?', default=False)

    price_to_unlock = models.BigIntegerField('Стоимость открытия категории если она платная', default=0, blank=True)

    available_to_users = models.ManyToManyField('Profile',
                                                verbose_name='Профили пользователей которым доступна категория',
                                                blank=True,
                                                through='CategoryUnlockPurchase',
                                                related_name='availability_to_profile')

    def __str__(self):
        return _truncate(f'{self.section.topic.title} > {self.section.title} > {self.title}')

    class Meta:
        verbose_name = 'категория'
        verbose_name_plural = 'категории'

    complete_event_name = UserEvents.CATEGORY_COMPLETE
    achievement_event_name = UserEvents.RECEIVED_CATEGORY_ACHIEVEMENT
    reward_event_name = UserEvents.RECEIVED_PER_CATEGORY_REWARD


    def is_available_to_user(self, profile):
        return not self.is_payable or self.is_unlocked_by(profile)

    def is_unlocked_by(self, profile):
        return self.is_unlocked_for_cash(profile) or self.is_unlocked_for_coins(profile)

    def is_unlocked_for_coins(self, profile):
        try:
            CategoryUnlockPurchase.objects.get(category_to_unlock=self,
                                               profile=profile,
                                               google_play_purchase__isnull=True)
            return True
        except CategoryUnlockPurchase.DoesNotExist:
            return False

    def is_unlocked_for_cash(self, profile):
        try:
            cup = CategoryUnlockPurchase.objects.get(category_to_unlock=self,
                                                     profile=profile,
                                                     google_play_purchase__isnull=False)
            return cup.google_play_purchase.status == PurchaseStatus.VALID
        except CategoryUnlockPurchase.DoesNotExist:
            return False

    def is_completion_condition_met_by(self, profile):
        pass # todo

    def handle_complete(self, profile):
        user_events = super(QuoteCategory, self).handle_complete(profile)
        if self.section.is_completion_condition_met_by(profile):
            user_events += self.section.handle_complete(profile)
        return user_events


def get_levels_complete_by_profile(profile, category=None):
    if category is None:
        return Quote.objects.filter(complete_by_users=profile)
    else:
        return Quote.objects.filter(complete_by_users=profile,
                                    category=category)


class Quote(RewardableEntity):
    text = models.CharField("Текст цитаты", max_length=256)
    author = models.ForeignKey(QuoteAuthor, on_delete=models.SET_NULL, null=True)
    category = models.ForeignKey(QuoteCategory, on_delete=models.SET_NULL, null=True)

    order_in_category = models.BigIntegerField('Порядковый номер уровня в категории', default=0, blank=True)

    def __str__(self):
        return _truncate(self.text)

    class Meta:
        verbose_name = 'цитата'
        verbose_name_plural = 'цитаты'

    complete_event_name = UserEvents.LEVEL_COMPLETE
    achievement_event_name = UserEvents.RECEIVED_LEVEL_ACHIEVEMENT
    reward_event_name = UserEvents.RECEIVED_PER_LEVEL_REWARD

    class NoAccess(Exception):
        pass

    def get_reward(self, profile):
        return profile.settings.reward_per_level_completion

    def is_completion_condition_met_by(self, profile):
        return True

    def mark_complete(self, profile, solution=None):
        if not self.category.is_available_to_user(profile):
            raise self.NoAccess('The user have the category locked and he cannot complete this level.')

        user_events = self.handle_complete(profile)

        if self.category.is_completion_condition_met_by(profile):
            user_events += self.category.handle_complete(profile)

        return user_events


class BalanceRechargeProduct(models.Model):
    admin_title = models.CharField("Название для админки", max_length=256)
    balance_recharge = models.IntegerField("Сумма пополнения баланса", default=1)

    google_play_product = models.ForeignKey('api.GooglePlayProduct', on_delete=models.SET_NULL, null=True, blank=True)
    # app_store_product = models.ForeignKey('api.AppStoreProduct', on_delete=models.SET_NULL, null=True, blank=True)

    objects = ProductManager()

    class Meta:
        verbose_name = 'IAP продукт'
        verbose_name_plural = 'продукты'


class GameBalance(models.Model):
    initial_profile_balance = models.BigIntegerField(default=0)
    reward_per_level_completion = models.BigIntegerField(default=5)
    reward_per_doubleup = models.BigIntegerField(default=5)

    objects = GameBalanceManager()

    class Meta:
        verbose_name = 'игровой баланс'
        verbose_name_plural = 'игровой баланс'

    def __str__(self):
        return f'{self.pk}) Монет в начале: {self.initial_profile_balance}, вознаграждение за прохождение левела {self.reward_per_level_completion}'


class Profile(models.Model):
    device_sessions = models.ManyToManyField('api.DeviceSession')
    balance = models.PositiveIntegerField(default=0)
    nickname = models.CharField(max_length=256, default='Пан Инкогнито')

    settings = models.ForeignKey('GameBalance', on_delete=models.CASCADE, null=True, default=None)

    objects = ProfileManager()

    class Meta:
        verbose_name = 'профиль пользователя'
        verbose_name_plural = 'профили пользователей'

    def __str__(self):
        return f'#{self.pk} ({self.nickname})'


class CategoryUnlockPurchase(models.Model):
    profile = models.ForeignKey(Profile, on_delete=models.CASCADE)
    category_to_unlock = models.ForeignKey(QuoteCategory, on_delete=models.CASCADE)

    google_play_purchase = models.ForeignKey('api.GooglePlayIAPPurchase', on_delete=models.SET_NULL, null=True, blank=True)

    date_created = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Покупка доступа к категории'
        verbose_name_plural = 'Покупки доступов к категориям'
