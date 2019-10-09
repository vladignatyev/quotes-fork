from django.utils import timezone

from django.db import models

from django.conf import settings
from django.db.models.signals import post_save

from django.forms.models import model_to_dict
from django.urls import reverse


from api.models import DeviceSession, PurchaseStatus, GooglePlayProduct, AppStoreProduct

import re



def _truncate(text, length=50, suffix='...'):
    '''
    >>> _truncate('Карл у Клары украл коралы', length=10, suffix='˚˚˚')
    >>> Карл у Кла˚˚˚
    '''
    return f'{text}'[:length] + (suffix if len(text) > length else '')


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


class TopicManager(models.Manager):
    topic_fields = ['title']
    section_fields = ['title', 'id', 'on_complete_achievement']
    category_fields = ['title', 'id', 'on_complete_achievement', 'icon',
                       'is_payable', 'price_to_unlock',
                       'is_event', 'event_due_date', 'event_title', 'event_icon',
                       'event_description', 'event_win_achievement']


    def get_flattened(self, pk, current_user=None):
        '''
        Flattens hierahical models under Topic into lists,
        mark categories opened/payable depending on user payments,
        adds progress values to every category
        '''
        # todo: implement filtering by available_to_users==current_user
        # Perform requests
        topic = self.get(pk=pk)

        sections = Section.objects.filter(topic__pk=pk).all()
        categories = QuoteCategory.objects.filter(section__topic=topic).all()

        flat_topic = model_to_dict(topic, fields=self.topic_fields)
        flat_topic['sections'] = [ model_to_dict(section, fields=self.section_fields) for section in sections ]
        flat_topic['uri'] = reverse('topic-detail', kwargs={'pk': topic.pk})

        for category in categories:
            for section in flat_topic['sections']:
                section['uri'] = reverse('section-detail', kwargs={'pk': section['id']})
                if category.section.id == section['id']:
                    categories_per_section = section.get('categories', [])
                    categories_per_section += [model_to_dict(category)]
                    section['categories'] = categories_per_section

        return flat_topic


# TODO: extract it as a base class for rewardable models: Topic, Section, QuoteCategory, Quote
class RewardableEntity(models.Model):
    class Meta:
        abstract = True

    on_complete_achievement = models.ForeignKey(Achievement, on_delete=models.SET_NULL, null=True, blank=True)
    bonus_reward = models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение')
    complete_by_users = models.ManyToManyField('Profile', verbose_name='Юзеры которые прошли и должны получить вознаграждение', blank=True,
                                                          related_name="%(app_label)s_%(class)s_complete_by_users",
                                                          related_query_name="%(app_label)s_%(class)s_complete_by_users_objs")


class Topic(models.Model):
    title = models.CharField("Название Темы", max_length=256)
    hidden = models.BooleanField(default=False)

    on_complete_achievement = models.ForeignKey(Achievement, on_delete=models.SET_NULL, null=True, blank=True)
    bonus_reward = models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение Темы, если есть')

    complete_by_users = models.ManyToManyField('Profile',  verbose_name='Юзеры которые прошли тему', blank=True, related_name='topic_complete_by_users')

    def __str__(self):
        return _truncate(f'{self.title}')

    class Meta:
        verbose_name = 'тема'
        verbose_name_plural = 'темы'

    objects = TopicManager()


class Section(models.Model):
    title = models.CharField("Название Раздела", max_length=256)
    topic = models.ForeignKey(Topic, on_delete=models.CASCADE)

    on_complete_achievement = models.ForeignKey(Achievement, on_delete=models.SET_NULL, null=True, blank=True)
    bonus_reward = models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение раздела, если есть')

    complete_by_users = models.ManyToManyField('Profile',  verbose_name='Юзеры которые прошли секцию', blank=True, related_name='section_complete_by_users')


    def __str__(self):
        return _truncate(f'{self.title}')

    class Meta:
        verbose_name = 'раздел'
        verbose_name_plural = 'разделы'


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


