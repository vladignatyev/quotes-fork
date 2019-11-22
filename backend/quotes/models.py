import uuid

import re
import os
import logging
logger = logging.getLogger(__name__)

from functools import lru_cache

from django.conf import settings
from django.db import models
from django.db.models.signals import post_save
from django.db import transaction

from django.forms.models import model_to_dict
from django.urls import reverse

from django.utils import timezone
from django.utils.safestring import mark_safe


from api.models import DeviceSession, PurchaseStatus, GooglePlayProduct, AppStoreProduct

from .events import UserEvents
from .utils import _truncate
from .rewardable import RewardableEntity

from .managers import *
from .storage import get_profiles_storage

from .itemimage import ItemWithImageMixin

from quoterank.quoterank import handle_rank_update

from django.utils.html import escape



class QuoteAuthor(models.Model):
    name = models.CharField("Автор цитаты", max_length=256)

    def __str__(self):
        return _truncate(self.name)

    class Meta:
        verbose_name = 'автор цитат'
        verbose_name_plural = 'авторы цитат'


class Achievement(models.Model):
    icon = models.CharField("Имя иконки в приложении", max_length=256)
    title = models.CharField("Название", max_length=256)
    received_text = models.TextField("Текст для юзера при вручении ему достижения", default='')
    description_text = models.TextField("Описание достижения для юзера", default='')

    opened_by_users = models.ManyToManyField('Profile', verbose_name='Профили пользователей которые открыли ачивку', blank=True, through='AchievementReceiving')

    def __str__(self):
        return _truncate(f'{self.title}')

    class Meta:
        verbose_name = 'достижение'
        verbose_name_plural = 'достижения'


class AchievementReceiving(models.Model):
    achievement = models.ForeignKey('Achievement', verbose_name="Достижение", on_delete=models.CASCADE)
    profile = models.ForeignKey('Profile', verbose_name="Профиль", on_delete=models.CASCADE)
    received_at = models.DateTimeField("Когда получено достижение", auto_now_add=True)

    class Meta:
        verbose_name = 'достижения vs юзеры'
        verbose_name_plural = 'достижения vs юзеры'

    def __str__(self):
        return f'{self.profile} - {self.achievement}'


class Topic(RewardableEntity):
    title = models.CharField("Название Темы", max_length=256)

    # Publishing status
    is_published = models.BooleanField("Тема опубликована?", default=True, blank=True)
    order = models.PositiveIntegerField("Порядок сортировки: чем ниже значение, тем выше Тема в списке", default=1000, blank=True)
    tags = models.ManyToManyField('quotes.Tag', blank=True)

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

    def handle_complete(self, profile, save_profile=True):
        return super(Topic, self).handle_complete(profile, save_profile)

    # todo: enable per-profile caching of required data
    def get_flat(self, profile):
        '''
        Flattens hierahical models under Topic into lists,
        mark categories opened/payable depending on user payments,
        adds progress values to every category
        '''


        # cache_bucket = get_profiles_storage().get_bucket(profile.pk)
        # key = f'Topic.get_flat({profile.pk}, {self.pk})'
        # result = cache_bucket.get(key, None)
        # if result is not None:
        #     return result

        sections = Section.objects.filter(topic=self, is_published=True).order_by('order').select_related('on_complete_achievement').all()
        # categories = QuoteCategory.objects.filter(section__topic=self).all()
        categories = QuoteCategory.objects.filter(section__in=sections, is_published=True).select_related('on_complete_achievement').order_by('order').all()

        flat_topic = {
            'id': self.pk,
            'title': self.title,
            'bonus_reward': self.bonus_reward,
            'order': self.order,
            'on_complete_achievement': self.on_complete_achievement.id if self.on_complete_achievement else None,
            'sections': [section.get_flat() for section in sections]
        }

        for category in categories:
            for section in flat_topic['sections']:
                if category.section.id == section['id']:
                    section['categories'] = section.get('categories', [])

                    flat_category = category.get_flat()
                    flat_category['is_available_to_user'] = category.is_available_to_user(profile)

                    levels_complete, levels_total = category.get_progress(profile)

                    flat_category['progress_levels_total'] = levels_total
                    flat_category['progress_levels_complete'] = levels_complete

                    section['categories'] += [flat_category]

        # cache_bucket[key] = flat_topic
        return flat_topic


