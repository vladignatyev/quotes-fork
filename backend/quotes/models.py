import uuid

import re
import logging
logger = logging.getLogger(__name__)


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
from .storage import get_profiles_storage


from functools import lru_cache



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
        levels_complete, levels_total = get_progress_profile_in_topic(profile.pk, self.pk)
        return levels_complete == levels_total

    def handle_complete(self, profile):
        return super(Topic, self).handle_complete(profile)


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
        levels_complete, levels_total = get_progress_profile_in_section(profile.pk, self.pk)
        return levels_complete == levels_total

    def handle_complete(self, profile):
        user_events = super(Section, self).handle_complete(profile)
        if self.topic.is_completion_condition_met_by(profile) == True:
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
    category = QuoteCategory.objects.get(pk=category_pk)
    if not category.is_available_to_user(profile):
        return None

    levels = get_all_levels_in_category(category_pk)
    complete_levels = get_levels_complete_by_profile_in_category(profile.pk, category_pk)

    result = []
    for item in levels:
        flat_item = {
            'id': item.id,
            'text': item.text,
            'author': item.author.name,
            'reward': item.get_reward(profile),
            'order': item.order_in_category,
            'complete': item in complete_levels,
            'splitted': quote_split(item.text)
        }
        result += [flat_item]
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
        try:
            unlock = get_unlock_for_category_and_profile(self.pk, profile.pk)
        except CategoryUnlockPurchase.DoesNotExist:
            return False

        return self.is_unlocked_for_cash(profile, unlock) or self.is_unlocked_for_coins(profile, unlock)

    def is_unlocked_for_coins(self, profile, unlock):
        if not unlock.google_play_purchase:
            return True

    def is_unlocked_for_cash(self, profile, unlock):
        if unlock.google_play_purchase:
            return unlock.google_play_purchase.status == PurchaseStatus.VALID

    def is_completion_condition_met_by(self, profile):
        levels_complete, levels_total = self.get_progress(profile)
        logger.debug('Category id %s complete condition: (%s, %s)', self.pk, levels_complete, levels_total)

        return levels_complete == levels_total

    def get_progress(self, profile):
        return get_progress_profile_in_category(profile.pk, self.pk)

    def handle_complete(self, profile):
        user_events = super(QuoteCategory, self).handle_complete(profile)

        logger.debug('Category id %s complete by profile %s', self.pk, profile.pk)

        if self.section.is_completion_condition_met_by(profile) == True:
            user_events += self.section.handle_complete(profile)
        return user_events





def get_progress_profile_in_topic(profile_pk, topic_pk):
    levels_complete = get_profiles_storage().get_levels_complete_by_profile_in_topic_count(profile_pk, topic_pk)
    levels_total = get_all_levels_in_topic_count(topic_pk)
    return (levels_complete, levels_total)

def get_progress_profile_in_section(profile_pk, section_pk):
    levels_complete = get_profiles_storage().get_levels_complete_by_profile_in_section_count(profile_pk, section_pk)
    levels_total = get_all_levels_in_section_count(section_pk)
    return (levels_complete, levels_total)

def get_progress_profile_in_category(profile_pk, category_pk):
    levels_complete = len(get_profiles_storage().get_levels_complete_by_profile_in_category(profile_pk, category_pk))
    levels_total = get_all_levels_in_category_count(category_pk)
    return (levels_complete, levels_total)


def get_levels_complete_by_profile_in_category(profile_pk, category_pk):
    return get_profiles_storage().get_levels_complete_by_profile_in_category(profile_pk, category_pk)


def clean_profile_progress_cache(profile):
    get_profiles_storage().clear_bucket(profile.pk)


# Content cacheable
@lru_cache(maxsize=2**16)
def get_all_levels_in_category(category_pk):
    return list(Quote.objects.filter(category=category_pk).all())

@lru_cache(maxsize=2**16)
def get_all_levels_in_category_count(category_pk):
    return Quote.objects.filter(category=category_pk).count()

@lru_cache(maxsize=2**16)
def get_all_levels_in_section_count(section_pk):
    return Quote.objects.filter(category__section=section_pk).count()

@lru_cache(maxsize=2**16)
def get_all_levels_in_topic_count(topic_pk):
    return Quote.objects.filter(category__section__topic=topic_pk).count()
# --


def clean_content_cache(*args, **kwargs):
    # logger.debug('Content Cache report:')
    # logger.debug('\tget_all_levels_in_category: %s', get_all_levels_in_category.cache_info())
    # logger.debug('\tget_all_levels_in_category_count: %s', get_all_levels_in_category_count.cache_info())
    # logger.debug('\tget_all_levels_in_section_count: %s', get_all_levels_in_section_count.cache_info())
    # logger.debug('\tget_all_levels_in_topic_count: %s', get_all_levels_in_topic_count.cache_info())

    get_all_levels_in_category.cache_clear()
    get_all_levels_in_category_count.cache_clear()
    get_all_levels_in_section_count.cache_clear()
    get_all_levels_in_topic_count.cache_clear()