class QuoteCategory(models.Model):
    section = models.ForeignKey(Section, on_delete=models.CASCADE, default=None, null=True)
    title = models.CharField("Название Категории", max_length=256)
    icon = models.CharField(max_length=256, default='', blank=True)

    # Events
    is_event = models.BooleanField(default=False)
    event_due_date = models.DateTimeField(default=timezone.now, blank=True)
    event_title = models.CharField(max_length=256, default='', blank=True)
    event_icon = models.CharField(max_length=256, default='', blank=True)
    event_description = models.TextField(default='', blank=True)
    event_win_achievement = models.ForeignKey(Achievement, on_delete=models.SET_NULL, null=True, blank=True)

    # Locked category
    is_payable = models.BooleanField('Категория платная?', default=False)

    price_to_unlock = models.BigIntegerField('Стоимость открытия категории если она платная', default=0, blank=True)

    available_to_users = models.ManyToManyField('Profile',
                                                verbose_name='Профили пользователей которым доступна категория',
                                                blank=True,
                                                through='CategoryUnlockPurchase',
                                                related_name='availability_to_profile')
    on_complete_achievement = models.ForeignKey(Achievement, on_delete=models.SET_NULL, null=True, blank=True, related_name='on_complete_achievement')

    complete_by_users = models.ManyToManyField('Profile',  verbose_name='Юзеры которые прошли категорию', blank=True, related_name='category_complete_by_users')
    bonus_reward = models.BigIntegerField(default=0, verbose_name='Бонус монет за прохождение категории, если есть')

    def __str__(self):
        return _truncate(f'{self.section.topic.title} > {self.section.title} > {self.title}')

    class Meta:
        verbose_name = 'категория'
        verbose_name_plural = 'категории'

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


def get_levels_complete_by_profile(profile, category=None):
    if category is not None:
        return Quote.objects.filter(complete_by_users=profile,
                                    category=category)
    else:
        return Quote.objects.filter(complete_by_users=profile)

def is_category_complete_by_user(profile, category):
    pass

def is_section_complete_by_user(profile, section):
    pass


class UserEvents:
    LEVEL_COMPLETE = 'level_complete'
    CATEGORY_COMPLETE = 'category_complete'

    RECEIVED_PER_LEVEL_REWARD = 'received_per_level_reward'
    RECEIVED_PER_CATEGORY_REWARD = 'received_per_category_reward'
    RECEIVED_PER_SECTION_REWARD = 'received_per_section_reward'
    RECEIVED_PER_TOPIC_REWARD = 'received_per_topic_reward'

    RECEIVED_CATEGORY_ACHIEVEMENT = 'received_category_achievement'
    RECEIVED_SECTION_ACHIEVEMENT = 'received_section_achievement'
    RECEIVED_TOPIC_ACHIEVEMENT = 'received_topic_achievement'

    RECEIVED_GENERAL_ACHIEVEMENT = 'received_general_achievement'

    @classmethod
    def new(cls, name, param):
        return (name, param)


class Quote(models.Model):
    text = models.CharField("Текст цитаты", max_length=256)
    author = models.ForeignKey(QuoteAuthor, on_delete=models.SET_NULL, null=True)
    category = models.ForeignKey(QuoteCategory, on_delete=models.SET_NULL, null=True)

    order_in_category = models.BigIntegerField('Порядковый номер уровня в категории', default=0, blank=True)
    complete_by_users = models.ManyToManyField('Profile',  verbose_name='Юзеры которые прошли уровень', blank=True, related_name='quote_complete_by_users')

    def __str__(self):
        return _truncate(self.text)

    class Meta:
        verbose_name = 'цитата'
        verbose_name_plural = 'цитаты'

    def mark_complete(self, profile, solution=None):
        '''
        solution parameter is unused for now
        '''

        # mark progress
        self.complete_by_users.add(profile)
        self.save()

        # user_events = [(UserEvents.LEVEL_COMPLETE,)]
        user_events = [UserEvents.new(UserEvents.LEVEL_COMPLETE, None)]
        return user_events + handle_level_complete(self, profile)