class Tag(models.Model):
    tag_value = models.CharField("Значение tag для клиента", max_length=256, default='')

    def __str__(self):
        return self.tag_value


class Section(RewardableEntity):
    title = models.CharField("Название Раздела", max_length=256)
    topic = models.ForeignKey(Topic, verbose_name="Тема", on_delete=models.CASCADE)

    # Publishing status
    is_published = models.BooleanField("Раздел (секция) опубликован?", default=True, blank=True)
    order = models.PositiveIntegerField("Порядок сортировки: чем ниже значение, тем выше Секция в Теме", default=1000, blank=True)

    tags = models.ManyToManyField('quotes.Tag', blank=True)

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

    def handle_complete(self, profile, save_profile=True):
        user_events = super(Section, self).handle_complete(profile, save_profile)
        if self.topic.is_completion_condition_met_by(profile) == True:
            user_events += self.topic.handle_complete(profile, save_profile)
        return user_events

    def get_flat(self):
        return {
            'id': self.pk,
            'title': self.title,
            'order': self.order,
            'tags': list(self.tags.all()),
            'bonus_reward': self.bonus_reward,
            'on_complete_achievement': self.on_complete_achievement.id if self.on_complete_achievement else None
        }


def quote_split(quote_item_text,
                special_case_re=r'\s*\^\s*',
                normal_case_re=r'\s+'):
    if '^' in quote_item_text:
        case = special_case_re
        prepared = quote_item_text
    else:
        case = normal_case_re
        merge_spaces = re.sub(r' +', ' ', quote_item_text)
        special_after_space = re.sub(r'\s+([?!])', '\g<1>', merge_spaces)

        prepared = special_after_space

    splitted_and_without_punctuation = [re.sub(r'[^\w\s]+', '', str) for str in re.split(case, prepared)]
    without_empty = filter(None, splitted_and_without_punctuation)
    lowercased = [str.lower() for str in without_empty]
    return lowercased


def get_levels(category_pk, profile):
    category = QuoteCategory.objects.select_related('author').get(pk=category_pk, is_published=True)

    if not category.is_available_to_user(profile):
        return None

    levels = get_all_levels_in_category(category_pk)
    complete_levels = get_levels_complete_by_profile_in_category(profile.pk, category_pk)

    result = []
    for item in levels:
        flat_item = {
            'id': item.id,
            'text': item.text,
            'author': item.author.name if item.author else None,
            'reward': item.get_reward(profile),
            'beautiful': beautiful_text(item.text),
            'order': item.order_in_category,
            'complete': item in complete_levels,
            'splitted': quote_split(item.text)
        }
        result += [flat_item]
    return result