@lru_cache(maxsize=2 ** 16)
def get_unlock_for_category_and_profile(category_pk, profile_pk):
    return CategoryUnlockPurchase.objects.get(category_to_unlock=category_pk,
                                              profile=profile_pk)

def clean_unlock_cache(*args, **kwargs):
    logger.debug('`get_unlock_for_category_and_profile` %s', get_unlock_for_category_and_profile.cache_info())

    get_unlock_for_category_and_profile.cache_clear()

class Quote(RewardableEntity):
    text = models.CharField("Текст цитаты", max_length=256)
    author = models.ForeignKey(QuoteAuthor, on_delete=models.SET_NULL, null=True)
    category = models.ForeignKey(QuoteCategory, on_delete=models.SET_NULL, null=True)

    order_in_category = models.BigIntegerField('Порядковый номер уровня в категории', default=0, blank=True)

    def get_absolute_url(self):
        return reverse('quote-preview', args=[self.id])

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

    def get_splitted(self):
        return quote_split(self.text)

    def get_reward(self, profile):
        return profile.settings.reward_per_level_completion

    def is_completion_condition_met_by(self, profile):
        return True

    def mark_complete(self, profile, solution=None):
        if not self.category.is_available_to_user(profile):
            raise self.NoAccess('The user have the category locked and he cannot complete this level.')

        logger.debug('Level id %s complete by profile %s', self.pk, profile.pk)

        user_events = self.handle_complete(profile)

        clean_profile_progress_cache(profile=profile)

        if self.category.is_completion_condition_met_by(profile) == True:
            user_events += self.category.handle_complete(profile)

        return user_events


class BalanceRechargeProduct(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    admin_title = models.CharField("Название", max_length=256)
    balance_recharge = models.IntegerField("Сумма пополнения баланса", default=1)

    google_play_product = models.ForeignKey('api.GooglePlayProduct', on_delete=models.SET_NULL, null=True, blank=True)
    # app_store_product = models.ForeignKey('api.AppStoreProduct', on_delete=models.SET_NULL, null=True, blank=True)

    objects = ProductManager()

    class Meta:
        verbose_name = 'IAP продукт'
        verbose_name_plural = 'продукты'

    def get_flat(self):
        flat = model_to_dict(self, fields=('id', 'admin_title', 'balance_recharge'))
        try:
            flat['sku'] = self.google_play_product.sku
        except ValueError:
            flat['sku'] = ''
        flat['id'] = self.id
        return flat


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


class CategoryUnlockTypes:
    NULL_UNLOCK = 'null_unlock'
    UNLOCK_FOR_COINS = 'unlock_for_coins'
    UNLOCK_BY_PURCHASE = 'unlock_by_purchase'

    choices = [
        ('null_unlock', '!! не использовать !!'),
        ('unlock_for_coins', 'Анлок за игровые монеты'),
        ('unlock_by_purchase', 'Анлок за покупку'),
    ]


class CategoryUnlockPurchase(models.Model):
    class InvalidPurchaseStatus(Exception):
        pass
    class InsufficientFunds(Exception):
        pass
    class AlreadyAvailable(Exception):
        pass

    type = models.CharField(blank=False, max_length=32, choices=CategoryUnlockTypes.choices, default=CategoryUnlockTypes.NULL_UNLOCK)
    profile = models.ForeignKey(Profile, on_delete=models.CASCADE)
    category_to_unlock = models.ForeignKey(QuoteCategory, on_delete=models.CASCADE)

    google_play_purchase = models.ForeignKey('api.GooglePlayIAPPurchase', on_delete=models.SET_NULL, null=True, blank=True)

    date_created = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = 'Покупка доступа к категории'
        verbose_name_plural = 'Покупки доступов к категориям'

    def do_unlock(self):
        if self.category_to_unlock.is_payable == False:
            raise self.AlreadyAvailable()

        if self.type == CategoryUnlockTypes.NULL_UNLOCK:
            raise ValueError('CategoryUnlockPurchase should be one of type specified in CategoryUnlockTypes, except NULL_UNLOCK')

        elif self.type == CategoryUnlockTypes.UNLOCK_FOR_COINS:

            new_balance = self.profile.balance - self.category_to_unlock.price_to_unlock
            if new_balance < 0:
                raise self.InsufficientFunds()

            self.category_to_unlock.available_to_users.add(self.profile)

            self.profile.balance = new_balance
            self.profile.save()
            self.save()
            logger.debug('Profile unlocking category: %s %s', self.profile, self.category_to_unlock.pk)

        elif self.type == CategoryUnlockTypes.UNLOCK_BY_PURCHASE:
            if self.google_play_purchase.status == PurchaseStatus.VALID:
                self.category_to_unlock.available_to_users.add(self.profile)
                self.save()
            else:
                raise self.InvalidPurchaseStatus('Tried to unlock category, but the status of IAP purchase was invalid.')