def handle_level_complete(quote, profile):
    # reward for level completion
    game_settings = GameBalance.objects.get_actual()
    per_level_reward = game_settings.reward_per_level_completion
    profile.balance = profile.balance + per_level_reward
    profile.save()

    user_events = [UserEvents.new(UserEvents.RECEIVED_PER_LEVEL_REWARD, per_level_reward)]

    if is_category_complete_by_user(profile, quote.category):
        user_events += handle_category_complete(quote, profile, quote.category)

    return user_events

def handle_category_complete(quote, profile, category):
    category.complete_by_users.add(profile)
    category.save()
    user_events = [UserEvents.new(UserEvents.CATEGORY_COMPLETE, category.pk)]

    if category.bonus_reward > 0:
        profile.balance = profile.balance + category.bonus_reward
        profile.save()
        user_events += [UserEvents.new(UserEvents.RECEIVED_PER_CATEGORY_REWARD,
                                       category.bonus_reward)]

    if category.on_complete_achievement:
        AchievementReceiving.create(achievement=category.on_complete_achievement,
                                    profile=profile)
        user_events += [UserEvents.new(UserEvents.RECEIVED_ACHIEVEMENT, category.on_complete_achievement.pk)]

    if is_section_complete_by_user(profile, category.section):
        user_events += handle_section_complete(quote, profile, category, section)

    return user_events

def handle_section_complete(quote, profile, category, section):

    return []

def handle_topic_complete(quote, profile):
    return []


class ProductManager(models.Manager):
    def get_by_store_product(self, store_product):
        if type(store_product) is GooglePlayProduct:
            return BalanceRechargeProduct.objects.get(google_play_product=store_product)
        elif type(store_product) is AppStoreProduct:
            return BalanceRechargeProduct.objects.get(app_store_product=store_product)
        else:
            raise Error('Unknown product type.')


class BalanceRechargeProduct(models.Model):
    admin_title = models.CharField("Название для админки", max_length=256)
    balance_recharge = models.IntegerField("Сумма пополнения баланса", default=1)

    google_play_product = models.ForeignKey('api.GooglePlayProduct', on_delete=models.SET_NULL, null=True, blank=True)
    # app_store_product = models.ForeignKey('api.AppStoreProduct', on_delete=models.SET_NULL, null=True, blank=True)

    objects = ProductManager()

    class Meta:
        verbose_name = 'IAP продукт'
        verbose_name_plural = 'продукты'



class GameBalanceManager(models.Manager):
    def get_actual(self):
        # TODO: implement LRU Cache in a manner it has been done in `api` module
        try:
            return self.model.objects.latest('pk')
        except self.model.DoesNotExist:
            game_settings = self.create(initial_profile_balance=0)
            return game_settings


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



class ProfileManager(models.Manager):
    def create(self, *args, **kwargs):
        profile = super(ProfileManager, self).create(*args, **kwargs)

        game_settings = GameBalance.objects.get_actual()

        profile.balance = kwargs.get('balance', game_settings.initial_profile_balance)
        profile.nickname = kwargs.get('nickname', '')

        return profile

    def get_by_session(self, device_session):
        return Profile.objects.get(device_sessions__pk__contains=device_session.pk)

    def get_by_token(self, device_session_token):
        return Profile.objects.get(device_sessions__token__contains=device_session_token)

    def get_by_auth_token(self, auth_token):
        session = DeviceSession.objects.get(auth_token=auth_token)
        return self.get_by_session(session)

    def unlock_category(self, profile, quote_category):
        balance = profile.balance
        price = quote_category.price_to_unlock
        new_balance = balance - price
        if new_balance < 0:
            return (False, 'Not enough funds.')
        profile.balance = new_balance
        quote_category.available_to_users.add(profile)
        return (True,'')


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