class QuoteCategory(RewardableEntity, ItemWithImageMixin):
    section = models.ForeignKey(Section, verbose_name="Раздел", on_delete=models.CASCADE, default=None, null=True)
    title = models.CharField("Название Категории", max_length=256)
    icon = models.CharField("Название иконки", max_length=256, default='', blank=True)

    # Publishing status
    is_published = models.BooleanField("Категория опубликована?", default=True, blank=True)
    order = models.PositiveIntegerField("Порядок сортировки: чем ниже значение, тем выше Категория в Разделе", default=1000, blank=True)

    # Events
    is_event = models.BooleanField("Это событие?", default=False)
    event_due_date = models.DateTimeField("Дата окончания события", default=timezone.now, blank=True)
    event_title = models.CharField("Название события", max_length=256, default='', blank=True)
    event_icon = models.CharField("Иконка события", max_length=256, default='', blank=True)
    event_description = models.TextField("Описание события для юзера", default='', blank=True)
    event_win_achievement = models.ForeignKey(Achievement, verbose_name="Достижение, вручаемое при прохождении события", on_delete=models.SET_NULL, null=True, blank=True,
                                              related_name='on_event_complete_achievement')

    # Locked category
    is_payable = models.BooleanField('Категория платная?', default=False)

    price_to_unlock = models.BigIntegerField('Стоимость открытия категории если она платная', default=0, blank=True)

    available_to_users = models.ManyToManyField('Profile',
                                                verbose_name='Профили пользователей которым доступна категория',
                                                blank=True,
                                                through='CategoryUnlockPurchase',
                                                related_name='availability_to_profile')

    tags = models.ManyToManyField('quotes.Tag', blank=True)

    def __str__(self):
        return _truncate(f'{self.section.topic.title} > {self.section.title} > {self.title}')

    class Meta:
        verbose_name = 'категория'
        verbose_name_plural = 'категории'

    complete_event_name = UserEvents.CATEGORY_COMPLETE
    achievement_event_name = UserEvents.RECEIVED_CATEGORY_ACHIEVEMENT
    reward_event_name = UserEvents.RECEIVED_PER_CATEGORY_REWARD


    item_image = models.FileField('Картинка категории 512х512', upload_to='quotecategoryimages', null=True, blank=True)
    item_image_preview = models.FileField('Превью картинки категории 256х256', upload_to='quotecategoryimages-preview', null=True, blank=True)

    @mark_safe
    def item_image_view(self):
        return u'<img src="%s" />' % escape(self.get_image_url())
    item_image_view.short_description = 'Картинка категории'
    item_image_view.allow_tags = True

    @mark_safe
    def item_preview_image_view(self):
        return u'<img width="128" src="%s" />' % escape(self.get_image_url())
    item_preview_image_view.short_description = 'Картинка категории'
    item_preview_image_view.allow_tags = True


    def is_available_to_user(self, profile):
        return not self.is_payable or self.is_unlocked_by(profile)

    def is_unlocked_by(self, profile):
        try:
            unlock = get_unlock_for_category_and_profile(self.pk, profile.pk)
        except CategoryUnlockPurchase.DoesNotExist:
            return False

        return unlock.is_unlocked()
        # return self.is_unlocked_for_cash(profile, unlock) or self.is_unlocked_for_coins(profile, unlock)

    def is_unlocked_for_coins(self, profile, unlock):
        return not unlock.google_play_purchase and unlock.status == CategoryUnlockPurchaseStatus.COMPLETE

    def is_unlocked_for_cash(self, profile, unlock):
        return unlock.google_play_purchase and unlock.status == CategoryUnlockPurchaseStatus.COMPLETE
            # return unlock.google_play_purchase.status == PurchaseStatus.VALID

    def is_completion_condition_met_by(self, profile):
        levels_complete, levels_total = self.get_progress(profile)
        logger.debug('Category id %s complete condition: (%s, %s)', self.pk, levels_complete, levels_total)

        return levels_complete == levels_total

    def get_progress(self, profile):
        return get_progress_profile_in_category(profile.pk, self.pk)

    def handle_complete(self, profile, save_profile=True):
        user_events = super(QuoteCategory, self).handle_complete(profile, save_profile)

        logger.debug('Category id %s complete by profile %s', self.pk, profile.pk)

        if self.section.is_completion_condition_met_by(profile) == True:
            user_events += self.section.handle_complete(profile)
        return user_events

    def get_flat(self):
        return {
            'id': self.id,
            'icon': self.icon,
            'title': self.title,
            'section_id': self.section.id,
            'is_payable': self.is_payable,
            'price_to_unlock': self.price_to_unlock,
            'bonus_reward': self.bonus_reward,
            'image': self.get_image_url(),
            'image-preview': self.get_image_preview_url(),
            'on_complete_achievement': self.on_complete_achievement.id if self.on_complete_achievement else None
        }

    def save_images(self, *args, **kwargs):
        self.save_images()
        return super(QuoteCategory, self).save(*args, **kwargs)



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
# @lru_cache(maxsize=2**16)
def get_all_levels_in_category(category_pk):
    return list(Quote.objects.filter(category=category_pk).order_by('-order_in_category').order_by('-date_added').all())

# @lru_cache(maxsize=2**16)
def get_all_levels_in_category_count(category_pk):
    return Quote.objects.filter(category=category_pk).count()

# @lru_cache(maxsize=2**16)
def get_all_levels_in_section_count(section_pk):
    return Quote.objects.filter(category__section=section_pk).count()

# @lru_cache(maxsize=2**16)
def get_all_levels_in_topic_count(topic_pk):
    return Quote.objects.filter(category__section__topic=topic_pk).count()
# --


def clean_content_cache(*args, **kwargs):
    # logger.debug('Content Cache report:')
    # logger.debug('\tget_all_levels_in_category: %s', get_all_levels_in_category.cache_info())
    # logger.debug('\tget_all_levels_in_category_count: %s', get_all_levels_in_category_count.cache_info())
    # logger.debug('\tget_all_levels_in_section_count: %s', get_all_levels_in_section_count.cache_info())
    # logger.debug('\tget_all_levels_in_topic_count: %s', get_all_levels_in_topic_count.cache_info())

    # get_all_levels_in_category.cache_clear()
    # get_all_levels_in_category_count.cache_clear()
    # get_all_levels_in_section_count.cache_clear()
    # get_all_levels_in_topic_count.cache_clear()

    pass

# @lru_cache(maxsize=2 ** 16)
def get_unlock_for_category_and_profile(category_pk, profile_pk):
    return CategoryUnlockPurchase.objects.get(category_to_unlock=category_pk,
                                              profile=profile_pk)

def clean_unlock_cache(*args, **kwargs):
    # logger.debug('`get_unlock_for_category_and_profile` %s', get_unlock_for_category_and_profile.cache_info())

    # get_unlock_for_category_and_profile.cache_clear()
    pass

class Quote(RewardableEntity):
    text = models.CharField("Текст цитаты", max_length=256)
    author = models.ForeignKey(QuoteAuthor, verbose_name="Автор", on_delete=models.SET_NULL, null=True, blank=True)
    category = models.ForeignKey(QuoteCategory, verbose_name="Категория", on_delete=models.SET_NULL, null=True)

    order_in_category = models.BigIntegerField('Порядковый номер уровня в категории', default=0, blank=True)

    date_added = models.DateTimeField(auto_now_add=True)

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

    def get_beautiful(self):
        return beautiful_text(self.text)

    get_beautiful.short_description = 'Красивый текст'


    @mark_safe
    def bubbles(self, obj=None):
        markup = """
        <span class="bubbles">
        """
        obj = obj or self
        for q in obj.get_splitted():
            markup += f'<span>{q}</span>'

        markup += "</span>"
        return markup

    bubbles.short_description = 'Предпросмотр'
    bubbles.allow_tags = True


    def get_reward(self, profile):
        return profile.settings.reward_per_level_completion

    def is_completion_condition_met_by(self, profile):
        return True  # stub

    def mark_complete(self, profile, solution=None):
        if not self.category.is_available_to_user(profile):
            raise self.NoAccess('The user have the category locked and he cannot complete this level.')

        logger.debug('Level id %s complete by profile %s', self.pk, profile.pk)

        user_events = self.handle_complete(profile, save_profile=False)

        if self.category.is_completion_condition_met_by(profile) == True:
            user_events += self.category.handle_complete(profile, save_profile=False)

        user_events += handle_rank_update(profile, user_events)

        self._post_mark_complete(profile)
        return user_events

    def _post_mark_complete(self, profile):
        clean_profile_progress_cache(profile=profile)
        profile.save()


def beautiful_text(text):
    if '^' in text:
        text = text.replace('^', ' ')
    beautified = text[0].capitalize() + text[1:]
    beautified = re.sub('(\s+)(["\'“”«»-–—])', ' \g<2>', beautified)
    beautified = re.sub('(\s+)([\?!¿])', '\g<2>', beautified)
    beautified = re.sub('\s+', ' ', beautified)
    return beautified


class BalanceRechargeProduct(models.Model):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    admin_title = models.CharField("Название продукта для юзера", max_length=256)
    balance_recharge = models.IntegerField("Сумма пополнения баланса", default=1)

    google_play_product = models.ForeignKey('api.GooglePlayProduct',
                                            verbose_name="Соответствующий продукт в Google Play",
                                            on_delete=models.SET_NULL, null=True, blank=True)
    # app_store_product = models.ForeignKey('api.AppStoreProduct', on_delete=models.SET_NULL, null=True, blank=True)

    is_featured = models.BooleanField("Показывать как самый выгодный?", default=False, blank=True)

    objects = ProductManager()

    class Meta:
        verbose_name = 'Продукт «Начисление монеток»'
        verbose_name_plural = 'продукты'

    def get_flat(self):
        flat = model_to_dict(self, fields=('id', 'admin_title', 'balance_recharge', 'is_featured'))
        if self.google_play_product is not None:
            flat['sku'] = self.google_play_product.sku
        else:
            flat['sku'] = ''
        flat['id'] = self.id
        return flat


class GameBalance(models.Model):
    initial_profile_balance = models.BigIntegerField("Начальный баланс монет у юзера", default=0)
    reward_per_level_completion = models.BigIntegerField("Вознаграждение за решение цитаты", default=5)
    reward_per_doubleup = models.BigIntegerField("Вознаграждение за double-up", default=5)

    objects = GameBalanceManager()

    class Meta:
        verbose_name = 'игровой баланс'
        verbose_name_plural = 'игровой баланс'

    def __str__(self):
        return f'{self.pk}) Монет в начале: {self.initial_profile_balance}, вознаграждение за прохождение левела {self.reward_per_level_completion}'


class Profile(models.Model):
    device_sessions = models.ManyToManyField('api.DeviceSession', verbose_name="Сессии устройств")
    balance = models.PositiveIntegerField("Баланс", default=0)
    nickname = models.CharField("Никнейм", max_length=256, default='Пан Инкогнито')

    last_active = models.DateTimeField("Дата последней активности", auto_now=True)

    settings = models.ForeignKey('GameBalance', verbose_name="Соответствующий объект Игрового баланса", on_delete=models.CASCADE, null=True, default=None)

    is_banned = models.BooleanField("Забанен?", default=False, blank=True)

    objects = ProfileManager()

    class Meta:
        verbose_name = 'профиль пользователя'
        verbose_name_plural = 'профили пользователей'

    def __str__(self):
        return f'#{self.pk} ({self.nickname})'

    def get_flat(self):
        return {
            'id': self.pk,
            'last_active': self.last_active.strftime('%Y-%m-%dT%H:%M:%S%z'),
            'nickname': self.nickname,
            'balance': self.balance,
            'is_banned': self.is_banned,
            'top_position_change_since_last_update': self.profilerank.position_change_since_last_update,
            'reward_per_level_completion': self.settings.reward_per_level_completion,
            'reward_per_doubleup': self.settings.reward_per_doubleup,
            'initial_profile_balance': self.settings.initial_profile_balance
        }


class CategoryUnlockTypes:
    NULL_UNLOCK = 'null_unlock'
    UNLOCK_FOR_COINS = 'unlock_for_coins'
    UNLOCK_BY_PURCHASE = 'unlock_by_purchase'

    choices = [
        ('null_unlock', '!! не использовать !!'),
        ('unlock_for_coins', 'Анлок за игровые монеты'),
        ('unlock_by_purchase', 'Анлок за покупку'),
    ]



class InvalidPurchaseStatus(Exception):
    pass


class InsufficientFunds(Exception):
    pass


class AlreadyAvailable(Exception):
    pass


class CategoryUnlockPurchaseStatus:
    DEFAULT = UNKNOWN = 'unknown'
    COMPLETE = 'complete'
    ERROR = 'error'

    choices = (
        ('unknown', 'Неизвестно (только что создан?)'),
        ('complete', 'Успешно обработан'),
        ('error', 'Ошибка'),
    )


class CategoryUnlockPurchase(models.Model):
    type = models.CharField("Тип анлока (за монеты или за покупку)", blank=False, max_length=32, choices=CategoryUnlockTypes.choices, default=CategoryUnlockTypes.NULL_UNLOCK)
    profile = models.ForeignKey(Profile, verbose_name="Юзер", on_delete=models.CASCADE)
    category_to_unlock = models.ForeignKey(QuoteCategory, verbose_name="Категория",  on_delete=models.CASCADE)

    google_play_purchase = models.ForeignKey('api.GooglePlayIAPPurchase', verbose_name="Соотв. покупка в Google Play", on_delete=models.SET_NULL, null=True, blank=True)

    date_created = models.DateTimeField("Дата создания", auto_now_add=True)

    status = models.CharField(max_length=64, choices=CategoryUnlockPurchaseStatus.choices, default=CategoryUnlockPurchaseStatus.DEFAULT)

    class Meta:
        verbose_name = 'Покупка доступа к категории'
        verbose_name_plural = 'Покупки доступов к категориям'

    def is_unlocked(self):
        return self.status == CategoryUnlockPurchaseStatus.COMPLETE

    def do_unlock(self):
        if self.type == CategoryUnlockTypes.UNLOCK_FOR_COINS:
            with transaction.atomic():
                profile = Profile.objects.select_for_update().get(pk=self.profile.pk)

                new_balance = profile.balance - self.category_to_unlock.price_to_unlock

                if new_balance >= 0:
                    self.status = CategoryUnlockPurchaseStatus.COMPLETE
                    self.save()
                    profile.balance = new_balance
                    profile.save()

                    logger.debug('Profile unlocking category: %s %s', self.profile, self.category_to_unlock.pk)
                    return

            raise InsufficientFunds()

        elif self.type == CategoryUnlockTypes.UNLOCK_BY_PURCHASE:
            with transaction.atomic():
                if self.google_play_purchase.status == PurchaseStatus.PURCHASED:
                    self.status = CategoryUnlockPurchaseStatus.COMPLETE
                    self.save()
                else:
                    raise InvalidPurchaseStatus('Tried to unlock category, but the status of IAP purchase was invalid.')

        elif self.type == CategoryUnlockTypes.NULL_UNLOCK:
            raise ValueError('CategoryUnlockPurchase should be one of type specified in CategoryUnlockTypes, except NULL_UNLOCK')


    def undo_unlock(self):  # TODO: cover with tests
        if self.type == CategoryUnlockTypes.UNLOCK_FOR_COINS:
            with transaction.atomic():
                profile = Profile.objects.select_for_update().get(pk=self.profile.pk)

                new_balance = profile.balance + self.category_to_unlock.price_to_unlock
                profile.save()
            self.delete()

        elif self.type == CategoryUnlockTypes.UNLOCK_BY_PURCHASE:
            self.delete()
        elif self.type == CategoryUnlockTypes.NULL_UNLOCK:
            raise ValueError('CategoryUnlockPurchase should be one of type specified in CategoryUnlockTypes, except NULL_UNLOCK')
